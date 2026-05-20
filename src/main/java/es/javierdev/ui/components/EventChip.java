package es.javierdev.ui.components;

import es.javierdev.models.CalendarEvent;
import es.javierdev.models.EventCategory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class EventChip extends JPanel {
    private CalendarEvent event;
    private EventCategory category;
    private Runnable onEdit;
    private Runnable onDelete;

    public EventChip(CalendarEvent event, EventCategory category, Runnable onEdit, Runnable onDelete) {
        this.event = event;
        this.category = category;
        this.onEdit = onEdit;
        this.onDelete = onDelete;

        setOpaque(false);
        setLayout(new BorderLayout(5, 0));
        setBorder(new EmptyBorder(2, 4, 2, 4));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Priority indicator
        JPanel priorityIndicator = new JPanel();
        priorityIndicator.setPreferredSize(new Dimension(4, 20));
        priorityIndicator.setBackground(Color.decode(event.getPriorityColor()));
        add(priorityIndicator, BorderLayout.WEST);

        // Title
        JLabel titleLabel = new JLabel(event.title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        titleLabel.setForeground(Color.WHITE);
        add(titleLabel, BorderLayout.CENTER);

        // Context menu
        JPopupMenu popup = new JPopupMenu();
        JMenuItem editItem = new JMenuItem("✏️ Editar");
        JMenuItem deleteItem = new JMenuItem("🗑️ Eliminar");

        editItem.addActionListener(e -> { if (onEdit != null) onEdit.run(); });
        deleteItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Eliminar evento \"" + event.title + "\"?",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION && onDelete != null) {
                onDelete.run();
            }
        });

        popup.add(editItem);
        popup.add(deleteItem);

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    popup.show(EventChip.this, e.getX(), e.getY());
                } else if (e.getClickCount() == 2 && onEdit != null) {
                    onEdit.run();
                }
            }
        });

        applyStyle();
    }

    private void applyStyle() {
        if (category != null) {
            setBackground(Color.decode(category.color));
        } else {
            setBackground(Color.decode("#4285F4"));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 6, 6));
        g2.dispose();
    }
}

