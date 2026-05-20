package es.javierdev.models;


import java.time.LocalDateTime;
import java.util.UUID;

public class CalendarEvent {

    // Identificador único del evento
    public String id;

    // Información básica
    public String title;
    public String description;
    public String location;

    // Categoría y prioridad
    public String categoryId;
    public String priority;

    // Fechas y horas
    public LocalDateTime start;
    public LocalDateTime end;

    // Configuración de recordatorio
    public boolean reminder;
    public int reminderMinutesBefore;
    public boolean notified;
    public String recurrence;

    // Metadatos
    public long createdAt;
    public long updatedAt;


    public CalendarEvent() {
        this.id = UUID.randomUUID().toString();
        this.priority = "MEDIUM";
        this.reminder = false;
        this.reminderMinutesBefore = 15;
        this.notified = false;
        this.recurrence = "NONE";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }


    public CalendarEvent(String title, LocalDateTime start, LocalDateTime end, String categoryId) {
        this();
        this.title = title;
        this.start = start;
        this.end = end;
        this.categoryId = categoryId;
    }


    public String getPriorityColor() {
        if (priority == null) return "#4285F4";
        return switch (priority) {
            case "HIGH" -> "#EA4335";
            case "MEDIUM" -> "#FBBC04";
            case "LOW" -> "#34A853";
            default -> "#4285F4";
        };
    }


    @Override
    public String toString() {
        return String.format("Event{id='%s', title='%s', start=%s}",
                id != null ? id.substring(0, 8) + "..." : "null",
                title,
                start != null ? start.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm")) : "null");
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CalendarEvent that = (CalendarEvent) obj;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }






}