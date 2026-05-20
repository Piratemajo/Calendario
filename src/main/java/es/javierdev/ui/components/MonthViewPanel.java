package es.javierdev.ui.components;

import es.javierdev.models.CalendarEvent;
import es.javierdev.services.EventManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

public class MonthViewPanel extends JPanel {
    private YearMonth currentYearMonth;
    private EventManager eventManager;
    private Runnable onRefresh;

    public MonthViewPanel(YearMonth ym, EventManager manager, Runnable onRefresh) {
        this.currentYearMonth = ym;
        this.eventManager = manager;
        this.onRefresh = onRefresh;
        setLayout(new GridLayout(0, 7, 3, 3));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        refreshData();
    }

    private void refreshData() {
        removeAll();
        System.out.println(">>> [DEBUG] Refreshing month view for: " + currentYearMonth);

        // Headers
        String[] headers = {"Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom"};
        for (String h : headers) {
            JLabel label = new JLabel(h, SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            label.setForeground(new Color(90, 90, 90));
            label.setBackground(new Color(245, 245, 245));
            label.setOpaque(true);
            label.setBorder(new EmptyBorder(5, 0, 5, 0));
            add(label);
        }

        // Empty slots
        int firstDayOfWeek = currentYearMonth.atDay(1).getDayOfWeek().getValue();
        int emptySlots = (firstDayOfWeek == 7) ? 6 : firstDayOfWeek - 1;

        for (int i = 0; i < emptySlots; i++) {
            JPanel empty = new JPanel();
            empty.setBackground(new Color(250, 250, 250));
            add(empty);
        }

        List<CalendarEvent> allEvents = eventManager.getEvents();
        System.out.println(">>> [DEBUG] Total eventos en sistema: " + allEvents.size());

        int daysInMonth = currentYearMonth.lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            final int finalDay = day;
            final YearMonth finalMonth = currentYearMonth;

            // Filtrar eventos que caen en ESTE dia especifico
            List<CalendarEvent> dayEvents = allEvents.stream()
                    .filter(e -> {
                        if (e.start == null) return false;
                        LocalDate eventDate = e.start.toLocalDate();
                        boolean matches = eventDate.getYear() == finalMonth.getYear() &&
                                eventDate.getMonthValue() == finalMonth.getMonthValue() &&
                                eventDate.getDayOfMonth() == finalDay;
                        if (matches) {
                            System.out.println(">>> [DEBUG] Evento encontrado para dia " + finalDay + ": " + e.title);
                        }
                        return matches;
                    })
                    .collect(Collectors.toList());

            System.out.println(">>> [DEBUG] Dia " + finalDay + " tiene " + dayEvents.size() + " eventos");

            DayCell cell = new DayCell(finalDay, dayEvents, eventManager, currentYearMonth, onRefresh);
            add(cell);
        }

        revalidate();
        repaint();
        System.out.println(">>> [DEBUG] MonthViewPanel refreshed");
    }

    public void updateMonth(YearMonth ym) {
        System.out.println(">>> [DEBUG] Updating month to: " + ym);
        this.currentYearMonth = ym;
        refreshData();
    }
}