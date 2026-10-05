package com.kolin.clipsniper.data

import com.kolin.clipsniper.util.ChatTextCleaner
import java.util.LinkedList

/**
 * 维护最近复制的 5 条记录管理器 (先进先出 FIFO 队列)
 */
object ClipQueueManager {
    private const val MAX_CAPACITY = 5

    // 内存队列，存储原始复制文本
    private val rawQueue = LinkedList<String>()

    val lock = Any()

    /**
     * 当监听到新的剪贴板内容时调用
     */
    fun push(rawText: String) {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) return

        synchronized(lock) {
            // 如果已存在相同内容，先移除再放入最前（保证最新）
            rawQueue.remove(trimmed)
            rawQueue.addFirst(trimmed)

            // 超出 5 条淘汰最旧的
            while (rawQueue.size > MAX_CAPACITY) {
                rawQueue.removeLast()
            }
        }
    }

    /**
     * 获取最新 5 条，返回已经经过清洗后的干净正文列表
     */
    fun getCleanedList(): List<CleanedItem> {
        synchronized(lock) {
            return rawQueue.map { raw ->
                val clean = ChatTextCleaner.clean(raw)
                CleanedItem(
                    raw = raw,
                    cleaned = clean,
                    isCleaned = raw != clean
                )
            }
        }
    }

    fun clearQueue() {
        synchronized(lock) {
            rawQueue.clear()
        }
    }
}

data class CleanedItem(
    val raw: String,
    val cleaned: String,
    val isCleaned: Boolean,
    var isSelected: Boolean = true
)
