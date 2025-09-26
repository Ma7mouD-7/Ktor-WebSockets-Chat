package com.example

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.annotations.Nullable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

// A dedicated class for managing connections and broadcasting messages.
class ChatServer {
    // Using ConcurrentHashMap is a modern and robust way for thread-safe collections.
    val connections = ConcurrentHashMap<WebSocketSession, String>()

    // Centralized function to broadcast messages to all connected clients.
    suspend fun broadcast(message: String) {
        connections.forEach { (session, _) ->
            try {
                session.send(message)
            } catch (e: Exception) {
                // In case of an error, we can assume the connection is dead and remove it.
                connections.remove(session)
            }
        }
    }
}

// Data classes for structured messaging using JSON.
// This makes the protocol much more robust and extensible than simple string parsing.
@Serializable
sealed class Message {
    @Serializable
    data class UserJoin(val username: String) : Message()
    @Serializable
    data class ChatMessage(val from: String, val content: String) : Message()
    @Serializable
    data class SystemUpdate(val content: String) : Message()
}

@Serializable
data class Response(val action: Action, val name: String,  val message: String? = null)

enum class Action(){
    USER_JOIN,
    MSG,
    BYE,
}

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    // Create an instance of our ChatServer.
    val chatServer = ChatServer()

    routing {
        webSocket("/ws") {
            var userName = "Anonymous"

            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        val response = Json.decodeFromString<Response>(text)
                        val currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                        val userName = response.name
                        when(response.action) {
                            Action.USER_JOIN -> {
                                chatServer.connections[this] = userName
                                val welcomeMessage = "Welcome $userName! Total users: ${chatServer.connections.size}"
                                send(welcomeMessage) // Send welcome message only to the new user

                                val joinMessage = "$userName joined the chat! Total users: ${chatServer.connections.size}"
                                chatServer.broadcast(joinMessage)
                            }
                            Action.MSG -> {
                                val messageContent = response.message?: ""
                                val finalMessage = "[$currentTime] [$userName]: $messageContent"

                                // Handle server-side commands
                                if (messageContent.startsWith("!")) {
                                    send(finalMessage)
                                    when {
                                        messageContent.equals("!users", ignoreCase = true) -> {
                                            val userList = chatServer.connections.values.joinToString(", ")
                                            val systemMessage = "[$currentTime] [SYSTEM] Connected user(s): $userList"
                                            send(systemMessage) // Send only to the user who asked
                                        }
                                        messageContent.equals("!usr_cnt", ignoreCase = true) -> {
                                            val systemMessage = "[$currentTime] [SYSTEM] Total users: ${chatServer.connections.size}"
                                            send(systemMessage) // Send only to the user who asked
                                        }
                                    }
                                }
                                else{
                                    chatServer.broadcast(finalMessage)
                                }
                            }

                            Action.BYE -> close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))

                        }
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                println("Connection closed normally for $userName")
            } catch (e: Throwable) {
                println("Connection error for $userName: ${e.localizedMessage}")
            } finally {
                // On disconnection, remove the user and notify others.
                if (chatServer.connections.containsKey(this)) {
                    chatServer.connections.remove(this)
                    val leaveMessage = "$userName left the chat. Total users: ${chatServer.connections.size}"
                    chatServer.broadcast(leaveMessage)
                }
            }
        }
    }
}
