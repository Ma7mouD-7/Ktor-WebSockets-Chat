package com.example

import com.example.dto.Action
import com.example.dto.IncomingMessage
import com.example.dto.OutgoingMessage
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

class ChatServer {
    val connections = ConcurrentHashMap<WebSocketSession, String>()

    suspend fun broadcast(message: OutgoingMessage) {
        val jsonString = Json.encodeToString(message)
        connections.keys.forEach { session ->
            try {
                session.send(jsonString)
            } catch (e: Exception) {
                connections.remove(session)
            }
        }
    }
}


fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }

    val chatServer = ChatServer()

    routing {
        webSocket("/ws") {
            var userName = "Anonymous"

            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        val incomingMsg = Json.decodeFromString<IncomingMessage>(text)
                        val currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                        userName = incomingMsg.name

                        when (incomingMsg.action) {
                            Action.USER_JOIN -> {
                                chatServer.connections[this] = userName

                                val welcomeMsg = OutgoingMessage("system", "System", "Welcome, $userName!", currentTime)
                                sendSerialized(welcomeMsg)

                                val joinAnnouncement =
                                    OutgoingMessage("join", "System", "$userName has joined.", currentTime)
                                chatServer.broadcast(joinAnnouncement)
                            }

                            Action.MSG -> {
                                val messageContent = incomingMsg.message ?: ""
                                if (messageContent.startsWith("!")) {
                                    val responseContent = when {
                                        messageContent.equals("!users", ignoreCase = true) ->
                                            "Connected: ${chatServer.connections.values.joinToString(", ")}"
                                        messageContent.equals("!count", ignoreCase = true) ->
                                            "Connected: ${chatServer.connections.size} Persons"
                                        else -> "Unknown command: $messageContent"
                                    }
                                    val systemResponse =
                                        OutgoingMessage("system", "System", responseContent, currentTime)
                                    sendSerialized(systemResponse)
                                } else {
                                    val chatMessage = OutgoingMessage("chat", userName, messageContent, currentTime)
                                    chatServer.broadcast(chatMessage)
                                }
                            }

                            Action.BYE -> {
                                close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                            }
                        }
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                if (chatServer.connections.containsKey(this)) {
                    val userWhoLeft = chatServer.connections.remove(this) ?: "Someone"
                    val currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                    val leaveMessage = OutgoingMessage("leave", "System", "$userWhoLeft has left.", currentTime)
                    chatServer.broadcast(leaveMessage)
                }
            }
        }
    }
}

