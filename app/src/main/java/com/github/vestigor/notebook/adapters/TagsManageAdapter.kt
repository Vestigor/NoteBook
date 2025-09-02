package com.github.vestigor.notebook.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.vestigor.notebook.databinding.ItemTagManageBinding
import com.github.vestigor.notebook.models.TagWithCount

// 在标签管理界面显示标签及其相关笔记数量
class TagsManageAdapter(
    // 点击“编辑”按钮时的回调函数
    private val onEditClick: (TagWithCount) -> Unit,
    // 点击“删除”按钮时的回调函数
    private val onDeleteClick: (TagWithCount) -> Unit
) : ListAdapter<TagWithCount, TagsManageAdapter.TagViewHolder>(TagDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val binding = ItemTagManageBinding.inflate(
            LayoutInflater.from(parent.context), // 从父视图获取 LayoutInflater
            parent,
            false
        )
        return TagViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(getItem(position)) // 传入当前位置的 tag
    }

    inner class TagViewHolder(
        private val binding: ItemTagManageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tag: TagWithCount) {
            // 设置标签名称
            binding.textViewTagName.text = tag.name
            // 显示笔记数量
            binding.textViewNoteCount.text = "${tag.noteCount}篇笔记"

            // 编辑按钮点击事件
            binding.buttonEdit.setOnClickListener {
                onEditClick(tag)
            }

            // 删除按钮点击事件
            binding.buttonDelete.setOnClickListener {
                onDeleteClick(tag)
            }
        }
    }

    class TagDiffCallback : DiffUtil.ItemCallback<TagWithCount>() {
        // 判断是否是同一个标签
        override fun areItemsTheSame(oldItem: TagWithCount, newItem: TagWithCount): Boolean {
            return oldItem.id == newItem.id
        }

        // 判断内容是否相同
        override fun areContentsTheSame(oldItem: TagWithCount, newItem: TagWithCount): Boolean {
            return oldItem == newItem
        }
    }
}