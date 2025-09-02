package com.github.vestigor.notebook.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.github.vestigor.notebook.database.entities.Note
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.BufferedWriter
import java.text.SimpleDateFormat
import java.util.*

// 导出工具类
object ExportUtils {

    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    fun exportToTxt(context: Context, note: Note): Uri? {
        return try {
            val fileName = "Note_${dateFormat.format(Date())}.txt"

            // 使用公共文档目录
            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            if (!documentsDir.exists()) {
                documentsDir.mkdirs()
            }

            val file = File(documentsDir, fileName)

            // 使用UTF-8 with BOM编码写入文件，确保Windows记事本等程序能正确识别
            FileOutputStream(file).use { fos ->
                // 写入UTF-8 BOM (Byte Order Mark)
                fos.write(0xEF)
                fos.write(0xBB)
                fos.write(0xBF)

                // 使用BufferedWriter提高写入效率
                BufferedWriter(OutputStreamWriter(fos, "UTF-8")).use { writer ->
                    writer.write("标题: ${note.title}\n")
                    writer.write("创建时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(note.createdDate)}\n")
                    writer.write("修改时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(note.modifiedDate)}\n")
                    writer.write("\n")
                    writer.write("内容:\n")
                    // 将富文本内容转换为纯文本
                    val plainContent = if (note.formattedContent.isNotEmpty()) {
                        // 移除HTML标签，只保留纯文本
                        android.text.Html.fromHtml(note.formattedContent, android.text.Html.FROM_HTML_MODE_LEGACY).toString()
                    } else {
                        note.content
                    }
                    writer.write(plainContent)
                    writer.flush()
                }
            }

            // 通知系统文件已创建
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = Uri.fromFile(file)
            context.sendBroadcast(mediaScanIntent)

            // 提示用户文件保存位置
            Toast.makeText(context, "已保存到: Documents/$fileName", Toast.LENGTH_LONG).show()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }

    // 简化的保存文件方法，只支持TXT格式
    fun saveFileLocally(context: Context, note: Note): Boolean {
        return exportToTxt(context, note) != null
    }
}