package io.github.angelsl.wabbitemu.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import io.github.angelsl.wabbitemu.utils.PreferenceConstants

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        startActivity(
            Intent(
                this,
                if (prefs.getString(PreferenceConstants.ROM_PATH.toString(), "").isNullOrEmpty())
                    ChooseFileActivity::class.java
                else
                    WabbitemuActivity::class.java
            )
        )
    }

    companion object {
        const val ROM_PATH_KEY = "ROM_PATH"
        const val PREFS_NAME = "settings"
    }
}