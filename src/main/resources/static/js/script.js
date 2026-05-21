// Auth Tab Switching
function switchAuthTab(tabName) {
    const tabLogin = document.getElementById('tab-login');
    const tabRegister = document.getElementById('tab-register');
    const formLogin = document.getElementById('form-login');
    const formRegister = document.getElementById('form-register');

    if (tabLogin && tabRegister && formLogin && formRegister) {
        if (tabName === 'login') {
            tabLogin.classList.add('active');
            tabRegister.classList.remove('active');
            formLogin.classList.add('active');
            formRegister.classList.remove('active');
        } else {
            tabRegister.classList.add('active');
            tabLogin.classList.remove('active');
            formRegister.classList.add('active');
            formLogin.classList.remove('active');
        }
    }
}

// Education Section Tab Switching
function switchEduTab(tabName) {
    document.querySelectorAll('.edu-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.edu-content-panel').forEach(panel => panel.classList.remove('active'));
    
    const targetBtn = document.getElementById('btn-' + tabName);
    const targetPanel = document.getElementById('panel-' + tabName);
    
    if (targetBtn && targetPanel) {
        targetBtn.classList.add('active');
        targetPanel.classList.add('active');
    }
}

// Symptom Logger Checklist Toggles
function toggleSymptomCheckbox(element, checkboxId) {
    const cb = document.getElementById(checkboxId);
    if (cb) {
        cb.checked = !cb.checked;
        if (cb.checked) {
            element.classList.add('badge-pink');
            element.classList.remove('badge-lavender');
        } else {
            element.classList.add('badge-lavender');
            element.classList.remove('badge-pink');
        }
        
        // Compile symptoms list into hidden text input
        const checkedList = [];
        document.querySelectorAll('.symptom-tag-input:checked').forEach(input => {
            checkedList.push(input.value);
        });
        const hiddenInput = document.getElementById('symptoms-compiled');
        if (hiddenInput) {
            hiddenInput.value = checkedList.join(', ');
        }
    }
}

// rule-based Chatbot AJAX handlers
async function sendChatbotMessage() {
    const input = document.getElementById('chat-input');
    const container = document.getElementById('chat-messages');
    if (!input || !container || input.value.trim() === '') return;
    
    const userText = input.value.trim();
    input.value = '';
    
    // Append User Message
    appendChatBubble(userText, 'user');
    
    // Append Typing status
    const typingBubble = appendChatBubble("Thinking...", 'bot typing');
    
    try {
        const params = new URLSearchParams();
        params.append('message', userText);
        
        const response = await fetch('/api/chatbot/message', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: params
        });
        const data = await response.json();
        
        // Remove typing bubble and append actual message
        if (typingBubble) typingBubble.remove();
        appendChatBubble(data.response, 'bot');
    } catch (error) {
        if (typingBubble) typingBubble.remove();
        appendChatBubble("I'm sorry, I'm having trouble connecting right now. 💕 Please try again later.", 'bot');
    }
}

function appendChatBubble(text, sender) {
    const container = document.getElementById('chat-messages');
    if (!container) return null;
    
    const bubble = document.createElement('div');
    bubble.className = `chat-bubble chat-bubble-${sender.includes('bot') ? 'bot' : 'user'}`;
    bubble.textContent = text;
    container.appendChild(bubble);
    container.scrollTop = container.scrollHeight;
    return bubble;
}

// Emotional Support Chat Input enter key triggers
document.addEventListener("DOMContentLoaded", () => {
    const chatInput = document.getElementById('chat-input');
    if (chatInput) {
        chatInput.addEventListener("keypress", (event) => {
            if (event.key === "Enter") {
                sendChatbotMessage();
            }
        });
    }

    // Initialize breathing guide if elements exist
    const textEl = document.getElementById('breathing-instruction');
    if (textEl) {
        const states = ["Inhale deeply...", "Hold...", "Exhale slowly...", "Hold..."];
        let index = 0;
        setInterval(() => {
            index = (index + 1) % states.length;
            textEl.textContent = states[index];
        }, 2000); // 8 seconds total cycle (2s each) synced with breathing-circle scaling
    }
});

// Interactive Period Calendar Generation
function renderDashboardCalendar(lastPeriodStr, cycleLen, nextPeriodStr, fertileStartStr, fertileEndStr) {
    const container = document.getElementById('calendar-container');
    if (!container) return;

    // Parse Dates
    const today = new Date();
    const currentYear = today.getFullYear();
    const currentMonth = today.getMonth(); // 0-indexed

    // Calendar Calculations
    const firstDayIndex = new Date(currentYear, currentMonth, 1).getDay();
    const totalDays = new Date(currentYear, currentMonth + 1, 0).getDate();
    
    const monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
    document.getElementById('calendar-month-name').textContent = monthNames[currentMonth] + " " + currentYear;

    let calendarHTML = `
        <div class="calendar-day-header">Sun</div>
        <div class="calendar-day-header">Mon</div>
        <div class="calendar-day-header">Tue</div>
        <div class="calendar-day-header">Wed</div>
        <div class="calendar-day-header">Thu</div>
        <div class="calendar-day-header">Fri</div>
        <div class="calendar-day-header">Sat</div>
    `;

    // Empty spaces for previous month
    for (let i = 0; i < firstDayIndex; i++) {
        calendarHTML += `<div class="calendar-day text-muted" style="border: none; background: transparent;"></div>`;
    }

    // Date references for coloring
    const parseLocalDate = (dateStr) => {
        if (!dateStr) return null;
        const parts = dateStr.split('-');
        return new Date(parts[0], parts[1] - 1, parts[2]);
    };

    const nextPeriod = parseLocalDate(nextPeriodStr);
    const lastPeriod = parseLocalDate(lastPeriodStr);
    const fertileStart = parseLocalDate(fertileStartStr);
    const fertileEnd = parseLocalDate(fertileEndStr);

    for (let day = 1; day <= totalDays; day++) {
        const cellDate = new Date(currentYear, currentMonth, day);
        let classes = "calendar-day";

        // Check if date is today
        if (cellDate.toDateString() === today.toDateString()) {
            classes += " today";
        }

        // Highlight predicted period days (next 5 days from nextPeriodDate, or past from lastPeriodDate)
        let isPeriod = false;
        if (nextPeriod) {
            const nextPeriodEnd = new Date(nextPeriod);
            nextPeriodEnd.setDate(nextPeriod.getDate() + 4);
            if (cellDate >= nextPeriod && cellDate <= nextPeriodEnd) {
                isPeriod = true;
            }
        }
        if (lastPeriod && !isPeriod) {
            const lastPeriodEnd = new Date(lastPeriod);
            lastPeriodEnd.setDate(lastPeriod.getDate() + 4);
            if (cellDate >= lastPeriod && cellDate <= lastPeriodEnd) {
                isPeriod = true;
            }
        }

        // Highlight predicted fertile window days
        let isFertile = false;
        if (fertileStart && fertileEnd && !isPeriod) {
            if (cellDate >= fertileStart && cellDate <= fertileEnd) {
                isFertile = true;
            }
        }

        if (isPeriod) {
            classes += " period";
        } else if (isFertile) {
            classes += " fertile";
        }

        calendarHTML += `<div class="${classes}">${day}</div>`;
    }

    container.innerHTML = calendarHTML;
}
