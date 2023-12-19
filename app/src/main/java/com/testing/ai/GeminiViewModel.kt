package com.testing.ai

import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class GeminiViewModel : ViewModel() {

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-pro-vision",
            apiKey = "AIzaSyAb1RRfg2FmZwZSDdPRWLZ-7CjaFKXRKQ8",
        )
    }

    val isLoading: MutableState<Boolean> = mutableStateOf(false)
    val chat: MutableState<List<Message>> = mutableStateOf(listOf())

    val images: SnapshotStateList<Bitmap> = mutableStateListOf()

    fun addImage(bitmap: Bitmap) {
        images.add(bitmap)
    }


    fun sendPrompt(prompt: String, images: List<Bitmap>) {
        val chunks = mutableListOf("")
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.value = true
            chat.value += Message(
                images = images,
                text = prompt
            )

            val inputContent = content {
                images.forEach { imageBitmap ->
                    image(imageBitmap)
                }
                text(prompt)
            }

            generativeModel.generateContentStream(inputContent)
                .catch {
                    chat.value += Message(
                        id = 2,
                        text = it.localizedMessage ?: "Unknown Error",
                    )
                }
                .collect { chunk ->
                    chunks += chunk.text.toString()
                    val last = chat.value.last()
                    if (last.id == 1) {
                        chat.value = chat.value.filter { message ->
                            message != last
                        }
                    }
                    chat.value += Message(
                        id = 1,
                        text = chunks.joinToString(""),
                    )
                }
            chunks.clear()
            isLoading.value = false
        }
    }

    fun reset() {
        viewModelScope.launch {
            images.clear()
        }
    }

    fun removeImage(image: Bitmap) {
        images.remove(image)
    }

    data class Message(
        val id: Int = 0, //0 user, 1 Gemini
        val images: List<Bitmap> = emptyList(),
        val text: String = ""
    )
}