package com.cymylive.trianglelog.data

import android.content.Context
import kotlinx.serialization.json.Json
import java.io.File

/**
 * 极简本地存储：全部数据序列化成一个 JSON 文件，放在 App 私有目录。
 * 没有数据库、没有迁移，备份 = 拷这个文件。
 */
object Store {

    private const val FILE_NAME = "triangle-log.json"

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Volatile
    private var cached: AppData? = null

    private fun file(context: Context): File = File(context.filesDir, FILE_NAME)

    @Synchronized
    fun load(context: Context): AppData {
        cached?.let { return it }
        val f = file(context)
        val data = try {
            if (f.exists() && f.length() > 0) {
                json.decodeFromString(AppData.serializer(), f.readText())
            } else {
                AppData()
            }
        } catch (e: Exception) {
            AppData()
        }
        cached = data
        return data
    }

    @Synchronized
    fun save(context: Context, data: AppData) {
        cached = data
        val f = file(context)
        val tmp = File(context.filesDir, FILE_NAME + ".tmp")
        tmp.writeText(json.encodeToString(AppData.serializer(), data))
        if (f.exists()) f.delete()
        tmp.renameTo(f)
    }

    /** 导出用：直接给 JSON 文本 */
    fun exportJson(context: Context): String =
        json.encodeToString(AppData.serializer(), load(context))
}
