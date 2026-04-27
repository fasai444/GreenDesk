/**
 * GreenDesk - Auth Guard
 * Gestion de l'authentification côté frontend.
 * Inclure ce script dans toutes les pages pour afficher l'état de connexion dans la navbar.
 */

const AUTH = {
    currentUser: null,

    async fetchCurrentUser() {
        try {
            const res = await fetch('/api/auth/me', { credentials: 'include' });
            if (res.ok) {
                this.currentUser = await res.json();
                return this.currentUser;
            }
        } catch (_) {}
        this.currentUser = null;
        return null;
    },

    async logout() {
        await fetch('/api/auth/logout', { method: 'POST', credentials: 'include' });
        window.location.href = '/login.html';
    },

    /**
     * Initialise la zone auth dans la navbar (#nav-auth-area).
     * Si l'utilisateur est connecté → affiche son nom + bouton déconnexion.
     * Sinon → affiche un bouton "Connexion".
     */
    async initNavbar() {
        const area = document.getElementById('nav-auth-area');
        if (!area) return;

        const user = await this.fetchCurrentUser();

        if (user) {
            const isAdmin = user.role === 'ADMIN';
            area.innerHTML = `
                ${isAdmin ? `<li class="nav-item">
                    <a class="nav-link" href="/admin.html">
                        <i class="fas fa-shield-halved me-1"></i>Admin
                    </a>
                </li>` : ''}
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle d-flex align-items-center gap-1" href="#"
                       data-bs-toggle="dropdown" aria-expanded="false">
                        <span class="rounded-circle d-inline-flex align-items-center justify-content-center"
                              style="width:28px;height:28px;background:rgba(255,255,255,0.22);font-size:0.8rem;font-weight:700;">
                            ${user.username.charAt(0).toUpperCase()}
                        </span>
                        ${user.username}
                    </a>
                    <ul class="dropdown-menu dropdown-menu-end shadow border-0" style="min-width:200px;">
                        <li>
                            <span class="dropdown-item-text text-muted" style="font-size:0.78rem;">
                                <i class="fas fa-circle-dot me-1 text-success"></i>
                                ${user.role === 'ADMIN' ? 'Administrateur' : 'Utilisateur'}
                            </span>
                        </li>
                        <li><hr class="dropdown-divider my-1"></li>
                        <li>
                            <span class="dropdown-item-text text-muted" style="font-size:0.75rem;">
                                ${user.email}
                            </span>
                        </li>
                        <li><hr class="dropdown-divider my-1"></li>
                        <li>
                            <button class="dropdown-item text-danger" onclick="AUTH.logout()">
                                <i class="fas fa-right-from-bracket me-2"></i>Déconnexion
                            </button>
                        </li>
                    </ul>
                </li>`;
        } else {
            area.innerHTML = `
                <li class="nav-item">
                    <a class="nav-link" href="/login.html">
                        <i class="fas fa-right-to-bracket me-1"></i>Connexion
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="/register.html">
                        <i class="fas fa-user-plus me-1"></i>S'inscrire
                    </a>
                </li>`;
        }
    },

    /**
     * Protège une page : redirige vers login.html si non connecté.
     * Optionnel : passer 'ADMIN' pour exiger le rôle admin.
     */
    async requireAuth(requiredRole = null) {
        const user = await this.fetchCurrentUser();
        if (!user) {
            window.location.href = '/login.html?redirect=' + encodeURIComponent(window.location.pathname);
            return null;
        }
        if (requiredRole && user.role !== requiredRole) {
            window.location.href = '/login.html?error=forbidden';
            return null;
        }
        return user;
    }
};

// Auto-initialiser la navbar dès que le DOM est prêt
document.addEventListener('DOMContentLoaded', () => AUTH.initNavbar());
