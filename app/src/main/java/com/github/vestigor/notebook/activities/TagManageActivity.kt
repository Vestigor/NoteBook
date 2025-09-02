package com.github.vestigor.notebook.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.vestigor.notebook.R
import com.github.vestigor.notebook.adapters.TagsManageAdapter
import com.github.vestigor.notebook.database.entities.Tag
import com.github.vestigor.notebook.databinding.ActivityTagManageBinding
import com.github.vestigor.notebook.models.TagWithCount
import com.github.vestigor.notebook.utils.Constants
import com.github.vestigor.notebook.viewmodels.TagManageViewModel
import android.os.Build

class TagManageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTagManageBinding
    private lateinit var viewModel: TagManageViewModel
    private lateinit var tagsAdapter: TagsManageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTagManageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 处理状态栏遮挡问题
        setupStatusBar()

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "标签管理"

        // 获取 ViewModel 实例
        viewModel = ViewModelProvider(this)[TagManageViewModel::class.java]

        // 初始化 RecyclerView
        setupRecyclerView()

        // 观察 LiveData 数据变化
        setupObservers()
    }

    private fun setupStatusBar() {
        // 处理状态栏遮挡问题
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.decorView.systemUiVisibility =
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.statusBarColor = android.graphics.Color.TRANSPARENT
        }

        // 为根布局添加顶部padding
        binding.root.setPadding(0, getStatusBarHeight(), 0, 0)
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    // 初始化 RecyclerView，用于展示标签
    private fun setupRecyclerView() {
        tagsAdapter = TagsManageAdapter(
            onEditClick = { tag ->  // 点击编辑
                showEditTagDialog(tag)
            },
            onDeleteClick = { tag ->  // 点击删除
                showDeleteTagDialog(tag)
            }
        )

        binding.recyclerViewTags.apply {
            layoutManager = LinearLayoutManager(this@TagManageActivity)  // 列表布局
            adapter = tagsAdapter
        }
    }

    private fun setupObservers() {
        // 标签列表
        viewModel.tagsWithCount.observe(this) { tags ->
            tagsAdapter.submitList(tags)
            binding.textViewHint.text = "最多创建${Constants.MAX_TAG_COUNT}个标签"
        }

        // 标签数量
        viewModel.tagCount.observe(this) { count ->
            binding.textViewHint.text = "已创建 $count/${Constants.MAX_TAG_COUNT} 个标签"
        }
    }

    // 创建菜单
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_tag_manage, menu)
        return true
    }

    // 处理菜单点击事件
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // 返回按钮
            android.R.id.home -> {
                finish()
                true
            }
            // 新增标签按钮
            R.id.action_add -> {
                showAddTagDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // "新增标签"对话框
    private fun showAddTagDialog() {
        // 判断是否超过最大标签数量
        viewModel.tagCount.value?.let { count ->
            if (count >= Constants.MAX_TAG_COUNT) {
                Toast.makeText(this, "已达到最大标签数量限制", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // 创建自定义对话框视图
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_tag, null)
        val editText = dialogView.findViewById<EditText>(R.id.editTextTagName)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("保存") { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotEmpty()) {
                    viewModel.addTag(name)
                    Toast.makeText(this, "标签已添加", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "标签名称不能为空", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    // 显示"编辑标签"对话框
    private fun showEditTagDialog(tag: TagWithCount) {
        // 创建自定义对话框视图
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_tag, null)
        val editText = dialogView.findViewById<EditText>(R.id.editTextTagName)
        editText.setText(tag.name) // 显示原标签名

        // 设置光标到文本末尾
        editText.setSelection(tag.name.length)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("保存") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty() && newName != tag.name) {
                    viewModel.updateTag(tag.id, newName)
                    Toast.makeText(this, "标签已更新", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    // 显示"删除标签"对话框
    // 如果标签下有笔记，会提示笔记也会受影响
    private fun showDeleteTagDialog(tag: TagWithCount) {
        val message = if (tag.noteCount > 0) {
            "确定要删除标签\"${tag.name}\"吗？\n相关的${tag.noteCount}篇笔记将取消此标签"
        } else {
            "确定要删除标签\"${tag.name}\"吗？"
        }

        AlertDialog.Builder(this)
            .setTitle("删除标签")
            .setMessage(message)
            .setPositiveButton("确认删除") { _, _ ->
                viewModel.deleteTag(tag.id)
                Toast.makeText(this, "标签已删除", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }
}