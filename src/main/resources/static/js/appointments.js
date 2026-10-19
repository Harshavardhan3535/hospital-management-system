// ==========================================
// Appointments Module
// ==========================================

let allAppointments = [];
let allPatients = [];
let allDoctors = [];
let deleteTargetId = null;

// ===== INIT =====
document.addEventListener('DOMContentLoaded', async () => {
    if (!requireAuth()) return;

    const username = getUsername() || 'User';
    document.getElementById('userInfo').innerHTML = `Welcome, <strong>${username}</strong>`;

    await Promise.all([loadPatients(), loadDoctors(), loadAppointments()]);
});

// ===== LOAD DROPDOWN DATA =====
async function loadPatients() {
    try {
        const res = await apiFetch('/api/patients');
        if (res.ok) allPatients = await res.json();
    } catch (err) { console.error(err); }
}

async function loadDoctors() {
    try {
        const res = await apiFetch('/api/doctors');
        if (res.ok) allDoctors = await res.json();
    } catch (err) { console.error(err); }
}

function populateDropdowns() {
    const patientSelect = document.getElementById('aPatientId');
    patientSelect.innerHTML = '<option value="">-- Select Patient --</option>' +
        allPatients.map(p => `<option value="${p.id}">${escapeHtml(p.name)} (${escapeHtml(p.email)})</option>`).join('');

    const doctorSelect = document.getElementById('aDoctorId');
    doctorSelect.innerHTML = '<option value="">-- Select Doctor --</option>' +
        allDoctors.map(d => `<option value="${d.id}">Dr. ${escapeHtml(d.name)} (${escapeHtml(d.specialization)})</option>`).join('');
}

// ===== LOAD APPOINTMENTS =====
async function loadAppointments() {
    try {
        const res = await apiFetch('/api/appointments');
        if (!res.ok) throw new Error('Failed to load appointments');
        allAppointments = await res.json();
        renderTable(allAppointments);
    } catch (err) {
        document.getElementById('appointmentTableBody').innerHTML =
            `<tr><td colspan="7" class="error">${err.message}</td></tr>`;
    }
}

// ===== RENDER =====
function renderTable(appointments) {
    const tbody = document.getElementById('appointmentTableBody');
    const emptyState = document.getElementById('emptyState');

    if (appointments.length === 0) {
        tbody.innerHTML = '';
        emptyState.style.display = 'block';
        return;
    }

    emptyState.style.display = 'none';

    tbody.innerHTML = appointments.map(a => `
        <tr>
            <td>${a.id}</td>
            <td><strong>${a.appointmentDate}</strong></td>
            <td>${escapeHtml(a.timeSlot)}</td>
            <td>${escapeHtml(a.patientName)}</td>
            <td>Dr. ${escapeHtml(a.doctorName)}</td>
            <td><span class="badge status-${a.status.toLowerCase()}">${a.status}</span></td>
            <td class="actions">
                <button class="btn-icon" onclick="editAppointment(${a.id})" title="Edit">✏️</button>
                <button class="btn-icon" onclick="openDeleteModal(${a.id})" title="Delete">🗑️</button>
            </td>
        </tr>
    `).join('');
}

// ===== FILTER =====
function filterAppointments() {
    const q = document.getElementById('searchInput').value.toLowerCase().trim();
    const status = document.getElementById('statusFilter').value;

    let filtered = allAppointments;

    if (q) {
        filtered = filtered.filter(a =>
            (a.patientName || '').toLowerCase().includes(q) ||
            (a.doctorName || '').toLowerCase().includes(q) ||
            (a.appointmentDate || '').includes(q)
        );
    }

    if (status) {
        filtered = filtered.filter(a => a.status === status);
    }

    renderTable(filtered);
}

// ===== MODAL =====
function openAppointmentModal() {
    document.getElementById('appointmentForm').reset();
    document.getElementById('appointmentId').value = '';
    document.getElementById('modalTitle').textContent = 'Book Appointment';
    document.getElementById('aDate').value = new Date().toISOString().split('T')[0];
    document.getElementById('formError').style.display = 'none';
    populateDropdowns();
    document.getElementById('appointmentModal').style.display = 'flex';
}

function closeAppointmentModal() {
    document.getElementById('appointmentModal').style.display = 'none';
}

function editAppointment(id) {
    const appt = allAppointments.find(a => a.id === id);
    if (!appt) return;

    document.getElementById('appointmentId').value = appt.id;
    populateDropdowns();
    
    document.getElementById('aPatientId').value = appt.patientId || '';
    document.getElementById('aDoctorId').value = appt.doctorId || '';
    document.getElementById('aDate').value = appt.appointmentDate || '';
    document.getElementById('aTimeSlot').value = appt.timeSlot || '';
    document.getElementById('aStatus').value = appt.status || 'SCHEDULED';
    document.getElementById('aNotes').value = appt.notes || '';

    document.getElementById('modalTitle').textContent = 'Edit Appointment';
    document.getElementById('formError').style.display = 'none';
    document.getElementById('appointmentModal').style.display = 'flex';
}

// ===== SAVE =====
async function saveAppointment(event) {
    event.preventDefault();

    const id = document.getElementById('appointmentId').value;
    const saveBtn = document.getElementById('saveBtn');
    const errorDiv = document.getElementById('formError');

    errorDiv.style.display = 'none';
    saveBtn.disabled = true;
    saveBtn.textContent = 'Saving...';

    const body = {
        patientId: parseInt(document.getElementById('aPatientId').value),
        doctorId: parseInt(document.getElementById('aDoctorId').value),
        appointmentDate: document.getElementById('aDate').value,
        timeSlot: document.getElementById('aTimeSlot').value.trim(),
        status: document.getElementById('aStatus').value,
        notes: document.getElementById('aNotes').value.trim() || null
    };

    if (!body.patientId || !body.doctorId) {
        errorDiv.textContent = 'Please select both a patient and a doctor.';
        errorDiv.style.display = 'block';
        saveBtn.disabled = false;
        saveBtn.textContent = 'Save';
        return;
    }

    try {
        const url = id ? `/api/appointments/${id}` : '/api/appointments';
        const method = id ? 'PUT' : 'POST';

        const res = await apiFetch(url, {
            method,
            body: JSON.stringify(body)
        });

		if (!res.ok) {
		    const err = await res.json();
		    throw new Error(err.message || 'Save failed');
		}

		// 👇 ADD THIS LINE
		showToast(id ? 'Appointment updated successfully' : 'Appointment booked successfully');

		closeAppointmentModal();
		await loadAppointments();
    } catch (err) {
        errorDiv.textContent = err.message;
        errorDiv.style.display = 'block';
    } finally {
        saveBtn.disabled = false;
        saveBtn.textContent = 'Save';
    }
}

// ===== DELETE =====
function openDeleteModal(id) {
    deleteTargetId = id;
    document.getElementById('deleteModal').style.display = 'flex';
}

function closeDeleteModal() {
    document.getElementById('deleteModal').style.display = 'none';
    deleteTargetId = null;
}

async function confirmDelete() {
    if (!deleteTargetId) return;

    const btn = document.getElementById('confirmDeleteBtn');
    btn.disabled = true;
    btn.textContent = 'Deleting...';

    try {
        const res = await apiFetch(`/api/appointments/${deleteTargetId}`, {
            method: 'DELETE'
        });

        if (!res.ok && res.status !== 204) {
            const err = await res.json();
            throw new Error(err.message || 'Delete failed');
        }

        closeDeleteModal();
        await loadAppointments();
		showToast('Deleted successfully'); // 👈 ADD THIS
		} catch (err) {
		    // 👇 REPLACE alert WITH THIS
		    showToast('Delete failed: ' + err.message, 'error');
		    closeDeleteModal();
		} finally {
		    btn.disabled = false;
		    btn.textContent = 'Delete';
		}
}

// ===== HELPERS =====
function escapeHtml(str) {
    if (str == null) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}