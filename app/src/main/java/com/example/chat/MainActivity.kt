package com.example.chat

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class MainActivity : AppCompatActivity() {

    // 1) DECLARACIÓN: dentro de la clase, fuera de cualquier función
    private lateinit var tvMissatges: TextView
    private lateinit var scrollMissatges: ScrollView
    private lateinit var etMissatge: EditText
    private lateinit var btnEnvia: Button
    private var wsclient: MyWebSocketClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 2) ASIGNACIÓN: dentro de onCreate, después de setContentView
        tvMissatges = findViewById(R.id.tvMissatges)
        scrollMissatges = findViewById(R.id.scrollMissatges)
        etMissatge = findViewById(R.id.etMissatge)
        btnEnvia = findViewById(R.id.btnEnvia)

        btnEnvia.setOnClickListener {
            enviaMissatge()
        }

        connectaWS()
    }

    inner class MyWebSocketClient(uri: URI) : WebSocketClient(uri) {
        override fun onOpen(handshakedata: ServerHandshake?) {
            runOnUiThread {
                tvMissatges.append("Connectat\n")
                tvMissatges.append("Codi d'estat: ${handshakedata?.httpStatus}\n")
                tvMissatges.append("Descripció: ${handshakedata?.httpStatusMessage}\n")
                scrollMissatges.post { scrollMissatges.fullScroll(View.FOCUS_DOWN) }
            }
        }

        override fun onMessage(message: String?) {
            runOnUiThread {
                tvMissatges.append((message ?: "") + "\n")
                scrollMissatges.post { scrollMissatges.fullScroll(View.FOCUS_DOWN) }
            }
        }

        override fun onClose(code: Int, reason: String?, remote: Boolean) {
            android.util.Log.d("WS", "onClose code=$code reason=$reason remote=$remote")
            runOnUiThread {
                tvMissatges.append("Connexió tancada\n")
                tvMissatges.append("Codi: $code, Raó: $reason, Remote: $remote\n")
                scrollMissatges.post { scrollMissatges.fullScroll(View.FOCUS_DOWN) }
            }
        }

        override fun onError(ex: Exception?) {
            android.util.Log.e("WS", "onError", ex)
            runOnUiThread {
                tvMissatges.append("Error: ${ex?.message}\n")
                scrollMissatges.post { scrollMissatges.fullScroll(View.FOCUS_DOWN) }
            }
        }
    }

    private fun connectaWS() {
        val uri = URI("ws://10.0.2.2:8888") // utilitzeu wss:// si va sobre HTTPS
        wsclient = MyWebSocketClient(uri)
        wsclient?.connect()
    }

    fun enviaMissatge() {
        val text = etMissatge.text.toString().trim()

        if (wsclient?.isOpen == true && text!="") {
            wsclient?.send(text)
            etMissatge.text.clear()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        wsclient?.close()
    }
}