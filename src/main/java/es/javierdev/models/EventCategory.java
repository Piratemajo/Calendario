package es.javierdev.models;

import java.util.UUID;

public class EventCategory {
    public String id;
    public String name;
    public String color;
    public boolean visible;

    public EventCategory() {
        this.id = UUID.randomUUID().toString();
        this.visible = true;
    }

    public EventCategory(String name, String color) {
        this();
        this.name = name;
        this.color = color;
    }
}