// app.js - Lógica e Interactividad Completa para la Landing Page y Aplicación PowerGo

// 1. BASE DE DATOS SIMULADA (LocalStorage)
const DEFAULT_STATIONS = [
    { id: 'st-1', name: 'Centro Comercial Santa Fe', powerBanks: 15, capacity: 20 },
    { id: 'st-2', name: 'Universidad de Antioquia', powerBanks: 8, capacity: 15 }
];

const DEFAULT_USERS = [
    {
        name: 'Santiago Aristizabal',
        email: 'cliente@powergo.com',
        phone: '3001234567',
        password: '123',
        role: 'user'
    },
    {
        name: 'Administrador PowerGo',
        email: 'admin@powergo.com',
        phone: '3119876543',
        password: 'admin',
        role: 'admin'
    }
];

const DEFAULT_LOANS = [
    {
        id: 'loan-1',
        userEmail: 'cliente@powergo.com',
        deviceId: 'PB-021',
        status: 'Activo',
        date: '16/07/2026',
        stationSource: 'Centro Comercial Santa Fe',
        stationDest: ''
    }
];

function initDatabase() {
    if (!localStorage.getItem('pg_stations')) {
        localStorage.setItem('pg_stations', JSON.stringify(DEFAULT_STATIONS));
    }
    if (!localStorage.getItem('pg_users')) {
        localStorage.setItem('pg_users', JSON.stringify(DEFAULT_USERS));
    }
    if (!localStorage.getItem('pg_loans')) {
        localStorage.setItem('pg_loans', JSON.stringify(DEFAULT_LOANS));
    }
}

const getStations = () => JSON.parse(localStorage.getItem('pg_stations'));
const setStations = (data) => localStorage.setItem('pg_stations', JSON.stringify(data));
const getUsers = () => JSON.parse(localStorage.getItem('pg_users'));
const setUsers = (data) => localStorage.setItem('pg_users', JSON.stringify(data));
const getLoans = () => JSON.parse(localStorage.getItem('pg_loans'));
const setLoans = (data) => localStorage.setItem('pg_loans', JSON.stringify(data));

// Estado de sesión activa
let currentUser = null;
let currentReturnLoanId = null;

// 2. ENRUTADOR Y COMPORTAMIENTO DE SECCIONES (LANDING VS WORKSPACE)

// Desplazamiento suave en la landing page
function scrollToSection(sectionId) {
    // Cerrar contenedores de aplicación
    document.getElementById('auth-section-container').classList.add('d-none');
    document.getElementById('app-workspace-container').classList.add('d-none');
    
    // Mostrar landing page
    document.getElementById('landing-page-container').classList.remove('d-none');
    
    // Desplazar a la sección
    const element = document.getElementById(sectionId);
    if (element) {
        element.scrollIntoView({ behavior: 'smooth' });
    }
    
    // Actualizar HUD del flujo
    updateNavigationMapHUD('inicio');
}

// Mostrar login o registro
function showAuthView(viewType) {
    // Esconder landing y workspace
    document.getElementById('landing-page-container').classList.add('d-none');
    document.getElementById('app-workspace-container').classList.add('d-none');
    
    // Mostrar contenedor de autenticación
    const container = document.getElementById('auth-section-container');
    container.classList.remove('d-none');
    
    // Alternar vistas dentro de autenticación
    if (viewType === 'login') {
        document.getElementById('auth-login').classList.remove('d-none');
        document.getElementById('auth-registro').classList.add('d-none');
    } else {
        document.getElementById('auth-login').classList.add('d-none');
        document.getElementById('auth-registro').classList.remove('d-none');
    }
    
    // Scroll al tope
    window.scrollTo({ top: 0, behavior: 'smooth' });
    
    // Actualizar HUD
    updateNavigationMapHUD('auth');
}

function closeAuthViews() {
    document.getElementById('auth-section-container').classList.add('d-none');
    document.getElementById('landing-page-container').classList.remove('d-none');
    window.scrollTo({ top: 0, behavior: 'smooth' });
    updateNavigationMapHUD('inicio');
}

// Abrir el workspace de la aplicación privada
function showAppDashboard() {
    if (!currentUser) {
        showToast('Acceso Denegado', 'Por favor inicia sesión para entrar al panel.', 'info');
        showAuthView('login');
        return;
    }
    
    // Ocultar landing y auth
    document.getElementById('landing-page-container').classList.add('d-none');
    document.getElementById('auth-section-container').classList.add('d-none');
    
    // Mostrar Workspace
    document.getElementById('app-workspace-container').classList.remove('d-none');
    
    // Activar pestaña por defecto (dashboard)
    switchWorkspaceTab('dashboard');
    
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function closeAppDashboard() {
    document.getElementById('app-workspace-container').classList.add('d-none');
    document.getElementById('landing-page-container').classList.remove('d-none');
    window.scrollTo({ top: 0, behavior: 'smooth' });
    updateNavigationMapHUD('inicio');
}

// Navegar directamente a una pestaña del workspace desde el exterior
function navigateToWorkspaceTab(tabId) {
    if (!currentUser) {
        showToast('Iniciar Sesión', 'Debes iniciar sesión para realizar esta acción.', 'info');
        showAuthView('login');
        return;
    }
    showAppDashboard();
    switchWorkspaceTab(tabId);
}

// Alternar pestañas del workspace
function switchWorkspaceTab(tabId) {
    // Ocultar todos los paneles del workspace
    document.querySelectorAll('.workspace-pane').forEach(pane => {
        pane.classList.add('d-none');
    });
    
    // Mostrar el panel activo
    document.getElementById(`pane-${tabId}`).classList.remove('d-none');
    
    // Desactivar todos los botones nav pill
    document.querySelectorAll('#workspaceTabs .nav-link').forEach(btn => {
        btn.classList.remove('active');
    });
    
    // Activar el botón correspondiente
    document.getElementById(`tab-${tabId}`).classList.add('active');
    
    // Actualizar HUD
    if (tabId === 'dashboard') {
        updateNavigationMapHUD('dashboard');
        renderDashboardStats();
    } else {
        updateNavigationMapHUD('features');
    }
    
    // Renderizados específicos
    if (tabId === 'estaciones') {
        renderWorkspaceStations();
    } else if (tabId === 'prestamos') {
        renderLoans();
    } else if (tabId === 'perfil') {
        loadProfileData();
    } else if (tabId === 'admin') {
        renderAdminPanel();
    }
}

// Actualizar colores del mapa de flujo HUD
function updateNavigationMapHUD(state) {
    document.querySelectorAll('.flowchart-node').forEach(node => {
        node.classList.remove('active-node');
    });

    const nodeInicio = document.getElementById('node-inicio');
    const nodeAuth = document.getElementById('node-auth');
    const nodeDashboard = document.getElementById('node-dashboard');
    const nodeFeatures = document.getElementById('node-features');

    if (state === 'inicio') {
        nodeInicio.classList.add('active-node');
    } else if (state === 'auth') {
        nodeAuth.classList.add('active-node');
    } else if (state === 'dashboard') {
        nodeDashboard.classList.add('active-node');
    } else if (state === 'features') {
        nodeFeatures.classList.add('active-node');
    }
}

// 3. AUTENTICACIÓN
function handleRegister(e) {
    e.preventDefault();
    const name = document.getElementById('reg-name').value.trim();
    const email = document.getElementById('reg-email').value.trim().toLowerCase();
    const phone = document.getElementById('reg-phone').value.trim();
    const password = document.getElementById('reg-password').value;

    const users = getUsers();
    if (users.find(u => u.email === email)) {
        showToast('Error de Registro', 'El correo ya se encuentra registrado.', 'danger');
        return;
    }

    const newUser = { name, email, phone, password, role: 'user' };
    users.push(newUser);
    setUsers(users);

    document.getElementById('form-registro').reset();
    showToast('Registro Exitoso', 'Tu cuenta ha sido creada. Iniciando sesión...', 'success');
    loginUser(newUser);
}

function handleLogin(e) {
    e.preventDefault();
    const email = document.getElementById('login-email').value.trim().toLowerCase();
    const password = document.getElementById('login-password').value;

    const users = getUsers();
    const user = users.find(u => u.email === email && u.password === password);

    if (!user) {
        showToast('Error de Ingreso', 'Correo o contraseña incorrectos.', 'danger');
        return;
    }

    document.getElementById('form-login').reset();
    showToast('Bienvenido', `Hola de nuevo, ${user.name}!`, 'success');
    loginUser(user);
}

function loginUser(user) {
    currentUser = user;
    
    // Actualizar botones de la cabecera
    document.getElementById('btn-login-container').classList.add('d-none');
    document.getElementById('btn-register-container').classList.add('d-none');
    document.getElementById('btn-user-container').classList.remove('d-none');
    
    // Cargar datos en el Workspace
    document.getElementById('dash-user-name').textContent = user.name;
    document.getElementById('dash-user-avatar').src = `https://api.dicebear.com/7.x/bottts/svg?seed=${encodeURIComponent(user.name)}`;

    // Mostrar pestaña de administración si es admin
    const adminTab = document.getElementById('tab-admin-item');
    const adminCard = document.getElementById('dashboard-admin-card-container');
    if (user.role === 'admin') {
        adminTab.classList.remove('d-none');
        adminCard.classList.remove('d-none');
    } else {
        adminTab.classList.add('d-none');
        adminCard.classList.add('d-none');
    }

    showAppDashboard();
}

function logout() {
    currentUser = null;
    document.getElementById('btn-user-container').classList.add('d-none');
    document.getElementById('btn-login-container').classList.remove('d-none');
    document.getElementById('btn-register-container').classList.remove('d-none');
    
    showToast('Sesión Cerrada', 'Has salido de tu cuenta correctamente.', 'success');
    closeAppDashboard();
}

function showForgotPassword() {
    showToast('Recuperación de Contraseña', 'Se ha enviado un enlace de recuperación a tu correo de prueba.', 'info');
}

// 4. FLUJO DE PERFIL
function loadProfileData() {
    if (!currentUser) return;
    document.getElementById('prof-name').value = currentUser.name;
    document.getElementById('prof-email').value = currentUser.email;
    document.getElementById('prof-phone').value = currentUser.phone;
    document.getElementById('prof-password').value = '';
}

function handleProfileUpdate(e) {
    e.preventDefault();
    if (!currentUser) return;

    const name = document.getElementById('prof-name').value.trim();
    const phone = document.getElementById('prof-phone').value.trim();
    const password = document.getElementById('prof-password').value;

    const users = getUsers();
    const index = users.findIndex(u => u.email === currentUser.email);

    if (index !== -1) {
        users[index].name = name;
        users[index].phone = phone;
        if (password) {
            users[index].password = password;
        }
        setUsers(users);

        currentUser = users[index];
        document.getElementById('dash-user-name').textContent = currentUser.name;
        document.getElementById('dash-user-avatar').src = `https://api.dicebear.com/7.x/bottts/svg?seed=${encodeURIComponent(currentUser.name)}`;

        showToast('Perfil Guardado', 'Tus datos se actualizaron correctamente.', 'success');
        switchWorkspaceTab('dashboard');
    }
}

// 5. FLUJO DE ALQUILERES Y DEVOLUCIONES
function requestLoan(stationId) {
    if (!currentUser) {
        showToast('Inicia Sesión', 'Inicia sesión para poder alquilar un power bank.', 'info');
        showAuthView('login');
        return;
    }

    // Comprobar préstamo activo
    const loans = getLoans();
    const activeLoan = loans.find(l => l.userEmail === currentUser.email && l.status === 'Activo');
    if (activeLoan) {
        showToast('Préstamo Activo Detectado', 'Ya tienes un cargador contigo. Debes devolverlo para poder alquilar otro.', 'danger');
        return;
    }

    // Comprobar existencias
    const stations = getStations();
    const station = stations.find(s => s.id === stationId);

    if (!station || station.powerBanks <= 0) {
        showToast('Sin existencias', 'Esta estación no posee power banks disponibles por ahora.', 'danger');
        return;
    }

    // Procesar préstamo
    station.powerBanks -= 1;
    setStations(stations);

    const randomIdNum = Math.floor(100 + Math.random() * 900);
    const newLoan = {
        id: `loan-${Date.now()}`,
        userEmail: currentUser.email,
        deviceId: `PB-${randomIdNum}`,
        status: 'Activo',
        date: new Date().toLocaleDateString('es-ES'),
        stationSource: station.name,
        stationDest: ''
    };

    loans.push(newLoan);
    setLoans(loans);

    showToast('Retiro Autorizado', `Solicitud de cargador ${newLoan.deviceId} aprobada en la estación ${station.name}.`, 'success');
    navigateToWorkspaceTab('prestamos');
    
    // Recargar componentes de estaciones
    renderLandingStations();
}

function openReturnModal(loanId) {
    currentReturnLoanId = loanId;
    
    const select = document.getElementById('modal-return-station');
    select.innerHTML = '';
    
    const stations = getStations();
    stations.forEach(s => {
        const option = document.createElement('option');
        option.value = s.id;
        option.textContent = `${s.name} (${s.capacity - s.powerBanks} slots libres)`;
        select.appendChild(option);
    });

    const myModal = new bootstrap.Modal(document.getElementById('returnModal'));
    myModal.show();
}

function submitReturn() {
    if (!currentReturnLoanId) return;

    const select = document.getElementById('modal-return-station');
    const stationId = select.value;

    const stations = getStations();
    const station = stations.find(s => s.id === stationId);

    if (!station) {
        showToast('Error', 'Estación seleccionada inválida.', 'danger');
        return;
    }

    // Validar capacidad
    if (station.powerBanks >= station.capacity) {
        showToast('Estación Completa', 'No hay slots libres en esta estación para depositar el cargador.', 'danger');
        return;
    }

    // Registrar devolución
    const loans = getLoans();
    const loan = loans.find(l => l.id === currentReturnLoanId);

    if (loan) {
        station.powerBanks += 1;
        setStations(stations);

        loan.status = 'Devuelto';
        loan.stationDest = station.name;
        setLoans(loans);

        const modalEl = document.getElementById('returnModal');
        const modal = bootstrap.Modal.getInstance(modalEl);
        modal.hide();

        showToast('Devolución Completada', `Cargador depositado con éxito en ${station.name}.`, 'success');
        switchWorkspaceTab('prestamos');
        
        // Recargar estaciones
        renderLandingStations();
    }
}

// 6. RENDERIZACIÓN Y FILTRADOS

// Estadísticas generales para la Landing Page
function renderLandingStats() {
    const stations = getStations();
    const totalBanks = stations.reduce((acc, st) => acc + st.powerBanks, 0);
    
    document.getElementById('home-total-banks').textContent = totalBanks;
    document.getElementById('home-total-stations').textContent = stations.length;
}

// Render de estaciones en la Landing Page pública
function renderLandingStations() {
    const list = document.getElementById('stations-list');
    list.innerHTML = '';
    const stations = getStations();

    stations.forEach(s => {
        const slots = s.capacity - s.powerBanks;
        const percent = Math.round((s.powerBanks / s.capacity) * 100);
        let progressColor = 'bg-success';
        if (percent < 25) progressColor = 'bg-danger';
        else if (percent < 60) progressColor = 'bg-warning';

        const card = document.createElement('div');
        card.className = 'col-md-6 col-lg-6 station-card-item';
        card.dataset.name = s.name.toLowerCase();
        card.innerHTML = `
            <div class="glass-panel p-4 card-station h-100 d-flex flex-column justify-content-between">
                <div>
                    <div class="d-flex justify-content-between align-items-start mb-3">
                        <h4 class="fw-700 text-white mb-0">${s.name}</h4>
                        <span class="badge badge-energy"><i class="bi bi-lightning-fill me-1"></i>${s.powerBanks} Libres</span>
                    </div>
                    
                    <div class="mb-4">
                        <div class="d-flex justify-content-between small text-white mb-1">
                            <span>Disponibilidad</span>
                            <span>${s.powerBanks}/${s.capacity} Cargadores</span>
                        </div>
                        <div class="progress" style="height: 6px; background-color: rgba(255,255,255,0.05);">
                            <div class="progress-bar ${progressColor}" role="progressbar" style="width: ${percent}%"></div>
                        </div>
                    </div>
                    
                    <div class="d-flex justify-content-between text-white-50 small border-top border-secondary border-opacity-20 pt-3">
                        <span><i class="bi bi-unlock-fill me-1 text-success"></i> ${slots} slots vacíos</span>
                        <span><i class="bi bi-geo-alt-fill me-1 text-primary"></i> Estación Física</span>
                    </div>
                </div>
                
                <button class="btn btn-primary-custom w-100 mt-4 py-2.5 fw-700" onclick="requestLoan('${s.id}')">
                    <i class="bi bi-lightning-charge-fill me-1"></i>Alquilar Cargador
                </button>
            </div>
        `;
        list.appendChild(card);
    });
}

function filterStations() {
    const query = document.getElementById('search-station').value.toLowerCase();
    document.querySelectorAll('#stations-list .station-card-item').forEach(card => {
        if (card.dataset.name.includes(query)) {
            card.style.display = 'block';
        } else {
            card.style.display = 'none';
        }
    });
}

// Render de estaciones dentro del Workspace privado
function renderWorkspaceStations() {
    const list = document.getElementById('workspace-stations-list');
    list.innerHTML = '';
    const stations = getStations();

    stations.forEach(s => {
        const slots = s.capacity - s.powerBanks;
        const percent = Math.round((s.powerBanks / s.capacity) * 100);
        let progressColor = 'bg-success';
        if (percent < 25) progressColor = 'bg-danger';
        else if (percent < 60) progressColor = 'bg-warning';

        const card = document.createElement('div');
        card.className = 'col-md-6 col-lg-6 w-station-card-item';
        card.dataset.name = s.name.toLowerCase();
        card.innerHTML = `
            <div class="glass-panel p-4 card-station h-100 d-flex flex-column justify-content-between">
                <div>
                    <div class="d-flex justify-content-between align-items-start mb-3">
                        <h4 class="fw-700 text-white mb-0">${s.name}</h4>
                        <span class="badge badge-energy"><i class="bi bi-lightning-fill me-1"></i>${s.powerBanks} Libres</span>
                    </div>
                    
                    <div class="mb-4">
                        <div class="d-flex justify-content-between small text-white mb-1">
                            <span>Disponibilidad</span>
                            <span>${s.powerBanks}/${s.capacity} Cargadores</span>
                        </div>
                        <div class="progress" style="height: 6px; background-color: rgba(255,255,255,0.05);">
                            <div class="progress-bar ${progressColor}" role="progressbar" style="width: ${percent}%"></div>
                        </div>
                    </div>
                    
                    <div class="d-flex justify-content-between text-white-50 small border-top border-secondary border-opacity-20 pt-3">
                        <span><i class="bi bi-unlock-fill me-1 text-success"></i> ${slots} slots vacíos</span>
                        <span><i class="bi bi-geo-alt-fill me-1 text-primary"></i> Estación Física</span>
                    </div>
                </div>
                
                <button class="btn btn-primary-custom w-100 mt-4 py-2.5 fw-700" onclick="requestLoan('${s.id}')">
                    <i class="bi bi-lightning-charge-fill me-1"></i>Solicitar Retiro
                </button>
            </div>
        `;
        list.appendChild(card);
    });
}

function filterStationsWorkspace() {
    const query = document.getElementById('search-station-workspace').value.toLowerCase();
    document.querySelectorAll('#workspace-stations-list .w-station-card-item').forEach(card => {
        if (card.dataset.name.includes(query)) {
            card.style.display = 'block';
        } else {
            card.style.display = 'none';
        }
    });
}

// Estadísticas del Workspace
function renderDashboardStats() {
    if (!currentUser) return;
    
    const loans = getLoans();
    const activeLoan = loans.find(l => l.userEmail === currentUser.email && l.status === 'Activo');
    const myHistoryCount = loans.filter(l => l.userEmail === currentUser.email).length;

    const statusText = document.getElementById('dash-loan-status-text');
    const statTotal = document.getElementById('dash-stat-total');
    const statDate = document.getElementById('dash-stat-date');

    statTotal.textContent = myHistoryCount;

    if (activeLoan) {
        statusText.innerHTML = `Préstamo Activo: <span class="text-success fw-800">${activeLoan.deviceId}</span>`;
        statDate.textContent = activeLoan.date;
    } else {
        statusText.textContent = 'Sin préstamos activos';
        const lastLoan = [...loans].reverse().find(l => l.userEmail === currentUser.email);
        statDate.textContent = lastLoan ? lastLoan.date : '-';
    }
}

// Renderizado de la lista de Préstamos
function renderLoans() {
    if (!currentUser) return;

    const activeContainer = document.getElementById('active-loan-container');
    const tableBody = document.getElementById('history-table-body');
    
    activeContainer.innerHTML = '';
    tableBody.innerHTML = '';

    const loans = getLoans();
    const activeLoan = loans.find(l => l.userEmail === currentUser.email && l.status === 'Activo');
    const myHistory = loans.filter(l => l.userEmail === currentUser.email);

    if (activeLoan) {
        activeContainer.innerHTML = `
            <div class="text-center py-4">
                <div class="battery-icon-animated mb-4">
                    <div class="battery-level" style="animation-duration: 1.5s;"></div>
                </div>
                <h3 class="fw-800 text-white mb-1">${activeLoan.deviceId}</h3>
                <span class="badge bg-success bg-opacity-20 text-success border border-success border-opacity-30 mb-4 px-3 py-1.5 fw-700">
                    <i class="bi bi-clock-history me-1"></i>Estado: Activo
                </span>
                
                <div class="bg-dark bg-opacity-40 p-3 rounded-3 border border-secondary border-opacity-30 text-start mb-4">
                    <div class="d-flex justify-content-between mb-2">
                        <span class="text-white-50 small">Fecha de Retiro:</span>
                        <span class="text-white small fw-700">${activeLoan.date}</span>
                    </div>
                    <div class="d-flex justify-content-between">
                        <span class="text-white-50 small">Estación de Origen:</span>
                        <span class="text-white small fw-700">${activeLoan.stationSource}</span>
                    </div>
                </div>

                <button class="btn btn-primary-custom w-100 py-3" onclick="openReturnModal('${activeLoan.id}')">
                    <i class="bi bi-arrow-left-right me-2"></i>Devolver Cargador
                </button>
            </div>
        `;
    } else {
        activeContainer.innerHTML = `
            <div class="text-center py-5">
                <i class="bi bi-battery-fill text-muted-custom display-4 mb-3 d-block"></i>
                <h5 class="text-white fw-700">Sin alquileres activos</h5>
                <p class="text-white-50 small px-4 mb-3">Los detalles de tu alquiler de energía aparecerán en esta sección cuando retires un cargador.</p>
                <button class="btn btn-secondary-custom btn-sm px-3" onclick="navigateToWorkspaceTab('estaciones')">Alquilar Ahora</button>
            </div>
        `;
    }

    if (myHistory.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="5" class="text-center py-4 text-white-50 small">No posees actividades en el historial.</td>
            </tr>
        `;
        return;
    }

    myHistory.slice().reverse().forEach(l => {
        const badgeClass = l.status === 'Activo' ? 'bg-success bg-opacity-25 text-success border border-success border-opacity-25' : 'bg-secondary bg-opacity-25 text-white-50';
        const row = document.createElement('tr');
        row.innerHTML = `
            <td class="text-white py-3 fw-700">${l.deviceId}</td>
            <td class="text-white-50 py-3 small">${l.stationSource}</td>
            <td class="text-white-50 py-3 small">${l.stationDest || '-'}</td>
            <td class="text-white-50 py-3 small">${l.date}</td>
            <td class="py-3 text-end"><span class="badge ${badgeClass} px-2.5 py-1.5">${l.status}</span></td>
        `;
        tableBody.appendChild(row);
    });
}

// Módulo de Administración
function renderAdminPanel() {
    const tableBody = document.getElementById('admin-stations-body');
    tableBody.innerHTML = '';
    const stations = getStations();

    stations.forEach(s => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td class="text-white py-3 fw-700">${s.name}</td>
            <td class="text-center text-success py-3 fw-700">${s.powerBanks}</td>
            <td class="text-center text-white-50 py-3">${s.capacity}</td>
            <td class="text-end py-3">
                <button class="btn btn-outline-danger btn-sm border-0" onclick="deleteStation('${s.id}')">
                    <i class="bi bi-trash3-fill"></i>
                </button>
            </td>
        `;
        tableBody.appendChild(row);
    });
}

function handleAddStation(e) {
    e.preventDefault();
    const name = document.getElementById('admin-st-name').value.trim();
    const powerBanks = parseInt(document.getElementById('admin-st-banks').value);
    const capacity = parseInt(document.getElementById('admin-st-capacity').value);

    if (powerBanks > capacity) {
        showToast('Configuración Inválida', 'La cantidad inicial no puede ser mayor que la capacidad total de slots.', 'danger');
        return;
    }

    const stations = getStations();
    if (stations.find(s => s.name.toLowerCase() === name.toLowerCase())) {
        showToast('Estación Duplicada', 'Ya existe una estación registrada con ese nombre.', 'danger');
        return;
    }

    const newStation = {
        id: `st-${Date.now()}`,
        name,
        powerBanks,
        capacity
    };

    stations.push(newStation);
    setStations(stations);

    document.getElementById('form-add-station').reset();
    showToast('Estación Registrada', `La estación "${name}" ha sido añadida de forma exitosa.`, 'success');
    
    // Recargar tablas
    renderAdminPanel();
    renderLandingStations();
    renderLandingStats();
}

function deleteStation(stationId) {
    let stations = getStations();
    const station = stations.find(s => s.id === stationId);
    
    if (confirm(`¿Estás seguro de que deseas eliminar la estación "${station.name}"?`)) {
        stations = stations.filter(s => s.id !== stationId);
        setStations(stations);
        showToast('Estación Removida', 'La estación fue removida de la red.', 'success');
        
        renderAdminPanel();
        renderLandingStations();
        renderLandingStats();
    }
}

// 7. ENVÍO DE FORMULARIO DE CONTACTO
function handleContactSubmit(e) {
    e.preventDefault();
    const name = document.getElementById('contact-name').value.trim();
    const email = document.getElementById('contact-email').value.trim();
    const subject = document.getElementById('contact-subject').value.trim();
    const message = document.getElementById('contact-message').value.trim();

    // Simular envío de datos a servidor de PowerGo
    showToast('Mensaje Recibido', `Gracias por contactarnos, ${name}. Responderemos a tu solicitud en menos de 24 horas.`, 'success');
    document.getElementById('form-contacto').reset();
}

// 8. NOTIFICACIONES (Toast)
function showToast(title, body, type = 'success') {
    const toastEl = document.getElementById('liveToast');
    const toastTitle = document.getElementById('toast-title');
    const toastBody = document.getElementById('toast-body');
    const toastIcon = document.getElementById('toast-icon');

    // Estilos según severidad
    toastIcon.className = 'bi me-2';
    if (type === 'success') {
        toastIcon.classList.add('bi-check-circle-fill', 'text-success');
        toastEl.style.borderColor = 'var(--primary)';
    } else if (type === 'danger') {
        toastIcon.classList.add('bi-exclamation-triangle-fill', 'text-danger');
        toastEl.style.borderColor = '#ef4444';
    } else {
        toastIcon.classList.add('bi-info-circle-fill', 'text-info');
        toastEl.style.borderColor = 'var(--accent)';
    }

    toastTitle.textContent = title;
    toastBody.textContent = body;

    const toast = new bootstrap.Toast(toastEl, { delay: 4500 });
    toast.show();
}

// 9. CARGA INICIAL
window.addEventListener('DOMContentLoaded', () => {
    initDatabase();
    renderLandingStats();
    renderLandingStations();
    updateNavigationMapHUD('inicio');
});
