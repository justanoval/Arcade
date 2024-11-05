package com.justanoval.game.components

import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.boss.dragon.EnderDragonEntity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import net.minecraft.world.event.GameEvent
import java.util.function.Predicate
import kotlin.reflect.KClass

class MobSwapComponent(
    var range: Double = 128.0
): GameComponent() {

    private val blacklist: List<KClass<out Entity>> = listOf(
        EnderDragonEntity::class
    )

    override fun onPlayerTick(player: ServerPlayerEntity, delta: Int) {
        val target: Entity? = player.getLookingAt { true }

        if (target != null && target is LivingEntity && !blacklist.contains(target::class) ) {
            val playerLocation = player.pos
            val targetLocation = target.pos

            if (player.canTeleport() && target.canTeleport()) {
                player.teleport(targetLocation)
                target.teleport(playerLocation)
            }
        }
    }

    private fun LivingEntity.teleport(pos: Vec3d) {
        if (this.hasVehicle()) {
            this.stopRiding()
        }

        if (this.pos.isInRange(pos, 0.3)) {
            this.teleportRandomly(16.0)
        } else {
            this.requestTeleport(pos.x, pos.y, pos.z)
        }

        playTeleportEffects(this)
    }

    private fun LivingEntity.teleportRandomly(diameter: Double) {
        for (i in 0..15) {
            val x: Double = this.x + (this.random.nextDouble() - 0.5) * diameter
            val y = MathHelper.clamp(
                this.y + (this.random.nextDouble() - 0.5) * diameter,
                world.bottomY.toDouble(),
                (world.bottomY + (world as ServerWorld).logicalHeight - 1).toDouble()
            )
            val z: Double = this.z + (this.random.nextDouble() - 0.5) * diameter

            if (this.teleport(x, y, z, true)) {
                world.emitGameEvent(GameEvent.TELEPORT, this.pos, GameEvent.Emitter.of(this))
                this.onLanding()
                break
            }
        }
    }

    private fun playTeleportEffects(entity: Entity) {
        entity.world.playSound(null, entity.x, entity.y, entity.z, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.MASTER)
    }

    private fun Entity.canTeleport(): Boolean {
        return !this.isRemoved && this.isAlive
    }

    private fun ServerPlayerEntity.getLookingAt(predicate: Predicate<Entity>): Entity? {
        val hitResult = this.getHitResult(predicate)

        if (hitResult != null && hitResult.type == HitResult.Type.ENTITY) {
            return (hitResult as EntityHitResult).entity
        }

        return null
    }

    private fun ServerPlayerEntity.getHitResult(predicate: Predicate<Entity>): HitResult? {
        val start: Vec3d = this.eyePos
        val offset: Vec3d = this.rotationVector.multiply(range)
        val end: Vec3d = start.add(offset)
        val box: Box = this.boundingBox.stretch(offset).expand(1.0)

        val entityHitResult = ProjectileUtil.raycast(this, start, end, box, predicate, range * range)

        var closestBlockHit: BlockHitResult? = null
        var closestBlockDistance = Double.MAX_VALUE

        var currentPos = start
        val step = offset.normalize().multiply(0.1)

        while (currentPos.squaredDistanceTo(start) < range * range) {
            val blockHitResult = this.world.raycast(
                RaycastContext(currentPos, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, this)
            )

            if (blockHitResult.type == HitResult.Type.BLOCK) {
                val blockDistance = blockHitResult.pos.squaredDistanceTo(start)
                val blockPos = (blockHitResult as BlockHitResult).blockPos
                val blockState = this.world.getBlockState(blockPos)

                if (blockState.isOpaqueFullCube) {
                    closestBlockHit = blockHitResult
                    closestBlockDistance = blockDistance
                    break
                } else if (blockDistance < closestBlockDistance) {
                    closestBlockHit = blockHitResult
                    closestBlockDistance = blockDistance
                }
            }

            currentPos = currentPos.add(step)
        }

        if (closestBlockHit != null && entityHitResult != null) {
            val entityDistance = entityHitResult.pos.squaredDistanceTo(start)

            return if (closestBlockDistance < entityDistance) closestBlockHit else entityHitResult
        }

        return closestBlockHit ?: entityHitResult
    }
}