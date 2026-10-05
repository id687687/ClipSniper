package com.kolin.clipsniper.ui

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kolin.clipsniper.data.CleanedItem
import com.kolin.clipsniper.data.ClipQueueManager
import com.kolin.clipsniper.data.NoteRepository
import com.kolin.clipsniper.databinding.ActivitySnippetCollectBinding

class SnippetCollectActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySnippetCollectBinding
    private lateinit var adapter: SnippetCheckAdapter
    private val items = mutableListOf<CleanedItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySnippetCollectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowAppearance()
        loadData()
        setupListeners()
    }

    private fun setupWindowAppearance() {
        // 设置底部弹窗全宽样式
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window.setGravity(Gravity.BOTTOM)
    }

    private fun loadData() {
        items.clear()
        items.addAll(ClipQueueManager.getCleanedList())

        adapter = SnippetCheckAdapter(items) {
            updateButtonState()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        updateButtonState()
    }

    private fun updateButtonState() {
        val selectedCount = items.count { it.isSelected }
        binding.btnSave.text = if (selectedCount > 0) {
            "📥 一键收录到笔记 (${selectedCount}条)"
        } else {
            "请至少选择一条"
        }
        binding.btnSave.isEnabled = selectedCount > 0
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener {
            finish()
        }

        binding.btnSelectAll.setOnClickListener {
            val allSelected = items.all { it.isSelected }
            items.forEach { it.isSelected = !allSelected }
            adapter.notifyDataSetChanged()
            updateButtonState()
        }

        binding.btnSave.setOnClickListener {
            val selectedTexts = items.filter { it.isSelected }.map { it.cleaned }
            if (selectedTexts.isNotEmpty()) {
                NoteRepository(this).addBatch(selectedTexts)
                triggerHapticFeedback()
                Toast.makeText(this, "✅ 成功收录 ${selectedTexts.size} 条文案！", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun triggerHapticFeedback() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(50)
        }
    }
}
