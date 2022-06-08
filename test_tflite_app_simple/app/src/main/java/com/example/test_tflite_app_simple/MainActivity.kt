package com.example.test_tflite_app_simple

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.test_tflite_app_simple.databinding.ActivityMainBinding
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tflite: Interpreter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            tflite = Interpreter(loadModelFile())
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        binding.btnPredict.setOnClickListener {
            val prediction = doInference(binding.edtInput.text.toString())
            println(prediction)
            binding.tvHw.text = prediction.toString()
        }

    }

    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = this.assets.openFd("degree.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declareLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declareLength)
    }


    private fun doInference(inputString: String): Float {
        val inputVal = FloatArray(1)
        inputVal[0] = inputString.toFloat()
        val output = Array(1) {
            FloatArray(
                1
            )
        }
        tflite.run(inputVal, output)
        return output[0][0]
    }

}