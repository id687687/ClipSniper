package com.kolin.clipsniper.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kolin.clipsniper.data.NoteRepository
import com.kolin.clipsniper.data.Snippet
import com.kolin.clipsniper.databinding.ActivityMainBinding
import com.kolin.clipsniper.service.FloatingBubbleService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: NoteRepository
    private lateinit var adapter: SavedSnippetAdapter
    private val snippetList = mutableListOf<Snippet>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = NoteRepository(this)

        setupRecyclerView()
        setupActions()
    }

    override fun onResume() {
        super.onResume()
        refreshList()
        updateServiceSwitchState()
    }

    private fun setupRecyclerView() {
        adapter = SavedSnippetAdapter(
            list = snippetList,
            onCopy = { snippet ->
                copyToClipboard(snippet.content)
                Toast.makeText(this, "已复制到剪贴板！", Toast.LENGTH_SHORT).show()
            },
            onDelete = { snippet ->
                repository.delete(snippet.id)
                refreshList()
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun refreshList() {
        snippetList.clear()
        snippetList.addAll(repository.getAll())
        adapter.notifyDataSetChanged()
        binding.tvCount.text = "共已收集 ${snippetList.size} 条精选文案"
    }

    private fun updateServiceSwitchState() {
        // 检查悬浮窗权限状态
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.switchService.isChecked = Settings.canDrawOverlays(this)
        }
    }

    private fun setupActions() {
        // 悬浮球开关
        binding.switchService.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkOverlayPermissionAndStart()
            } else {
                stopService(Intent(this, FloatingBubbleService::class.java))
            }
        }

        // 一键全部复制
        binding.btnCopyAll.setOnClickListener {
            if (snippetList.isEmpty()) {
                Toast.makeText(this, "暂无收录内容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val allText = snippetList.joinToString("\n\n---\n\n") { it.content }
            copyToClipboard(allText)
            Toast.makeText(this, "✅ 已全部复制到剪贴板！", Toast.LENGTH_SHORT).show()
        }

        // 分享导出
        binding.btnExport.setOnClickListener {
            if (snippetList.isEmpty()) {
                Toast.makeText(this, "暂无收录内容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, repository.exportToMarkdown())
            }
            startActivity(Intent.createChooser(shareIntent, "导出段子库至..."))
        }

        // 清空确认
        binding.btnClearAll.setOnClickListener {
            if (snippetList.isEmpty()) return@setOnClickListener
            AlertDialog.Builder(this)
                .setTitle("确认清空")
                .setMessage("确定要清空所有已收集的文案吗？此操作无法撤销。")
                .setPositiveButton("清空") { _, _ ->
                    repository.clear()
                    refreshList()
                }
                .setNegativeButton("取消", null)
                .show()
        }
    }

    private fun checkOverlayPermissionAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "请授予“悬浮窗”权限以开启剪刀手小胶囊", Toast.LENGTH_LONG).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        } else {
            val intent = Intent(this, FloatingBubbleService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            Toast.makeText(this, "✂️ 悬浮剪刀手已开启，长按微信群复制即可！", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyToClipboard(text: String) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("snippet", text))
    }
}
