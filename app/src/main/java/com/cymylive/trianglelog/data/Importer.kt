package com.cymylive.trianglelog.data

import android.content.Context
import kotlinx.serialization.json.Json
import java.io.File

/** 导入备份：返回 null 表示成功，返回字符串表示错误原因 */
object Importer {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun importFromText(context: Context, text: String): String? {
        val parsed = try {
            json.decodeFromString(AppData.serializer(), text)
        } catch (e: Exception) {
            return "文件格式不对，不是本 App 的备份：" + (e.message ?: "")
        }
        if (parsed.version <= 0) return "备份文件版本异常"

        // 导入前先把现有数据存一份，出问题能找回
        try {
            val current = Store.load(context)
            val backup = File(context.filesDir, "before-import-" + System.currentTimeMillis() + ".json")
            backup.writeText(json.encodeToString(AppData.serializer(), current))
        } catch (e: Exception) {
        }

        return try {
            Store.save(context, parsed)
            null
        } catch (e: Exception) {
            "写入失败：" + (e.message ?: "")
        }
    }
}
