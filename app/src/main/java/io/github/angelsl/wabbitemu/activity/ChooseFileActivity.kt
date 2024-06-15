package io.github.angelsl.wabbitemu.activity

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import io.github.angelsl.wabbitemu.R
import io.github.angelsl.wabbitemu.calc.CalcModel
import io.github.angelsl.wabbitemu.components.SimpleRadioGroup
import io.github.angelsl.wabbitemu.utils.PreferenceConstants
import kotlinx.coroutines.launch

class ChooseFileActivity : ComponentActivity() {
    companion object {
        val strings = mapOf(
            "ti73" to "TI-73",
            "ti84p" to "TI-84 Plus",
            "ti83p" to "TI-83 Plus",
            "ti85" to "TI-85",
            "ti81" to "TI-81",
            "ti82" to "TI-82",
            "ti86" to "TI-86",
            "ti84pse" to "TI-84 Plus SE",
            "ti84pcse" to "TI-84 Plus C SE",
            "ti83pse" to "TI-83 Plus SE",
            "ti83" to "TI-83",
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Wabbitemu)
        setContent {

            val scope = rememberCoroutineScope()
            var selectedModel by remember {
                mutableStateOf(CalcModel.NO_CALC)
            }

            // Create an ActivityResultLauncher for the file picker intent
            val filePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument(),
                onResult = { uri: Uri? ->
                    uri?.let {
                        val fileName = getFileName(it)
                        if (isValidFile(fileName)) {
                            // Handle the file as needed
                            scope.launch {
                                handleFile(uri, selectedModel)
                            }
                        } else {
                            Toast.makeText(
                                this@ChooseFileActivity,
                                "Please select a .rom or .sav file",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )


            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "A ROM file is needed to make this app work. Previous versions included a wizard/scraper bot for downloading a ROM, but Texas Instruments updated their website, and the scraper bot stopped working. The project is open-source, please feel free to contribute.")
                SimpleRadioGroup(
                    CalcModel.entries,
                    selectedModel,
                    { strings[it.name.replace("_", "").lowercase()] ?: "None" }) {
                    selectedModel = it
                }
                Button(onClick = {
                    filePickerLauncher.launch(arrayOf("*/*"))
                }) {
                    Text("Choose ROM file")
                }
            }
        }
    }


    private fun isValidFile(fileName: String?): Boolean {
        return fileName != null && (fileName.endsWith(".rom") || fileName.endsWith(".sav"))
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            cursor.use {
                if (it != null && it.moveToFirst()) {
                    result = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }

    private fun handleFile(uri: Uri, model: CalcModel) {
        // Your code to handle the file here
        // For example, read the file, process its content, etc.
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.edit()
            .putString(PreferenceConstants.ROM_PATH.toString(), uri.toString())
            .putBoolean(PreferenceConstants.FIRST_RUN.toString(), false)
            .putInt(PreferenceConstants.ROM_MODEL.toString(), model.modelInt)
            .apply()

        Toast.makeText(this@ChooseFileActivity, "File good! $uri", Toast.LENGTH_LONG).show()
        startActivity(Intent(this, WabbitemuActivity::class.java))
    }
}