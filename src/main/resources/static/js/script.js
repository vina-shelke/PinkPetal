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

    // Initialize swipe cards deck if present
    const swipeDeck = document.querySelector('.swipe-deck-container');
    if (swipeDeck) {
        updateSwipeDeckLayout();
    }
    
    // Initialize community facts if present
    const factTextEl = document.getElementById('community-fact-text');
    if (factTextEl) {
        factTextEl.textContent = DID_YOU_KNOW_FACTS[0];
        currentFactIndex = 0;
    }

    // Initialize daily affirmation if present
    const affirmationOutput = document.getElementById('affirmation-output');
    if (affirmationOutput) {
        affirmationOutput.textContent = POSITIVE_AFFIRMATIONS[0];
    }
});

// --- 🌸 Did You Know? Facts Rotation ---
const DID_YOU_KNOW_FACTS = [
    "Did you know? Sleep quality can affect period cramps. Poor sleep makes your body more sensitive to pain! 🌙",
    "Your resting heart rate actually fluctuates during your cycle, peaking during the luteal phase! 💓",
    "Craving chocolate? Cocoa contains magnesium, which helps relax uterine muscles and ease cramps! 🍫",
    "A warm bath doesn't just feel good; it increases blood flow and relaxes pelvic muscles. 🛁",
    "Hormones like progesterone can affect your vocal cords, making your voice sound slightly lower or raspy! 🗣️",
    "Mild aerobic exercise releases endorphins, which act as natural painkillers to relieve period discomfort. 🏃‍♀️",
    "During ovulation, high estrogen levels can boost your mood, energy, and social confidence! 🌟"
];
let currentFactIndex = 0;

function rotateCommunityFact() {
    const factTextEl = document.getElementById('community-fact-text');
    const factCardEl = document.getElementById('community-fact-card');
    if (factTextEl && factCardEl) {
        let nextIndex;
        do {
            nextIndex = Math.floor(Math.random() * DID_YOU_KNOW_FACTS.length);
        } while (nextIndex === currentFactIndex);
        
        currentFactIndex = nextIndex;
        
        // Soft fade animation
        factCardEl.style.opacity = 0;
        factCardEl.style.transform = "scale(0.98)";
        
        setTimeout(() => {
            factTextEl.textContent = DID_YOU_KNOW_FACTS[currentFactIndex];
            factCardEl.style.opacity = 1;
            factCardEl.style.transform = "scale(1)";
        }, 300);
    }
}

// --- 🎴 Comfort Swipe Cards Deck Controller ---
let activeCardIndex = 0;

function swipeComfortCard(direction) {
    const activeCard = document.querySelector(`.swipe-card.card-0`);
    if (!activeCard) return;
    
    // Add animation classes
    if (direction === 'left') {
        activeCard.classList.add('swipe-left');
    } else {
        activeCard.classList.add('swipe-right');
    }
    
    activeCardIndex++;
    
    // Stagger deck update
    setTimeout(() => {
        updateSwipeDeckLayout();
    }, 200);
}

function updateSwipeDeckLayout() {
    const cards = document.querySelectorAll('.swipe-card:not(.swipe-left):not(.swipe-right):not(.reset-card)');
    
    cards.forEach((card, idx) => {
        // Clear old stacking classes
        card.classList.remove('card-0', 'card-1', 'card-2', 'card-hidden');
        
        if (idx === 0) {
            card.classList.add('card-0');
        } else if (idx === 1) {
            card.classList.add('card-1');
        } else if (idx === 2) {
            card.classList.add('card-2');
        } else {
            card.classList.add('card-hidden');
        }
    });
    
    // If no regular cards left (all swiped)
    const activeCards = document.querySelectorAll('.swipe-card:not(.swipe-left):not(.swipe-right):not(.reset-card)');
    const resetCard = document.querySelector('.swipe-card.reset-card');
    if (activeCards.length === 0 && resetCard) {
        resetCard.classList.remove('card-hidden');
        resetCard.classList.add('card-0');
    }
}

function resetComfortSwipeDeck() {
    activeCardIndex = 0;
    const cards = document.querySelectorAll('.swipe-card');
    
    cards.forEach((card) => {
        card.classList.remove('swipe-left', 'swipe-right', 'card-0', 'card-1', 'card-2', 'card-hidden');
    });
    
    const resetCard = document.querySelector('.swipe-card.reset-card');
    if (resetCard) {
        resetCard.classList.add('card-hidden');
    }
    
    // Re-initialize standard layout
    updateSwipeDeckLayout();
}

// --- 🧠 Wellness Mini Quiz Controller ---
const QUIZ_QUESTIONS = [
    {
        title: "Which hormone is dominant during the PMS phase?",
        options: ["Estrogen 🧬", "Progesterone 🌸", "Testosterone 🩸", "Oxytocin 💕"],
        correctIndex: 1,
        explanation: "Progesterone peaks during the Luteal/PMS phase. Its subsequent drop triggers your period! Progesterone supports mood, but fluctuations can cause sensitivity. 🌸"
    },
    {
        title: "What type of heat is best for easing period cramps?",
        options: ["Dry heat (air blower) 💨", "Moist heat (warm bath/pack) 🛁", "Cold compress (ice pack) ❄️", "Warm ambient room temperature ☀️"],
        correctIndex: 1,
        explanation: "Moist heat from warm baths or moist heating pads penetrates tissues deeper, enhancing blood flow and relaxing pelvic muscle cramps more effectively! 🛁"
    },
    {
        title: "How does dehydration affect your menstrual symptoms?",
        options: ["It eases active cramps 💧", "It worsens bloating & cramps 😰", "It has no physical impact 🤷‍♀️", "It reduces fluid retention 🍉"],
        correctIndex: 1,
        explanation: "Dehydration signals the body to conserve water, which worsens bloating. Additionally, low hydration intensifies muscle spasms, making cramps more painful. Keep sipping! 💧"
    }
];

let quizCurrentIndex = 0;
let quizScore = 0;

function startQuiz() {
    quizCurrentIndex = 0;
    quizScore = 0;
    
    const introScreen = document.getElementById('quiz-intro-screen');
    const questionScreen = document.getElementById('quiz-question-screen');
    const scoreScreen = document.getElementById('quiz-score-screen');
    
    if (introScreen) introScreen.style.display = 'none';
    if (scoreScreen) scoreScreen.style.display = 'none';
    if (questionScreen) questionScreen.style.display = 'block';
    
    renderQuizQuestion();
}

function renderQuizQuestion() {
    const q = QUIZ_QUESTIONS[quizCurrentIndex];
    if (!q) return;
    
    // Update progress bar
    const progressPercent = (quizCurrentIndex / QUIZ_QUESTIONS.length) * 100;
    const progressBar = document.getElementById('quiz-progress-bar');
    if (progressBar) progressBar.style.width = `${progressPercent}%`;
    
    // Update title
    const qTitle = document.getElementById('quiz-q-title');
    if (qTitle) qTitle.textContent = q.title;
    
    // Render options
    const optionsContainer = document.getElementById('quiz-options-container');
    if (optionsContainer) {
        optionsContainer.innerHTML = '';
        q.options.forEach((opt, idx) => {
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'quiz-option btn text-start';
            btn.textContent = opt;
            btn.onclick = () => checkQuizAnswer(btn, idx);
            optionsContainer.appendChild(btn);
        });
    }
    
    // Hide explanation and Next button initially
    const explanationBlock = document.getElementById('quiz-explanation-block');
    if (explanationBlock) explanationBlock.style.display = 'none';
    
    const nextBtn = document.getElementById('quiz-next-btn');
    if (nextBtn) {
        nextBtn.style.display = 'none';
        nextBtn.textContent = (quizCurrentIndex === QUIZ_QUESTIONS.length - 1) ? 'See Results 📊' : 'Next Question ➡️';
    }
}

function checkQuizAnswer(selectedBtn, selectedIndex) {
    const q = QUIZ_QUESTIONS[quizCurrentIndex];
    if (!q) return;
    
    // Disable all options
    const allOptions = document.querySelectorAll('.quiz-option');
    allOptions.forEach(btn => {
        btn.disabled = true;
        btn.onclick = null;
    });
    
    const explanationText = document.getElementById('quiz-explanation-text');
    const explanationBlock = document.getElementById('quiz-explanation-block');
    
    if (selectedIndex === q.correctIndex) {
        selectedBtn.classList.add('correct');
        quizScore++;
        if (explanationText) {
            explanationText.innerHTML = `<strong>Correct! 🎉</strong> ${q.explanation}`;
        }
    } else {
        selectedBtn.classList.add('incorrect');
        // Highlight correct one
        if (allOptions[q.correctIndex]) {
            allOptions[q.correctIndex].classList.add('correct');
        }
        if (explanationText) {
            explanationText.innerHTML = `<strong>Not quite. 🌸</strong> ${q.explanation}`;
        }
    }
    
    if (explanationBlock) explanationBlock.style.display = 'block';
    
    const nextBtn = document.getElementById('quiz-next-btn');
    if (nextBtn) nextBtn.style.display = 'block';
}

function handleQuizNext() {
    quizCurrentIndex++;
    if (quizCurrentIndex < QUIZ_QUESTIONS.length) {
        renderQuizQuestion();
    } else {
        showQuizResults();
    }
}

function showQuizResults() {
    const questionScreen = document.getElementById('quiz-question-screen');
    const scoreScreen = document.getElementById('quiz-score-screen');
    
    if (questionScreen) questionScreen.style.display = 'none';
    if (scoreScreen) scoreScreen.style.display = 'block';
    
    // Update progress bar to 100%
    const progressBar = document.getElementById('quiz-progress-bar');
    if (progressBar) progressBar.style.width = `100%`;
    
    const scoreText = document.getElementById('quiz-score-text');
    const scoreAdvice = document.getElementById('quiz-score-advice');
    
    if (scoreText) {
        scoreText.textContent = `You scored ${quizScore} / ${QUIZ_QUESTIONS.length}!`;
    }
    
    if (scoreAdvice) {
        if (quizScore === QUIZ_QUESTIONS.length) {
            scoreAdvice.textContent = "Amazing! You have deep knowledge of your cycle and wellness. Keep up this beautiful care for your body! 💖";
        } else if (quizScore >= 1) {
            scoreAdvice.textContent = "Great job! Taking quizzes helps build solid wellness awareness. Continue listening to your body every single day. 🌸";
        } else {
            scoreAdvice.textContent = "No worries at all! Cycle wellness is a journey of continuous learning. Your body will guide you as you track and read more! 🤍";
        }
    }
}

// --- 💖 Daily Affirmations Draw Controller ---
const POSITIVE_AFFIRMATIONS = [
    "You are strong 💕",
    "Rest is productive too.",
    "Your emotions are valid and normal.",
    "You are doing your best, and that is enough.",
    "Be gentle with your body today.",
    "This phase will pass, take it one day at a time.",
    "You deserve peace, comfort, and kindness.",
    "It is okay to put yourself first today.",
    "You are beautiful and capable inside and out.",
    "Your body knows how to heal and cycle. Trust it."
];

function drawNewAffirmation() {
    const textEl = document.getElementById('affirmation-output');
    if (textEl) {
        const index = Math.floor(Math.random() * POSITIVE_AFFIRMATIONS.length);
        textEl.style.transition = "opacity 0.25s ease";
        textEl.style.opacity = 0;
        setTimeout(() => {
            textEl.textContent = POSITIVE_AFFIRMATIONS[index];
            textEl.style.opacity = 1;
        }, 250);
    }
}

// ==========================================================================
// 🌸 Know Your Body - Educational Wellness Section JavaScript Helpers
// ==========================================================================

// Interactive Anatomy Info Switching
function showAnatomyInfo(partId) {
    // Remove active class from all SVG parts
    document.querySelectorAll('.anatomy-part').forEach(p => p.classList.remove('active'));
    
    // Add active class to corresponding SVG part
    const svgPart = document.getElementById('path-' + partId);
    if (svgPart) {
        svgPart.classList.add('active');
    }
    
    // Update detail text
    const details = {
        'ovaries': {
            title: "Ovaries 🌸",
            desc: "The ovaries are small, oval-shaped glands located on either side of the uterus. They house your eggs and produce critical hormones: Estrogen and Progesterone. Each month, during ovulation, one ovary releases a mature egg."
        },
        'tubes': {
            title: "Fallopian Tubes 🧬",
            desc: "These are two thin tubes that connect the ovaries to the uterus. When an egg is released, it travels down the fallopian tube, where fertilization by sperm typically occurs."
        },
        'uterus': {
            title: "Uterus (Womb) 🤰",
            desc: "A hollow, muscular, pear-shaped organ where a fetus develops during pregnancy. The inner lining, called the endometrium, thickens each month in preparation for a fertilized egg. If pregnancy does not occur, this lining sheds as your period."
        },
        'cervix': {
            title: "Cervix 🔒",
            desc: "The lower, narrow neck of the uterus that connects to the vagina. It acts as a gateway and produces cervical mucus, which changes in texture and thickness throughout your cycle to facilitate or block sperm entry."
        },
        'vagina': {
            title: "Vagina 🌷",
            desc: "A flexible, muscular canal connecting the cervix to the outside of the body. It serves as the passageway for menstrual flow to exit, and is the birth canal during delivery."
        }
    };
    
    const info = details[partId];
    if (info) {
        const titleEl = document.getElementById('anatomy-detail-title');
        const descEl = document.getElementById('anatomy-detail-desc');
        if (titleEl && descEl) {
            titleEl.textContent = info.title;
            descEl.textContent = info.desc;
        }
    }
}

// Menstrual cycle phase switcher
function selectCyclePhase(phaseId) {
    document.querySelectorAll('.phase-step').forEach(step => step.classList.remove('active'));
    document.querySelectorAll('.phase-detail-pane').forEach(pane => pane.style.display = 'none');
    
    const activeStep = document.getElementById('step-' + phaseId);
    const activePane = document.getElementById('pane-' + phaseId);
    
    if (activeStep && activePane) {
        activeStep.classList.add('active');
        activePane.style.display = 'block';
    }
}

// Myth flip card toggle
function toggleMythCard(cardElement) {
    cardElement.classList.toggle('flipped');
}

// Breathing coach helper for Emotional Wellness page
let eduBreathingTimer = null;
function startEduBreathingCoach() {
    const coachBox = document.getElementById('edu-breathing-box');
    const breathText = document.getElementById('edu-breath-text');
    if (!coachBox || !breathText) return;
    
    // Clear any existing intervals
    if (eduBreathingTimer) clearInterval(eduBreathingTimer);
    
    const states = [
        { text: "Inhale deeply...", class: "breathing-in", duration: 4000 },
        { text: "Hold...", class: "breathing-hold", duration: 2000 },
        { text: "Exhale slowly...", class: "breathing-out", duration: 4000 },
        { text: "Hold...", class: "breathing-hold", duration: 2000 }
    ];
    
    let currentStateIndex = 0;
    
    function runState() {
        const state = states[currentStateIndex];
        breathText.textContent = state.text;
        
        // Update classes
        coachBox.classList.remove('breathing-in', 'breathing-hold', 'breathing-out');
        coachBox.classList.add(state.class);
        
        currentStateIndex = (currentStateIndex + 1) % states.length;
        
        // Schedule next state
        eduBreathingTimer = setTimeout(runState, state.duration);
    }
    
    runState();
}

// Initialize on load
document.addEventListener("DOMContentLoaded", () => {
    // Start the breathing coach if element is present
    if (document.getElementById('edu-breathing-box')) {
        startEduBreathingCoach();
    }
    
    // Load default anatomy details
    if (document.getElementById('anatomy-detail-title')) {
        showAnatomyInfo('uterus');
    }
});


