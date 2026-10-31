package io.github.isaquefontinele.lido

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import io.github.isaquefontinele.lido.capture.CaptureResult
import io.github.isaquefontinele.lido.capture.CaptureService
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/** Invisible share target: saves the shared link, confirms with a toast and closes. */
class ShareActivity : ComponentActivity() {
    private val captureService: CaptureService by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) return

        val text = intent.takeIf { it.action == Intent.ACTION_SEND }
            ?.getStringExtra(Intent.EXTRA_TEXT)
        if (text == null) {
            finish()
            return
        }

        lifecycleScope.launch {
            val result = captureService.saveSharedText(text, intent.getStringExtra(Intent.EXTRA_SUBJECT))
            val message = when (result) {
                is CaptureResult.Saved -> R.string.share_saved
                CaptureResult.NoUrl -> R.string.share_no_url
            }
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
