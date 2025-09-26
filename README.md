# **Ktor WebSocket Chat**

A simple real-time chat application built with a **Ktor** backend and a clean, responsive frontend using vanilla **HTML, CSS, and JavaScript**. This project serves as an excellent example of setting up a WebSocket server in Kotlin for instant, bidirectional communication.

The application features a modern chat interface where messages sent by the user are aligned to the right, and messages from other users are aligned to the left, mimicking popular chat apps.

## **✨ Features**

* **Real-time Messaging:** Instant message delivery using WebSockets.  
* **User Presence:** Real-time notifications when users join or leave the chat.  
* **Modern UI:** A clean and responsive chat interface with message bubbles aligned based on the sender.  
* **Structured Communication:** Uses JSON for clear and extensible communication between the client and server.  
* **Private Commands:** Ability to handle server-side commands (e.g., \!users) that only the requester can see.  
* **Clean Architecture:** Well-organized project structure with separation of concerns (routing, serialization, WebSocket logic, DTOs).

## **🛠️ Tech Stack**

* **Backend:**  
  * [Kotlin](https://kotlinlang.org/)  
  * [Ktor](https://ktor.io/) \- For building the web server and WebSocket endpoint.  
  * [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) \- For JSON serialization/deserialization.  
* **Frontend:**  
  * HTML5  
  * CSS3 (with Flexbox for layout)  
  * Vanilla JavaScript (ES6+)

## **🚀 How to Run Locally**

To get a local copy up and running, follow these simple steps.

### **Prerequisites**

* JDK 11 or higher.  
* IntelliJ IDEA (Community or Ultimate) is recommended.

### **Steps**

1. **Clone the repository:**  
   ``` git clone https://github.com/Ma7mouD-7/Ktor-WebSockets-Chat.git ```

3. **Open the project:**  
   * Open IntelliJ IDEA.  
   * Select File \> Open... and choose the cloned project directory.  
   * Trust the project and wait for Gradle to sync the dependencies.  
4. **Run the application:**  
   * Navigate to src/main/kotlin/com/example/Application.kt.  
   * Click the green play button next to the main function to start the server.  
5. **Open the chat:**  
   * Open your web browser and go to http://localhost:8080.  
   * Open multiple tabs or browsers to simulate a conversation between different users.

## **📂 Project Structure**

```  
├── build.gradle.kts        \# Gradle build script  
├── src  
│   ├── main  
│   │   ├── kotlin  
│   │   │   └── com  
│   │   │       └── example  
│   │   │           ├── dto  \# Data classes (DTOs) for JSON    
│   │   │           │   └── Action.kt  
│   │   │           │   └── IncomingMessage.kt  
│   │   │           │   └── OutgoingMessage.kt         
│   │   │           ├── Application.kt           \# Main application entry point  
│   │   │           ├── Routing.kt               \# Handles HTTP routing and serving static files  
│   │   │           ├── Serialization.kt         \# Configures JSON content negotiation  
│   │   │           └── Sockets.kt               \# Core WebSocket logic  
│   │   └── resources  
│   │       ├── application.yaml     \# Ktor configuration  
│   │       ├── logback.xml          \# Logging configuration  
│   │       └── static               \# Frontend files  
│   │           ├── index.html  
│   │           ├── main.js  
│   │           └── styles.css  
└── 
```

## **📄 License**

This project is licensed under the MIT License \- see the LICENSE file for details.
