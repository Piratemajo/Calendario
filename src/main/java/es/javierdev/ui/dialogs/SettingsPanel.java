package es.javierdev.ui.dialogs;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import es.javierdev.models.AppSettings;
import es.javierdev.models.EventCategory;
import es.javierdev.services.EventManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;

public class SettingsPanel extends JPanel {
    private final EventManager eventManager;
    private final JFrame parentFrame;

    // Componentes
    private JComboBox<String> themeCombo;
    private JCheckBox notificationsCheck;
    private JCheckBox startMinimizedCheck;
    private JList<String> categoriesList;
    private DefaultListModel<String> categoriesModel;

    public SettingsPanel(EventManager manager, JFrame parent) {
        this.eventManager = manager;
        this.parentFrame = parent;

        setLayout(new BorderLayout(15, 15));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        buildUI();
    }

    private void buildUI() {
        AppSettings settings = eventManager.getSettings();

        // TÍTULO
        JLabel titleLabel = new JLabel("Configuracion");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(32, 33, 36));
        titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));

        // CONTENIDO PRINCIPAL
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // SECCION 1: APARIENCIA
        row = addSectionHeader(content, "Apariencia", gbc, row);

        // Tema
        gbc.gridx = 0; gbc.gridy = ++row; gbc.gridwidth = 2;
        content.add(createLabel("Tema de la aplicacion"), gbc);

        gbc.gridy = ++row;
        themeCombo = new JComboBox<>(new String[]{"Claro", "Oscuro"});
        themeCombo.setSelectedItem(settings.theme.equals("dark") ? "Oscuro" : "Claro");
        themeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        themeCombo.setPreferredSize(new Dimension(200, 30));
        content.add(themeCombo, gbc);

        // SECCION 2: NOTIFICACIONES
        row = addSectionHeader(content, "Notificaciones", gbc, row);

        // Activar notificaciones
        gbc.gridy = ++row;
        notificationsCheck = new JCheckBox("Mostrar notificaciones del sistema");
        notificationsCheck.setSelected(settings.showNotifications);
        notificationsCheck.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        notificationsCheck.setBackground(Color.WHITE);
        content.add(notificationsCheck, gbc);

        row = addSectionHeader(content, "Comportamiento", gbc, row);

        // Iniciar minimizado
        gbc.gridy = ++row;
        startMinimizedCheck = new JCheckBox("Iniciar aplicacion minimizada");
        startMinimizedCheck.setSelected(settings.startMinimized);
        startMinimizedCheck.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        startMinimizedCheck.setBackground(Color.WHITE);
        content.add(startMinimizedCheck, gbc);

        // SECCION 4: CATEGORIAS
        row = addSectionHeader(content, "Categorias de Eventos", gbc, row);

        // Lista de categorias
        gbc.gridy = ++row; gbc.gridwidth = 2;
        categoriesModel = new DefaultListModel<>();
        refreshCategoriesList();

        categoriesList = new JList<>(categoriesModel);
        categoriesList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        categoriesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoriesList.setPreferredSize(new Dimension(300, 80));
        categoriesList.setBorder(new LineBorder(new Color(220, 220, 220)));

        JScrollPane categoriesScroll = new JScrollPane(categoriesList);
        categoriesScroll.setPreferredSize(new Dimension(300, 100));
        content.add(categoriesScroll, gbc);

        // Botones de categorias
        gbc.gridy = ++row; gbc.gridwidth = 1; gbc.weightx = 0.5;

        JPanel catButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        catButtons.setOpaque(false);

        JButton addCatBtn = createSmallButton("Anadir");
        JButton editCatBtn = createSmallButton("Editar");
        JButton deleteCatBtn = createSmallButton("Eliminar");

        addCatBtn.addActionListener(e -> showCategoryDialog(null));
        editCatBtn.addActionListener(e -> {
            int idx = categoriesList.getSelectedIndex();
            if (idx >= 0) {
                EventCategory selected = eventManager.getCategories().get(idx);
                showCategoryDialog(selected);
            } else {
                JOptionPane.showMessageDialog(this, "Selecciona una categoria para editar",
                        "Informacion", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        deleteCatBtn.addActionListener(e -> deleteSelectedCategory());

        catButtons.add(addCatBtn);
        catButtons.add(editCatBtn);
        catButtons.add(deleteCatBtn);
        content.add(catButtons, gbc);

        // SECCION 5: DATOS
        row = addSectionHeader(content, "Gestion de Datos", gbc, row);

        gbc.gridy = ++row; gbc.gridwidth = 1;
        JPanel dataButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        dataButtons.setOpaque(false);

        JButton exportBtn = createActionButton("Exportar", "Exportar todos los datos a JSON");
        JButton importBtn = createActionButton("Importar", "Importar datos desde JSON");

        exportBtn.addActionListener(e -> exportData());

        dataButtons.add(exportBtn);
        dataButtons.add(importBtn);
        content.add(dataButtons, gbc);

        // BOTON GUARDAR
        gbc.gridy = ++row; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 15, 10, 15);

        JButton saveBtn = new JButton("Guardar Configuracion");
        saveBtn.setBackground(new Color(66, 133, 244));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setFocusPainted(false);
        saveBtn.setBorderPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.setPreferredSize(new Dimension(200, 35));

        saveBtn.addActionListener(e -> saveSettings(themeCombo, notificationsCheck, startMinimizedCheck));

        content.add(saveBtn, gbc);

        // ENSAMBLAR
        add(titleLabel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);
        add(scrollPane, BorderLayout.CENTER);
    }

    private int addSectionHeader(JPanel panel, String title, GridBagConstraints gbc, int row) {
        gbc.gridx = 0; gbc.gridy = ++row; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 15, 8, 15);

        JLabel header = new JLabel(title);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setForeground(new Color(95, 99, 104));
        header.setBorder(new EmptyBorder(0, 0, 5, 0));
        panel.add(header, gbc);

        gbc.insets = new Insets(10, 15, 10, 10);
        return row;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(new Color(60, 60, 60));
        return label;
    }

    private JButton createSmallButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setBackground(new Color(245, 245, 245));
        btn.setForeground(new Color(60, 60, 60));
        btn.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(85, 28));
        return btn;
    }

    private JButton createActionButton(String text, String tooltip) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(new Color(245, 245, 245));
        btn.setForeground(new Color(60, 60, 60));
        btn.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setToolTipText(tooltip);
        btn.setPreferredSize(new Dimension(120, 35));
        return btn;
    }

    private void refreshCategoriesList() {
        categoriesModel.clear();
        for (EventCategory cat : eventManager.getCategories()) {
            String display = String.format("%s [%s]", cat.name, cat.color);
            categoriesModel.addElement(display);
        }
    }

    private void showCategoryDialog(EventCategory category) {
        JDialog dialog = new JDialog(parentFrame,
                category == null ? "Nueva Categoria" : "Editar Categoria", true);
        dialog.setSize(420, 280);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Nombre
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        form.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        JTextField nameField = new JTextField(category != null ? category.name : "");
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(nameField, gbc);

        // Color
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(new JLabel("Color (hex):"), gbc);
        gbc.gridx = 1;
        JTextField colorField = new JTextField(category != null ? category.color : "#4285F4");
        colorField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(colorField, gbc);

        // Selector de color visual
        gbc.gridx = 1; gbc.gridy = 2;
        JButton colorPicker = new JButton("Elegir color");
        colorPicker.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        colorPicker.addActionListener(e -> {
            Color selected = JColorChooser.showDialog(dialog, "Seleccionar Color",
                    Color.decode(colorField.getText()));
            if (selected != null) {
                String hex = String.format("#%02X%02X%02X",
                        selected.getRed(), selected.getGreen(), selected.getBlue());
                colorField.setText(hex);
            }
        });
        form.add(colorPicker, gbc);

        // Botones
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setBackground(Color.WHITE);
        JButton saveBtn = new JButton("Guardar");
        JButton cancelBtn = new JButton("Cancelar");

        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String color = colorField.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "El nombre es obligatorio",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!color.matches("^#[0-9A-Fa-f]{6}$")) {
                JOptionPane.showMessageDialog(dialog, "Color invalido. Usa formato #RRGGBB",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (category == null) {
                eventManager.getCategories().add(new EventCategory(name, color));
            } else {
                category.name = name;
                category.color = color;
            }
            eventManager.saveCategories();
            refreshCategoriesList();
            dialog.dispose();
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttons.add(saveBtn);
        buttons.add(cancelBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteSelectedCategory() {
        int idx = categoriesList.getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una categoria para eliminar",
                    "Informacion", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        EventCategory selected = eventManager.getCategories().get(idx);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Eliminar categoria \"" + selected.name + "\"?\n\n" +
                        "Los eventos con esta categoria quedaran sin categoria.",
                "Confirmar eliminacion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            eventManager.getCategories().remove(idx);
            eventManager.saveCategories();
            refreshCategoriesList();
        }
    }

    private void exportData() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Exportar datos del calendario");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Archivos JSON", "json"));
        chooser.setSelectedFile(new File("backup_calendario.json"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            if (!path.endsWith(".json")) path += ".json";

            try {
                eventManager.exportData(path);
                JOptionPane.showMessageDialog(this,
                        "Datos exportados correctamente a:\n" + path,
                        "Exito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al exportar:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }



    private void saveSettings(JComboBox<String> themeCombo,
                              JCheckBox notificationsCheck,
                              JCheckBox startMinimizedCheck) {

        AppSettings settings = eventManager.getSettings();

        String selectedTheme = (String) themeCombo.getSelectedItem();
        settings.theme = selectedTheme.equals("Oscuro") ? "dark" : "light";
        settings.showNotifications = notificationsCheck.isSelected();
        settings.startMinimized = startMinimizedCheck.isSelected();

        eventManager.setSettings(settings);

        applyTheme(settings.theme);

        JOptionPane.showMessageDialog(this,
                "Configuracion guardada correctamente",
                "Exito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void applyTheme(String theme) {
        try {
            if (theme.equals("dark")) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } else {
                UIManager.setLookAndFeel(new FlatLightLaf());
            }
            SwingUtilities.updateComponentTreeUI(parentFrame);
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ex) {
            System.out.println(">> [ERROR] " +ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "No se pudo aplicar el tema: " + ex.getMessage(),
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
    }
}