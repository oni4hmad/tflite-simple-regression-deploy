package com.example.test_tflite_app_simple

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.test_tflite_app_simple.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.Charset


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
            val prediction = preprocessing(binding.edtInput.text.toString())
            Log.d("prediction", prediction?.joinToString().toString())

            getJsonFromAssets(this, "classes.json")?.let { jsonStr ->
                val classList = getArrayListFromJsonStr(jsonStr)
                val predictionMap = classList.let { listClass ->
                    mapPrediction(prediction, listClass).toList().sortedByDescending { (_, value) -> value}.toMap()
                }
                Log.d("predictionMap", predictionMap.toString())

                val classPrediction = predictionMap.entries.first()
                Log.d("predictionMap first", "key=${classPrediction.key}, value=${classPrediction.value}")

                binding.tvOutput.text = getResponRandomize(classPrediction.key)
            }
        }

    }

    private fun getResponRandomize(intentTag: String): String? {
        val botIntent = getBotIntent("intents2.json")
        val responses = botIntent.listIntent.firstOrNull { it.tag == intentTag }!!.listReponses
        return responses.random()
    }

    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = this.assets.openFd("iswara_model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declareLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declareLength)
    }

    private fun preprocessing(chat: String): FloatArray? {

        val jsonWordStr = getJsonFromAssets(this, "words.json")
        val wordList = jsonWordStr?.let {
            json -> getArrayListFromJsonStr(json)
        }

        val chatWords = clean_up_sentence(chat)

        val bag_of_words = wordList?.let { words ->
            bagOfWords(chatWords, words)
        }

        return bag_of_words?.let {
            doInference(bag_of_words)
        }
    }

    private fun doInference(bag_of_words: FloatArray): FloatArray {
        val output = Array(1) {
            FloatArray(12)
        }
        tflite.run(bag_of_words, output)
        return output[0]
    }

    private fun mapPrediction(prediction: FloatArray?, classList: ArrayList<String>): HashMap<String, Float> {
        val hashMap = HashMap<String, Float>()
        prediction?.forEachIndexed { index, item ->
            hashMap.put(classList[index], item)
        }
        return hashMap
    }

    /* --------------------------------------- */

    private fun getBotIntent(fileName: String): BotIntent {
        val gson = Gson()
        val jsonIntentsStr = getJsonFromAssets(this, fileName)
        Log.d("jsonIntentsStr", jsonIntentsStr.toString())
        val botIntent: BotIntent = gson.fromJson(jsonIntentsStr, BotIntent::class.java)
        Log.d("botIntent", botIntent.toString())
        return botIntent
    }

    // return bag of words array: 0 or 1 for words that exist in wordList
    private fun bagOfWords(chatWords: ArrayList<String>, wordList: ArrayList<String>): FloatArray {
        val size = wordList.size
        val bag = FloatArray(size)
        chatWords.forEach { chatWord ->
            wordList.forEachIndexed { index, wordItem ->
                if (chatWord == wordItem) {
                    bag[index] = 1F
                    Log.d("bow loop", "sama!")
                }
            }
        }
        return bag
    }

    private fun clean_up_sentence(chat: String): ArrayList<String> {
        // lowercasing & tokenize using regex
        val text = chat.lowercase()
        val regex = """(\w+|\d|[^\s-])""".toRegex()
        val matches = regex.findAll(text)
        val arr = ArrayList<String>()
        matches.forEach { arr.add(it.groupValues[0]) }

        // lemmatizer
        /* gatau implementasinya di kotlin/java */

        return arr
    }

    private fun getJsonFromAssets(context: Context, fileName: String): String? {
        return try {
            val inputStream: InputStream = context.assets.open(fileName)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charset.forName("UTF-8"))
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun getArrayListFromJsonStr(jsonObjStr: String): ArrayList<String> {
        val gson = GsonBuilder().create()
        val list = gson.fromJson<ArrayList<String>>(jsonObjStr, object : TypeToken<ArrayList<String>>(){}.type)
        return list
    }

}