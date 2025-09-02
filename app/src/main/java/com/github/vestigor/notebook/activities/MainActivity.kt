package com.github.vestigor.notebook.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.github.vestigor.notebook.R
import com.github.vestigor.notebook.adapters.NotesAdapter
import com.github.vestigor.notebook.adapters.TagsFilterAdapter
import com.github.vestigor.notebook.database.entities.Tag
import com.github.vestigor.notebook.databinding.ActivityMainBinding
import com.github.vestigor.notebook.models.NoteWithTag
import com.github.vestigor.notebook.utils.Constants
import com.github.vestigor.notebook.utils.ExportUtils
import com.github.vestigor.notebook.viewmodels.MainViewModel
import com.google.android.material.chip.Chip

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding // 视图绑定对象
    private lateinit var viewModel: MainViewModel     // ViewModel，管理笔记和标签数据
    private lateinit var notesAdapter: NotesAdapter   // 笔记列表适配器

    // 动态权限请求启动器
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化视图绑定
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 处理状态栏遮挡问题
        setupStatusBar()

        // 设置顶部工具栏
        setSupportActionBar(binding.toolbar)

        // 获取 ViewModel
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // 初始化各个模块
        setupRecyclerView()  // 笔记列表
        setupTagsFilter()    // 标签筛选
        setupObservers()     // LiveData 观察者
        setupListeners()     // 事件监听器
        checkPermissions()   // 检查存储权限

        // 初始化时选择"全部"标签，显示所有笔记
        viewModel.selectTag(null)
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

    // 初始化笔记列表
    private fun setupRecyclerView() {
        notesAdapter = NotesAdapter(
            // 点击笔记 -> 打开编辑页面
            onItemClick = { note ->
                val intent = Intent(this, NoteEditActivity::class.java)
                intent.putExtra(Constants.EXTRA_NOTE_ID, note.id)
                startActivity(intent)
            },

            // 点击"更多"按钮 -> 弹出菜单
            onMoreClick = { note, view ->
                showNoteOptionsMenu(note, view)
            }
        )

        binding.recyclerViewNotes.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = notesAdapter

            // 设置固定的item高度以保持一致性
            setHasFixedSize(true)
        }
    }

    private fun setupTagsFilter() {
        // 添加"全部"标签
        val allChip = Chip(this).apply {
            text = "全部"
            isCheckable = true
            isChecked = true
            setChipBackgroundColorResource(R.color.chip_selected)
            setOnClickListener {
                viewModel.selectTag(null)       // 显示全部笔记
                updateChipSelection(this)  // 更新 UI 选中状态
            }
        }
        binding.chipGroupTags.addView(allChip)
    }

    private fun setupObservers() {
        // 观察笔记数据变化
        viewModel.notes.observe(this) { notes ->
            notesAdapter.submitList(notes)
            // 如果没有笔记
            binding.emptyView.visibility = if (notes.isEmpty()) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
        }

        // 观察标签数据变化
        viewModel.tags.observe(this) { tags ->
            updateTagChips(tags)
        }
    }

    // 更新标签 Chip
    private fun updateTagChips(tags: List<Tag>) {
        // 保留第一个"全部"标签
        while (binding.chipGroupTags.childCount > 1) {
            binding.chipGroupTags.removeViewAt(1)
        }

        // 动态添加标签
        tags.forEach { tag ->
            val chip = Chip(this).apply {
                text = tag.name
                isCheckable = true
                setChipBackgroundColorResource(R.color.chip_background)
                setOnClickListener {
                    viewModel.selectTag(tag.id)  // 按标签筛选笔记
                    updateChipSelection(this)
                }
            }
            binding.chipGroupTags.addView(chip)
        }
    }

    // 更新 Chip 的选中状态
    private fun updateChipSelection(selectedChip: Chip) {
        for (i in 0 until binding.chipGroupTags.childCount) {
            val chip = binding.chipGroupTags.getChildAt(i) as Chip
            chip.isChecked = chip == selectedChip
            if (chip.isChecked) {
                chip.setChipBackgroundColorResource(R.color.chip_selected)
            } else {
                chip.setChipBackgroundColorResource(R.color.chip_background)
            }
        }
    }

    // 设置事件监听器
    private fun setupListeners() {
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, NoteEditActivity::class.java))
        }
    }

    // 初始化菜单
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // 处理菜单点击事件
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {   // 搜索笔记
                startActivity(Intent(this, SearchActivity::class.java))
                true
            }
            R.id.action_tags -> {     // 标签管理
                startActivity(Intent(this, TagManageActivity::class.java))
                true
            }
            R.id.action_settings -> {  // 设置
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showNoteOptionsMenu(note: NoteWithTag, view: android.view.View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.menu_note_options, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                // 删除笔记
                R.id.action_delete -> {
                    showDeleteConfirmDialog(note)
                    true
                }
                // 导出为 TXT
                R.id.action_export_txt -> {
                    // 直接保存到本地
                    val success = ExportUtils.saveFileLocally(this, note.toNote())
                    if (!success) {
                        Toast.makeText(this, "导出失败", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun showDeleteConfirmDialog(note: NoteWithTag) {
        AlertDialog.Builder(this)
            .setTitle("确认删除")
            .setMessage("确定要删除笔记\"${note.title}\"吗？\n此操作不可恢复")
            .setPositiveButton("确认删除") { _, _ ->
                viewModel.deleteNote(note.id)
                Toast.makeText(this, "笔记已删除", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    // 检查存储权限
    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    // Android 10及以上不需要存储权限即可写入Documents目录
                }
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // 权限已授予
                }
                shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                    AlertDialog.Builder(this)
                        .setTitle("需要存储权限")
                        .setMessage("导出笔记需要存储权限")
                        .setPositiveButton("授予权限") { _, _ ->
                            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                        .setNegativeButton("取消", null)
                        .show()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        }
    }
}