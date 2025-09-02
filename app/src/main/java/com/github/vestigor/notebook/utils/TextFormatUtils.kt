package com.github.vestigor.notebook.utils

import android.graphics.Color
import android.graphics.Typeface
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.EditText

// 文本格式化工具类
object TextFormatUtils {

    // 切换选中文本的加粗状态
    // 如果选中的文本已经加粗，则移除加粗；否则添加加粗
    fun toggleBold(editText: EditText) {
        val start = editText.selectionStart
        val end = editText.selectionEnd

        if (start < 0 || start >= end) return

        val spannable = editText.text as Spannable
        // 查找选区内已有的加粗样式
        val existingSpans = spannable.getSpans(start, end, StyleSpan::class.java)
            .filter { it.style == Typeface.BOLD }

        if (existingSpans.isNotEmpty()) {
            // 移除已有加粗
            existingSpans.forEach { spannable.removeSpan(it) }
        } else {
            // 添加加粗样式
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    // 切换文本的斜体状态
    // 如果文本已经斜体，则移除斜体；否则添加斜体
    fun toggleItalic(editText: EditText) {
        val start = editText.selectionStart
        val end = editText.selectionEnd

        if (start < 0 || start >= end) return

        val spannable = editText.text as Spannable
        val existingSpans = spannable.getSpans(start, end, StyleSpan::class.java)
            .filter { it.style == Typeface.ITALIC }

        if (existingSpans.isNotEmpty()) {
            existingSpans.forEach { spannable.removeSpan(it) }
        } else {
            spannable.setSpan(
                StyleSpan(Typeface.ITALIC),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    // 设置文本的颜色
    // 先移除已有的颜色样式，再添加新的颜色
    fun setTextColor(editText: EditText, color: Int) {
        val start = editText.selectionStart
        val end = editText.selectionEnd

        if (start < 0 || start >= end) return

        val spannable = editText.text as Spannable
        val existingSpans = spannable.getSpans(start, end, ForegroundColorSpan::class.java)

        existingSpans.forEach { spannable.removeSpan(it) }

        spannable.setSpan(
            ForegroundColorSpan(color),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    // 将 Spanned 对象转换为 HTML 字符串
    fun toHtml(spanned: Spanned): String {
        return Html.toHtml(spanned, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
    }

    // 将 HTML 字符串转换为 Spanned 对象
    fun fromHtml(html: String): Spanned {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(html)
        }
    }

    // 获取 Spanned 对象的纯文本内容
    fun getPlainText(spanned: Spanned): String {
        return spanned.toString()
    }
}