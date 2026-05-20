package es.javierdev.ui.components;

import es.javierdev.models.CalendarEvent;
import es.javierdev.models.EventCategory;
import es.javierdev.services.EventManager;
import es.javierdev.ui.dialogs.EventDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class DayCell extends JPanel {
    private final int day;
    private final List<CalendarEvent> events;
    private final EventManager eventManager;
    private final Runnable onRefresh;
    private final LocalDate date;
    private final YearMonth yearMonth;

    public DayCell(int day, List<CalendarEvent> events, EventManager manager,
                   YearMonth ym, Runnable onRefresh) {
        this.day = day;
        this.events = events;
        this.eventManager = manager;
        this.onRefresh = onRefresh;
        this.date = ym.atDay(day);
        this.yearMonth = ym;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new LineBorder(new Color(230, 230, 230), 1, true));
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(0, 100));

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 3));
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JLabel dayLabel = new JLabel(String.valueOf(day));
        dayLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        if (day == LocalDate.now().getDayOfMonth() && YearMonth.now().equals(ym)) {
            dayLabel.setForeground(new Color(66, 133, 244));
            dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            header.setBackground(new Color(232, 240, 254));
        }

        header.add(dayLabel);
        add(header);

        // Events - Copias finales para usar en lambdas
        renderEvents();

        // Double click to add - Lambda con variables finales
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showAddEventDialog();
                }
            }
        });
    }

    private void renderEvents() {
        System.out.println(">>> [DEBUG] DayCell renderEvents() - Dia: " + day + ", Eventos: " + (events != null ? events.size() : 0));

        // Copias finales para usar en lambdas
        final EventManager finalEventManager = eventManager;
        final Runnable finalOnRefresh = onRefresh;
        final YearMonth finalYearMonth = yearMonth;

        if (events == null || events.isEmpty()) {
            // Mostrar indicador visual de que el dia esta vacio (para debug)
            JLabel emptyHint = new JLabel(" ");
            emptyHint.setBorder(new EmptyBorder(2, 5, 2, 5));
            add(emptyHint);
            return;
        }

        int count = 0;
        for (CalendarEvent e : events) {
            if (count < 3) {
                final CalendarEvent finalEvent = e; // Variable final para lambda

                System.out.println(">>> [DEBUG] Renderizando chip para: " + e.title);

                EventCategory cat = finalEventManager.getCategories().stream()
                        .filter(c -> c.id.equals(finalEvent.categoryId))
                        .findFirst()
                        .orElse(null);

                EventChip chip = new EventChip(finalEvent, cat,
                        // Lambda 1: Editar
                        () -> {
                            System.out.println(">>> [DEBUG] Editar evento: " + finalEvent.id);
                            showEditEventDialog(finalEvent);
                        },
                        // Lambda 2: Eliminar - usa variables finales
                        () -> {
                            System.out.println(">>> [DEBUG] Eliminar evento: " + finalEvent.id);
                            finalEventManager.deleteEvent(finalEvent.id);
                            if (finalOnRefresh != null) finalOnRefresh.run();
                        }
                );

                chip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
                chip.setAlignmentX(Component.LEFT_ALIGNMENT);
                add(chip);
                count++;
            }
        }
        if (events.size() > 3) {
            JLabel more = new JLabel("+ " + (events.size() - 3) + " mas");
            more.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            more.setForeground(Color.GRAY);
            more.setAlignmentX(Component.LEFT_ALIGNMENT);
            more.setBorder(new EmptyBorder(0, 5, 2, 5));
            add(more);
        }
    }

    private void showAddEventDialog() {
        EventDialog dialog = new EventDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                eventManager, null, date
        );
        dialog.setVisible(true);
        if (dialog.isConfirmed() && onRefresh != null) {
            onRefresh.run();
        }
    }

    private void showEditEventDialog(CalendarEvent event) {
        EventDialog dialog = new EventDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                eventManager, event, null
        );
        dialog.setVisible(true);
        if (dialog.isConfirmed() && onRefresh != null) {
            onRefresh.run();
        }
    }
}