package com.kolin.clipsniper.util

/**
 * 专门清洗即时通讯软件（微信、QQ等）复制内容中的昵称、时间戳与杂质
 */
object ChatTextCleaner {

    // 匹配常见聊天时间戳行的正则表达式
    // 例如: "2026年10月06日 00:58", "2026-10-06 00:58:12", "10月6日 00:58", "00:58", "昨天 14:20"
    private val TIME_REGEX = Regex(
        """^(?:(?:\d{2,4}[年\-\/.]\d{1,2}[月\-\/.]\d{1,2}[日号]?\s*)?(?:昨天|今天|前天)?\s*)?\d{1,2}:\d{2}(?::\d{2})?$"""
    )

    /**
     * 清洗文本：
     * 输入示例：
     * 等我长大就变帅
     * 2026年10月06日 00:58
     * 我通过了你的跑友验证请求，现在我们可以开始约跑了
     */
    fun clean(raw: String?): String {
        if (raw.isNullOrBlank()) return ""

        val lines = raw.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (lines.isEmpty()) return ""

        // 寻找时间戳所在的那一行下标
        val timeIndex = lines.indexOfFirst { line ->
            TIME_REGEX.matches(line)
        }

        return if (timeIndex != -1) {
            // 找到了时间戳行：
            // 时间戳的上一行如果存在，即为昵称行。
            // 正文内容从 timeIndex + 1 开始截取全部后续行
            val contentLines = lines.drop(timeIndex + 1)
            if (contentLines.isNotEmpty()) {
                contentLines.joinToString("\n").trim()
            } else {
                // 如果时间戳就是最后一行，可能原格式特殊，回退保底
                lines.filterIndexed { index, _ -> index != timeIndex && index != (timeIndex - 1) }
                    .joinToString("\n").trim()
            }
        } else {
            // 没检测到标准时间戳，尝试检查常见冒号前缀（例如 "张三: 段子内容"）
            val singleLineColonRegex = Regex("""^[^:\n]{1,20}[:：]\s*(.+)""", RegexOption.DOT_MATCHES_ALL)
            val match = singleLineColonRegex.find(raw.trim())
            if (match != null && match.groupValues.size > 1) {
                match.groupValues[1].trim()
            } else {
                // 普通纯文本，原样保留
                raw.trim()
            }
        }
    }
}
