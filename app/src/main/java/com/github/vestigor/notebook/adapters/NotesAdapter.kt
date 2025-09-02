package com.github.vestigor.notebook.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.vestigor.notebook.databinding.ItemNoteBinding
import com.github.vestigor.notebook.models.NoteWithTag
import com.github.vestigor.notebook.utils.DateUtils

// 笔记列表适配器
// 继承自 ListAdapter，用于高效刷新列表
// 负责把 NoteWithTag 数据绑定到 RecyclerView 的 item 上
class NotesAdapter(
    private val onItemClick: (NoteWithTag) -> Unit,
    private val onMoreClick: (NoteWithTag, View) -> Unit
) : ListAdapter<NoteWithTag, NotesAdapter.NoteViewHolder>(NoteDiffCallback()) {

    // 创建 ViewHolder，加载 item 布局
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoteViewHolder(binding)
    }

    // 绑定数据到 ViewHolder
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // 负责绑定数据到单个 item 视图
    inner class NoteViewHolder(
        private val binding: ItemNoteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: NoteWithTag) {
            binding.textViewTitle.text = note.title

            // 限制内容显示为2行，确保卡片高度一致
            binding.textViewContent.apply {
                text = note.content.replace("\n", " ").trim()
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
            }

            // 设置日期
            binding.textViewDate.text = DateUtils.formatDate(note.modifiedDate)

            // 显示标签
            if (note.tagName != null) {
                binding.chipTag.visibility = View.VISIBLE
                binding.chipTag.text = note.tagName
            } else {
                binding.chipTag.visibility = View.GONE
            }

            // 点击笔记卡片，触发回调
            binding.root.setOnClickListener {
                onItemClick(note)
            }

            binding.buttonMore.setOnClickListener {
                onMoreClick(note, it)
            }
        }
    }

    // DiffUtil 回调，用于比较列表数据变化
    class NoteDiffCallback : DiffUtil.ItemCallback<NoteWithTag>() {
        // 判断是否是同一个笔记
        override fun areItemsTheSame(oldItem: NoteWithTag, newItem: NoteWithTag): Boolean {
            return oldItem.id == newItem.id
        }

        // 判断内容是否相同
        override fun areContentsTheSame(oldItem: NoteWithTag, newItem: NoteWithTag): Boolean {
            return oldItem == newItem
        }
    }
}