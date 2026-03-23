/**
 * MainActivity.kt  —  ScratchMagic demo
 *
 * Author : Sangita Patel
 * GitHub : https://github.com/sangitapatel
 */

package com.sangitapatel.scratchmagic.demo

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sangitapatel.scratchmagic.ScratchMagicView
import com.sangitapatel.scratchmagic.demo.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding

    private val foilUrl = "https://picsum.photos/seed/smv2024/900/450"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        card1_solidColor()
        card2_localDrawable()
        card3_urlImage()
    }

    // ── Card 1 : Solid colour foil ────────────────────────────────────────────

    private fun card1_solidColor() {
        b.smvColor.listener = object : ScratchMagicView.ScratchListener {
            override fun onProgress(view: ScratchMagicView, percent: Float) {
                val pct = percent.toInt()
                b.tvPct1.text    = "Scratched: $pct%"
                b.prog1.progress = pct
            }
            override fun onDone(view: ScratchMagicView) {
                toast("Card 1 — revealed! 🎉")
            }
        }
        b.btnReveal1.setOnClickListener { b.smvColor.reveal() }
        b.btnReset1.setOnClickListener {
            b.smvColor.reset()
            b.tvPct1.text    = "Scratched: 0%"
            b.prog1.progress = 0
        }
    }

    // ── Card 2 : Local drawable foil ──────────────────────────────────────────

    private fun card2_localDrawable() {
        b.smvDrawable.listener = object : ScratchMagicView.ScratchListener {
            override fun onProgress(view: ScratchMagicView, percent: Float) { }
            override fun onDone(view: ScratchMagicView) {
                toast("Card 2 — revealed! 🏆")
            }
        }
        b.btnReveal2.setOnClickListener { b.smvDrawable.reveal() }
        b.btnReset2.setOnClickListener  { b.smvDrawable.reset() }
    }

    // ── Card 3 : URL image (no third-party lib) ───────────────────────────────

    private fun card3_urlImage() {
        loadFoilFromUrl(foilUrl)

        b.smvUrl.listener = object : ScratchMagicView.ScratchListener {
            override fun onProgress(view: ScratchMagicView, percent: Float) {
                b.tvStatus3.text = "Scratched: ${percent.toInt()}%"
            }
            override fun onDone(view: ScratchMagicView) {
                b.tvStatus3.text = "✅ Revealed!"
                toast("Card 3 — revealed! 💰")
            }
        }
        b.btnReveal3.setOnClickListener { b.smvUrl.reveal() }
        b.btnReset3.setOnClickListener {
            b.smvUrl.reset()
            loadFoilFromUrl(foilUrl)
            b.tvStatus3.text = "⏳ Loading foil image…"
        }
    }

    private fun loadFoilFromUrl(url: String) {
        lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    val conn = URL(url).openConnection() as HttpURLConnection
                    conn.connectTimeout = 8000
                    conn.readTimeout    = 8000
                    conn.connect()
                    val bmp = BitmapFactory.decodeStream(conn.inputStream)
                    conn.disconnect()
                    bmp
                } catch (e: Exception) {
                    null
                }
            }
            if (bitmap != null) {
                b.smvUrl.foilDrawable = BitmapDrawable(resources, bitmap)
            } else {
                b.tvStatus3.text = "❌ Image load failed"
            }
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
