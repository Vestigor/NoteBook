package com.github.vestigor.notebook.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.vestigor.notebook.adapters.NotesAdapter
import com.github.vestigor.notebook.databinding.ActivitySearchBinding
import com.github.vestigor.notebook.utils.Constants
import com.github.vestigor.notebook.viewmodels.SearchViewModel
import android.os.Build

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding  // 视图绑定对象
    private lateinit var viewModel: SearchViewModel      // 搜索功能对应的 ViewModel
    private lateinit var searchAdapter: NotesAdapter     // 搜索结果列表的适配器

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 处理状态栏遮挡问题
        setupStatusBar()

        // 设置顶部工具栏
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)  // 启用返回按钮
        supportActionBar?.title = "搜索笔记"                 // 设置标题

        // 初始化 ViewModel
        viewModel = ViewModelProvider(this)[SearchViewModel::class.java]

        // 初始化功能模块
        setupRecyclerView()  // 设置结果列表
        setupSearch()        // 设置搜索输入框逻辑
        setupObservers()     // 观察搜索结果
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

    // 初始化 RecyclerView，用于展示搜索结果
    private fun setupRecyclerView() {
        searchAdapter = NotesAdapter(
            // 点击搜索结果 -> 打开笔记编辑页面
            onItemClick = { note ->
                val intent = Intent(this, NoteEditActivity::class.java)
                intent.putExtra(Constants.EXTRA_NOTE_ID, note.id)
                startActivity(intent)
            },
            onMoreClick = { _, _ -> }
        )

        binding.recyclerViewResults.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)  // 竖直线性布局
            adapter = searchAdapter
        }
    }

    // 设置搜索输入框逻辑
    private fun setupSearch() {
        // 监听输入框文本变化
        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val keyword = s?.toString()?.trim() ?: ""
                if (keyword.isNotEmpty()) {
                    // 输入非空时执行搜索
                    viewModel.search(keyword)
                } else {
                    // 输入为空时清空结果
                    searchAdapter.submitList(emptyList())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 点击"清空"按钮 -> 清空搜索框
        binding.buttonClear.setOnClickListener {
            binding.editTextSearch.text.clear()
        }

        // 自动弹出键盘
        binding.editTextSearch.requestFocus()
    }

    private fun setupObservers() {
        // 更新 RecyclerView 数据
        viewModel.searchResults.observe(this) { results ->
            searchAdapter.submitList(results)

            // 如果结果为空且输入框非空，显示"没有结果"视图
            binding.emptyView.visibility = if (results.isEmpty() &&
                binding.editTextSearch.text.isNotEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {  // 点击左上角返回
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}