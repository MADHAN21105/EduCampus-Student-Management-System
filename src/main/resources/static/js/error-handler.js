// Custom Error Handling Utility for EduCampus
// This file provides user-friendly error messages instead of default browser alerts

// Create toast notification container
function createToastContainer() {
    if (!document.getElementById('toast-container')) {
        const container = document.createElement('div');
        container.id = 'toast-container';
        container.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 10000;
            display: flex;
            flex-direction: column;
            gap: 10px;
        `;
        document.body.appendChild(container);
    }
}

// Show toast notification
function showToast(message, type = 'error') {
    createToastContainer();

    const toast = document.createElement('div');
    toast.style.cssText = `
        background: ${type === 'error' ? '#fee2e2' : type === 'success' ? '#d1fae5' : '#dbeafe'};
        color: ${type === 'error' ? '#991b1b' : type === 'success' ? '#065f46' : '#1e40af'};
        padding: 1rem 1.5rem;
        border-radius: 8px;
        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        min-width: 300px;
        max-width: 500px;
        display: flex;
        align-items: center;
        gap: 0.75rem;
        animation: slideIn 0.3s ease-out;
        border-left: 4px solid ${type === 'error' ? '#dc2626' : type === 'success' ? '#059669' : '#2563eb'};
    `;

    const icon = type === 'error' ? '❌' : type === 'success' ? '✅' : 'ℹ️';
    toast.innerHTML = `
        <span style="font-size: 1.2rem;">${icon}</span>
        <span style="flex: 1; font-weight: 500;">${message}</span>
        <button onclick="this.parentElement.remove()" style="background: none; border: none; cursor: pointer; font-size: 1.2rem; color: inherit; opacity: 0.7;">×</button>
    `;

    document.getElementById('toast-container').appendChild(toast);

    // Auto remove after 5 seconds
    setTimeout(() => {
        toast.style.animation = 'slideOut 0.3s ease-out';
        setTimeout(() => toast.remove(), 300);
    }, 5000);
}

// Add animation styles
if (!document.getElementById('toast-styles')) {
    const style = document.createElement('style');
    style.id = 'toast-styles';
    style.textContent = `
        @keyframes slideIn {
            from {
                transform: translateX(400px);
                opacity: 0;
            }
            to {
                transform: translateX(0);
                opacity: 1;
            }
        }
        @keyframes slideOut {
            from {
                transform: translateX(0);
                opacity: 1;
            }
            to {
                transform: translateX(400px);
                opacity: 0;
            }
        }
    `;
    document.head.appendChild(style);
}

// Custom error handler
function handleError(error, context = '') {
    console.error(`Error in ${context}:`, error);

    let message = 'An unexpected error occurred. Please try again.';

    // Network errors
    if (error.message === 'Failed to fetch' || error.name === 'NetworkError') {
        message = '🌐 Unable to connect to server. Please check your internet connection.';
    }
    // Timeout errors
    else if (error.name === 'TimeoutError') {
        message = '⏱️ Request timed out. Please try again.';
    }
    // Authentication errors
    else if (error.status === 401 || error.status === 403) {
        message = '🔒 Session expired. Please login again.';
        setTimeout(() => {
            localStorage.clear();
            window.location.href = 'login.html';
        }, 2000);
    }
    // Server errors
    else if (error.status >= 500) {
        message = '🔧 Server error. Please try again later.';
    }
    // Not found errors
    else if (error.status === 404) {
        message = '🔍 Resource not found. Please refresh the page.';
    }
    // Custom error message
    else if (error.message) {
        message = error.message;
    }

    showToast(message, 'error');
}

// Enhanced fetch wrapper with error handling
async function safeFetch(url, options = {}) {
    try {
        const response = await fetch(url, {
            ...options,
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            }
        });

        if (!response.ok) {
            const error = new Error(await response.text() || 'Request failed');
            error.status = response.status;
            throw error;
        }

        return response;
    } catch (error) {
        handleError(error, `Fetch ${url}`);
        throw error;
    }
}

// Success notification
function showSuccess(message) {
    showToast(message, 'success');
}

// Info notification
function showInfo(message) {
    showToast(message, 'info');
}

// Replace default alert
window.customAlert = function (message, type = 'info') {
    showToast(message, type);
};

// Global error handler for uncaught errors
window.addEventListener('error', (event) => {
    event.preventDefault();
    handleError(event.error || new Error(event.message), 'Global');
});

// Global promise rejection handler
window.addEventListener('unhandledrejection', (event) => {
    event.preventDefault();
    handleError(event.reason, 'Promise');
});

console.log('✅ EduCampus Error Handler loaded successfully');
