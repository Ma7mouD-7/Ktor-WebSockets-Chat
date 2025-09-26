let ws = null;
const messages = document.getElementById('messages');
const messageInput = document.getElementById('messageInput');
const nameInput = document.getElementById('nameInput');
const connectBtn = document.getElementById('connectBtn');
const disconnectBtn = document.getElementById('disconnectBtn');
const sendBtn = document.getElementById('sendBtn');
const status = document.getElementById('status');


function connect() {
    const userName = nameInput.value.trim();

    if (!userName) {
        alert('Please enter your name before connecting!');
        nameInput.focus();
        return;
    }
    if (userName.length > 20) {
        alert('Name must be 20 characters or less!');
        return;
    }

    // IMPROVEMENT: Use dynamic host instead of hardcoded IP.
    const host = window.location.host || 'localhost:8080';
    const wsUrl = `ws://${host}/ws`;

    try {
        ws = new WebSocket(wsUrl);

        ws.onopen = function() {
            ws.send(JSON.stringify({
                action : "USER_JOIN",
                name : userName
            }));
            updateStatus(`Connected as: ${userName}`, true);
            addMessage(`Connected to server!`, 'system-message');
        };

        ws.onmessage = function(event) {
            const isSystem = event.data.includes(" joined ") || event.data.includes(" left ");
            addMessage(event.data, isSystem ? 'system-message' : 'user-message');
        };

        ws.onclose = function() {
            updateStatus('Disconnected', false);
            if (ws) { // Check if it was a clean disconnect
                 addMessage('Disconnected from server.', 'system-message');
            }
            ws = null;
        };

        ws.onerror = function(error) {
            updateStatus('Connection Error', false);
            addMessage('Connection error occurred. Check the console.', 'system-message');
            console.error('WebSocket Error:', error);
        };

    } catch (error) {
        addMessage('Failed to connect: ' + error.message, 'system-message');
    }
}

function disconnect() {
    if (ws) {
        ws.close();
    }
}

function sendMessage() {

    const userName = nameInput.value.trim();
    const message = messageInput.value.trim();

    if (message && ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({
            action : "MSG",
            name : userName,
            message: message
        }));
        messageInput.value = '';
    }
}

function handleKeyPress(event) {
    if (event.key === 'Enter') {
        sendMessage();
    }
}

function addMessage(message, type) {
    const messageItem = document.createElement('li');
    messageItem.className = (type === 'system-message') ? 'message system-message' : 'message';
    messageItem.textContent = message;
    messages.appendChild(messageItem);
    messages.scrollTop = messages.scrollHeight;
}

function updateStatus(statusText, isConnected) {
    status.textContent = statusText;
    status.className = 'status ' + (isConnected ? 'connected' : 'disconnected');

    connectBtn.disabled = isConnected;
    disconnectBtn.disabled = !isConnected;
    messageInput.disabled = !isConnected;
    sendBtn.disabled = !isConnected;
    nameInput.disabled = isConnected;
}