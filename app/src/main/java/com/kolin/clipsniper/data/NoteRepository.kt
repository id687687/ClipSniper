package com.kolin.clipsniper.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Snippet(
    val id: String = System.currentTimeMillis().toString() + "_" + (1000..9999).random(),
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

class NoteRepository(private val context: Context) {
    private val file = File(context.filesDir, "saved_snippets.json")

    @Synchronized
    fun getAll(): MutableList<Snippet> {
        if (!file.exists()) return mutableListOf()
        val list = mutableListOf<Snippet>()
        try {
            val jsonStr = file.readText()
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    Snippet(
                        id = obj.getString("id"),
                        content = obj.getString("content"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    @Synchronized
    fun saveAll(list: List<Snippet>) {
        try {
            val array = JSONArray()
            for (item in list) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("content", item.content)
                    put("timestamp", item.timestamp)
                }
                array.put(obj)
            }
            file.writeText(array.toString(2))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Synchronized
    fun addBatch(contents: List<String>) {
        val current = getAll()
        val newItems = contents.filter { it.isNotBlank() }.map { Snippet(content = it.trim()) }
        current.addAll(0, newItems) // 最新的放最前
        saveAll(current)
    }

    @Synchronized
    fun delete(id: String) {
        val current = getAll()
        current.removeAll { it.id == id }
        saveAll(current)
    }

    @Synchronized
    fun clear() {
        saveAll(emptyList())
    }

    fun exportToMarkdown(): String {
        val items = getAll()
        val sb = StringBuilder("# 我的群聊灵感段子库\n\n")
        items.forEachIndexed { index, snippet ->
            sb.append("### 段子 ${items.size - index} (收录于 ${snippet.getFormattedDate()})\n\n")
            sb.append(snippet.content).append("\n\n---\n\n")
        }
        return sb.toString()
    }
}
