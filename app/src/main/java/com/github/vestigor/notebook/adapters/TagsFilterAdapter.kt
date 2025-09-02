package com.github.vestigor.notebook.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.vestigor.notebook.database.entities.Tag
import com.github.vestigor.notebook.databinding.ItemTagFilterBinding
// 标签筛选适配器
class TagsFilterAdapter(
    private val onTagClick: (Tag?) -> Unit
) : ListAdapter<Tag, TagsFilterAdapter.TagViewHolder>(TagDiffCallback()) {

    // 当前选中的标签 ID（null 表示没有选中）
    private var selectedTagId: Long? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val binding = ItemTagFilterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TagViewHolder(binding)
    }

    // 绑定数据到 ViewHolder
    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // 设置选中的标签，并刷新界面
    fun setSelectedTag(tagId: Long?) {
        selectedTagId = tagId
        notifyDataSetChanged()
    }

    inner class TagViewHolder(
        private val binding: ItemTagFilterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tag: Tag) {
            // 设置标签文字
            binding.chip.text = tag.name
            // 判断当前标签是否被选中
            binding.chip.isChecked = tag.id == selectedTagId

            // 如果再次点击已选中的标签 -> 取消选择；否则 -> 选中新标签
            binding.chip.setOnClickListener {
                onTagClick(if (tag.id == selectedTagId) null else tag)
                setSelectedTag(if (tag.id == selectedTagId) null else tag.id)
            }
        }
    }

    class TagDiffCallback : DiffUtil.ItemCallback<Tag>() {
        override fun areItemsTheSame(oldItem: Tag, newItem: Tag): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Tag, newItem: Tag): Boolean {
            return oldItem == newItem
        }
    }
}