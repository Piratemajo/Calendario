package es.javierdev.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import es.javierdev.adapters.LocalDateTimeTypeAdapter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import es.javierdev.utils.Constants;

public class WebServer {
    private final EventManager eventManager;


    public WebServer(EventManager em) {
        this.eventManager = em;
    }

    public void start() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(Constants.WEB_SERVER_PORT), 0);

            server.createContext("/api/events", exchange -> {
                String json = new GsonBuilder()
                        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                        .create()
                        .toJson(eventManager.getEvents());
                sendResponse(exchange, json, "application/json");
            });

            server.createContext("/api/categories", exchange -> {
                String json = new Gson().toJson(eventManager.getCategories());
                sendResponse(exchange, json, "application/json");
            });

            server.createContext("/", exchange -> {
                String html = generateFrontend();
                sendResponse(exchange, html, "text/html");
            });

            server.setExecutor(null);
            server.start();
            System.out.println(">>> Servidor Web corriendo en http://localhost:" + Constants.WEB_SERVER_PORT);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void sendResponse(HttpExchange exchange, String response, String type) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private String generateFrontend() {
        return """
    <!DOCTYPE html>
    <html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Javierdev Calendar Web</title>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
        <style>
            :root {
                --primary: #4285F4;
                --success: #34A853;
                --warning: #FBBC04;
                --danger: #EA4335;
                --dark: #202124;
                --gray: #5f6368;
                --light: #f8f9fa;
                --border: #e0e0e0;
                --shadow: 0 4px 12px rgba(0,0,0,0.1);
                --shadow-lg: 0 10px 40px rgba(0,0,0,0.15);
            }

            * { box-sizing: border-box; margin: 0; padding: 0; }
           \s
            body {
                font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                min-height: 100vh;
                padding: 20px;
                color: var(--dark);
            }

            .container {
                max-width: 1200px;
                margin: 0 auto;
            }

            /* Header */
            .header {
                background: white;
                padding: 30px;
                border-radius: 16px;
                margin-bottom: 25px;
                box-shadow: var(--shadow-lg);
            }

            .header-top {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 20px;
                flex-wrap: wrap;
                gap: 15px;
            }

            .header h1 {
                font-size: 1.8em;
                font-weight: 700;
                color: var(--dark);
                display: flex;
                align-items: center;
                gap: 10px;
            }

            .header p {
                color: var(--gray);
                font-size: 0.95em;
            }

            .refresh-btn {
                background: var(--primary);
                color: white;
                border: none;
                padding: 10px 20px;
                border-radius: 8px;
                font-weight: 600;
                cursor: pointer;
                transition: all 0.3s;
                display: flex;
                align-items: center;
                gap: 8px;
            }

            .refresh-btn:hover {
                background: #3367d6;
                transform: translateY(-2px);
            }

            .refresh-btn:active {
                transform: translateY(0);
            }

            /* Stats */
            .stats {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
                gap: 15px;
                margin-top: 20px;
            }

            .stat-card {
                background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
                padding: 25px;
                border-radius: 12px;
                text-align: center;
                border: 1px solid var(--border);
                transition: all 0.3s;
            }

            .stat-card:hover {
                transform: translateY(-5px);
                box-shadow: var(--shadow);
            }

            .stat-number {
                font-size: 2.5em;
                font-weight: 700;
                color: var(--primary);
                margin-bottom: 5px;
            }

            .stat-label {
                color: var(--gray);
                font-size: 0.9em;
                font-weight: 500;
            }

            /* Filters */
            .filters {
                background: white;
                padding: 20px;
                border-radius: 12px;
                margin-bottom: 20px;
                box-shadow: var(--shadow);
                display: flex;
                gap: 15px;
                flex-wrap: wrap;
                align-items: center;
            }

            .search-input {
                flex: 1;
                min-width: 250px;
                padding: 12px 16px;
                border: 2px solid var(--border);
                border-radius: 8px;
                font-size: 0.95em;
                transition: border-color 0.3s;
            }

            .search-input:focus {
                outline: none;
                border-color: var(--primary);
            }

            .filter-select {
                padding: 12px 16px;
                border: 2px solid var(--border);
                border-radius: 8px;
                font-size: 0.95em;
                background: white;
                cursor: pointer;
            }

            .filter-select:focus {
                outline: none;
                border-color: var(--primary);
            }

            /* Events Grid */
            .events-grid {
                display: grid;
                grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
                gap: 20px;
            }

            .event-card {
                background: white;
                border-radius: 12px;
                overflow: hidden;
                box-shadow: var(--shadow);
                transition: all 0.3s;
                border-left: 5px solid var(--primary);
                position: relative;
            }

            .event-card:hover {
                transform: translateY(-5px);
                box-shadow: var(--shadow-lg);
            }

            .event-card.priority-HIGH {
                border-left-color: var(--danger);
            }

            .event-card.priority-MEDIUM {
                border-left-color: var(--warning);
            }

            .event-card.priority-LOW {
                border-left-color: var(--success);
            }

            .event-header {
                padding: 20px;
                border-bottom: 1px solid var(--light);
            }

            .event-title {
                font-size: 1.2em;
                font-weight: 600;
                color: var(--dark);
                margin-bottom: 8px;
            }

            .event-category {
                display: inline-block;
                padding: 4px 12px;
                border-radius: 20px;
                font-size: 0.75em;
                font-weight: 600;
                color: white;
                background: var(--primary);
            }

            .event-body {
                padding: 20px;
            }

            .event-meta {
                display: flex;
                flex-wrap: wrap;
                gap: 15px;
                margin-bottom: 15px;
                font-size: 0.9em;
                color: var(--gray);
            }

            .event-meta-item {
                display: flex;
                align-items: center;
                gap: 6px;
            }

            .event-description {
                color: var(--gray);
                font-size: 0.9em;
                line-height: 1.6;
                margin-top: 15px;
                padding-top: 15px;
                border-top: 1px solid var(--light);
            }

            .event-priority {
                position: absolute;
                top: 15px;
                right: 15px;
                padding: 4px 10px;
                border-radius: 6px;
                font-size: 0.7em;
                font-weight: 700;
                color: white;
            }

            .priority-HIGH .event-priority { background: var(--danger); }
            .priority-MEDIUM .event-priority { background: var(--warning); color: var(--dark); }
            .priority-LOW .event-priority { background: var(--success); }

            /* Loading & Empty States */
            .loading {
                text-align: center;
                padding: 60px 20px;
                color: white;
                font-size: 1.2em;
            }

            .loading-spinner {
                width: 50px;
                height: 50px;
                border: 4px solid rgba(255,255,255,0.3);
                border-top-color: white;
                border-radius: 50%;
                animation: spin 1s linear infinite;
                margin: 0 auto 20px;
            }

            @keyframes spin {
                to { transform: rotate(360deg); }
            }

            .empty-state {
                text-align: center;
                padding: 60px 20px;
                background: white;
                border-radius: 12px;
                box-shadow: var(--shadow);
            }

            .empty-state-icon {
                font-size: 4em;
                margin-bottom: 20px;
            }

            .empty-state h2 {
                color: var(--dark);
                margin-bottom: 10px;
            }

            .empty-state p {
                color: var(--gray);
            }

            /* Last Updated */
            .last-updated {
                text-align: center;
                color: rgba(255,255,255,0.8);
                font-size: 0.85em;
                margin-top: 30px;
                padding: 15px;
            }

            /* Responsive */
            @media (max-width: 768px) {
                body { padding: 10px; }
                .header { padding: 20px; }
                .header h1 { font-size: 1.4em; }
                .stats { grid-template-columns: 1fr; }
                .events-grid { grid-template-columns: 1fr; }
                .filters { flex-direction: column; }
                .search-input { width: 100%; }
            }
        </style>
    </head>
    <body>
        <div class="container">
            <!-- Header -->
            <div class="header">
                <div class="header-top">
                    <div>
                        <h1> Javierdev Calendar</h1>
                        <p>Sincronizado con tu aplicación de escritorio</p>
                    </div>
                    <button class="refresh-btn" onclick="loadEvents()">
                        🔄 Actualizar
                    </button>
                </div>
               \s
                <div class="stats">
                    <div class="stat-card">
                        <div class="stat-number" id="totalEvents">-</div>
                        <div class="stat-label">Eventos Totales</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-number" id="todayEvents">-</div>
                        <div class="stat-label">Hoy</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-number" id="weekEvents">-</div>
                        <div class="stat-label">Esta Semana</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-number" id="highPriorityEvents">-</div>
                        <div class="stat-label">Prioridad Alta</div>
                    </div>
                </div>
            </div>

            <!-- Filters -->
            <div class="filters">
                <input type="text" class="search-input" id="searchInput"\s
                       placeholder=" Buscar eventos por título, descripción o ubicación..."
                       oninput="filterEvents()">
                <select class="filter-select" id="priorityFilter" onchange="filterEvents()">
                    <option value="">Todas las Prioridades</option>
                    <option value="HIGH"> Alta</option>
                    <option value="MEDIUM"> Media</option>
                    <option value="LOW"> Baja</option>
                </select>
                <select class="filter-select" id="categoryFilter" onchange="filterEvents()">
                    <option value="">Todas las Categorías</option>
                </select>
            </div>

            <!-- Events Grid -->
            <div id="app" class="events-grid">
                <div class="loading">
                    <div class="loading-spinner"></div>
                    <p>Cargando eventos...</p>
                </div>
            </div>

            <!-- Last Updated -->
            <div class="last-updated" id="lastUpdated"></div>
        </div>

        <script>
            let allEvents = [];
            let categories = [];

            // Formatear fecha
            function formatDate(dateString) {
                const date = new Date(dateString);
                const options = {\s
                    weekday: 'short',\s
                    year: 'numeric',\s
                    month: 'short',\s
                    day: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit'
                };
                return date.toLocaleDateString('es-ES', options);
            }

            // Formatear solo hora
            function formatTime(dateString) {
                const date = new Date(dateString);
                return date.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' });
            }

            // Obtener color de categoría
            function getCategoryColor(categoryId) {
                const cat = categories.find(c => c.id === categoryId);
                return cat ? cat.color : '#4285F4';
            }

            // Obtener nombre de categoría
            function getCategoryName(categoryId) {
                const cat = categories.find(c => c.id === categoryId);
                return cat ? cat.name : 'Sin categoría';
            }

            // Cargar eventos
            async function loadEvents() {
                try {
                    const [eventsRes, categoriesRes] = await Promise.all([
                        fetch('/api/events'),
                        fetch('/api/categories')
                    ]);
                   \s
                    allEvents = await eventsRes.json();
                    categories = await categoriesRes.json();
                   \s
                    // Actualizar estadísticas
                    updateStats();
                   \s
                    // Llenar filtro de categorías
                    updateCategoryFilter();
                   \s
                    // Renderizar eventos
                    filterEvents();
                   \s
                    // Actualizar timestamp
                    document.getElementById('lastUpdated').textContent =\s
                        'Última actualización: ' + new Date().toLocaleTimeString('es-ES');
                   \s
                } catch (err) {
                    document.getElementById('app').innerHTML = `
                        <div class="empty-state">
                            <div class="empty-state-icon"></div>
                            <h2>Error de Conexión</h2>
                            <p>No se pudo conectar con la aplicación de escritorio.<br>
                            Asegúrate de que esté ejecutándose.</p>
                        </div>
                    `;
                    console.error('Error loading events:', err);
                }
            }

            // Actualizar estadísticas
            function updateStats() {
                const today = new Date().toISOString().split('T')[0];
                const weekAgo = new Date();
                weekAgo.setDate(weekAgo.getDate() - 7);

                document.getElementById('totalEvents').textContent = allEvents.length;
               \s
                document.getElementById('todayEvents').textContent = allEvents.filter(e =>\s
                    e.start.startsWith(today)
                ).length;
               \s
                document.getElementById('weekEvents').textContent = allEvents.filter(e => {
                    const eventDate = new Date(e.start);
                    return eventDate >= weekAgo;
                }).length;
               \s
                document.getElementById('highPriorityEvents').textContent = allEvents.filter(e =>\s
                    e.priority === 'HIGH'
                ).length;
            }

            // Actualizar filtro de categorías
            function updateCategoryFilter() {
                const select = document.getElementById('categoryFilter');
                select.innerHTML = '<option value="">Todas las Categorías</option>';
                categories.forEach(cat => {
                    const option = document.createElement('option');
                    option.value = cat.id;
                    option.textContent = cat.name;
                    option.style.backgroundColor = cat.color;
                    select.appendChild(option);
                });
            }

            // Filtrar eventos
            function filterEvents() {
                const search = document.getElementById('searchInput').value.toLowerCase();
                const priority = document.getElementById('priorityFilter').value;
                const category = document.getElementById('categoryFilter').value;

                let filtered = allEvents.filter(e => {
                    const matchSearch = !search ||\s
                        e.title.toLowerCase().includes(search) ||
                        (e.description && e.description.toLowerCase().includes(search)) ||
                        (e.location && e.location.toLowerCase().includes(search));
                   \s
                    const matchPriority = !priority || e.priority === priority;
                    const matchCategory = !category || e.categoryId === category;
                   \s
                    return matchSearch && matchPriority && matchCategory;
                });

                // Ordenar por fecha
                filtered.sort((a, b) => new Date(a.start) - new Date(b.start));

                renderEvents(filtered);
            }

            // Renderizar eventos
            function renderEvents(events) {
                const app = document.getElementById('app');
               \s
                if (events.length === 0) {
                    app.innerHTML = `
                        <div class="empty-state" style="grid-column: 1 / -1;">
                            <div class="empty-state-icon"></div>
                            <h2>No hay eventos</h2>
                            <p>No se encontraron eventos con los filtros actuales</p>
                        </div>
                    `;
                    return;
                }

                app.innerHTML = events.map(e => {
                    const categoryColor = getCategoryColor(e.categoryId);
                    const categoryName = getCategoryName(e.categoryId);
                   \s
                    return `
                        <div class="event-card priority-${e.priority || 'MEDIUM'}">
                            <div class="event-priority">${e.priority || 'MEDIUM'}</div>
                            <div class="event-header">
                                <div class="event-title">${escapeHtml(e.title)}</div>
                                <span class="event-category" style="background: ${categoryColor}">
                                    ${escapeHtml(categoryName)}
                                </span>
                            </div>
                            <div class="event-body">
                                <div class="event-meta">
                                    <div class="event-meta-item">
                                         ${formatDate(e.start)}
                                    </div>
                                    <div class="event-meta-item">
                                         ${formatTime(e.start)} - ${formatTime(e.end)}
                                    </div>
                                    ${e.location ? `
                                    <div class="event-meta-item">
                                         ${escapeHtml(e.location)}
                                    </div>
                                    ` : ''}
                                    ${e.reminder ? `
                                    <div class="event-meta-item">
                                         ${e.reminderMinutesBefore} min antes
                                    </div>
                                    ` : ''}
                                </div>
                                ${e.description ? `
                                <div class="event-description">
                                    ${escapeHtml(e.description)}
                                </div>
                                ` : ''}
                            </div>
                        </div>
                    `;
                }).join('');
            }

            // Escapar HTML para prevenir XSS
            function escapeHtml(text) {
                if (!text) return '';
                const div = document.createElement('div');
                div.textContent = text;
                return div.innerHTML;
            }

            // Auto-refresh cada 30 segundos
            setInterval(loadEvents, 30000);

            // Cargar al iniciar
            loadEvents();
        </script>
    </body>
    </html>
   \s""";
    }
}