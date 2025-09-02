package com.github.vestigor.notebook.dialogs

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AlertDialog

class ColorPickerDialog(
    private val context: Context,
    private val onColorSelected: (Int) -> Unit
) {

    private val colors = listOf(
        Pair("黑色", Color.BLACK),
        Pair("红色", Color.RED),
        Pair("蓝色", Color.BLUE),
        Pair("绿色", Color.GREEN),
        Pair("黄色", Color.YELLOW),
        Pair("紫色", Color.parseColor("#9C27B0")),
        Pair("橙色", Color.parseColor("#FF9800")),
        Pair("粉色", Color.parseColor("#E91E63"))
    )

    fun show() {
        val colorNames = colors.map { it.first }.toTypedArray()

        AlertDialog.Builder(context)
            .setTitle("选择颜色")
            .setItems(colorNames) { _, which ->
                onColorSelected(colors[which].second)
            }
            .setNegativeButton("取消", null)
            .show()
    }
}