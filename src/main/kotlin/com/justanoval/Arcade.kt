package com.justanoval

import com.justanoval.command.CommandRegistry
import com.justanoval.command.suggestion.ActiveGamesSuggestionProvider
import com.justanoval.command.suggestion.GamesSuggestionProvider
import com.justanoval.command.suggestion.TeamsSuggestionProvider
import com.justanoval.commands.GameCommand
import com.justanoval.config.FileLoader
import com.justanoval.config.GameConfig
import com.justanoval.config.LangConfig
import com.justanoval.game.GameFactory
import com.justanoval.game.GameRegistry
import com.justanoval.game.components.*
import com.justanoval.placeholder.PlayerPlaceholders
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.scoreboard.Team
import net.minecraft.server.MinecraftServer
import net.minecraft.util.Identifier
import org.apache.logging.log4j.core.tools.picocli.CommandLine.InitializationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KParameter
import kotlin.reflect.full.*

object Arcade : ModInitializer {
	val ID: String = "arcade"
	val logger: Logger = LoggerFactory.getLogger(ID)

	lateinit var server: MinecraftServer
	lateinit var lang: LangConfig
	lateinit var games: GameConfig

	fun id(path: String): Identifier {
		return Identifier.of(ID, path)
	}

	override fun onInitialize() {
		PlayerPlaceholders

		// Event registry
		ServerLifecycleEvents.START_DATA_PACK_RELOAD.register { server, _ ->
			this.loadConfigs()
			this.loadGames(server)
		}

		ServerLifecycleEvents.SERVER_STARTED.register { server: MinecraftServer ->
			this.server = server
			this.loadConfigs()
			this.loadGames(server)
		}

		// Command registry
		CommandRegistry.registerSuggestionProvider(id("games"), GamesSuggestionProvider)
		CommandRegistry.registerSuggestionProvider(id("active_games"), ActiveGamesSuggestionProvider)
		CommandRegistry.registerSuggestionProvider(id("teams"), TeamsSuggestionProvider)

		CommandRegistry.registerArgumentInterpreter(GameFactory::class) { string: String ->
			GameRegistry.items[id(string)] ?: error("Invalid game name.")
		}

		CommandRegistry.registerArgumentInterpreter(Team::class) { string: String ->
			server.scoreboard.getTeam(string) ?: error("Invalid team name.")
		}

		CommandRegistry.registerCommand(GameCommand)

		// Component registry
		GameComponentRegistry.register(id("action_bar"), ActionBarComponent::class)
		GameComponentRegistry.register(id("sidebar"), SidebarComponent::class)
		GameComponentRegistry.register(id("announcements"), AnnouncementsComponent::class)
		GameComponentRegistry.register(id("mob_swap"), MobSwapComponent::class)
	}

	private fun loadConfigs() {
		this.lang = FileLoader.loadConfig("lang", LangConfig::class)
		this.games = FileLoader.loadConfig("games", GameConfig::class)
	}

	private fun loadGames(server: MinecraftServer) {
		for (entry in FileLoader.loadJsonFiles("games")) {
			val id = entry.key
			val settings = entry.value
			val components: MutableList<GameComponent> = mutableListOf()

			val componentList = settings.getOrDefault("components", null)
			if (componentList !is List<*>) {
				logger.warn("There was an issue creating game $id: \"components\" must be an array")
				continue
			}

			for (obj in componentList) {
				try {
					components.add(loadComponent(obj))
				} catch (e: InitializationException) {
					logger.warn("There was an issue adding a component to game $id: ${e.message}")
				}
			}

			var name = settings.getOrDefault("name", "Game")
			if (name !is String) {
				logger.warn("There was an issue creating game $id: \"name\" must be a string")
				name = "Game"
			}

			var length = settings.getOrDefault("length", null)
			if (length !is Double) {
				logger.warn("There was an issue creating game $id: \"length\" must be a long.")
				length = 0.0
			}

			GameRegistry.register(GameFactory(id, server, name, length.toInt(), components))
		}
	}

	private fun loadComponent(obj: Any?): GameComponent {
		val component: GameComponent? = when (obj) {
			is String -> GameComponentRegistry.get(Identifier.of(obj))?.createInstance()
			is Map<*, *> -> {
				val id = obj["id"]
				if (id !is String) {
					throw InitializationException("$obj must contain key 'id' with value string")
				}
				buildComponent(id, obj)
			}
			else -> throw InitializationException("$obj must be a string or object")
		}

		if (component == null) {
			throw InitializationException("$obj is not a valid component id")
		}

		return component
	}

	private fun buildComponent(id: String, obj: Map<*, *>): GameComponent {
		val clazz = GameComponentRegistry.get(Identifier.of(id))
			?: throw InitializationException("$obj is not a valid component id")

		val constructor = clazz.primaryConstructor

		if (constructor != null) {
			val parameters = constructor.parameters
			val args = mutableMapOf<KParameter, Any?>()

			for (parameter in parameters) {
				var paramName = parameter.name

				if (parameter.hasAnnotation<SerializedName>()) {
					paramName = parameter.findAnnotations(SerializedName::class).component1().value
				}

				if (paramName != null && obj.containsKey(paramName)) {
					val value = obj.getOrDefault(paramName, null)
					if (value != null) {
						args[parameter] = value
					}
				} else if (!parameter.isOptional) {
					throw IllegalArgumentException("Missing required parameter '$paramName' for component '$id'.")
				}
			}

			return constructor.callBy(args)
		}

		return clazz.createInstance()
	}
}