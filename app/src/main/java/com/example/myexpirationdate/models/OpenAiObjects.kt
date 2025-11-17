
package com.example.myexpirationdate.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class URLObject(
    val url: String
)

@Serializable
data class ContentItem(
    val type: String,
    val text: String? = null,
    @SerialName("image_url")
    val imageUrl: URLObject? = null
)

// Request-side message: content is an array for multimodal input
@Serializable
data class MessageRequest(
    val role: String,
    val content: List<ContentItem>
)

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<MessageRequest>,
    val temperature: Double? = null
)

// Response-side message: content is a plain string
@Serializable
data class ChatMessageResponse(
    val role: String,
    val content: String
)

@Serializable
data class Choice(
    val index: Int,
    val message: ChatMessageResponse
)

@Serializable
data class ChatResponse(
    val id: String,
    val choices: List<Choice>
)
