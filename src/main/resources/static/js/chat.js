let stompClient = null;
let currentConversationId = null;

function connect() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        loadConversations();
    }, function (error) {
        console.error('WebSocket connection error:', error);
        setTimeout(connect, 5000);
    });
}

async function loadConversations() {
    try {
        const response = await fetch('/api/chat/conversations');
        const conversations = await response.json();

        const listEl = document.getElementById('conversationsList');
        listEl.innerHTML = '';

        conversations.forEach(conv => {
            const item = document.createElement('div');
            item.className = 'conversation-item';
            item.innerHTML = `
                <div><strong>${conv.teacher.name} - ${conv.student.name}</strong></div>
                <div style="font-size: 12px; color: var(--color-text-secondary);">
                    ${conv.lastMessageAt ? new Date(conv.lastMessageAt).toLocaleString() : 'Chưa có tin nhắn'}
                </div>
            `;
            item.onclick = () => selectConversation(conv.conversationId, conv.teacher, conv.student);
            listEl.appendChild(item);
        });
    } catch (error) {
        console.error('Error loading conversations:', error);
    }
}

async function selectConversation(conversationId, teacher, student) {
    currentConversationId = conversationId;

    document.querySelectorAll('.conversation-item').forEach(item => {
        item.classList.remove('active');
    });
    event.target.closest('.conversation-item').classList.add('active');

    const otherPerson = currentUserId === teacher.id ? student : teacher;
    document.getElementById('chatHeader').textContent = `Chat với ${otherPerson.name}`;
    document.getElementById('messageInput').disabled = false;
    document.getElementById('sendButton').disabled = false;

    if (stompClient && stompClient.connected) {
        stompClient.subscribe(`/topic/conversation.${conversationId}`, function (message) {
            const msg = JSON.parse(message.body);
            displayMessage(msg);
        });
    }

    await loadMessages(conversationId);
}

async function loadMessages(conversationId) {
    try {
        const response = await fetch(`/api/chat/messages/${conversationId}`);
        const messages = await response.json();

        const container = document.getElementById('messagesContainer');
        container.innerHTML = '';

        messages.forEach(msg => displayMessage(msg));
        container.scrollTop = container.scrollHeight;
    } catch (error) {
        console.error('Error loading messages:', error);
    }
}

function displayMessage(msg) {
    const container = document.getElementById('messagesContainer');
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${msg.isMine ? 'mine' : ''}`;

    messageDiv.innerHTML = `
        <div class="message-content">${escapeHtml(msg.content)}</div>
        <div class="message-time">${new Date(msg.sentAt).toLocaleTimeString()}</div>
    `;

    container.appendChild(messageDiv);
    container.scrollTop = container.scrollHeight;
}

function sendMessage() {
    const input = document.getElementById('messageInput');
    const content = input.value.trim();

    if (!content || !currentConversationId) return;

    if (stompClient && stompClient.connected) {
        stompClient.send('/app/chat.send', {}, JSON.stringify({
            conversationId: currentConversationId,
            content: content
        }));

        input.value = '';
    }
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

document.addEventListener('DOMContentLoaded', function () {
    connect();

    const sendButton = document.getElementById('sendButton');
    const messageInput = document.getElementById('messageInput');

    sendButton.addEventListener('click', sendMessage);
    messageInput.addEventListener('keypress', function (e) {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });
});
