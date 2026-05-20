package es.javierdev.services;

import es.javierdev.models.CalendarEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NotificationService {
    private final EventManager eventManager;
    private final ScheduledExecutorService scheduler;
    private TrayIcon trayIcon;

    public NotificationService(EventManager em) {
        this.eventManager = em;
        this.scheduler = Executors.newScheduledThreadPool(1);
        System.out.println(">>> [NOTIFICACIONES] Servicio iniciado");
        startMonitoring();
    }

    private void startMonitoring() {
        scheduler.scheduleAtFixedRate(() -> {
            checkReminders();
        }, 0, 30, TimeUnit.SECONDS);
    }

    private void checkReminders() {
        LocalDateTime now = LocalDateTime.now();
        System.out.println(">>> [NOTIFICACIONES] Verificando recordatorios a las: " +
                now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));

        int checked = 0;
        int notified = 0;

        for (CalendarEvent e : eventManager.getEvents()) {
            checked++;

            if (!e.reminder) {
                continue;
            }

            if (e.notified) {
                continue;
            }

            if (e.start == null) {
                continue;
            }

            // Calcular tiempo de recordatorio
            LocalDateTime reminderTime = e.start.minusMinutes(e.reminderMinutesBefore);
            LocalDateTime eventStart = e.start;

            System.out.println(">>> [NOTIFICACIONES] Evento: '" + e.title +
                    "' | Recordatorio: " + reminderTime.format(DateTimeFormatter.ofPattern("HH:mm")) +
                    " | Inicio: " + eventStart.format(DateTimeFormatter.ofPattern("HH:mm")) +
                    " | Ahora: " + now.format(DateTimeFormatter.ofPattern("HH:mm")) +
                    " | Notificado: " + e.notified);

            long minutesUntilReminder = ChronoUnit.MINUTES.between(now, reminderTime);
            long minutesUntilEvent = ChronoUnit.MINUTES.between(now, eventStart);

            boolean isTimeToNotify =
                    !now.isBefore(reminderTime) &&
                            now.isBefore(eventStart) &&
                            minutesUntilReminder >= -2 &&
                            minutesUntilEvent <= e.reminderMinutesBefore + 2;

            if (isTimeToNotify) {
                System.out.println(">>> [NOTIFICACIONES]  ENVIANDO notificación para: " + e.title);
                sendNotification(e);

                e.notified = true;
                eventManager.updateEvent(e);
                notified++;
            }
        }

        if (notified > 0) {
            System.out.println(">>> [NOTIFICACIONES] Notificaciones enviadas: " + notified + "/" + checked);
        }
    }

    private void sendNotification(CalendarEvent event) {
        if (!eventManager.getSettings().showNotifications) {
            System.out.println(">>> [NOTIFICACIONES] Notificaciones desactivadas en configuración");
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                String message = "" + event.title + "\n\n" +
                        "" + event.start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n" +
                        " Comienza en " + event.reminderMinutesBefore + " minutos";

                if (event.location != null && !event.location.isEmpty()) {
                    message += "\n " + event.location;
                }
                if (event.description != null && !event.description.isEmpty()) {
                    message += "\n " + event.description.substring(0, Math.min(50, event.description.length()));
                }

                System.out.println(">>> [NOTIFICACIONES] Mostrando JOptionPane: " + event.title);

                // Crear diálogo modal pero con timeout para no bloquear
                JOptionPane pane = new JOptionPane(
                        message,
                        JOptionPane.INFORMATION_MESSAGE,
                        JOptionPane.DEFAULT_OPTION,
                        null,
                        new Object[]{"Entendido"},
                        "Entendido"
                );

                JDialog dialog = pane.createDialog(null, "Recordatorio de Evento");
                dialog.setModal(false);
                dialog.setVisible(true);

                new Thread(() -> {
                    try {
                        Thread.sleep(60000);
                        dialog.dispose();
                    } catch (InterruptedException e) {
                    }
                }).start();
                if (SystemTray.isSupported()) {
                    SystemTray tray = SystemTray.getSystemTray();

                    Image icon = null;

                    if (icon == null) {
                        icon = createDefaultIcon();
                    }

                    if (trayIcon == null) {
                        trayIcon = new TrayIcon(icon, "Javierdev Calendario");
                        trayIcon.setImageAutoSize(true);
                        trayIcon.addActionListener(ev -> {
                            trayIcon.displayMessage("Javierdev Calendario",
                                    "Haz click para ver la aplicación",
                                    TrayIcon.MessageType.INFO);
                        });
                        try {
                            tray.add(trayIcon);
                            System.out.println(">>> [NOTIFICACIONES] TrayIcon añadido a la bandeja");
                        } catch (AWTException ex) {
                            System.err.println(">>> [NOTIFICACIONES] Error al añadir TrayIcon: " + ex.getMessage());
                        }
                    }

                    trayIcon.displayMessage(
                            " " + event.title,
                            "Comienza en " + event.reminderMinutesBefore + " minutos",
                            TrayIcon.MessageType.INFO
                    );
                    System.out.println(">>> [NOTIFICACIONES] Notificación de bandeja mostrada");
                } else {
                    System.out.println(">>> [NOTIFICACIONES] SystemTray no soportado en este sistema");
                }

            } catch (Exception ex) {
                System.err.println(">>> [NOTIFICACIONES] Error al mostrar notificación: " + ex.getMessage());
            }
        });
    }

    /**
     * Crea un icono por defecto de 16x16 píxeles
     */
    private Image createDefaultIcon() {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        // Fondo transparente
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, 16, 16);
        g2.setComposite(AlphaComposite.SrcOver);

        // Círculo de color
        g2.setColor(new Color(66, 133, 244));
        g2.fillOval(2, 2, 12, 12);

        // Símbolo de campana simple
        g2.setColor(Color.WHITE);
        g2.fillOval(5, 6, 6, 5);
        g2.fillRect(7, 4, 2, 2);

        g2.dispose();
        return image;
    }

}