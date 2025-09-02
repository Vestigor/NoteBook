package com.github.vestigor.notebook.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.vestigor.notebook.databinding.ActivitySettingsBinding
import android.os.Build

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 使用 ViewBinding 加载布局
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 处理状态栏遮挡问题
        setupStatusBar()

        // 设置顶部工具栏，并启用返回按钮
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "设置"

        // 初始化 UI 显示
        setupViews()

        // 初始化事件监听器
        setupListeners()
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

    private fun setupViews() {
        // 设置版本号
        binding.textViewVersion.text = "v1.0.0"
    }

    private fun setupListeners() {
        binding.layoutAbout.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("关于")
            .setMessage("""
                Simple Notes
                版本：1.0.0
                
                一款简单易用的笔记应用
                
                功能特点：
                • 支持富文本编辑
                • 标签分类管理
                • 导出TXT文件
                • 快速搜索
                • 智能保存提醒
                
                © 2025 All rights reserved
            """.trimIndent())
            .setPositiveButton("确定", null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}