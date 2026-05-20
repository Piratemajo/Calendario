package es.javierdev.ui;

import com.formdev.flatlaf.FlatLightLaf;
import es.javierdev.services.EventManager;
import es.javierdev.services.NotificationService;
import es.javierdev.services.WebServer;
import es.javierdev.ui.components.MonthViewPanel;
import es.javierdev.ui.dialogs.EventDialog;
import es.javierdev.ui.dialogs.SettingsPanel;
import es.javierdev.utils.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class Calendario extends JFrame {
    private YearMonth currentYearMonth;
    private MonthViewPanel monthView;
    private EventManager eventManager;
    private JLabel monthLabel;
    private NotificationService notificationService;

    public Calendario() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.out.println(">> [ERROR] " + e.getMessage());
        }

        setTitle(Constants.APP_TITLE + " " + Constants.VERSION);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        setLocationRelativeTo(null);

        eventManager = new EventManager();
        currentYearMonth = YearMonth.now();

        buildUI();
        new WebServer(eventManager).start();
        notificationService = new NotificationService(eventManager);
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(new Color(245, 245, 245));


        // Top Bar
        JPanel topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);

        // Main Content
        JPanel mainContent = createMainContent();
        add(mainContent, BorderLayout.CENTER);
    }



    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(15, 0));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new EmptyBorder(15, 25, 15, 25));

        // Navigation
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        navPanel.setOpaque(false);

        JButton prevBtn = createNavButton("<");
        JButton nextBtn = createNavButton(">");
        JButton todayBtn = createNavButton("Hoy");

        prevBtn.addActionListener(e -> changeMonth(-1));
        nextBtn.addActionListener(e -> changeMonth(1));
        todayBtn.addActionListener(e -> {
            currentYearMonth = YearMonth.now();
            updateView();
        });

        navPanel.add(prevBtn);
        navPanel.add(todayBtn);
        navPanel.add(nextBtn);

        // Month Label
        monthLabel = new JLabel(
                currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                SwingConstants.CENTER
        );
        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));

        // Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton settingsBtn = createActionButton("Configuracion", "Configuracion");
        JButton addEventBtn = createActionButton("+ Nuevo Evento", "Anadir Evento");

        settingsBtn.addActionListener(e -> showSettings());
        addEventBtn.addActionListener(e -> showAddEventDialog());

        actionPanel.add(settingsBtn);
        actionPanel.add(addEventBtn);

        topBar.add(navPanel, BorderLayout.WEST);
        topBar.add(monthLabel, BorderLayout.CENTER);
        topBar.add(actionPanel, BorderLayout.EAST);

        return topBar;
    }

    private JPanel createMainContent() {
        JPanel mainContent = new JPanel(new BorderLayout(10, 10));
        mainContent.setBackground(new Color(245, 245, 245));
        mainContent.setBorder(new EmptyBorder(0, 25, 25, 25));

        // Calendar
        JPanel calendarCard = new JPanel(new BorderLayout());
        calendarCard.setBackground(Color.WHITE);
        calendarCard.setBorder(new EmptyBorder(20, 20, 20, 20));

        monthView = new MonthViewPanel(currentYearMonth, eventManager, () -> {
            System.out.println(">>> [DEBUG] onRefresh callback triggered!");
            SwingUtilities.invokeLater(() -> {
                updateView();
                System.out.println(">>> [DEBUG] UI updated");
            });
        });
        JScrollPane scrollPane = new JScrollPane(monthView);
        scrollPane.setBorder(null);

        calendarCard.add(scrollPane, BorderLayout.CENTER);

        mainContent.add(calendarCard, BorderLayout.CENTER);

        return mainContent;
    }

    private JButton createNavButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        b.setBackground(Color.WHITE);
        b.setForeground(new Color(95, 99, 104));
        b.setBorder(BorderFactory.createLineBorder(new Color(218, 220, 224)));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setFocusPainted(false);
        return b;
    }

    private JButton createActionButton(String text, String tooltip) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(new Color(66, 133, 244));
        b.setForeground(Color.WHITE);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setToolTipText(tooltip);
        return b;
    }

    private void changeMonth(int delta) {
        currentYearMonth = currentYearMonth.plusMonths(delta);
        updateView();
    }

    private void updateView() {
        System.out.println(">>> [DEBUG] updateView() called");
        monthLabel.setText(currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));

        if (monthView != null) {
            monthView.updateMonth(currentYearMonth);
        }

        revalidate();
        repaint();
    }

    private void showSettings() {
        JDialog dialog = new JDialog(this, "Configuracion", true);
        dialog.setSize(650, 700);
        dialog.setMinimumSize(new Dimension(600, 650));
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(true);

        SettingsPanel settingsPanel = new SettingsPanel(eventManager, this);
        dialog.add(settingsPanel);

        dialog.setVisible(true);
    }

    private void showAddEventDialog() {
        EventDialog dialog = new EventDialog(this, eventManager, null, LocalDate.now());
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            updateView();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Calendario().setVisible(true));
    }
}