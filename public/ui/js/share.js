// share.js - Page de téléchargement public ObsiLock
const BASE_URL = 'http://api.obsilock.iris.a3n.fr:8080';

// Extraire le token depuis l'URL (?token=XXX ou #XXX)
function getToken() {
    const params = new URLSearchParams(window.location.search);
    return params.get('token') || window.location.hash.replace('#', '');
}

function formatSize(bytes) {
    if (!bytes) return 'Taille inconnue';
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
    return (bytes / (1024 * 1024 * 1024)).toFixed(1) + ' GB';
}

function formatDate(dateStr) {
    if (!dateStr) return 'Sans expiration';
    const d = new Date(dateStr);
    return d.toLocaleDateString('fr-FR', { day: '2-digit', month: 'long', year: 'numeric' });
}

function getFileIcon(name) {
    if (!name) return '📄';
    const ext = name.split('.').pop().toLowerCase();
    const icons = {
        'pdf': '📑', 'doc': '📝', 'docx': '📝', 'xls': '📊', 'xlsx': '📊',
        'ppt': '📊', 'pptx': '📊', 'zip': '🗜️', 'rar': '🗜️', '7z': '🗜️',
        'mp4': '🎬', 'mkv': '🎬', 'avi': '🎬', 'mp3': '🎵', 'wav': '🎵',
        'jpg': '🖼️', 'jpeg': '🖼️', 'png': '🖼️', 'gif': '🖼️', 'webp': '🖼️',
        'txt': '📄', 'csv': '📊', 'json': '💾', 'xml': '💾',
    };
    return icons[ext] || '📄';
}

function showError(msg) {
    document.getElementById('loading').style.display = 'none';
    document.getElementById('content').style.display = 'none';
    const card = document.getElementById('share-card');
    card.innerHTML += `
        <div style="margin-top: 20px; color: #ff4757;">
            <div style="font-size: 2rem; margin-bottom: 10px;">❌</div>
            <div style="font-weight: 600;">${msg}</div>
        </div>
    `;
}

async function loadShareInfo(token) {
    try {
        const res = await fetch(`${BASE_URL}/s/${token}`);
        if (res.status === 404) { showError('Ce lien de partage est invalide ou inexistant.'); return; }
        if (res.status === 410) {
            const data = await res.json();
            const msgs = {
                'revoked': 'Ce lien de partage a été révoqué par son propriétaire.',
                'expired': 'Ce lien de partage a expiré.',
                'no_uses_left': 'Ce lien de partage a atteint son nombre maximum d\'utilisations.',
            };
            showError(msgs[data.reason] || 'Ce lien de partage n\'est plus valide.');
            return;
        }
        if (!res.ok) { showError('Erreur lors du chargement des informations du partage.'); return; }

        const data = await res.json();
        const meta = data.metadata;

        // Afficher le contenu
        document.getElementById('loading').style.display = 'none';
        document.getElementById('content').style.display = 'block';

        // Label du partage
        if (data.label) {
            document.getElementById('share-label').textContent = `"${data.label}"`;
        }

        // Infos fichier
        document.getElementById('file-icon').textContent = getFileIcon(meta.name);
        document.getElementById('file-name').textContent = meta.name || 'Fichier';

        const expiresStr = data.expires_at ? `Expire le ${formatDate(data.expires_at)}` : 'Sans expiration';
        const sizeStr = meta.size ? formatSize(meta.size) : '';
        const usesStr = data.remaining_uses !== null ? ` • ${data.remaining_uses} téléchargement(s) restant(s)` : '';
        document.getElementById('file-meta').textContent = `${sizeStr} • ${expiresStr}${usesStr}`;

        // Données réelles de l'icon pour dossier vs fichier
        if (data.kind === 'folder') {
            document.getElementById('file-icon').textContent = '📁';
            document.getElementById('btn-download').textContent = '📁 Ce partage est un dossier — Téléchargement par fichier disponible';
            document.getElementById('btn-download').disabled = true;
            return;
        }

        // Bouton de téléchargement
        const btn = document.getElementById('btn-download');
        btn.addEventListener('click', () => downloadFile(token, meta.name, btn));

    } catch (e) {
        console.error(e);
        showError('Impossible de contacter le serveur. Vérifiez votre connexion Internet.');
    }
}

async function downloadFile(token, filename, btn) {
    btn.disabled = true;
    btn.innerHTML = `<div class="spinner" style="border: 3px solid #000; border-top-color: transparent; width: 18px; height: 18px; border-radius: 50%; animation: spin 1s linear infinite;"></div> Téléchargement en cours...`;

    // Ajouter animation spinner si pas déjà présent
    if (!document.getElementById('spin-style')) {
        const s = document.createElement('style');
        s.id = 'spin-style';
        s.textContent = '@keyframes spin { to { transform: rotate(360deg); } }';
        document.head.appendChild(s);
    }

    try {
        const res = await fetch(`${BASE_URL}/s/${token}/download`, { method: 'POST' });

        if (!res.ok) {
            const data = await res.json().catch(() => ({}));
            const errMsg = data.error || `Erreur HTTP ${res.status}`;
            document.getElementById('error-message').style.display = 'block';
            document.getElementById('error-message').textContent = '⚠️ ' + errMsg;
            btn.disabled = false;
            btn.innerHTML = `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="7 10 12 15 17 10"></polyline><line x1="12" y1="15" x2="12" y2="3"></line></svg> Réessayer`;
            return;
        }

        // Déclencher le download
        const blob = await res.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename || 'obsilock_download';
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);

        // Indiquer succès
        btn.innerHTML = `✅ Téléchargement démarré !`;
        btn.style.backgroundColor = '#2a2e33';
        btn.style.color = '#94E01E';

        // Rafraîchir les infos (le remaining_uses a peut être changé)
        setTimeout(() => loadShareInfo(getToken()), 1500);

    } catch (e) {
        console.error(e);
        document.getElementById('error-message').style.display = 'block';
        document.getElementById('error-message').textContent = '⚠️ Erreur réseau : ' + e.message;
        btn.disabled = false;
        btn.innerHTML = 'Réessayer';
    }
}

// Démarrage
const token = getToken();
if (!token) {
    showError('Aucun token de partage spécifié dans l\'URL. Vérifiez le lien que vous avez reçu.');
} else {
    loadShareInfo(token);
}
