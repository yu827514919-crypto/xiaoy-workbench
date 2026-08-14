package com.example.xiaoy.data

import android.content.Context
import com.google.gson.Gson
import java.io.File

/**
 * 本地持久化：把整份 AppData 序列化成 JSON 存到应用私有目录。
 * 首次启动写入示例数据；之后每次改动即时落盘，刷新后数据保留。
 */
class AppRepository(private val context: Context) {

    private val file: File = File(context.filesDir, "xiaoy_data.json")
    private val gson = Gson()

    fun load(): AppData {
        if (!file.exists()) {
            val seed = SampleData.seed()
            save(seed)
            return seed
        }
        return try {
            val text = file.readText()
            if (text.isBlank()) SampleData.seed() else gson.fromJson(text, AppData::class.java)
                ?: SampleData.seed()
        } catch (e: Exception) {
            // 数据损坏时回退到示例数据，避免白屏
            SampleData.seed()
        }
    }

    fun save(data: AppData) {
        try {
            file.writeText(gson.toJson(data))
        } catch (_: Exception) {
            // 写入失败静默，内存中仍保留最新状态
        }
    }

    fun exportJson(): String = gson.toJson(load())
}
