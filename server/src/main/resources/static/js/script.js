function toggleMobileMenu() {
    const menu = document.getElementById("mobileMenu");
    if (menu) {
        menu.classList.toggle("active");
    }
}

function openModal() {
    const modalOverlay = document.getElementById("modalOverlay");
    if (modalOverlay) {
        modalOverlay.classList.add("active");
        document.body.style.overflow = "hidden";
    }
}

function closeModal() {
    const modalOverlay = document.getElementById("modalOverlay");
    const submitBtn = document.querySelector('[form="changeDetailsForm"]');
    if (modalOverlay) {
        modalOverlay.classList.remove("active");
        document.body.style.overflow = "auto";
    }
    if (submitBtn) {
        submitBtn.classList.remove("loading");
        submitBtn.disabled = false;
    }
}

function closeModalOnOverlay(event) {
    if (event.target === event.currentTarget) {
        closeModal();
    }
}

function openCancelModal() {
    const cancelModalOverlay = document.getElementById("cancelModalOverlay");
    if (cancelModalOverlay) {
        cancelModalOverlay.classList.add("active");
        document.body.style.overflow = "hidden";
    }
}

function closeCancelModal() {
    const cancelModalOverlay = document.getElementById("cancelModalOverlay");
    if (cancelModalOverlay) {
        cancelModalOverlay.classList.remove("active");
        document.body.style.overflow = "auto";
    }
}

function closeCancelModalOnOverlay(event) {
    if (event.target === event.currentTarget) {
        closeCancelModal();
    }
}

function openSeatingModal() {
    const modal = document.getElementById("seatingModalOverlay");
    if (modal) {
        modal.classList.add("active");
        document.body.style.overflow = "hidden";
    }

    const seatingNotification = document.getElementById("seatingNotification");
    const mobileMenuNotification = document.getElementById("mobileMenuNotification");
    if (seatingNotification) {
        seatingNotification.style.display = "inline-block";
    }
    if (mobileMenuNotification) {
        mobileMenuNotification.style.display = "inline-block";
    }
}

function closeSeatingModal() {
    const modal = document.getElementById("seatingModalOverlay");
    if (modal) {
        modal.classList.remove("active");
        document.body.style.overflow = "auto";
    }
}

function notificationIcon() {
    const showConfirm = document.getElementById("showConfirm");
    const showConfirmMobile = document.getElementById("showConfirmMobile");
    if (showConfirm) {
        showConfirm.style.display = "inline-block";
    }
    if (showConfirmMobile) {
        showConfirmMobile.style.display = "inline-block";
    }

    document.querySelectorAll(".notification-dot").forEach((dot) => {
        dot.style.display = "inline-block";
    });
}

function declineSeating() {
    closeSeatingModal();
}

function applyButtonPressAnimation(button) {
    if (!button) {
        return;
    }

    button.addEventListener("mousedown", function () {
        if (!this.disabled && !this.classList.contains("loading")) {
            this.style.transform = "scale(0.98)";
        }
    });

    button.addEventListener("mouseup", function () {
        this.style.transform = "";
    });

    button.addEventListener("mouseleave", function () {
        this.style.transform = "";
    });
}

function formatPhoneNumber(digits) {
    let normalized = String(digits || "").replace(/\D/g, "");
    if (normalized.startsWith("63")) {
        normalized = normalized.slice(2);
    }
    normalized = normalized.slice(0, 10);

    if (normalized.length === 0) {
        return "";
    }

    let formatted = "+63";
    if (normalized.length > 0) {
        formatted += " " + normalized.slice(0, 3);
    }
    if (normalized.length > 3) {
        formatted += " " + normalized.slice(3, 6);
    }
    if (normalized.length > 6) {
        formatted += " " + normalized.slice(6, 10);
    }
    return formatted;
}

function attachStandardPhoneFormatter(input, options) {
    if (!input) {
        return;
    }

    const keepPrefix = Boolean(options && options.keepPrefix);
    const prefix = "+63";

    if (keepPrefix && !input.value.trim()) {
        input.value = prefix;
    }

    input.addEventListener("input", function () {
        const formatted = formatPhoneNumber(this.value);
        this.value = keepPrefix ? (formatted || prefix) : formatted;
    });

    if (keepPrefix) {
        input.addEventListener("keydown", function (event) {
            const cursorPos = this.selectionStart;
            if (cursorPos <= prefix.length + 1 && (event.key === "Backspace" || event.key === "Delete")) {
                event.preventDefault();
            }
        });

        input.addEventListener("paste", function (event) {
            event.preventDefault();
            const pastedText = event.clipboardData.getData("text");
            const formatted = formatPhoneNumber(pastedText);
            this.value = formatted || prefix;
        });
    }
}

function removePhoneSpaces(input) {
    if (input) {
        input.value = input.value.replace(/\s/g, "");
    }
}

function setupEnterNavigation(form, selector, submitGuard) {
    if (!form) {
        return;
    }

    const inputs = Array.from(form.querySelectorAll(selector));
    inputs.forEach((input, index) => {
        input.addEventListener("keypress", function (event) {
            if (event.key !== "Enter") {
                return;
            }

            event.preventDefault();

            if (index === inputs.length - 1) {
                if (!submitGuard || submitGuard()) {
                    form.requestSubmit();
                }
                return;
            }

            const nextInput = inputs[index + 1];
            if (nextInput) {
                nextInput.focus();
            }
        });
    });
}

const APP_TIME_ZONE = "Asia/Manila";

function parseServerDateTime(dateTimeString) {
    if (!dateTimeString) {
        return null;
    }

    const normalized = String(dateTimeString)
        .trim()
        .replace(" ", "T")
        .replace(/Z$/, "+00:00");
    const withOffset = /([+-]\d{2}:\d{2})$/.test(normalized) ? normalized : `${normalized}+00:00`;
    const date = new Date(withOffset);
    return Number.isNaN(date.getTime()) ? null : date;
}

function formatDateTime(date) {
    if (!date) {
        return "";
    }

    return date.toLocaleString(undefined, {
        year: "numeric",
        month: "long",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
        hour12: true,
        timeZone: APP_TIME_ZONE
    });
}

function toStatusClass(status) {
    return `status-${String(status || "").replace(/\s+/g, "-")}`;
}

function initializeCommonUi() {
    document.addEventListener("click", function (event) {
        const menu = document.getElementById("mobileMenu");
        const toggle = document.querySelector(".navbar-toggle");

        if (menu && toggle && !menu.contains(event.target) && !toggle.contains(event.target)) {
            menu.classList.remove("active");
        }
    });

    document.addEventListener("keydown", function (event) {
        if (event.key !== "Escape") {
            return;
        }

        closeModal();
        closeCancelModal();
        closeSeatingModal();
    });

    const seatingModalOverlay = document.getElementById("seatingModalOverlay");
    if (seatingModalOverlay) {
        seatingModalOverlay.addEventListener("click", function (event) {
            if (event.target === this) {
                closeSeatingModal();
            }
        });
    }
}

function initializeLoginPage() {
    const form = document.getElementById("login");
    const submitBtn = document.getElementById("open");
    const phoneInput = document.getElementById("Phone");
    const referenceInput = document.getElementById("Reference");

    if (!form || !submitBtn || !phoneInput || !referenceInput) {
        return;
    }

    attachStandardPhoneFormatter(phoneInput, { keepPrefix: false });

    referenceInput.addEventListener("input", function () {
        this.value = this.value.toUpperCase();
    });

    form.addEventListener("submit", function () {
        if (!form.checkValidity()) {
            return;
        }

        submitBtn.classList.add("loading");
        submitBtn.disabled = true;
        removePhoneSpaces(phoneInput);
    });

    setupEnterNavigation(form, "input", function () {
        return form.checkValidity();
    });
    applyButtonPressAnimation(submitBtn);
}

function initializeRegistrationPage() {
    const agree = document.getElementById("agree");
    const proceedBtn = document.getElementById("submit");
    const form = document.getElementById("infoForm");
    const phoneInput = document.getElementById("Phone");

    if (!agree || !proceedBtn || !form || !phoneInput) {
        return;
    }

    attachStandardPhoneFormatter(phoneInput, { keepPrefix: true });

    agree.addEventListener("change", function () {
        proceedBtn.disabled = !agree.checked;
    });

    form.addEventListener("submit", function () {
        if (!form.checkValidity()) {
            return;
        }

        proceedBtn.classList.add("loading");
        proceedBtn.disabled = true;
        removePhoneSpaces(phoneInput);
    });

    setupEnterNavigation(form, "input, select", function () {
        return !proceedBtn.disabled;
    });
    applyButtonPressAnimation(proceedBtn);
}

function initializeReservationPage() {
    const page = document.body;
    const changeDetailsForm = document.getElementById("changeDetailsForm");
    const changeSubmitBtn = document.querySelector('[form="changeDetailsForm"]');
    const phoneInput = document.getElementById("customerPhone");
    const confirmForm = document.querySelector('#seatingModalOverlay form');
    const confirmBtn = confirmForm ? confirmForm.querySelector(".seating-btn-confirm") : null;
    const phoneDisplay = document.getElementById("phoneDisplay");
    const createdElement = document.getElementById("createdTime");
    const countupElement = document.getElementById("countup");
    const queueCount = document.getElementById("queue");
    const cancelBtn = document.getElementById("cancelbtn");
    const changeBtn = document.getElementById("changebtn");
    const cancelBtnMobile = document.getElementById("cancelbtnmobile");
    const changeBtnMobile = document.getElementById("changebtnmobile");
    const statusLabel = document.getElementById("statusLabel");

    if (!changeDetailsForm || !countupElement || !page) {
        return;
    }

    let currentStatus = page.dataset.status || "";
    const reference = page.dataset.reference || "";
    const pendingTimeStr = page.dataset.pendingDatetime || "";
    const completeTimeStr = page.dataset.completeDatetime || "";
    const cancelledTimeStr = page.dataset.cancelledDatetime || "";
    const noShowTimeStr = page.dataset.noShowDatetime || "";
    const reservationId = page.dataset.reservationId;
    const startTime = parseServerDateTime(pendingTimeStr);
    let countupInterval = null;
    let statusInterval = null;
    let seatingShown = false;

    function formatPhoneDisplay() {
        if (phoneDisplay) {
            const formatted = formatPhoneNumber(phoneDisplay.textContent);
            if (formatted) {
                phoneDisplay.textContent = formatted;
            }
        }
    }

    function displayCreatedTime() {
        if (createdElement) {
            const parsedDate = parseServerDateTime(createdElement.textContent);
            if (parsedDate) {
                createdElement.textContent = formatDateTime(parsedDate);
            }
        }
    }

    function updateButtons(status) {
        const isPending = status === "Pending";
        if (queueCount) {
            queueCount.style.display = isPending ? "inline-flex" : "none";
        }
        if (cancelBtn) {
            cancelBtn.style.display = isPending ? "inline-flex" : "none";
        }
        if (changeBtn) {
            changeBtn.style.display = isPending ? "inline-flex" : "none";
        }
        if (cancelBtnMobile) {
            cancelBtnMobile.style.display = isPending ? "inline-flex" : "none";
        }
        if (changeBtnMobile) {
            changeBtnMobile.style.display = isPending ? "inline-flex" : "none";
        }
    }

    function updateCountUpColor(status) {
        Array.from(countupElement.classList).forEach((className) => {
            if (["Pending", "Confirm", "Seated", "Complete", "Cancelled", "No-Show"].includes(className)) {
                countupElement.classList.remove(className);
            }
        });
        countupElement.classList.add(String(status || "").replace(/\s+/g, "-"));
    }

    function updateStatusLabel(status) {
        if (!statusLabel) {
            return;
        }

        Array.from(statusLabel.classList).forEach((className) => {
            if (className.startsWith("status-")) {
                statusLabel.classList.remove(className);
            }
        });

        statusLabel.classList.add(toStatusClass(status));
        statusLabel.textContent = status;
    }

    function updateProgressBar(status) {
        const statusMap = {
            Pending: 6,
            Confirm: 12,
            Seated: 18,
            Complete: 24,
            Cancelled: 24,
            "No Show": 24
        };

        const activeSegments = statusMap[status] || 0;
        const segments = document.querySelectorAll(".progress-bar .progress-segment");

        segments.forEach((segment, index) => {
            const segmentNumber = index + 1;
            Array.from(segment.classList).forEach((className) => {
                if (className === "active" || className.startsWith("status-")) {
                    segment.classList.remove(className);
                }
            });

            if (segmentNumber <= activeSegments) {
                segment.classList.add("active", toStatusClass(status));
            }
        });
    }

    function stopReservationIntervals() {
        if (countupInterval) {
            clearInterval(countupInterval);
            countupInterval = null;
        }
        if (statusInterval) {
            clearInterval(statusInterval);
            statusInterval = null;
        }
    }

    function updateCountUp() {
        if (!startTime) {
            countupElement.textContent = "00:00:00";
            return;
        }

        let finishTime = null;
        if (currentStatus === "Complete") {
            finishTime = parseServerDateTime(completeTimeStr);
        } else if (currentStatus === "Cancelled") {
            finishTime = parseServerDateTime(cancelledTimeStr);
        } else if (currentStatus === "No Show") {
            finishTime = parseServerDateTime(noShowTimeStr);
        }

        const diff = Math.max((finishTime || new Date()) - startTime, 0);
        const hours = Math.floor(diff / 1000 / 3600);
        const minutes = Math.floor((diff / 1000 % 3600) / 60);
        const seconds = Math.floor((diff / 1000) % 60);

        countupElement.textContent = [
            String(hours).padStart(2, "0"),
            String(minutes).padStart(2, "0"),
            String(seconds).padStart(2, "0")
        ].join(":");

        if (finishTime) {
            stopReservationIntervals();
        }
    }

    function pollReservationStatus() {
        if (!reservationId) {
            return;
        }

        fetch(`/reservation/status?reservationId=${reservationId}`)
            .then((response) => response.json())
            .then((data) => {
                if (!data.status || data.status === currentStatus) {
                    return;
                }

                currentStatus = data.status;
                updateButtons(currentStatus);
                updateCountUpColor(currentStatus);
                updateProgressBar(currentStatus);
                updateStatusLabel(currentStatus);
                updateCountUp();

                if (currentStatus === "Complete" || currentStatus === "Cancelled" || currentStatus === "No Show") {
                    stopReservationIntervals();
                }
            })
            .catch((error) => console.error("Status check failed", error));
    }

    function connectReservationSocket() {
        if (!reference || !window.Stomp) {
            return;
        }

        const wsProtocol = window.location.protocol === "https:" ? "wss:" : "ws:";
        const wsUrl = `${wsProtocol}//${window.location.host}/ws`;
        const stompClient = Stomp.client(wsUrl);
        stompClient.reconnect_delay = 5000;

        stompClient.connect(
            {},
            function () {
                stompClient.subscribe(`/topic/account.${reference}`, function () {
                    if (!seatingShown) {
                        openSeatingModal();
                        notificationIcon();
                        seatingShown = true;
                    }
                });

                if (currentStatus === "Pending") {
                    fetch(`/pendingNotifications?reference=${reference}`)
                        .then((response) => {
                            if (!response.ok) {
                                throw new Error(`HTTP ${response.status}`);
                            }
                            return response.json();
                        })
                        .then((data) => {
                            if (Array.isArray(data) && data.length > 0 && !seatingShown) {
                                openSeatingModal();
                                notificationIcon();
                                seatingShown = true;
                            }
                        })
                        .catch((error) => console.error("Failed to fetch pending notifications:", error));
                }
            },
            function (error) {
                console.error("STOMP connection failed:", error);
            }
        );
    }

    attachStandardPhoneFormatter(phoneInput, { keepPrefix: false });

    changeDetailsForm.addEventListener("submit", function () {
        if (!changeDetailsForm.checkValidity()) {
            return;
        }

        if (changeSubmitBtn) {
            changeSubmitBtn.classList.add("loading");
            changeSubmitBtn.disabled = true;
        }
        removePhoneSpaces(phoneInput);
    });

    if (confirmForm && confirmBtn) {
        confirmForm.addEventListener("submit", function () {
            confirmBtn.classList.add("loading");
            confirmBtn.disabled = true;
        });
    }

    applyButtonPressAnimation(changeSubmitBtn);
    applyButtonPressAnimation(confirmBtn);
    formatPhoneDisplay();
    displayCreatedTime();
    updateButtons(currentStatus);
    updateCountUpColor(currentStatus);
    updateProgressBar(currentStatus);
    updateStatusLabel(currentStatus);
    updateCountUp();
    connectReservationSocket();

    if (currentStatus !== "Complete" && currentStatus !== "Cancelled" && currentStatus !== "No Show") {
        countupInterval = setInterval(updateCountUp, 1000);
        if (reservationId) {
            statusInterval = setInterval(pollReservationStatus, 10000);
        }
    }
}

window.toggleMobileMenu = toggleMobileMenu;
window.openModal = openModal;
window.closeModal = closeModal;
window.closeModalOnOverlay = closeModalOnOverlay;
window.openCancelModal = openCancelModal;
window.closeCancelModal = closeCancelModal;
window.closeCancelModalOnOverlay = closeCancelModalOnOverlay;
window.openSeatingModal = openSeatingModal;
window.closeSeatingModal = closeSeatingModal;
window.notificationIcon = notificationIcon;
window.declineSeating = declineSeating;

initializeCommonUi();

switch (document.body.dataset.page) {
    case "login":
        initializeLoginPage();
        break;
    case "registration":
        initializeRegistrationPage();
        break;
    case "reservation-data":
        initializeReservationPage();
        break;
    default:
        break;
}
