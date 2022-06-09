package com.example.test_tflite_app_simple

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.test_tflite_app_simple.chatbot.Chatbot
import com.example.test_tflite_app_simple.databinding.ActivityMainBinding
import java.nio.channels.FileChannel
import java.nio.charset.Charset


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mChatbot: Chatbot
    private var respondCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mChatbot = Chatbot(this)
        mChatbot.setOnChatbotResponded { respond, input, inputFormat ->
            when(input) {
                Chatbot.Input.ENDED -> {
                    showOptions("ended!", inputFormat.title.toString(), inputFormat.options?.joinToString().toString())
                }
                Chatbot.Input.CHAT -> {
                    showOptions("ended!", inputFormat.title.toString(), inputFormat.options?.joinToString().toString())
                }
                Chatbot.Input.RADIO_BUTTON -> {
                    showOptions("ended!", inputFormat.title.toString(), inputFormat.options?.joinToString().toString())
                }
                Chatbot.Input.CHECK_BOX -> {
                    showToast("check box! t: ${inputFormat.title.toString()} o: ${
                        inputFormat.options?.joinToString().toString()} ")
                }
            }

            binding.tvOutput.text = respond

            respondCount++
            binding.tvCount.text = respondCount.toString()
        }

        binding.btnPredict.setOnClickListener {
            val chatStr = binding.edtInput.text.toString()
            mChatbot.chat(chatStr)
        }
        binding.btnResetSession.setOnClickListener {
            binding.tvOutput.text = ""
            mChatbot.resetSession()
        }
    }

    private fun showOptions(type: String, title: String, options: String) {

    }

    private fun showToast(text: String) {
        Toast.makeText(this,text,Toast.LENGTH_SHORT).show()
    }

}