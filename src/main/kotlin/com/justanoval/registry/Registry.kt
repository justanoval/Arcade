package com.justanoval.registry

import net.minecraft.util.Identifier

open class Registry<T> {
    val items: MutableMap<Identifier, T> = mutableMapOf()

    open fun get(id: Identifier): T? {
        return items[id]
    }

    open fun register(id: Identifier, item: T) {
        items[id] = item
    }

    open fun unregister(id: Identifier) {
        items.remove(id)
    }
}