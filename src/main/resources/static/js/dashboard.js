// ==========================================
// Dashboard — Load stats and welcome
// ==========================================

document.addEventListener('DOMContentLoaded', async () => {
    if (!requireAuth()) return;

    // Show user info in navbar
    const username = getUsername() || 'User';
    const roles = getRoles();
    document.getElementById('userInfo').innerHTML =
        `Welcome, <strong>${username}</strong> (${roles.join(', ').replace(/ROLE_/g, '')})`;

    document.getElementById('welcomeMessage').textContent =
        `Welcome back, ${username}!`;

    // Load stats in parallel
    await Promise.all([
        loadCount('patientCount', '/api/patients'),
        loadCount('doctorCount', '/api/doctors'),
        loadCount('appointmentCount', '/api/appointments')
    ]);

    // Load pending bills count
    try {
        const res = await apiFetch('/api/bills/status/PENDING');
        if (res.ok) {
            const bills = await res.json();
            document.getElementById('pendingBills').textContent = bills.length;
        }
    } catch (err) {
        document.getElementById('pendingBills').textContent = '0';
    }
});

async function loadCount(elementId, endpoint) {
    try {
        const res = await apiFetch(endpoint);
        if (res.ok) {
            const data = await res.json();
            document.getElementById(elementId).textContent = data.length;
        } else {
            document.getElementById(elementId).textContent = '0';
        }
    } catch (err) {
        document.getElementById(elementId).textContent = 'Error';
    }
}