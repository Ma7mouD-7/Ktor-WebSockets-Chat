package com.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class OutgoingMessage(
    val type: String, // "chat", "system", "join", "leave"
    val sender: String,
    val content: String,
    val timestamp: String
)