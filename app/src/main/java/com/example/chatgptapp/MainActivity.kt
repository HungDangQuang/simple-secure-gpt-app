package com.example.chatgptapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionChunk
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import kotlin.time.Duration.Companion.seconds

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()
    // creating variables on below line.
    lateinit var txtResponse: TextView
    lateinit var idTVQuestion: TextView
    lateinit var etQuestion: TextInputEditText
    lateinit var btSendQuestion: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        etQuestion=findViewById<TextInputEditText>(R.id.etQuestion)
        //val btnSubmit=findViewById<Button>(R.id.btnSubmit)
        idTVQuestion=findViewById<TextView>(R.id.idTVQuestion)
        txtResponse=findViewById<TextView>(R.id.txtResponse)
        btSendQuestion = findViewById<Button>(R.id.bt_send_question)

        btSendQuestion.setOnClickListener {
            // validating text
            val question = etQuestion.text.toString().trim()
            runBlocking {
                val res = testResponse("What is Android?")
                txtResponse.text = res
            }

        }

        /** btnSubmit.setOnClickListener {
        val question=etQuestion.text.toString().trim()
        Toast.makeText(this,question, Toast.LENGTH_SHORT).show()
        if(question.isNotEmpty()){
        getResponse(question) { response ->
        runOnUiThread {
        txtResponse.text = response
        }
        }
        }
        } */


        etQuestion.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {

                // setting response tv on below line.
                txtResponse.text = "Please wait.."

                // validating text
                val question = etQuestion.text.toString().trim()
                Toast.makeText(this,question, Toast.LENGTH_SHORT).show()
                if(question.isNotEmpty()){
                    getResponse(question) { response ->
                        runOnUiThread {
                            txtResponse.text = response
                        }
                    }
                }
                return@OnEditorActionListener true
            }
            false
        })


    }
    fun getResponse(question: String, callback: (String) -> Unit){

        // setting text on for question on below line.
        idTVQuestion.text = question
        etQuestion.setText("")

        val apiKey="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"
        val url="https://api.openai.com/v1/engines/text-davinci-003/completions"

        val requestBody="""
            {
            "prompt": "$question",
            "max_tokens": 500,
            "temperature": 0
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("error","API failed",e)
            }

            override fun onResponse(call: Call, response: Response) {
                val body=response.body?.string()
                if (body != null) {
                    Log.v("data",body)
                }
                else{
                    Log.v("data","empty")
                }
                val jsonObject= JSONObject(body)
                val jsonArray: JSONArray =jsonObject.getJSONArray("choices")
                val textResult=jsonArray.getJSONObject(0).getString("text")
                callback(textResult)
            }
        })
    }

    suspend fun testResponse(text:String) : String? {
        val openai = OpenAI(
            token = "MY_KEY",
            timeout = Timeout(socket = 60.seconds),
            // additional configurations...
        )

        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.User,
                    content = text
                )
            )
        )
        val completion: ChatCompletion = openai.chatCompletion(chatCompletionRequest)
        Log.d("HUNG", "testResponse: ${completion.choices.first().message.content}")
        return completion.choices.first().message.content
    }

}