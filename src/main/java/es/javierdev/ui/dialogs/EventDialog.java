package es.javierdev.ui.dialogs;

import com.formdev.flatlaf.FlatClientProperties;
import es.javierdev.models.CalendarEvent;
import es.javierdev.models.EventCategory;
import es.javierdev.services.EventManager;
import es.javierdev.utils.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class EventDialog extends JDialog {

    // === CONSTANTES ===
    private static final int DIALOG_WIDTH = 550;
    private static final int DIALOG_HEIGHT = 750;
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private static final String[] PRIORITY_OPTIONS = {"LOW", "MEDIUM", "HIGH"};


    // === ESTADO ===
    private boolean confirmed = false;
    private CalendarEvent event;
    private final EventManager eventManager;
    private final boolean isNewEvent;

    // === COMPONENTES UI ===
    private JTextField titleField;
    private JLabel titleCounterLabel;
    private JComboBox<String> categoryCombo;
    private JSpinner startDateSpinner, startTimeSpinner;
    private JSpinner endDateSpinner, endTimeSpinner;
    private JCheckBox allDayCheck;
    private JTextField locationField;
    private JComboBox<String> priorityCombo;
    private JComboBox<String> recurrenceCombo;
    private JTextArea descriptionArea;
    private JLabel descriptionCounterLabel;
    private JCheckBox reminderCheck;
    private JSpinner reminderMinutesSpinner;
    private JPanel colorPreview;
    private String customColor = null;

    // === VALIDACIÓN ===
    private boolean titleValid = false;
    private boolean datesValid = false;

    public EventDialog(JFrame parent, EventManager manager, CalendarEvent existingEvent, LocalDate defaultDate) {
        super(parent, existingEvent == null ? "Nuevo Evento" : "Editar Evento", true);
        this.eventManager = manager;
        this.event = existingEvent != null ? existingEvent : new CalendarEvent();
        this.isNewEvent = (existingEvent == null);

        if (defaultDate != null && existingEvent == null) {
            this.event.start = defaultDate.atTime(9, 0);
            this.event.end = defaultDate.atTime(10, 0);
        }

        // Configuración del diálogo
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Atajos de teclado
        registerKeyboardActions();

        buildUI();
    }

    // ─────────────────────────────────────────────────────────
    // CONSTRUCCIÓN DE LA UI
    // ─────────────────────────────────────────────────────────

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);

        // Header con título e instrucciones
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Formulario principal con scroll
        add(createFormScrollPane(), BorderLayout.CENTER);

        // Botones de acción
        add(createButtonPanel(), BorderLayout.SOUTH);

        // Validación inicial
        validateForm();
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(10, 10));
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(20, 25, 15, 25));

        JLabel titleLabel = new JLabel(isNewEvent ? "Crear Nuevo Evento" : "Editar Evento");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(32, 33, 36));

        JLabel subtitleLabel = new JLabel("Los campos marcados con * son obligatorios");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitleLabel.setForeground(new Color(95, 99, 104));

        header.add(titleLabel, BorderLayout.WEST);
        header.add(subtitleLabel, BorderLayout.SOUTH);

        return header;
    }

    private JScrollPane createFormScrollPane() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(10, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.weightx = 1.0;

        int row = 0;

        // === SECCIÓN 1: INFORMACIÓN BÁSICA ===
        row = addSectionTitle(formPanel, "Información Básica", gbc, row);

        // Título (obligatorio)
        row = addTitleField(formPanel, gbc, row);

        // Categoría
        row = addCategoryField(formPanel, gbc, row);



        // === SECCIÓN 2: FECHA Y HORA ===
        row = addSectionTitle(formPanel, "Fecha y Hora", gbc, row);

        // Evento todo el día
        row = addAllDayOption(formPanel, gbc, row);

        // Fechas y horas (se muestran/ocultan según allDay)
        row = addDateTimeFields(formPanel, gbc, row);



        // === SECCIÓN 3: DETALLES ADICIONALES ===
        row = addSectionTitle(formPanel, "Detalles Adicionales", gbc, row);

        // Ubicación
        row = addLocationField(formPanel, gbc, row);



        // Prioridad
        row = addPriorityField(formPanel, gbc, row);

        // Descripción
        row = addDescriptionField(formPanel, gbc, row);

        // === SECCIÓN 4: RECORDATORIO ===
        row = addSectionTitle(formPanel, "Recordatorio", gbc, row);
        row = addReminderField(formPanel, gbc, row);

        // Panel con scroll
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);
        return scrollPane;
    }

    private int addSectionTitle(JPanel panel, String title, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = ++row;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 0, 12, 0);

        JLabel sectionLabel = new JLabel(title);
        sectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sectionLabel.setForeground(new Color(95, 99, 104));
        sectionLabel.setBorder(new EmptyBorder(0, 0, 5, 0));
        panel.add(sectionLabel, gbc);

        gbc.insets = new Insets(6, 0, 6, 0);
        return row;
    }

    private int addTitleField(JPanel panel, GridBagConstraints gbc, int row) {
        gbc.gridx = 0; gbc.gridy = ++row; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;

        JPanel titleRow = new JPanel(new BorderLayout(8, 0));
        titleRow.setOpaque(false);

        JLabel label = new JLabel("Título *");
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setToolTipText("Nombre del evento (obligatorio)");

        titleField = new JTextField(event.title != null ? event.title : "");
        titleField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ej: Reunión de equipo");
        titleField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { validateTitle(); }
            public void removeUpdate(DocumentEvent e) { validateTitle(); }
            public void changedUpdate(DocumentEvent e) { validateTitle(); }
        });

        titleCounterLabel = new JLabel("0/" + MAX_TITLE_LENGTH);
        titleCounterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        titleCounterLabel.setForeground(new Color(120, 120, 120));
        updateTitleCounter();

        JPanel rightPanel = new JPanel(new BorderLayout(5, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(titleField, BorderLayout.CENTER);
        rightPanel.add(titleCounterLabel, BorderLayout.EAST);

        titleRow.add(label, BorderLayout.WEST);
        titleRow.add(rightPanel, BorderLayout.CENTER);

        panel.add(titleRow, gbc);
        return row;
    }

    private int addCategoryField(JPanel panel, GridBagConstraints gbc, int row) {
        gbc.gridx = 0; gbc.gridy = ++row; gbc.gridwidth = 1; gbc.weightx = 0;
        JLabel label = new JLabel("Categoría");
        label.setToolTipText("Clasificación del evento");
        panel.add(label, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        categoryCombo = new JComboBox<>();
        categoryCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        categoryCombo.putClientProperty(FlatClientProperties.STYLE, "arrowType: chevron");

        for (EventCategory cat : eventManager.getCategories()) {
            categoryCombo.addItem(cat.name);
            if (event.categoryId != null && event.categoryId.equals(cat.id)) {
                categoryCombo.setSelectedItem(cat.name);
            }
        }
        if (categoryCombo.getItemCount() == 0) {
            categoryCombo.addItem("Sin categoría");
        }
        panel.add(categoryCombo, gbc);
        return row;
    }



    private int addAllDayOption(JPanel panel, GridBagConstraints gbc, int row) {
        gbc.gridx = 0; gbc.gridy = ++row; gbc.gridwidth = 2;
        allDayCheck = new JCheckBox("Evento de todo el día");
        allDayCheck.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        allDayCheck.setBackground(Color.WHITE);
        allDayCheck.setSelected(event.start != null && event.end != null &&
                event.start.toLocalTime().equals(LocalTime.MIDNIGHT) &&
                event.end.toLocalTime().equals(LocalTime.MIDNIGHT));
        allDayCheck.addActionListener(e -> toggleTimeFields());
        panel.add(allDayCheck, gbc);
        return row;
    }

    private int addDateTimeFields(JPanel panel, GridBagConstraints gbc, int row) {
        // Fecha Inicio
        gbc.gridx = 0; gbc.gridy = ++row; gbc.gridwidth = 1; gbc.weightx = 0;
        JLabel startLabel = new JLabel("Fecha Inicio *");
        startLabel.setToolTipText("Día de inicio del evento");
        panel.add(startLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        startDateSpinner = createDatePicker(event.start != null ?
                Date.from(event.start.atZone(ZoneId.systemDefault()).toInstant()) : new Date());
        startDateSpinner.addChangeListener(e -> validateDates());
        panel.add(startDateSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = ++row; gbc.weightx = 0;
        JLabel timeStartLabel = new JLabel("Hora Inicio");
        timeStartLabel.setToolTipText("Hora de inicio del evento");
        panel.add(timeStartLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        startTimeSpinner = createTimePicker(event.start != null ?
                Date.from(event.start.atZone(ZoneId.systemDefault()).toInstant()) : new Date());
        startTimeSpinner.addChangeListener(e -> validateDates());
        panel.add(startTimeSpinner, gbc);

        // Fecha Fin
        gbc.gridx = 0; gbc.gridy = ++row; gbc.weightx = 0;
        JLabel endLabel = new JLabel("Fecha Fin *");
        endLabel.setToolTipText("Día de fin del evento");
        panel.add(endLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        endDateSpinner = createDatePicker(event.end != null ?
                Date.from(event.end.atZone(ZoneId.systemDefault()).toInstant()) : new Date());
        endDateSpinner.addChangeListener(e -> validateDates());
        panel.add(endDateSpinner, gbc);

        // Hora Fin
        gbc.gridx = 0; gbc.gridy = ++row; gbc.weightx = 0;
        JLabel timeEndLabel = new JLabel("Hora Fin");
        timeEndLabel.setToolTipText("Hora de fin del evento");
        panel.add(timeEndLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        endTimeSpinner = createTimePicker(event.end != null ?
                Date.from(event.end.atZone(ZoneId.systemDefault()).toInstant()) : new Date());
        endTimeSpinner.addChangeListener(e -> validateDates());
        panel.add(endTimeSpinner, gbc);

        // Label de validación de fechas
        gbc.gridx = 0; gbc.gridy = ++row; gbc.gridwidth = 2;
        JLabel dateValidationLabel = new JLabel();
        dateValidationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        dateValidationLabel.setForeground(new Color(234, 67, 53));
        dateValidationLabel.setVisible(false);
        panel.add(dateValidationLabel, gbc);

        // Guardar referencia para actualizar
        SwingUtilities.invokeLater(() -> {
            toggleTimeFields();
            validateDates();
        });

        return row;
    }



    private int addLocationField(JPanel panel, GridBagConstraints gbc, int row) {
        gbc.gridx = 0; gbc.gridy = ++row; gbc.gridwidth = 1; gbc.weightx = 0;
        JLabel label = new JLabel("Ubicación");
        label.setToolTipText("Lugar físico o dirección del evento");
        panel.add(label, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        locationField = new JTextField(event.location != null ? event.location : "");
        locationField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        locationField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ej: Sala de reuniones A");
        panel.add(locationField, gbc);
        return row;
    }



    private int addPriorityField(JPanel panel, GridBagConstraints gbc, int row) {
        gbc.gridx = 0; gbc.gridy = ++row; gbc.gridwidth = 1; gbc.weightx = 0;
        JLabel label = new JLabel("Prioridad");
        label.setToolTipText("Nivel de importancia del evento");
        panel.add(label, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        priorityCombo = new JComboBox<>(PRIORITY_OPTIONS);
        priorityCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        priorityCombo.setSelectedItem(event.priority != null ? event.priority : "MEDIUM");

        // Colores según prioridad
        priorityCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value.equals("HIGH")) label.setForeground(new Color(234, 67, 53));
                else if (value.equals("MEDIUM")) label.setForeground(new Color(251, 188, 4));
                else if (value.equals("LOW")) label.setForeground(new Color(52, 168, 83));
                return label;
            }
        });
        panel.add(priorityCombo, gbc);
        return row;
    }

    private int addDescriptionField(JPanel panel, GridBagConstraints gbc, int row) {
        gbc.gridx = 0; gbc.gridy = ++row; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel label = new JLabel("Descripción");
        label.setToolTipText("Detalles adicionales del evento");
        panel.add(label, gbc);

        gbc.gridy = ++row;
        descriptionArea = new JTextArea(event.description != null ? event.description : "", 4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descriptionArea.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                "Agrega notas, agenda, participantes, etc.");
        descriptionArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateDescriptionCounter(); }
            public void removeUpdate(DocumentEvent e) { updateDescriptionCounter(); }
            public void changedUpdate(DocumentEvent e) { updateDescriptionCounter(); }
        });

        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        descriptionCounterLabel = new JLabel("0/" + MAX_DESCRIPTION_LENGTH);
        descriptionCounterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        descriptionCounterLabel.setForeground(new Color(120, 120, 120));
        descriptionCounterLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        updateDescriptionCounter();

        JPanel descPanel = new JPanel(new BorderLayout(0, 5));
        descPanel.setOpaque(false);
        descPanel.add(descScroll, BorderLayout.CENTER);
        descPanel.add(descriptionCounterLabel, BorderLayout.SOUTH);

        panel.add(descPanel, gbc);
        return row;
    }

    private int addReminderField(JPanel panel, GridBagConstraints gbc, int row) {
        gbc.gridx = 0; gbc.gridy = ++row; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.WEST;

        reminderCheck = new JCheckBox("Activar recordatorio");
        reminderCheck.setSelected(event.reminder);
        reminderCheck.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reminderCheck.setBackground(Color.WHITE);
        reminderCheck.setToolTipText("Recibir notificación antes del evento");
        reminderCheck.addActionListener(e -> reminderMinutesSpinner.setEnabled(reminderCheck.isSelected()));
        panel.add(reminderCheck, gbc);

        gbc.gridy = ++row; gbc.gridx = 1; gbc.weightx = 1;
        reminderMinutesSpinner = new JSpinner(new SpinnerNumberModel(
                event.reminderMinutesBefore > 0 ? event.reminderMinutesBefore : 15, 5, 1440, 5));
        reminderMinutesSpinner.setEnabled(event.reminder);
        reminderMinutesSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel reminderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        reminderPanel.setOpaque(false);
        reminderPanel.add(reminderMinutesSpinner);
        reminderPanel.add(new JLabel("minutos antes"));
        panel.add(reminderPanel, gbc);

        return row;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

        JButton saveBtn = createStyledButton("Guardar", Constants.PRIMARY_COLOR, true);
        JButton cancelBtn = createStyledButton("Cancelar", new Color(240, 240, 240), false);

        // Acción Guardar con validación completa
        saveBtn.addActionListener(e -> saveEvent());
        cancelBtn.addActionListener(e -> dispose());

        // Atajos: Enter = Guardar, Escape = Cancelar
        getRootPane().setDefaultButton(saveBtn);

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        return buttonPanel;
    }



    private JSpinner createDatePicker(Date initialDate) {
        JSpinner spinner = new JSpinner(new SpinnerDateModel(initialDate, null, null, Calendar.DAY_OF_MONTH));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return spinner;
    }

    private JSpinner createTimePicker(Date initialTime) {
        JSpinner spinner = new JSpinner(new SpinnerDateModel(initialTime, null, null, Calendar.MINUTE));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "HH:mm"));
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return spinner;
    }

    private JButton createStyledButton(String text, Color bg, boolean isPrimary) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(isPrimary ? Color.WHITE : new Color(60, 60, 60));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 38));
        if (!isPrimary) {
            btn.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        }
        return btn;
    }

    private void toggleTimeFields() {
        boolean isAllDay = allDayCheck.isSelected();
        startTimeSpinner.setEnabled(!isAllDay);
        endTimeSpinner.setEnabled(!isAllDay);
        validateDates();
    }


    private void updateColorPreview() {
        Color color = customColor != null ? Color.decode(customColor) : new Color(240, 240, 240);
        colorPreview.setBackground(color);
        colorPreview.setToolTipText(customColor != null ? customColor : "Sin color personalizado");
    }

    private void updateTitleCounter() {
        int length = titleField.getText().length();
        titleCounterLabel.setText(length + "/" + MAX_TITLE_LENGTH);
        titleCounterLabel.setForeground(length > MAX_TITLE_LENGTH * 0.9 ?
                new Color(234, 67, 53) : new Color(120, 120, 120));
    }

    private void updateDescriptionCounter() {
        int length = descriptionArea.getText().length();
        descriptionCounterLabel.setText(length + "/" + MAX_DESCRIPTION_LENGTH);
        descriptionCounterLabel.setForeground(length > MAX_DESCRIPTION_LENGTH * 0.9 ?
                new Color(234, 67, 53) : new Color(120, 120, 120));
    }

    // ─────────────────────────────────────────────────────────
    // VALIDACIÓN DEL FORMULARIO
    // ─────────────────────────────────────────────────────────

    private void validateTitle() {
        String title = titleField.getText().trim();
        titleValid = !title.isEmpty() && title.length() <= MAX_TITLE_LENGTH;
        titleField.setBorder(BorderFactory.createLineBorder(
                titleValid ? new Color(200, 200, 200) : new Color(234, 67, 53), titleValid ? 1 : 2));
        validateForm();
    }

    private void validateDates() {
        try {
            Date sDate = (Date) startDateSpinner.getValue();
            Date sTime = (Date) startTimeSpinner.getValue();
            Date eDate = (Date) endDateSpinner.getValue();
            Date eTime = (Date) endTimeSpinner.getValue();

            LocalDate startDate = sDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalTime startTime = allDayCheck.isSelected() ? LocalTime.MIDNIGHT :
                    sTime.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
            LocalDate endDate = eDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalTime endTime = allDayCheck.isSelected() ? LocalTime.MIDNIGHT :
                    eTime.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

            LocalDateTime start = LocalDateTime.of(startDate, startTime);
            LocalDateTime end = LocalDateTime.of(endDate, endTime);

            datesValid = !end.isBefore(start);
        } catch (Exception e) {
            datesValid = false;
        }
        validateForm();
    }

    private void validateForm() {
        // La validación completa se hace al guardar
    }


    private void saveEvent() {
        try {
            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                showError("El título es obligatorio", titleField);
                return;
            }
            if (title.length() > MAX_TITLE_LENGTH) {
                showError("El título no puede exceder " + MAX_TITLE_LENGTH + " caracteres", titleField);
                return;
            }

            String description = descriptionArea.getText().trim();
            if (description.length() > MAX_DESCRIPTION_LENGTH) {
                showError("La descripción no puede exceder " + MAX_DESCRIPTION_LENGTH + " caracteres", descriptionArea);
                return;
            }

            Date sDate = (Date) startDateSpinner.getValue();
            Date sTime = (Date) startTimeSpinner.getValue();
            Date eDate = (Date) endDateSpinner.getValue();
            Date eTime = (Date) endTimeSpinner.getValue();

            LocalDate startDate = sDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalTime startTime = allDayCheck.isSelected() ? LocalTime.MIDNIGHT :
                    sTime.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
            LocalDate endDate = eDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalTime endTime = allDayCheck.isSelected() ? LocalTime.MIDNIGHT :
                    eTime.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

            LocalDateTime start = LocalDateTime.of(startDate, startTime);
            LocalDateTime end = LocalDateTime.of(endDate, endTime);

            if (end.isBefore(start) || end.isEqual(start)) {
                showError("La fecha/hora de fin debe ser posterior a la de inicio", endDateSpinner);
                return;
            }

            // Obtener categoría
            String selectedCategoryName = (String) categoryCombo.getSelectedItem();
            String categoryId = null;
            for (EventCategory cat : eventManager.getCategories()) {
                if (cat.name.equals(selectedCategoryName)) {
                    categoryId = cat.id;
                    break;
                }
            }

            event.title = title;
            event.categoryId = categoryId;
            event.start = start;
            event.end = end;
            event.location = locationField.getText().trim();
            event.priority = (String) priorityCombo.getSelectedItem();
            event.description = description.isEmpty() ? null : description;
            event.reminder = reminderCheck.isSelected();
            event.reminderMinutesBefore = (Integer) reminderMinutesSpinner.getValue();



            if (isNewEvent) {
                eventManager.addEvent(event);
            } else {
                eventManager.updateEvent(event);
            }

            confirmed = true;
            dispose();

        } catch (Exception ex) {
            System.out.println(">> [ERROR] " +ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error al guardar: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showError(String message, Component focusComponent) {
        JOptionPane.showMessageDialog(this, message, "Error de validación", JOptionPane.ERROR_MESSAGE);
        if (focusComponent != null) {
            focusComponent.requestFocus();
        }
    }


    private void registerKeyboardActions() {
        // Ctrl+S o Enter: Guardar
        getRootPane().registerKeyboardAction(
                e -> saveEvent(),
                KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Escape: Cancelar
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }



    public boolean isConfirmed() {
        return confirmed;
    }
}