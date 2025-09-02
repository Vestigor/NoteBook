package com.github.vestigor.notebook.dialogs

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.github.vestigor.notebook.database.entities.Tag

class TagSelectDialog(
    private val context: Context,
    private val tags: List<Tag>,
    private val currentTag: Tag?,
    private val onTagSelected: (Tag?) -> Unit
) {

    fun show() {
        val tagNames = listOf("无标签") + tags.map { it.name }
        val checkedItem = if (currentTag == null) 0 else tags.indexOf(currentTag) + 1

        AlertDialog.Builder(context)
            .setTitle("选择标签")
            .setSingleChoiceItems(tagNames.toTypedArray(), checkedItem) { dialog, which ->
                val selectedTag = if (which == 0) null else tags[which - 1]
                onTagSelected(selectedTag)
                dialog.dismiss()
            }
            .setNegativeButton("取消", null)
            .show()
    }
}