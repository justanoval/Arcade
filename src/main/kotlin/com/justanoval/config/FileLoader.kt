package com.justanoval.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.justanoval.Arcade
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

object FileLoader {
    private val loader: FabricLoader = FabricLoader.getInstance()
    private val gson: Gson = GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()
    private val configFolder: Path get() = loader.configDir.resolve(Arcade.ID)
    private val mainFolder: Path get() = loader.gameDir.resolve(Arcade.ID)

    fun <T : Any> load(name: String, clazz: KClass<T>, folder: Path): T {
        try {
            confirmDirectory(folder)

            val filePath: Path = folder.resolve("$name.json")
            val file = filePath.toFile()

            return if (file.isFile()) {
                val jsonString = file.readText()
                val config: T = Gson().fromJson(jsonString, clazz.java)
                config
            } else {
                val defaultConfig: T = clazz.createInstance()
                save(name, defaultConfig, folder)
                defaultConfig
            }
        } catch (e: Exception) {
            Arcade.logger.warn("Couldn't load config! $clazz")
            Arcade.logger.warn(e.toString())
            throw RuntimeException(e)
        }
    }

    fun <T : Any> save(name: String, obj: T, folder: Path) {
        try {
            confirmDirectory(folder)
            val filePath: Path = folder.resolve("$name.json")
            val jsonString = gson.toJson(obj)
            filePath.toFile().writeText(jsonString)
        } catch (e: java.lang.Exception) {
            Arcade.logger.warn("Couldn't save config! " + obj.javaClass)
        }
    }

    // Config

    fun <T : Any> loadConfig(name: String, clazz: KClass<T>): T {
        return load(name, clazz, configFolder)
    }

    fun <T : Any> saveConfig(name: String, config: T) {
        return save(name, config, configFolder)
    }

    // Files

    fun <T : Any> loadFile(file: File, clazz: KClass<T>): T {
        try {
            return if (file.isFile()) {
                val jsonString = file.readText()
                val config: T = Gson().fromJson(jsonString, clazz.java)
                config
            } else {
                val defaultConfig: T = clazz.createInstance()
                saveFile(file, defaultConfig)
                defaultConfig
            }
        } catch (e: Exception) {
            Arcade.logger.warn("Couldn't load file! $clazz")
            Arcade.logger.warn(e.toString())
            throw RuntimeException(e)
        }
    }

    fun <T : Any> saveFile(file: File, obj: T) {
        try {
            val jsonString = gson.toJson(obj)
            file.writeText(jsonString)
        } catch (e: java.lang.Exception) {
            Arcade.logger.warn("Couldn't save file! " + obj.javaClass)
        }
    }

    // JSON

    fun loadJson(file: File): Map<String, Any?> {
        try {
            return if (file.isFile()) {
                loadJsonAsMap(file)
            } else {
                val defaultConfig: Map<String, Any?> = mapOf()
                saveJson(file, defaultConfig)
                defaultConfig
            }
        } catch (e: Exception) {
            Arcade.logger.warn("Couldn't load JSON! $file")
            Arcade.logger.warn(e.toString())
            throw RuntimeException(e)
        }
    }

    fun saveJson(file: File, obj: Map<String, Any?>) {
        try {
            val jsonString = gson.toJson(obj)
            file.writeText(jsonString)
        } catch (e: java.lang.Exception) {
            Arcade.logger.warn("Couldn't save JSON! $file")
        }
    }

    fun loadJsonFiles(path: String): Map<String, Map<String, Any?>> {
        val folder = mainFolder

        confirmDirectory(folder)

        try {
            return folder.resolve(path).toFile().listFiles()?.associate { file -> file.nameWithoutExtension to loadJson(file) } ?: mapOf()
        } catch (e: Exception) {
            Arcade.logger.warn("Couldn't load children from $path as JSON")
            Arcade.logger.warn(e.toString())
            throw RuntimeException(e)
        }
    }

    private fun loadJsonAsMap(file: File): Map<String, Any?> {
        val gson = Gson()
        val jsonString = file.readText()
        val mapType = object : TypeToken<Map<String, Any?>>() {}.type
        return gson.fromJson(jsonString, mapType)
    }

    // Misc.

    private fun confirmDirectory(folder: Path) {
        if (!Files.isDirectory(folder)) {
            if (Files.exists(folder)) {
                Files.deleteIfExists(folder)
            }
            Files.createDirectories(folder)
        }
    }
}