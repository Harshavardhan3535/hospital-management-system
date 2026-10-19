// ==========================================
// Patients Module
// ==========================================

let allPatients = [];
let deleteTargetId = null;

// ===== INIT =====
document.addEventListener('DOMContentLoaded', async () => {
    if (!requireAuth()) return;

    const username = getUsername() || 'User';
    document.getElementById('userInfo').innerHTML = `Welcome, <strong>${username}</strong>`;

    await loadPatients();
});

// ===== LOAD =====
async function loadPatients() {
    try {
        const res = await apiFetch('/api/patients');
        if (!res.ok) throw new Error('Failed to load patients');
        allPatients = await res.json();
        renderTable(allPatients);
    } catch (err) {
        document.getElementById('patientTableBody').innerHTML =
            `<tr><td colspan="7" class="error">${err.message}</td></tr>`;
    }
}

// ===== RENDER =====
function renderTable(patients) {
    const tbody = document.getElementById('patientTableBody');
    const emptyState = document.getElementById('emptyState');

    if (patients.length === 0) {
        tbody.innerHTML = '';
        emptyState.style.display = 'block';
        return;
    }

    emptyState.style.display = 'none';

    tbody.innerHTML = patients.map(p => `
        <tr>
            <td>${p.id}</td>
            <td><strong>${escapeHtml(p.name)}</strong></td>
            <td>${escapeHtml(p.email)}</td>
            <td>${escapeHtml(p.phone || '-')}</td>
            <td>${p.dob || '-'}</td>
            <td><span class="badge">${escapeHtml(p.bloodGroup || '-')}</span></td>
            <td class="actions">
                <button class="btn-icon" onclick="editPatient(${p.id})" title="Edit">✏️</button>
                <button class="btn-icon" onclick="openDeleteModal(${p.id}, '${escapeHtml(p.name).replace(/'/g, "\\'")}')" title="Delete">🗑️</button>
            </td>
        </tr>
    `).join('');
}

// ===== SEARCH =====
function filterPatients() {
    const q = document.getElementById('searchInput').value.toLowerCase().trim();
    if (!q) {
        renderTable(allPatients);
        return;
    }
    const filtered = allPatients.filter(p =>
        (p.name || '').toLowerCase().includes(q) ||
        (p.email || '').toLowerCase().includes(q) ||
        (p.phone || '').includes(q)
    );
    renderTable(filtered);
}

// ===== MODAL OPEN/CLOSE =====
function openPatientModal() {
    document.getElementById('patientForm').reset();
    document.getElementById('patientId').value = '';
    document.getElementById('modalTitle').textContent = 'Add Patient';
    document.getElementById('formError').style.display = 'none';
    document.getElementById('patientModal').style.display = 'flex';
}

function closePatientModal() {
    document.getElementById('patientModal').style.display = 'none';
}

function editPatient(id) {
    const patient = allPatients.find(p => p.id === id);
    if (!patient) return;

    document.getElementById('patientId').value = patient.id;
    document.getElementById('pName').value = patient.name || '';
    document.getElementById('pEmail').value = patient.email || '';
    document.getElementById('pPhone').value = patient.phone || '';
    document.getElementById('pBloodGroup').value = patient.bloodGroup || '';
    document.getElementById('pDob').value = patient.dob || '';
    document.getElementById('pAddress').value = patient.address || '';

    document.getElementById('modalTitle').textContent = 'Edit Patient';
    document.getElementById('formError').style.display = 'none';
    document.getElementById('patientModal').style.display = 'flex';
}

// ===== SAVE (CREATE or UPDATE) =====
async function savePatient(event) {
    event.preventDefault();

    const id = document.getElementById('patientId').value;
    const saveBtn = document.getElementById('saveBtn');
    const errorDiv = document.getElementById('formError');

    errorDiv.style.display = 'none';
    saveBtn.disabled = true;
    saveBtn.textContent = 'Saving...';

    const body = {
        name: document.getElementById('pName').value.trim(),
        email: document.getElementById('pEmail').value.trim(),
        phone: document.getElementById('pPhone').value.trim() || null,
        bloodGroup: document.getElementById('pBloodGroup').value || null,
        dob: document.getElementById('pDob').value || null,
        address: document.getElementById('pAddress').value.trim() || null
    };

	try {
	    const url = id ? `/api/patients/${id}` : '/api/patients';
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
	    showToast(id ? 'Patient updated successfully' : 'Patient added successfully');
	    
	    closePatientModal();
	    await loadPatients();
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
        const res = await apiFetch(`/api/patients/${deleteTargetId}`, {
            method: 'DELETE'
        });

        if (!res.ok && res.status !== 204) {
            const err = await res.json();
            throw new Error(err.message || 'Delete failed');
        }

        closeDeleteModal();
        await loadPatients();
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