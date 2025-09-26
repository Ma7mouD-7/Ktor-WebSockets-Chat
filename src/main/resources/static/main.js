let ws = null;
let currentUserName = '';
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
        alert('Please enter your name!');
        return;
    }
    currentUserName = userName;

    const host = window.location.host || 'localhost:8080';
    const wsUrl = `ws://${host}/ws`;
    ws = new WebSocket(wsUrl);

    ws.onopen = function() {
        ws.send(JSON.stringify({
            action: "USER_JOIN",
            name: userName
        }));
    };

    ws.onmessage = function(event) {
        const msgData = JSON.parse(event.data);
        addMessage(msgData);

        if (msgData.type === 'system' && msgData.content.includes('Welcome')) {
             updateStatus(`Connected as: ${currentUserName}`, true);
        }
    };

    ws.onclose = function() {
        updateStatus('Disconnected', false);
        addMessage({ type: 'system', content: 'Disconnected from server.' });
        currentUserName = '';
        ws = null;
    };

    ws.onerror = function(error) {
        console.error('WebSocket Error:', error);
        addMessage({ type: 'system', content: 'Connection error. Check console.' });
    };
}

function disconnect() {
    if (ws) {
        ws.send(JSON.stringify({
            action: "BYE",
            name: currentUserName
        }));
        ws.close();
    }
}

function sendMessage() {
    const message = messageInput.value.trim();
    if (message && ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({
            action: "MSG",
            name: currentUserName,
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

function addMessage(msgData) {
    const item = document.createElement('li');

    if (msgData.type === 'chat') {
        const isMine = msgData.sender === currentUserName;
        item.className = 'message-bubble ' + (isMine ? 'my-message' : 'other-message');

        const senderHtml = isMine ? '' : `<div class="sender">${msgData.sender}</div>`;

        item.innerHTML = `
            ${senderHtml}
            <p class="content">${msgData.content}</p>
            <span class="timestamp">${msgData.timestamp}</span>
        `;
    } else {
        item.className = 'system-message';
        item.textContent = msgData.content;
    }

    messages.appendChild(item);
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
