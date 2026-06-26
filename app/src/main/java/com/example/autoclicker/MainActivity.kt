package com.example.autoclicker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.autoclicker.databinding.ActivityMainBinding
import com.google.android.material.chip.Chip

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val serviceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateServiceStatus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupKeywordChips()

        binding.btnOpenSettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        binding.btnAddKeyword.setOnClickListener {
            showAddKeywordDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        updateServiceStatus()
        val filter = IntentFilter().apply {
            addAction(AutoClickerService.ACTION_SERVICE_CONNECTED)
            addAction(AutoClickerService.ACTION_SERVICE_DISCONNECTED)
        }
        registerReceiver(serviceReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(serviceReceiver)
    }

    private fun updateServiceStatus() {
        val enabled = isAccessibilityServiceEnabled()
        if (enabled) {
            binding.tvStatus.text = getString(R.string.status_active)
            binding.tvStatus.setTextColor(getColor(R.color.status_active))
            binding.statusIndicator.setBackgroundColor(getColor(R.color.status_active))
            binding.btnOpenSettings.text = getString(R.string.btn_manage_service)
        } else {
            binding.tvStatus.text = getString(R.string.status_inactive)
            binding.tvStatus.setTextColor(getColor(R.color.status_inactive))
            binding.statusIndicator.setBackgroundColor(getColor(R.color.status_inactive))
            binding.btnOpenSettings.text = getString(R.string.btn_enable_service)
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val serviceName = "${packageName}/${AutoClickerService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabledServices)
        while (splitter.hasNext()) {
            if (splitter.next().equals(serviceName, ignoreCase = true)) return true
        }
        return false
    }

    private fun setupKeywordChips() {
        binding.chipGroupKeywords.removeAllViews()
        for (keyword in AutoClickerService.targetKeywords) {
            addChip(keyword)
        }
    }

    private fun addChip(keyword: String) {
        val chip = Chip(this).apply {
            text = keyword
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                AutoClickerService.targetKeywords.remove(keyword)
                binding.chipGroupKeywords.removeView(this)
            }
        }
        binding.chipGroupKeywords.addView(chip)
    }

    private fun showAddKeywordDialog() {
        val input = EditText(this).apply {
            hint = getString(R.string.keyword_hint)
            setPadding(48, 32, 48, 16)
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_add_keyword_title)
            .setView(input)
            .setPositiveButton(R.string.add) { _, _ ->
                val keyword = input.text.toString().trim()
                if (keyword.isNotEmpty() && !AutoClickerService.targetKeywords.contains(keyword)) {
                    AutoClickerService.targetKeywords.add(keyword)
                    addChip(keyword)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
