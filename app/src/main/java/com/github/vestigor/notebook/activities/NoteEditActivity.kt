package com.github.vestigor.notebook.activities

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.github.vestigor.notebook.R
import com.github.vestigor.notebook.database.entities.Tag
import com.github.vestigor.notebook.databinding.ActivityNoteEditBinding
import com.github.vestigor.notebook.dialogs.ColorPickerDialog
import com.github.vestigor.notebook.dialogs.TagSelectDialog
import com.github.vestigor.notebook.utils.Constants
import com.github.vestigor.notebook.utils.PreferencesManager
import com.github.vestigor.notebook.utils.TextFormatUtils
import com.github.vestigor.notebook.viewmodels.NoteEditViewModel
import com.google.android.material.chip.Chip
import android.graphics.Typeface
import android.view.KeyEvent
import android.text.InputFilter
import android.view.WindowManager
import android.os.Build
import android.text.Spannable

class NoteEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteEditBinding
    private lateinit var viewModel: NoteEditViewModel

    private var noteId: Long = 0
    private var hasChanges = false
    private var originalTitle = ""
    private var originalContent = ""

    // 格式化状态
    private var isBoldEnabled = false
    private var isItalicEnabled = false
    private var currentTextColor: Int = Color.BLACK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 处理状态栏遮挡问题
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.decorView.systemUiVisibility =
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.statusBarColor = android.graphics.Color.TRANSPARENT
        }

        // 添加顶部padding避免状态栏遮挡
        binding.root.setPadding(0, getStatusBarHeight(), 0, 0)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "编辑笔记"

        viewModel = ViewModelProvider(this)[NoteEditViewModel::class.java]

        noteId = intent.getLongExtra(Constants.EXTRA_NOTE_ID, 0)

        setupViews()
        setupObservers()
        setupListeners()

        if (noteId > 0) {
            viewModel.loadNote(noteId)
        }
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
        // 使用固定的字体大小（16sp）
        binding.editTextContent.textSize = 16f

        // 设置标题输入限制，用于过滤回车符
        val noNewLineFilter = InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                if (source[i] == '\n' || source[i] == '\r') {
                    // 直接忽略回车符，返回空字符串
                    return@InputFilter ""
                }
            }
            null // 接受其他字符
        }

        binding.editTextTitle.filters = arrayOf(
            noNewLineFilter,
            InputFilter.LengthFilter(Constants.MAX_TITLE_LENGTH)
        )

        // 标题输入监听
        binding.editTextTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                binding.textViewTitleCount.text = "$length/${Constants.MAX_TITLE_LENGTH}"
                checkForChanges()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 禁止标题输入框响应回车键
        binding.editTextTitle.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                // 拦截回车键，不做任何处理
                true
            } else {
                false
            }
        }

        // 内容输入监听，支持实时格式化
        binding.editTextContent.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkForChanges()

                // 防止循环调用
                if (isFormatting) return

                // 只对新输入的文字应用格式（count > 0 表示有新字符输入）
                if (count > 0 && before == 0) {  // 确保是输入而不是替换
                    isFormatting = true

                    val spannable = binding.editTextContent.text
                    val inputStart = start
                    val inputEnd = start + count

                    // 应用当前激活的格式到新输入的文字
                    if (isBoldEnabled) {
                        spannable.setSpan(
                            StyleSpan(Typeface.BOLD),
                            inputStart,
                            inputEnd,
                            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    if (isItalicEnabled) {
                        spannable.setSpan(
                            StyleSpan(Typeface.ITALIC),
                            inputStart,
                            inputEnd,
                            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    if (currentTextColor != Color.BLACK) {
                        spannable.setSpan(
                            ForegroundColorSpan(currentTextColor),
                            inputStart,
                            inputEnd,
                            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    isFormatting = false
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // 让系统自动管理光标位置
            }
        })

        // 初始化格式按钮状态
        initializeFormatButtons()
    }

    private fun checkForChanges() {
        val currentTitle = binding.editTextTitle.text.toString()
        val currentContent = binding.editTextContent.text.toString()
        hasChanges = (currentTitle != originalTitle || currentContent != originalContent)
    }

    private fun initializeFormatButtons() {
        // 初始化时，所有格式按钮使用相同的默认颜色
        val defaultColor = getColor(R.color.format_button_default)

        binding.buttonBold.apply {
            setColorFilter(defaultColor)
            setBackgroundColor(Color.TRANSPARENT)
            alpha = 1.0f
        }

        binding.buttonItalic.apply {
            setColorFilter(defaultColor)
            setBackgroundColor(Color.TRANSPARENT)
            alpha = 1.0f
        }

        binding.buttonColor.apply {
            setColorFilter(defaultColor)
            setBackgroundColor(Color.TRANSPARENT)
            alpha = 1.0f
        }
    }

    private fun setupObservers() {
        viewModel.currentNote.observe(this) { note ->
            note?.let {
                originalTitle = it.title
                originalContent = it.content

                binding.editTextTitle.setText(it.title)

                // 恢复富文本内容
                val spannableContent = TextFormatUtils.fromHtml(it.formattedContent)
                binding.editTextContent.setText(spannableContent)

                binding.textViewModifiedTime.text = "最后修改：${com.github.vestigor.notebook.utils.DateUtils.formatDate(it.modifiedDate)}"
                hasChanges = false
            }
        }

        viewModel.selectedTag.observe(this) { tag ->
            updateTagChip(tag)
        }

        viewModel.tags.observe(this) { tags ->
            // 标签列表已加载
        }
    }

    private fun setupListeners() {
        binding.chipTag.setOnClickListener {
            showTagSelectDialog()
        }

        // 加粗按钮，切换状态模式
        binding.buttonBold.setOnClickListener {
            isBoldEnabled = !isBoldEnabled
            updateFormatButtonState()

            // 如果有选中的文字，对选中部分应用/取消格式
            val start = binding.editTextContent.selectionStart
            val end = binding.editTextContent.selectionEnd
            if (start >= 0 && end > start) {
                TextFormatUtils.toggleBold(binding.editTextContent)
            }
        }

        // 斜体按钮，切换状态模式
        binding.buttonItalic.setOnClickListener {
            isItalicEnabled = !isItalicEnabled
            updateFormatButtonState()

            // 如果有选中的文字，对选中部分应用/取消格式
            val start = binding.editTextContent.selectionStart
            val end = binding.editTextContent.selectionEnd
            if (start >= 0 && end > start) {
                TextFormatUtils.toggleItalic(binding.editTextContent)
            }
        }

        // 颜色按钮
        binding.buttonColor.setOnClickListener {
            showColorPickerDialog()
        }
    }

    private fun updateFormatButtonState() {
        val defaultColor = getColor(R.color.format_button_default)
        val activeBackgroundColor = getColor(R.color.format_button_active_bg)
        val activeIconColor = getColor(R.color.format_button_active_icon)

        // 更新加粗按钮状态
        binding.buttonBold.apply {
            if (isBoldEnabled) {
                setBackgroundColor(activeBackgroundColor)
                setColorFilter(activeIconColor)
            } else {
                setBackgroundColor(Color.TRANSPARENT)
                setColorFilter(defaultColor)
            }
        }

        // 更新斜体按钮状态
        binding.buttonItalic.apply {
            if (isItalicEnabled) {
                setBackgroundColor(activeBackgroundColor)
                setColorFilter(activeIconColor)
            } else {
                setBackgroundColor(Color.TRANSPARENT)
                setColorFilter(defaultColor)
            }
        }

        // 更新颜色按钮状态
        binding.buttonColor.apply {
            if (currentTextColor != Color.BLACK) {
                setColorFilter(currentTextColor)
            } else {
                setColorFilter(defaultColor)
            }
        }
    }

    private fun updateTagChip(tag: Tag?) {
        binding.chipTag.text = tag?.name ?: "选择标签"
        tag?.let {
            try {
                binding.chipTag.setChipBackgroundColorResource(android.R.color.white)
            } catch (e: Exception) {
                // 使用默认颜色
            }
        }
    }

    private fun showTagSelectDialog() {
        val tags = viewModel.tags.value ?: return
        val currentTag = viewModel.selectedTag.value

        TagSelectDialog(this, tags, currentTag) { selectedTag ->
            viewModel.selectTag(selectedTag)
        }.show()
    }

    private fun showColorPickerDialog() {
        ColorPickerDialog(this) { color ->
            currentTextColor = color
            updateFormatButtonState()

            // 如果有选中的文字，对选中部分应用颜色
            val start = binding.editTextContent.selectionStart
            val end = binding.editTextContent.selectionEnd
            if (start >= 0 && end > start) {
                if (color == Color.BLACK) {
                    // 如果选择黑色，移除颜色格式
                    val spannable = binding.editTextContent.text as Spannable
                    val colorSpans = spannable.getSpans(start, end, ForegroundColorSpan::class.java)
                    colorSpans.forEach { spannable.removeSpan(it) }
                } else {
                    TextFormatUtils.setTextColor(binding.editTextContent, color)
                }
            }
        }.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_note_edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_save -> {
                saveNote()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        val title = binding.editTextTitle.text.toString().trim()
        val content = binding.editTextContent.text.toString().trim()

        // 如果没有任何改变，直接返回
        if (!hasChanges) {
            super.onBackPressed()
            return
        }

        // 如果标题和内容都为空，直接返回，不保存
        if (title.isEmpty() && content.isEmpty()) {
            super.onBackPressed()
            return
        }

        // 如果有内容但没有标题，提示输入标题
        if (title.isEmpty() && content.isNotEmpty()) {
            showTitleInputDialog(content)
            return
        }

        // 如果有标题（无论是否有内容），询问是否保存
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_save_changes, null)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("保存") { _, _ ->
                saveNote()
            }
            .setNegativeButton("放弃") { _, _ ->
                super.onBackPressed()
            }
            .setNeutralButton("取消", null)
            .show()
    }

    private fun showTitleInputDialog(content: String) {
        // 创建自定义对话框视图
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_input_title, null)
        val editText = dialogView.findViewById<EditText>(R.id.editTextTitle)

        // 设置输入限制
        editText.filters = arrayOf(InputFilter.LengthFilter(Constants.MAX_TITLE_LENGTH))

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("保存") { _, _ ->
                val title = editText.text.toString().trim()
                if (title.isNotEmpty()) {
                    binding.editTextTitle.setText(title)
                    saveNote()
                } else {
                    Toast.makeText(this, "标题不能为空", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("放弃笔记") { _, _ ->
                super.onBackPressed()
            }
            .setNeutralButton("继续编辑", null)
            .show()
    }

    private fun saveNote() {
        val title = binding.editTextTitle.text.toString().trim()
        val content = binding.editTextContent.text.toString()

        // 如果标题为空，不保存
        if (title.isEmpty()) {
            Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show()
            return
        }

        // 保存富文本格式
        val formattedContent = TextFormatUtils.toHtml(binding.editTextContent.text)

        viewModel.saveNote(title, content, formattedContent)
        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
        hasChanges = false
        finish()
    }
}