package es.javierdev.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import es.javierdev.adapters.LocalDateTypeAdapter;
import es.javierdev.adapters.LocalDateTimeTypeAdapter;
import es.javierdev.models.AppSettings;
import es.javierdev.models.CalendarEvent;
import es.javierdev.models.EventCategory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.*;


public class EventManager {
    private List<CalendarEvent> events = new ArrayList<>();
    private List<EventCategory> categories = new ArrayList<>();
    private AppSettings settings = new AppSettings();

    private final String EVENTS_FILE = "calendar_events.json";
    private final String CATEGORIES_FILE = "calendar_categories.json";
    private final String SETTINGS_FILE = "calendar_settings.json";
    private final Gson gson;

    public EventManager() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(java.time.LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(java.time.LocalDate.class, new LocalDateTypeAdapter())
                .create();
        loadAll();
    }

    private void loadAll() {
        loadEvents();
        loadCategories();
        loadSettings();
    }

    private void loadEvents() {
        try {
            if (Files.exists(Paths.get(EVENTS_FILE))) {
                String json = Files.readString(Paths.get(EVENTS_FILE));
                System.out.println(">>> [DEBUG] Cargando eventos desde archivo...");
                events = gson.fromJson(json, new TypeToken<List<CalendarEvent>>(){}.getType());
                System.out.println(">>> [DEBUG] Eventos cargados: " + (events != null ? events.size() : 0));
            } else {
                System.out.println(">>> [DEBUG] No existe archivo de eventos, creando datos de ejemplo");
                createSampleData();
            }
        } catch (IOException e) {
            System.err.println("[Error]" + e.getMessage());
        }
    }

    private void loadCategories() {
        try {
            if (Files.exists(Paths.get(CATEGORIES_FILE))) {
                String json = Files.readString(Paths.get(CATEGORIES_FILE));
                categories = gson.fromJson(json, new TypeToken<List<EventCategory>>(){}.getType());
            } else {
                createDefaultCategories();
            }
        } catch (IOException e) {
            System.err.println("[Error]" + e.getMessage());
        }
    }

    private void loadSettings() {
        try {
            if (Files.exists(Paths.get(SETTINGS_FILE))) {
                String json = Files.readString(Paths.get(SETTINGS_FILE));
                settings = gson.fromJson(json, AppSettings.class);
            }
        } catch (IOException e) {
            System.err.println("[Error]" + e.getMessage());
        }
    }

    private void createDefaultCategories() {
        categories.add(new EventCategory("Trabajo", "#4285F4"));
        categories.add(new EventCategory("Personal", "#34A853"));
        categories.add(new EventCategory("Reuniones", "#EA4335"));
        categories.add(new EventCategory("Recordatorios", "#FBBC04"));
        saveCategories();
    }

    private void createSampleData() {
        if (categories.isEmpty()) createDefaultCategories();

        String workCat = categories.get(0).id;

        CalendarEvent event1 = new CalendarEvent("Reunion de Proyecto",
                java.time.LocalDateTime.now().withHour(10).withMinute(0),
                java.time.LocalDateTime.now().withHour(11).withMinute(30), workCat);
        event1.description = "Revisar progreso del sprint";
        event1.location = "Aula 102";
        event1.priority = "HIGH";
        event1.reminder = true;
        events.add(event1);


        saveEvents();
    }

    public void saveEvents() {
        try {
            Files.writeString(Paths.get(EVENTS_FILE), gson.toJson(events));
            System.out.println(">>> [DEBUG] Eventos guardados en archivo: " + events.size());
        } catch (IOException e) {
            System.err.println(">>> [ERROR] No se pudo guardar eventos: " + e.getMessage());
        }
    }

    public void saveCategories() {
        try {
            Files.writeString(Paths.get(CATEGORIES_FILE), gson.toJson(categories));
        } catch (IOException e) {
            System.err.println("[Error]" + e.getMessage());
        }
    }

    public void saveSettings() {
        try {
            Files.writeString(Paths.get(SETTINGS_FILE), gson.toJson(settings));
        } catch (IOException e) {
            System.err.println("[Error]" + e.getMessage());
        }
    }

    public void addEvent(CalendarEvent e) {
        System.out.println(">>> [DEBUG] Anadiendo evento: " + e.title);
        System.out.println(">>> [DEBUG] ID: " + e.id);
        System.out.println(">>> [DEBUG] Start: " + e.start);
        System.out.println(">>> [DEBUG] Category: " + e.categoryId);

        events.add(e);
        saveEvents();

        System.out.println(">>> [DEBUG] Total eventos ahora: " + events.size());
    }

    public void updateEvent(CalendarEvent e) {
        System.out.println(">>> [DEBUG] Actualizando evento: " + e.id);

        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).id.equals(e.id)) {
                events.set(i, e);
                saveEvents();
                System.out.println(">>> [DEBUG] Evento actualizado correctamente");
                return;
            }
        }
        System.out.println(">>> [DEBUG] Evento no encontrado, agregando como nuevo");
        events.add(e);
        saveEvents();
    }

    public void deleteEvent(String eventId) {
        System.out.println(">>> [DEBUG] Eliminando evento: " + eventId);
        events.removeIf(e -> e.id.equals(eventId));
        saveEvents();
    }


    public List<CalendarEvent> getEvents() {
        return events;
    }

    public List<EventCategory> getCategories() {
        return categories;
    }

    public AppSettings getSettings() {
        return settings;
    }

    public void setSettings(AppSettings s) {
        this.settings = s;
        saveSettings();
    }


    public void exportData(String filePath) {
        try {
            Map<String, Object> export = new HashMap<>();
            export.put("events", events);
            export.put("categories", categories);
            export.put("settings", settings);
            Files.writeString(Paths.get(filePath), gson.toJson(export));
        } catch (IOException e) {
            System.err.println("[Error]" + e.getMessage());
        }
    }

}