package com.example.test_tflite_app_simple

import com.google.gson.annotations.SerializedName

data class BotIntent(
    @SerializedName("intents")
    var listIntent: List<IntentItem>
)

data class IntentItem (
    @SerializedName("tag")
    var tag: String,
    @SerializedName("responses")
    var listReponses: List<String>
)
