package interfaz;

import com.formdev.flatlaf.FlatLightLaf;
import logica.GestorTareas;
import modelo.Tarea;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VentanaPrincipal extends JFrame {
    private final GestorTareas gestor;
    private JTable tablaTareas;
    private DefaultTableModel modeloTabla;
    private JTextField campoTitulo, campoDescripcion;
    private JButton btnAgregar, btnEditar, btnEliminar, btnCompletar;

    public VentanaPrincipal() {
        gestor = new GestorTareas();
        initUI();
        cargarTareasEnTabla();
    }

    private void initUI() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ex) {
            System.err.println("Error al aplicar FlatLaf: " + ex.getMessage());
        }

        setTitle("TaskMaster - Gestor de Tareas");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel formulario
        JPanel panelFormulario = new JPanel(new GridLayout(2, 2, 10, 10));
        campoTitulo = new JTextField();
        campoDescripcion = new JTextField();
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Nueva Tarea"));
        panelFormulario.add(new JLabel("Título:"));
        panelFormulario.add(campoTitulo);
        panelFormulario.add(new JLabel("Descripción:"));
        panelFormulario.add(campoDescripcion);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnAgregar = new JButton("➕ Agregar");
        btnEditar = new JButton("✏️ Editar");
        btnEliminar = new JButton("🗑️ Eliminar");
        btnCompletar = new JButton("✅ Marcar Completada");

        // Estado inicial de botones
        btnAgregar.setEnabled(true); // Habilitado al inicio para permitir agregar tareas
        btnEditar.setEnabled(false);
        btnEliminar.setEnabled(false);
        btnCompletar.setEnabled(false);

        panelBotones.add(btnAgregar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnCompletar);

        // Tabla
        modeloTabla = new DefaultTableModel(new String[]{"Título", "Descripción", "Estado"}, 0);
        tablaTareas = new JTable(modeloTabla) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaTareas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaTareas.setRowHeight(30);
        tablaTareas.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tablaTareas.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tablaTareas.setSelectionBackground(new Color(30, 60, 90));
        tablaTareas.setSelectionForeground(Color.WHITE);
        tablaTareas.getColumnModel().getColumn(2).setCellRenderer(new EstadoRenderer());

        JScrollPane scrollTabla = new JScrollPane(tablaTareas);

        // Contenedor
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.add(panelFormulario, BorderLayout.NORTH);
        panelSuperior.add(panelBotones, BorderLayout.CENTER);

        add(panelSuperior, BorderLayout.NORTH);
        add(scrollTabla, BorderLayout.CENTER);

        configurarEventos();
    }

    private void configurarEventos() {
        DocumentListener inputListener = new DocumentListenerAdapter(this::validarCampos);
        campoTitulo.getDocument().addDocumentListener(inputListener);
        campoDescripcion.getDocument().addDocumentListener(inputListener);

        btnAgregar.addActionListener(e -> {
            String titulo = campoTitulo.getText().trim();
            String desc = campoDescripcion.getText().trim();

            // Validar que los campos no estén vacíos antes de agregar
            if (titulo.isEmpty() || desc.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Por favor, complete todos los campos antes de agregar la tarea.",
                        "Campos incompletos",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            gestor.agregarTarea(new Tarea(titulo, desc));
            cargarTareasEnTabla();
            limpiarCamposParaAgregar(); // Nueva función específica para agregar
        });

        btnEliminar.addActionListener(e -> {
            int fila = tablaTareas.getSelectedRow();
            if (fila >= 0) {
                gestor.eliminarTarea(fila);
                cargarTareasEnTabla();
                limpiarCamposParaAgregar(); // Volver al modo agregar
            }
        });

        btnEditar.addActionListener(e -> {
            int fila = tablaTareas.getSelectedRow();
            if (fila >= 0) {
                String titulo = campoTitulo.getText().trim();
                String desc = campoDescripcion.getText().trim();
                gestor.editarTarea(fila, new Tarea(titulo, desc));
                cargarTareasEnTabla();
                limpiarCamposParaAgregar(); // Volver al modo agregar después de editar
            }
        });

        btnCompletar.addActionListener(e -> {
            int fila = tablaTareas.getSelectedRow();
            if (fila >= 0) {
                boolean estado = gestor.obtenerTareas().get(fila).isCompletada();
                gestor.marcarCompletada(fila, !estado);
                cargarTareasEnTabla();
                limpiarCamposParaAgregar(); // Volver al modo agregar
            }
        });

        tablaTareas.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return; // Evitar eventos duplicados

            int fila = tablaTareas.getSelectedRow();
            boolean seleccionada = fila >= 0;

            if (seleccionada) {
                // Modo EDICIÓN: cargar datos de la tarea seleccionada
                campoTitulo.setText(modeloTabla.getValueAt(fila, 0).toString());
                campoDescripcion.setText(modeloTabla.getValueAt(fila, 1).toString());

                // Habilitar botones de edición
                btnEditar.setEnabled(true);
                btnEliminar.setEnabled(true);
                btnAgregar.setEnabled(false); // Desactivar agregar en modo edición

                // Verificar si la tarea está completada para habilitar/deshabilitar el botón completar
                boolean esCompletada = gestor.obtenerTareas().get(fila).isCompletada();
                btnCompletar.setEnabled(!esCompletada);
            }
            // Nota: cuando no hay selección, se mantienen los estados actuales de los botones
        });
    }

    private void cargarTareasEnTabla() {
        modeloTabla.setRowCount(0);
        List<Tarea> tareas = gestor.obtenerTareas();
        for (Tarea t : tareas) {
            String estado = t.isCompletada() ? "✔ Completada" : "⏳ Pendiente";
            modeloTabla.addRow(new Object[]{t.getTitulo(), t.getDescripcion(), estado});
        }
    }

    // Nueva función específica para limpiar campos y volver al modo agregar
    private void limpiarCamposParaAgregar() {
        campoTitulo.setText("");
        campoDescripcion.setText("");
        tablaTareas.clearSelection();

        // Configurar botones para modo AGREGAR
        btnAgregar.setEnabled(true); // Siempre habilitado para permitir agregar
        btnEditar.setEnabled(false);
        btnEliminar.setEnabled(false);
        btnCompletar.setEnabled(false);
    }

    private void validarCampos() {
        boolean haySeleccion = tablaTareas.getSelectedRow() >= 0;

        // Si hay una selección, estamos en modo EDITAR
        // Si no hay selección, estamos en modo AGREGAR
        if (!haySeleccion) {
            btnAgregar.setEnabled(true); // Siempre habilitado en modo agregar
            btnEditar.setEnabled(false);
            btnEliminar.setEnabled(false);
            btnCompletar.setEnabled(false);
        }
    }

    private static class DocumentListenerAdapter implements DocumentListener {
        private final Runnable callback;

        public DocumentListenerAdapter(Runnable callback) {
            this.callback = callback;
        }

        @Override public void insertUpdate(DocumentEvent e) { callback.run(); }
        @Override public void removeUpdate(DocumentEvent e) { callback.run(); }
        @Override public void changedUpdate(DocumentEvent e) { callback.run(); }
    }

    private static class EstadoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String estado = value.toString();

            if (!isSelected) {
                if (estado.contains("✔")) {
                    c.setForeground(new Color(0, 128, 0)); // verde
                    setText("✔ Completada");
                } else {
                    c.setForeground(new Color(192, 0, 0)); // rojo
                    setText("⏳ Pendiente");
                }
            } else {
                c.setForeground(Color.WHITE);
            }

            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            return c;
        }
    }
}
