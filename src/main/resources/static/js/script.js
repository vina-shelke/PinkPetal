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

// --- 🌸 Premium Calm Mode & Smart Comfort JavaScript Helpers ---

// Log Mood button helper
function selectMood(btn, moodValue) {
    document.querySelectorAll('.mood-btn').forEach(b => {
        b.classList.remove('btn-brand');
        b.classList.add('btn-outline-secondary');
    });
    btn.classList.add('btn-brand');
    btn.classList.remove('btn-outline-secondary');
    
    const moodInput = document.getElementById('mood-input');
    if (moodInput) {
        moodInput.value = moodValue;
    }
}

// Affirmations rotation lists for Calm Mode
const CALM_AFFIRMATIONS = [
    "I am safe, and this feeling will pass. 🤍",
    "My body is strong, resilient, and doing its best. 🌸",
    "I give myself permission to rest and heal. 🌙",
    "Inhale peace, exhale tension. Let it all go. ✨",
    "It's okay to feel overwhelmed. I am kind to myself. 🌷",
    "I am surrounded by support and warmth. 💕",
    "My peace is worth protecting today. 🍃"
];
let affirmationIndex = 0;

function rotateAffirmation() {
    const textEl = document.getElementById('calm-affirmation-text');
    if (textEl) {
        affirmationIndex = (affirmationIndex + 1) % CALM_AFFIRMATIONS.length;
        textEl.style.transition = "opacity 0.3s ease";
        textEl.style.opacity = 0;
        setTimeout(() => {
            textEl.textContent = CALM_AFFIRMATIONS[affirmationIndex];
            textEl.style.opacity = 1;
        }, 300);
    }
}

// Ambient Sound Player handler
function changeAmbientSound(selectElement) {
    const player = document.getElementById('ambient-audio-player');
    if (player) {
        const selectedUrl = selectElement.value;
        if (selectedUrl) {
            player.src = selectedUrl;
            player.load();
            player.play().catch(e => console.log("Audio playback waiting for user interaction."));
        } else {
            player.pause();
        }
    }
}

// Emergency contact notification simulation
function simulateNotifyTrustedContact() {
    const btn = document.getElementById('notify-contact-btn');
    const status = document.getElementById('notify-contact-status');
    if (btn && status) {
        btn.disabled = true;
        btn.innerHTML = `<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>Notifying Contact...`;
        
        setTimeout(() => {
            btn.innerHTML = `<i class="fa-solid fa-check me-2"></i>Contact Alerted`;
            btn.className = "btn btn-success w-100 py-3 rounded-pill fw-bold";
            status.style.display = "block";
            status.innerHTML = `
                <div class="alert alert-success mt-3 text-xs p-3 border-0 rounded-3 text-start" style="background-color: rgba(46, 204, 113, 0.1); color: #27ae60; border-left: 4px solid #2ecc71 !important;">
                    <i class="fa-solid fa-circle-check me-2"></i><strong>Alert Sent!</strong> A simulated SMS alert has been dispatched to your emergency contact (Sisterhood Network). They have been requested to check in on you. 🌸
                </div>
            `;
        }, 1500);
    }
}

// Attach Calm Mode modal events
document.addEventListener("DOMContentLoaded", () => {
    const calmModal = document.getElementById('calmModeModal');
    if (calmModal) {
        let affirmationTimer = null;
        let breathingTimer = null;
        
        calmModal.addEventListener('show.bs.modal', () => {
            // Reset contact alert button state
            const btn = document.getElementById('notify-contact-btn');
            const status = document.getElementById('notify-contact-status');
            if (btn && status) {
                btn.disabled = false;
                btn.className = "btn btn-brand-outline w-100 py-3 rounded-pill fw-bold";
                btn.innerHTML = `<i class="fa-solid fa-paper-plane me-2"></i>Simulate Alert to Trusted Contact`;
                status.style.display = "none";
            }
            
            // Set first affirmation
            const textEl = document.getElementById('calm-affirmation-text');
            if (textEl) {
                textEl.textContent = CALM_AFFIRMATIONS[0];
                textEl.style.opacity = 1;
                affirmationIndex = 0;
            }
            
            // Start rotating affirmations every 5 seconds
            affirmationTimer = setInterval(rotateAffirmation, 5000);
            
            // Initialize breathing guide in modal
            const modalBreathText = document.getElementById('modal-breathing-instruction');
            if (modalBreathText) {
                const states = ["Inhale deeply...", "Hold...", "Exhale slowly...", "Hold..."];
                let index = 0;
                modalBreathText.textContent = states[0];
                breathingTimer = setInterval(() => {
                    index = (index + 1) % states.length;
                    modalBreathText.textContent = states[index];
                }, 2000); // Syncs with 8s keyframe
            }
        });
        
        calmModal.addEventListener('hidden.bs.modal', () => {
            // Stop timers
            if (affirmationTimer) clearInterval(affirmationTimer);
            if (breathingTimer) clearInterval(breathingTimer);
            
            // Pause ambient sound
            const player = document.getElementById('ambient-audio-player');
            const selectEl = document.getElementById('ambient-sound-select');
            if (player) {
                player.pause();
            }
            if (selectEl) {
                selectEl.value = "";
            }
        });
    }
});

