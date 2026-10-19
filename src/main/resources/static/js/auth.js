// ==========================================
// HMS — Authentication Module
// ==========================================

const API_BASE = 'http://localhost:8081';

// ==========================================
// Login handler
// ==========================================
document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    // If already logged in, redirect to dashboard
    if (window.location.pathname === '/login' && getToken()) {
        window.location.href = '/dashboard';
    }
});

async function handleLogin(event) {
    event.preventDefault();

    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;
    const errorDiv = document.getElementById('errorMessage');
    const loginBtn = document.getElementById('loginBtn');

    // Reset error
    errorDiv.style.display = 'none';
    errorDiv.textContent = '';

    // Disable button while processing
    loginBtn.disabled = true;
    loginBtn.textContent = 'Signing in...';

    try {
        const response = await fetch(`${API_BASE}/api/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || 'Login failed');
        }

        // Save token + user info in localStorage
        localStorage.setItem('hms_token', data.token);
        localStorage.setItem('hms_username', data.username);
        localStorage.setItem('hms_roles', JSON.stringify(data.roles || []));

        // Redirect to dashboard
        window.location.href = '/dashboard';

    } catch (error) {
        errorDiv.textContent = error.message || 'An error occurred. Please try again.';
        errorDiv.style.display = 'block';
    } finally {
        loginBtn.disabled = false;
        loginBtn.textContent = 'Sign In';
    }
}

// ==========================================
// Token helpers — used by other pages
// ==========================================
function getToken() {
    return localStorage.getItem('hms_token');
}

function getUsername() {
    return localStorage.getItem('hms_username');
}

function getRoles() {
    try {
        return JSON.parse(localStorage.getItem('hms_roles') || '[]');
    } catch {
        return [];
    }
}

function hasRole(role) {
    return getRoles().includes(role);
}

function logout() {
    localStorage.removeItem('hms_token');
    localStorage.removeItem('hms_username');
    localStorage.removeItem('hms_roles');
    window.location.href = '/login';
}

// ==========================================
// Require auth — call at top of protected pages
// ==========================================
function requireAuth() {
    if (!getToken()) {
        window.location.href = '/login';
        return false;
    }
    return true;
}

// ==========================================
// Fetch wrapper — auto adds Authorization header
// ==========================================
async function apiFetch(path, options = {}) {
    const token = getToken();
    const headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {})
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE}${path}`, {
        ...options,
        headers
    });

    // Token expired or invalid
    if (response.status === 401) {
        logout();
        throw new Error('Session expired. Please login again.');
    }

    return response;
}

// ==========================================
// Toast Notifications (Global)
// ==========================================
function showToast(message, type = 'success') {
    // Create container if it doesn't exist
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <span>${type === 'success' ? '✅' : '⚠️'}</span>
        <span>${message}</span>
    `;

    container.appendChild(toast);

    // Auto remove after 3 seconds
    setTimeout(() => {
        toast.classList.add('hiding');
        toast.addEventListener('animationend', () => toast.remove());
    }, 3000);
}

// ==========================================
// Global Footer injection (only on pages with main-container)
// ==========================================
document.addEventListener('DOMContentLoaded', () => {
    if (document.querySelector('.main-container') && !document.querySelector('.app-footer')) {
        const footer = document.createElement('footer');
        footer.className = 'app-footer';
        footer.innerHTML = `
            <p>🏥 Hospital Management System &copy; ${new Date().getFullYear()} — Built with Spring Boot & Thymeleaf</p>
        `;
        document.body.appendChild(footer);
    }
});