package com.example.test_tflite_app_simple

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.test_tflite_app_simple.chatbot.Chatbot
import com.example.test_tflite_app_simple.chatbot.InputFormat
import com.example.test_tflite_app_simple.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mChatbot: Chatbot
    private var respondCount: Int = 0
    private var dialog: MaterialAlertDialogBuilder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mChatbot = Chatbot(this)
        mChatbot.setOnChatbotResponded { respond, input, inputFormat ->

            setInputUI(this, input, inputFormat)

            when(input) {
                Chatbot.Input.ENDED -> {
                    showOptions("ended", inputFormat.title.toString(), inputFormat.options?.joinToString().toString())
                }
                Chatbot.Input.CHAT -> {
                    showOptions("chat", inputFormat.title.toString(), inputFormat.options?.joinToString().toString())
                }
                Chatbot.Input.RADIO_BUTTON -> {
                    showOptions("radio button", inputFormat.title.toString(), inputFormat.options?.joinToString().toString())
                }
                Chatbot.Input.CHECK_BOX -> {
                    showOptions("check box", inputFormat.title.toString(), inputFormat.options?.joinToString().toString())
                }
            }

            binding.tvOutput.text = respond

            respondCount++
            binding.tvCount.text = respondCount.toString()
        }

        binding.btnPredict.setOnClickListener {
            val chatStr = binding.edtInput.text.toString()
            binding.edtInput.setText("")
            if (chatStr.isNotEmpty()) {
                mChatbot.chat(chatStr)
            } else {
                binding.edtInput.error = "pilih/isi dulu kocak"
            }
        }
        binding.btnResetSession.setOnClickListener {
            binding.tvOutput.text = ""
            mChatbot.resetSession()
            binding.btnPredict.visibility = View.VISIBLE
        }
        binding.btnChooseOption.setOnClickListener {
            dialog?.show()
        }
    }

    private fun setInputUI(context: Context, input: Chatbot.Input, inputFormat: InputFormat) {

        /*
        * Parse data
        * */

        val itemsOption = inputFormat.options?.toTypedArray() ?: arrayOf("null")
        val itemsState = BooleanArray(itemsOption.size) { false }
        val radioCheckedItem = -1  /* -1 = tidak memilih apapun */
        var respond: String = ""

        when(input) {
            Chatbot.Input.ENDED -> {
                binding.btnPredict.visibility = View.GONE
                setToOption(false)
            }
            Chatbot.Input.CHAT -> {
                setToOption(false)
            }
            Chatbot.Input.RADIO_BUTTON -> {

                /*
                * Confirmation dialog: radio button
                * */

                dialog = MaterialAlertDialogBuilder(context)
                    .setTitle(inputFormat.title)
                    .setNeutralButton("Batal") { dialog, which ->
                        // Respond to neutral button press
                    }
                    .setPositiveButton("OK") { dialog, which ->
                        // Respond to positive button press

                        /* log */
                        showToast("$which") // -1 ?

                        /* set chat */
                        for (i in itemsState.indices) {
                            if (itemsState[i]) {
                                setChatTo(itemsOption[i])
                                break
                            }
                        }
                    }
                    // Single-choice items (initialized with checked item)
                    .setSingleChoiceItems(itemsOption, radioCheckedItem) { dialog, which ->
                        // Respond to item chosen

                        /* set selected item to true */
                        Arrays.fill(itemsState, false)
                        itemsState[which] = true

                        /* log */
                        showToast("${itemsOption[which]} : ${itemsState[which]}")
                        showToast("$which") // index array -> 0/1/2
                    }

                setToOption(true, inputFormat.title.toString())

            }
            Chatbot.Input.CHECK_BOX -> {

                /*
                * Confirmation dialog: checkbox
                * */

                dialog = MaterialAlertDialogBuilder(context)
                    .setTitle(inputFormat.title)
                    .setNeutralButton("Cancel") { dialog, which ->
                        // Respond to neutral button press
                    }
                    .setNegativeButton("Tidak") { dialog, which ->
                        // Respond to neutral button press
                    }
                    .setPositiveButton("OK") { dialog, which ->
                        // Respond to positive button press

                        /* log itemsState */

                        showToast("$which") // -1 ?
                        showToast(Arrays.deepToString(arrayOf(itemsState)).apply {
                            replace("true", "1")
                            replace("false", "0")
                        })

                        /* build chat respond */

                        respond = ""

                        for (i in 0..itemsState.size-1) {
                            if (itemsState[i]) {
                                if (respond.isNotEmpty())
                                    respond = concat(respond, ", ${itemsOption[i]}")
                                else respond = concat(respond, itemsOption[i])
                            }
                        }
                        setChatTo(respond)
                    }
                    //Multi-choice items (initialized with checked items)
                    .setMultiChoiceItems(itemsOption, itemsState) { dialog, which, checked ->
                        // Respond to item chosen

                        /* set selected item to true/false */
                        itemsState[which] = checked

                        /* log: index : [apakah tercheck (true) atau tidak (false)] */
                        showToast("$which : $checked")
                    }

                setToOption(true, inputFormat.title.toString())

            }
        }
    }

    private fun setToOption(isOption: Boolean, title: String = "null") {
        if (isOption) {
            binding.edtInput.isEnabled = false
            binding.btnChooseOption.text = title
            binding.btnChooseOption.visibility = View.VISIBLE
        } else {
            binding.edtInput.isEnabled = true
            binding.btnChooseOption.text = title
            binding.btnChooseOption.visibility = View.GONE
        }
    }

    private fun setChatTo(respond: String) {
        binding.edtInput.setText(respond)
    }

    private fun showOptions(type: String, title: String, options: String) {
        binding.tvInput.text = """
            [input]
            type: ${type}
            title: ${title}
            options: ${options}
        """.trimIndent()
    }

    private fun showToast(text: String) {
        Toast.makeText(this,text,Toast.LENGTH_SHORT).show()
    }

    private fun concat(vararg string: String): String {
        val sb = StringBuilder()
        for (s in string) {
            sb.append(s)
        }

        return sb.toString()
    }

}