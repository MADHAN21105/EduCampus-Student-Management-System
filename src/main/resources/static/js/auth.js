document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');

    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;

            try {
                const role = window.currentRole || 'STUDENT';
                const response = await fetch('/api/auth/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ username, password, role })
                });

                if (response.ok) {
                    const data = await response.json();
                    localStorage.setItem('token', data.token);
                    localStorage.setItem('role', data.role);
                    localStorage.setItem('username', username);

                    // Check if password is temporary
                    if (data.temporaryPassword) {
                        window.location.href = 'change-password.html';
                        return;
                    }

                    // Redirect based on role
                    redirectUser(data.role);
                } else {
                    const errorData = await response.json().catch(() => ({}));
                    const message = errorData.message || 'Invalid credentials. Please try again.';
                    showToast(message, 'error');
                }
            } catch (error) {
                console.error('Login error:', error);
                showToast('Unable to connect to server. Please check if the backend is running.', 'error');
            }
        });
    }

    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const username = document.getElementById('reg-username').value;
            const password = document.getElementById('reg-password').value;
            const role = document.getElementById('role').value;

            try {
                const response = await fetch('/api/auth/register', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ username, password, role })
                });

                if (response.ok) {
                    showSuccess('Registration successful! Redirecting to login...');
                    setTimeout(() => {
                        window.location.href = 'login.html';
                    }, 1500);
                } else {
                    const errText = await response.text();
                    showToast('Registration failed: ' + errText, 'error');
                }
            } catch (error) {
                console.error('Registration error:', error);
                showToast('Unable to connect to server. Please check if the backend is running.', 'error');
            }
        });
    }
});

function redirectUser(role) {
    if (role === 'ADMIN') window.location.href = 'admin-dashboard.html';
    else if (role === 'TEACHER') window.location.href = 'teacher-dashboard.html';
    else if (role === 'STUDENT') window.location.href = 'student-dashboard.html';
    else window.location.href = 'login.html';
}

function checkAuth() {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = 'login.html';
    }
    return token;
}

function logout() {
    localStorage.clear();
    window.location.href = 'login.html';
}
