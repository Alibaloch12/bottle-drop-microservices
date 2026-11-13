const API = '/api';
const BEVERAGE_TYPES = ['BEER', 'COLA', 'WATER', 'JUICE', 'ENERGY_DRINK'];

function showToast(message, isError = false) {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = 'show' + (isError ? ' error' : '');
    setTimeout(() => toast.className = '', 3000);
}

async function api(path, options = {}) {
    const response = await fetch(API + path, {
        headers: { 'Content-Type': 'application/json', ...options.headers },
        ...options
    });
    if (!response.ok) {
        const body = await response.json().catch(() => ({}));
        throw new Error(body.error || `Request failed (${response.status})`);
    }
    if (response.status === 204) return null;
    return response.json();
}

function populateBeverageSelects() {
    ['repl-beverage-type', 'empty-beverage-type'].forEach(id => {
        const select = document.getElementById(id);
        select.innerHTML = BEVERAGE_TYPES.map(t => `<option value="${t}">${t}</option>`).join('');
    });
}

function renderStockTable(tableId, items, stockType) {
    const tbody = document.querySelector(`#${tableId} tbody`);
    tbody.innerHTML = items.map(item => `
        <tr>
            <td>${item.beverageType}</td>
            <td>${item.quantity}</td>
            <td>
                <input type="number" class="stock-input" value="${item.quantity}" min="0"
                       id="${stockType}-${item.beverageType}">
                <button class="small" onclick="updateStock('${stockType}', '${item.beverageType}')">Save</button>
            </td>
        </tr>
    `).join('');
}

async function loadDashboard() {
    try {
        const status = await api('/warehouse');
        document.getElementById('warehouse-info').innerHTML = `
            <strong>${status.name}</strong> &mdash; ${status.location}<br>
            Pending replenishment requests: <strong>${status.pendingReplenishmentRequests}</strong>
        `;
        renderStockTable('beverage-table', status.beverages, 'beverage');
        renderStockTable('empty-table', status.empties, 'empty');
    } catch (e) {
        showToast(e.message, true);
    }
}

async function updateStock(stockType, beverageType) {
    const input = document.getElementById(`${stockType}-${beverageType}`);
    try {
        await api(`/warehouse/${stockType}s/${beverageType}`, {
            method: 'PUT',
            body: JSON.stringify({ quantity: parseInt(input.value, 10) })
        });
        showToast(`${beverageType} ${stockType} updated`);
        loadDashboard();
    } catch (e) {
        showToast(e.message, true);
    }
}

async function loadReplenishmentRequests() {
    try {
        const requests = await api('/replenishment-requests');
        const tbody = document.querySelector('#replenishment-table tbody');
        tbody.innerHTML = requests.map(r => `
            <tr>
                <td>${r.id}</td>
                <td>Bar #${r.barId}</td>
                <td>${r.beverageType}</td>
                <td>${r.quantity}</td>
                <td><span class="status ${r.status}">${r.status}</span></td>
                <td>${formatDate(r.createdAt)}</td>
                <td>
                    ${r.status === 'PENDING' ? `
                        <button class="small success" onclick="fulfillRequest(${r.id})">Fulfill</button>
                        <button class="small danger" onclick="rejectRequest(${r.id})">Reject</button>
                    ` : '&mdash;'}
                </td>
            </tr>
        `).join('');
    } catch (e) {
        showToast(e.message, true);
    }
}

async function submitReplenishmentRequest(event) {
    event.preventDefault();
    try {
        const result = await api('/replenishment-requests', {
            method: 'POST',
            body: JSON.stringify({
                barId: parseInt(document.getElementById('bar-id').value, 10),
                beverageType: document.getElementById('repl-beverage-type').value,
                quantity: parseInt(document.getElementById('repl-quantity').value, 10)
            })
        });
        showToast(`Request #${result.id} created (${result.status})`);
        loadReplenishmentRequests();
        loadDashboard();
    } catch (e) {
        showToast(e.message, true);
    }
}

async function fulfillRequest(id) {
    try {
        await api(`/replenishment-requests/${id}/fulfill`, { method: 'PATCH' });
        showToast(`Request #${id} fulfilled`);
        loadReplenishmentRequests();
        loadDashboard();
    } catch (e) {
        showToast(e.message, true);
    }
}

async function rejectRequest(id) {
    try {
        await api(`/replenishment-requests/${id}/reject`, { method: 'PATCH' });
        showToast(`Request #${id} rejected`);
        loadReplenishmentRequests();
    } catch (e) {
        showToast(e.message, true);
    }
}

async function loadEmptyTransfers() {
    try {
        const transfers = await api('/empty-transfers');
        const tbody = document.querySelector('#empty-transfer-table tbody');
        tbody.innerHTML = transfers.map(t => `
            <tr>
                <td>${t.id}</td>
                <td>Drop Point #${t.dropPointId}</td>
                <td>${t.beverageType}</td>
                <td>${t.quantity}</td>
                <td>${formatDate(t.receivedAt)}</td>
            </tr>
        `).join('');
    } catch (e) {
        showToast(e.message, true);
    }
}

async function submitEmptyTransfer(event) {
    event.preventDefault();
    try {
        const result = await api('/empty-transfers', {
            method: 'POST',
            body: JSON.stringify({
                dropPointId: parseInt(document.getElementById('drop-point-id').value, 10),
                beverageType: document.getElementById('empty-beverage-type').value,
                quantity: parseInt(document.getElementById('empty-quantity').value, 10)
            })
        });
        showToast(`Transfer #${result.id} recorded`);
        loadEmptyTransfers();
        loadDashboard();
    } catch (e) {
        showToast(e.message, true);
    }
}

function formatDate(iso) {
    return new Date(iso).toLocaleString();
}

populateBeverageSelects();
loadDashboard();
loadReplenishmentRequests();
loadEmptyTransfers();
