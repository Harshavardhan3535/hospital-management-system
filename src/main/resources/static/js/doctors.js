// ==========================================
// Doctors Module
// ==========================================

let allDoctors = [];
let allDepartments = [];
let deleteTargetId = null;

// ===== INIT =====
document.addEventListener('DOMContentLoaded', async () => {
    if (!requireAuth()) return;

    const username = getUsername() || 'User';
    document.getElementById('userInfo').innerHTML = `Welcome, <strong>${username}</strong>`;

    await Promise.all([loadDepartments(), loadDoctors()]);
});

// ===== LOAD DEPARTMENTS =====
async function loadDepartments() {
    try {
        const res = await apiFetch('/api/departments');
        if (!res.ok) throw new Error('Failed to load departments');
        allDepartments = await res.json();
    } catch (err) {
        console.error('Department load failed:', err);
    }
}

function populateDepartmentDropdown() {
    const select = document.getElementById('dDepartmentId');
    select.innerHTML = '<option value="">-- Select Department --</option>' +
        allDepartments.map(d => `<option value="${d.id}">${escapeHtml(d.name)}</option>`).join('');
}

// ===== LOAD DOCTORS =====
async function loadDoctors() {
    try {
        const res = await apiFetch('/api/doctors');
        if (!res.ok) throw new Error('Failed to load doctors');
        allDoctors = await res.json();
        renderTable(allDoctors);
    } catch (err) {
        document.getElementById('doctorTableBody').innerHTML =
            `<tr><td colspan="7" class="error">${err.message}</td></tr>`;
    }
}

// ===== RENDER =====
function renderTable(doctors) {
    const tbody = document.getElementById('doctorTableBody');
    const emptyState = document.getElementById('emptyState');

    if (doctors.length === 0) {
        tbody.innerHTML = '';
        emptyState.style.display = 'block';
        return;
    }

    emptyState.style.display = 'none';

    tbody.innerHTML = doctors.map(d => `
        <tr>
            <td>${d.id}</td>
            <td><strong>${escapeHtml(d.name)}</strong></td>
            <td>${escapeHtml(d.specialization)}</td>
            <td>${escapeHtml(d.email)}</td>
            <td>${escapeHtml(d.phone || '-')}</td>
            <td><span class="badge">${escapeHtml(d.departmentName || '-')}</span></td>
            <td class="actions">
                <button class="btn-icon" onclick="editDoctor(${d.id})" title="Edit">✏️</button>
                <button class="btn-icon" onclick="openDeleteModal(${d.id}, '${escapeHtml(d.name).replace(/'/g, "\\'")}')" title="Delete">🗑️</button>
            </td>
        </tr>
    `).join('');
}

// ===== SEARCH =====
function filterDoctors() {
    const q = document.getElementById('searchInput').value.toLowerCase().trim();
    if (!q) {
        renderTable(allDoctors);
        return;
    }
    const filtered = allDoctors.filter(d =>
        (d.name || '').toLowerCase().includes(q) ||
        (d.specialization || '').toLowerCase().includes(q) ||
        (d.email || '').toLowerCase().includes(q) ||
        (d.departmentName || '').toLowerCase().includes(q)
    );
    renderTable(filtered);
}

// ===== MODAL =====
function openDoctorModal() {
    document.getElementById('doctorForm').reset();
    document.getElementById('doctorId').value = '';
    document.getElementById('modalTitle').textContent = 'Add Doctor';
    document.getElementById('formError').style.display = 'none';
    populateDepartmentDropdown();
    document.getElementById('doctorModal').style.display = 'flex';
}

function closeDoctorModal() {
    document.getElementById('doctorModal').style.display = 'none';
}

function editDoctor(id) {
    const doctor = allDoctors.find(d => d.id === id);
    if (!doctor) return;

    document.getElementById('doctorId').value = doctor.id;
    document.getElementById('dName').value = doctor.name || '';
    document.getElementById('dSpecialization').value = doctor.specialization || '';
    document.getElementById('dEmail').value = doctor.email || '';
    document.getElementById('dPhone').value = doctor.phone || '';

    populateDepartmentDropdown();
    document.getElementById('dDepartmentId').value = doctor.departmentId || '';

    document.getElementById('modalTitle').textContent = 'Edit Doctor';
    document.getElementById('formError').style.display = 'none';
    document.getElementById('doctorModal').style.display = 'flex';
}

// ===== SAVE =====
async function saveDoctor(event) {
    event.preventDefault();

    const id = document.getElementById('doctorId').value;
    const saveBtn = document.getElementById('saveBtn');
    const errorDiv = document.getElementById('formError');

    errorDiv.style.display = 'none';
    saveBtn.disabled = true;
    saveBtn.textContent = 'Saving...';

    const body = {
        name: document.getElementById('dName').value.trim(),
        specialization: document.getElementById('dSpecialization').value.trim(),
        email: document.getElementById('dEmail').value.trim(),
        phone: document.getElementById('dPhone').value.trim() || null,
        departmentId: parseInt(document.getElementById('dDepartmentId').value)
    };

    if (!body.departmentId) {
        errorDiv.textContent = 'Please select a department';
        errorDiv.style.display = 'block';
        saveBtn.disabled = false;
        saveBtn.textContent = 'Save';
        return;
    }

    try {
        const url = id ? `/api/doctors/${id}` : '/api/doctors';
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
		showToast(id ? 'Doctor updated successfully' : 'Doctor added successfully');

		closeDoctorModal();
		await loadDoctors();
    } catch (err) {
        errorDiv.textContent = err.message;
        errorDiv.style.display = 'block';
    } finally {
        saveBtn.disabled = false;
        saveBtn.textContent = 'Save';
    }
}

// ===== DELETE =====
function openDeleteModal(id, name) {
    deleteTargetId = id;
    document.getElementById('deleteName').textContent = name;
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
        const res = await apiFetch(`/api/doctors/${deleteTargetId}`, {
            method: 'DELETE'
        });

        if (!res.ok && res.status !== 204) {
            const err = await res.json();
            throw new Error(err.message || 'Delete failed');
        }

        closeDeleteModal();
        await loadDoctors();
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