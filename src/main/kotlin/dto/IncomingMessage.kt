package com.example.dto

import com.example.dto.Action
import kotlinx.serialization.Serializable

@Serializable
data class IncomingMessage(val action: Action, val name: String, val message: String? = null)