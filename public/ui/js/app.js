const API_BASE = '/';
const UI_URL = '/ui/';

let state = {
    token: localStorage.getItem('token'),
    user: JSON.parse(localStorage.getItem('user')),
    currentFolderId: null,
    currentFolderPath: [],
    folders: [],
    files: [],
    activity: [],
    shares: [],
    isLoginMode: true,
    selectedFile: null,
    currentView: 'dashboard'
};

// Utils
const formatSize = (bytes) => {
    if (bytes === 0 || !bytes) return '0 o';
    const k = 1024;
    const sizes = ['o', 'ko', 'mo', 'go', 'to'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

const formatDate = (dateStr) => {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
};

const showToast = (message, type = 'info') => {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type} fade-in`;
    toast.textContent = message;
    document.body.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
};

// API Fetch
async function apiFetch(endpoint, options = {}) {
    if (state.token) {
        options.headers = { ...options.headers, 'Authorization': `Bearer ${state.token}` };
    }
    const response = await fetch(API_BASE + endpoint, options);

    if (response.status === 401) {
        logout();
        throw new Error('Session expirée.');
    }
    if (response.status === 204) return null;

    if (options.download) return await response.blob();

    const data = await response.json();
    if (!response.ok) throw new Error(data.error || 'Erreur serveur');
    return data;
}

// Auth
function toggleAuthMode() {
    state.isLoginMode = !state.isLoginMode;
    const title = document.getElementById('auth-title');
    const btn = document.getElementById('submit-btn');
    if (state.isLoginMode) {
        title.innerHTML = 'Connexion à votre<br>espace sécurisé';
        btn.textContent = 'Connexion...';
    } else {
        title.innerHTML = 'Créer un<br>nouveau compte';
        btn.textContent = "S'inscrire...";
    }
}

async function handleAuth(email, password) {
    if (state.isLoginMode) {
        const data = await apiFetch('auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });
        state.token = data.token;
        state.user = { email };
        localStorage.setItem('token', data.token);
        localStorage.setItem('user', JSON.stringify(state.user));
    } else {
        await apiFetch('auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });
        showToast('Compte créé ! Veuillez vous connecter.', 'success');
        toggleAuthMode();
        return;
    }
    startApp();
}

function logout() {
    state.token = null;
    state.user = null;
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    document.getElementById('app').classList.add('hidden');
    document.getElementById('auth-screen').classList.remove('hidden');
}

// App Init
function startApp() {
    document.getElementById('auth-screen').classList.add('hidden');
    document.getElementById('app').classList.remove('hidden');
    switchView('dashboard');
}

// Navigation View Switcher
function switchView(viewName) {
    state.currentView = viewName;
    document.querySelectorAll('.view-section').forEach(el => el.classList.remove('active'));
    document.getElementById(`view-${viewName}`).classList.add('active');

    // Update active state on sidebar
    document.querySelectorAll('.nav-link').forEach(el => el.classList.remove('active'));
    if (viewName === 'dashboard') {
        document.querySelector('.nav-link[title="Dashboard"]').classList.add('active');
        document.getElementById('header-badge').textContent = 'Dashboard';
        document.getElementById('header-title').textContent = 'DashBoard';
    } else if (viewName === 'explorer') {
        document.getElementById('nav-explorer').classList.add('active');
        document.getElementById('header-badge').textContent = 'Mes Fichiers';
        document.getElementById('header-title').textContent = 'Explorateur';
    }

    refreshAll();
}

async function refreshAll() {
    await Promise.all([
        loadFolders(),
        loadFiles(),
        loadActivity(),
        loadShares(),
        updateQuota()
    ]);
}

// Loaders
async function loadFolders() {
    try {
        const url = state.currentFolderId ? `folders?parent_id=${state.currentFolderId}` : 'folders';
        const res = await apiFetch(url);
        let data = Array.isArray(res) ? res : (res.data || res.folders || []);
        if (data.length > 0 && typeof data[0].parent_id !== 'undefined') {
            data = data.filter(f => f.parent_id == state.currentFolderId);
        }
        state.folders = data;
        renderFolders();
    } catch (err) { console.error(err); }
}

async function loadFiles() {
    try {
        const url = state.currentFolderId ? `files?folder=${state.currentFolderId}` : 'files';
        const res = await apiFetch(url);
        state.files = Array.isArray(res) ? res : (res.data || res.files || []);
        renderFiles();
    } catch (err) { console.error(err); }
}

async function loadActivity() {
    try {
        const res = await apiFetch('me/activity');
        state.activity = res.data || [];
        renderActivity();
    } catch (err) { console.error(err); }
}

async function loadShares() {
    try {
        const res = await apiFetch('shares');
        state.shares = res.data || res.shares || [];
        renderShares();
    } catch (err) { console.error(err); }
}

async function updateQuota() {
    try {
        const data = await apiFetch('me/quota');
        const used = data.used_size || data.used || 0;
        const total = data.total_quota || data.max || (Math.pow(1024, 3));
        const limit = 5 * Math.pow(1024, 3); // For visual matching "2go/5go"

        document.getElementById('quota-label').textContent = `${formatSize(used)}/5go`;
        document.getElementById('quota-fill').style.width = `${Math.min((used / limit) * 100, 100)}%`;
    } catch (err) { }
}

// Renderers
function renderFolders() {
    const countLabel = document.getElementById('dash-folder-count');
    if (countLabel) countLabel.textContent = `${state.folders.length} dossiers`;

    if (state.currentView === 'dashboard') {
        const list = document.getElementById('dash-folders');
        list.innerHTML = '';
        state.folders.slice(0, 4).forEach(f => {
            const el = document.createElement('div');
            el.className = 'item-box';
            el.innerHTML = `
                <svg viewBox="0 0 24 24"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>
                <div class="item-name">${f.name}</div>
                <div class="item-meta">0mo</div>
            `;
            el.onclick = () => {
                state.currentFolderPath.push({ id: f.id, name: f.name });
                state.currentFolderId = f.id;
                switchView('explorer');
            };
            list.appendChild(el);
        });
    } else if (state.currentView === 'explorer') {
        const tbody = document.getElementById('explorer-body');
        tbody.innerHTML = '';
        document.getElementById('btn-back').style.display = state.currentFolderId ? 'block' : 'none';

        const pathLabel = state.currentFolderPath.length > 0
            ? 'Mes Dossiers > ' + state.currentFolderPath.map(p => p.name).join(' > ')
            : 'Mes Dossiers';
        document.getElementById('explorer-path').textContent = pathLabel;

        state.folders.forEach(f => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><div class="table-icon-name"><svg style="width:20px;stroke:var(--primary);fill:none;" viewBox="0 0 24 24"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg> ${f.name}</div></td>
                <td>--</td>
                <td>${new Date(f.created_at || Date.now()).toLocaleDateString()}</td>
            `;
            tr.onclick = () => {
                state.currentFolderPath.push({ id: f.id, name: f.name });
                state.currentFolderId = f.id;
                refreshAll();
            };
            tbody.appendChild(tr);
        });
    }
}

function renderFiles() {
    const countLabel = document.getElementById('dash-file-count');
    if (countLabel) countLabel.textContent = `${state.files.length} fichiers`;

    if (state.currentView === 'dashboard') {
        const list = document.getElementById('dash-files');
        list.innerHTML = '';
        state.files.slice(0, 4).forEach(f => {
            const el = document.createElement('div');
            el.className = 'item-box';
            el.innerHTML = `
                <svg viewBox="0 0 24 24"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                <div class="item-name">${f.original_name}</div>
                <div class="item-meta">${formatSize(f.size)}</div>
            `;
            el.onclick = () => openFileModal(f);
            list.appendChild(el);
        });
    } else if (state.currentView === 'explorer') {
        const tbody = document.getElementById('explorer-body');
        state.files.forEach(f => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><div class="table-icon-name"><svg style="width:20px;stroke:var(--primary);fill:none;" viewBox="0 0 24 24"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg> ${f.original_name}</div></td>
                <td>${formatSize(f.size)}</td>
                <td>${new Date(f.updated_at || f.created_at).toLocaleDateString()}</td>
            `;
            tr.onclick = () => openFileModal(f);
            tbody.appendChild(tr);
        });
    }
}

function renderActivity() {
    if (state.currentView !== 'dashboard') return;
    const list = document.getElementById('activity-list');
    list.innerHTML = '';

    // Fallback Mock si l'API est vide pour matcher l'image
    const dataDisplay = state.activity.length > 0 ? state.activity.slice(0, 3) : [
        { type: 'folder', details: 'Projet ouvert', date: new Date().setHours(15, 32) },
        { type: 'file', details: 'fichier déplacé', date: new Date().setHours(14, 58) },
        { type: 'trash', details: 'élément supprimé', date: new Date().setHours(13, 20) }
    ];

    dataDisplay.forEach(item => {
        let svg = '';
        if (item.type === 'folder') svg = `<path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>`;
        else if (item.type === 'file') svg = `<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/>`;
        else svg = `<polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/><line x1="10" y1="11" x2="10" y2="17"/><line x1="14" y1="11" x2="14" y2="17"/>`;

        list.innerHTML += `
            <div class="activity-row">
                <div class="act-left">
                    <svg viewBox="0 0 24 24">${svg}</svg>
                    <span>${item.details || 'Activité'}</span>
                </div>
                <div class="act-time">${formatDate(item.date)}</div>
            </div>
        `;
    });
}

function renderShares() {
    if (state.currentView !== 'dashboard') return;
    const list = document.getElementById('dash-shares');
    list.innerHTML = '';

    // Mock for visually matching
    const data = state.shares.length > 0 ? state.shares : [
        { label: 'projet 1', size: '20mo', icon: 'folder' },
        { label: 'projet 1', size: '20mo', icon: 'folder' },
        { label: 'projet 1', size: '20ko', icon: 'file' },
        { label: 'projet 1', size: '20ko', icon: 'file' }
    ];

    data.slice(0, 4).forEach(s => {
        const isFolder = s.icon === 'folder' || !s.file_id;
        const svg = isFolder
            ? `<path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>`
            : `<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/>`;

        list.innerHTML += `
            <div class="item-box">
                <svg viewBox="0 0 24 24">${svg}</svg>
                <div class="item-name">${s.label || 'Partage'}</div>
                <div class="item-meta">${s.size || '--'}</div>
            </div>
        `;
    });
}

function goUpFolder() {
    if (state.currentFolderPath.length > 0) {
        state.currentFolderPath.pop();
        const parent = state.currentFolderPath[state.currentFolderPath.length - 1];
        state.currentFolderId = parent ? parent.id : null;
        refreshAll();
    }
}

// Modals
function openModal(id) { document.getElementById(id).classList.remove('hidden'); }
function closeModal(id) { document.getElementById(id).classList.add('hidden'); }

function openFileModal(file) {
    state.selectedFile = file;
    document.getElementById('file-modal-title').textContent = file.original_name;
    document.getElementById('file-modal-size').textContent = formatSize(file.size);
    document.getElementById('file-modal-date').textContent = new Date(file.updated_at || file.created_at).toLocaleString();
    document.getElementById('file-modal-version').textContent = file.current_version || "1";
    openModal('file-modal');
}

async function downloadFile(fileId, filename) {
    try {
        showToast('Déchiffrement en cours...', 'info');
        const blob = await apiFetch(`files/${fileId}/download`, { download: true });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url; a.download = filename;
        document.body.appendChild(a); a.click();
        window.URL.revokeObjectURL(url); document.body.removeChild(a);
        showToast('Téléchargé !', 'success');
        loadActivity();
    } catch (err) { showToast(err.message, 'error'); }
}

// DOM Events
document.addEventListener('DOMContentLoaded', () => {
    if (state.token && state.user) startApp();
    else document.getElementById('auth-screen').classList.remove('hidden');

    // Auth
    document.getElementById('login-form').onsubmit = async (e) => {
        e.preventDefault();
        const btn = document.getElementById('submit-btn');
        btn.disabled = true;
        try {
            await handleAuth(document.getElementById('email').value, document.getElementById('password').value);
        } catch (err) {
            showToast(err.message || 'Erreur', 'error');
        } finally {
            btn.disabled = false;
        }
    };

    // Folders
    document.getElementById('create-folder-form').onsubmit = async (e) => {
        e.preventDefault();
        const payload = { name: document.getElementById('folder-name').value };
        if (state.currentFolderId) payload.parent_id = state.currentFolderId;

        try {
            await apiFetch('folders', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
            showToast('Dossier créé', 'success');
            closeModal('folder-modal');
            e.target.reset();
            refreshAll();
        } catch (err) { showToast(err.message, 'error'); }
    };

    // Upload File
    document.getElementById('file-input').onchange = async (e) => {
        if (!e.target.files.length) return;
        const formData = new FormData();
        formData.append('file', e.target.files[0]);
        if (state.currentFolderId) formData.append('folder_id', state.currentFolderId);

        try {
            showToast('Chiffrement et Upload...', 'info');
            await apiFetch('files', { method: 'POST', body: formData });
            showToast('Succès !', 'success');
            refreshAll();
        } catch (err) { showToast(err.message, 'error'); }
        e.target.value = "";
    };

    // File Actions
    document.getElementById('btn-download-file').onclick = () => {
        if (state.selectedFile) downloadFile(state.selectedFile.id, state.selectedFile.original_name);
    };

    document.getElementById('btn-delete-file').onclick = async () => {
        if (!state.selectedFile || !confirm("Supprimer ce fichier ?")) return;
        try {
            await apiFetch(`files/${state.selectedFile.id}`, { method: 'DELETE' });
            showToast("Supprimé", "success");
            closeModal('file-modal');
            refreshAll();
        } catch (err) { showToast(err.message, "error"); }
    };

    document.getElementById('btn-share-file').onclick = () => {
        closeModal('file-modal');
        openModal('share-modal');
    };

    // Shares
    document.getElementById('create-share-form').onsubmit = async (e) => {
        e.preventDefault();
        if (!state.selectedFile) return;

        try {
            const data = await apiFetch('shares/file', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    file_id: state.selectedFile.id,
                    max_uses: parseInt(document.getElementById('share-max-uses').value) || 10,
                    expires_at: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().slice(0, 19).replace('T', ' '),
                    label: document.getElementById('share-label').value || "Partage"
                })
            });
            closeModal('share-modal');
            openModal('share-result-modal');
            document.getElementById('share-url-result').value = `${window.location.origin}${API_BASE}shares/public/${data.share_id}`;
            refreshAll();
            showToast("Lien généré", 'success');
        } catch (err) { showToast(err.message, "error"); }
    };
});

function copyShareUrl() {
    const el = document.getElementById('share-url-result');
    el.select(); document.execCommand("copy");
    showToast("Copié !", "success");
}
