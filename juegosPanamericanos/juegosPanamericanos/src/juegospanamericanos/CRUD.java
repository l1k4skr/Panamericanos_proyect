package juegospanamericanos;

//Import para imagenes 
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import java.awt.Image;

//Import para el sql
import java.sql.*;

//Para manejar las tablas 
import javax.swing.table.DefaultTableModel;

// Para las listas
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class CRUD extends javax.swing.JFrame {

    // Variables del CRUD deben ir aca
    // Conexion con base de datos
    private Connection con;

    // usar la herramienta de DefaultTableModel
    DefaultTableModel dtm = new DefaultTableModel();
    DefaultTableModel nat = new DefaultTableModel();

    public CRUD() {
        initComponents();
        // Para colocar los titulos de la tabla 1
        Conexion bd = new Conexion("panamericanos");
        this.con = bd.getConnection();

        String[] titulo = new String[] { "PAIS", "ORO", "PLATA", "BRONCE", "TOTAL" };
        dtm.setColumnIdentifiers(titulo);
        tablaMedallas.setModel(dtm);

        // Para colocar los titulos de la tabla 2

        String[] titulo_nat = new String[] { "Nombre", "País", "Nota 1", "Nota 2", "Nota 3", "Nota 4", "Nota 5",
                "Nota 6", "Nota 7", "Nota 8", "Factor", "N.F" };
        nat.setColumnIdentifiers(titulo_nat);
        tablaNotas.setModel(nat);

        // --------------------------Bloque para colocar banderas en la primera ventana-----------------//

        String[] imageNames = { "Argentina.png", "Brasil.png", "Bolivia.png", "Chile.png", "Colombia.png", "Canada.png",
                "Cuba.png", "EEUU.png", "Peru.png", "Mexico.png", "Republica Dominicana.png" };
        String[] imageNames_correct = { "Argentina.png", "Brasil.png", "Bolivia.png", "Chile.png", "Colombia.png",
                "Canada.png", "Cuba.png", "EEUU.png", "Peru.png", "Mexico.png", "Republica Dominicana.png" };

        // Eliminar ".png" de cada nombre de imagen
        for (int i = 0; i < imageNames.length; i++) {
            imageNames_correct[i] = imageNames[i].replaceAll("\\.png$", "");
            ;
        }

        // Imprimir los nombres de imagen actualizados
        for (String imageNameses : imageNames_correct) {
            System.out.println(imageNameses);
        }

        // Tamaño objetivo para las imágenes
        int targetWidth = 150;
        int targetHeight = 100;

        // Crea un DefaultComboBoxModel para el JComboBox
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(imageNames_correct);
        combo_pais.setModel(model);

        // Listener para el JComboBox que muestra la imagen seleccionada
        combo_pais.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                int selectedIndex = combo_pais.getSelectedIndex();
                if (selectedIndex != -1) {

                    String selectedImageName = imageNames[selectedIndex];
                    ImageIcon selectedImageIcon = new ImageIcon(
                            getClass().getResource("/recursos/" + selectedImageName));

                    // Escala la imagen al tamaño objetivo
                    Image selectedImage = selectedImageIcon.getImage();
                    Image scaledImage = selectedImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                    ImageIcon scaledIcon = new ImageIcon(scaledImage);
                    jLabel1.setIcon(scaledIcon);
                }
            }
        });
        try {

            ResultSet rs = read("medallas", this.con);

            while (rs.next()) {

                int id = rs.getInt(1);
                String nombre = rs.getString("nombre");
                int oro = rs.getInt("oro");
                int plata = rs.getInt("plata");
                int bronce = rs.getInt("bronce");
                int total_ = rs.getInt("total");

                this.dtm.addRow(new Object[] {
                        nombre, oro, plata, bronce, total_
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // -------------------------- Funciones SQL -----------------//

    private static String removeNombreColumn(String columns) {
        String[] parts = columns.split(",\s*");
        StringBuilder newColumns = new StringBuilder();

        for (String part : parts) {
            if (!part.startsWith("nombre")) {
                if (newColumns.length() > 0) {
                    newColumns.append(", ");
                }
                newColumns.append(part);
            }
        }
        return newColumns.toString();
    }
        private static String removeIdColumn(String columns) {
        String[] parts = columns.split(",\s*");
        StringBuilder newColumns = new StringBuilder();

        for (String part : parts) {
            if (!part.startsWith("id")) {
                if (newColumns.length() > 0) {
                    newColumns.append(", ");
                }
                newColumns.append(part);
            }
        }
        return newColumns.toString();
    }

    // Funcion para leer la base de datos
    public ResultSet read(String tableName, Connection conn) throws SQLException {
        System.out.println("Esto es this.con: " + conn);
        String sql = "SELECT * FROM " + tableName;
        Statement st = conn.createStatement();
        return st.executeQuery(sql);
    }

    // Funcion para leer el ultimo id de la base de datos
    public ResultSet read_last_id(String tableName, String column_name) throws SQLException {
        String sql = "SELECT MAX('" + column_name + "') FROM " + tableName;
        Statement st = this.con.createStatement();
        return st.executeQuery(sql);
    }

    // Funcion para borrar un id de la base de datos
    public void deleteRow(String tableName, int rowIndex) throws SQLException {
        String sql = "DELETE FROM " + tableName + " WHERE idColumn = ?;";
        try (Connection conn = this.con;
        
            PreparedStatement statement = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false); // Start a transaction
            
            statement.setInt(1, rowIndex); // Establece el índice de la fila seleccionada como parámetro para la consulta SQL

            statement.executeQuery();

            conn.commit(); // Commit the transaction
        } 
        catch (SQLException e) {
            if (this.con != null) {
                this.con.rollback(); // Rollback the transaction if an exception occurs
            }
            throw e;
        }
    }

    // Funcion para borrar toda la base de datos
    public void deleteAll(String tableName) throws SQLException {
        if (!isValidTableName(tableName)) {
            throw new IllegalArgumentException("Nombre de tabla inválido: " + tableName);
        }

        String sql = "DELETE FROM " + tableName + ";";

        try (Connection conn = this.con;
                PreparedStatement statement = conn.prepareStatement(sql)) {

//            conn.setAutoCommit(false);

               statement.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    // Funcion auxiliar para validar el nombre de la tabla
    private boolean isValidTableName(String tableName) {
        return tableName.matches("[a-zA-Z0-9_]+");
    }

    // Funcion para crear un dato de la base de datos
    public void create(String tableName, Object[] values) {
        try {
            String columns = ColumnNameFetcher.getColumnNames(this.con, tableName);
            // System.out.println(columns);
            columns = removeIdColumn(columns);
            // System.out.println(columns);

            String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (";

            for (int i = 0; i < values.length; i++) {
                sql += (i == 0) ? "?" : ", ?";
            }
            sql += ")";
            // System.out.println(sql);

            try (PreparedStatement pstmt = this.con.prepareStatement(sql)) {

                for (int i = 0; i < values.length; i++) {
                    pstmt.setObject(i + 1, values[i]);
                }
                // System.out.println(pstmt);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Funcion para actualizar un dato de la base de datos (funcion alternativa)
    public void update_(String tabla, int id, Object[] nuevosValores) throws SQLException {
        String sql = "UPDATE " + tabla
                + " SET numero_oro = ?, numero_plata = ?, numero_bronce = ?, total = ? WHERE id = ?";
        try (Connection conn = this.con;
                PreparedStatement statement = conn.prepareStatement(sql)) {
            for (int i = 0; i < nuevosValores.length; i++) {
                statement.setObject(i + 1, nuevosValores[i]);
            }
            statement.setInt(nuevosValores.length + 1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Funcion para actualizar un dato de la base de datos
    public void update_medallas(String tableName, Object[] values, String pais) {
        try {
            String columns = ColumnNameFetcher.getColumnNames(this.con, tableName);
            System.out.println(columns);

            columns = removeIdColumn(columns);
            System.out.println(columns);

            columns = removeNombreColumn(columns);
            System.out.println(columns);

            String[] parts = columns.split(",\s");


            // Construye la parte SET de la sentencia SQL
            StringBuilder setClause = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) {
                    setClause.append(", ");
                }
                setClause.append(parts[i]).append(" = ?");
            }

            // Construye la sentencia SQL completa
            System.out.println(setClause);
            String sql = "UPDATE " + tableName + " SET " + setClause + " WHERE nombre = ?";

            // Prepara el statement y asigna los valores
            try (PreparedStatement pstmt = this.con.prepareStatement(sql)) {
                for (int i = 0; i < values.length; i++) {
                    pstmt.setObject(i + 1, values[i]);
                }
                pstmt.setString(values.length + 1, pais);

                // Ejecuta la actualización
                int affectedRows = pstmt.executeUpdate();
                System.out.println(affectedRows + " rows affected."); // Opcional: Imprime cuántas filas fueron afectadas
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextField4 = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jLabel24 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        combo_pais = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        numOro = new javax.swing.JTextField();
        numPlata = new javax.swing.JTextField();
        numBronce = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaMedallas = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        textNombre = new javax.swing.JTextField();
        textPais = new javax.swing.JTextField();
        Nota1 = new javax.swing.JTextField();
        Nota5 = new javax.swing.JTextField();
        Nota2 = new javax.swing.JTextField();
        Nota6 = new javax.swing.JTextField();
        Nota3 = new javax.swing.JTextField();
        Nota7 = new javax.swing.JTextField();
        Nota4 = new javax.swing.JTextField();
        Nota8 = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        agregar_notas = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jTextField12 = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        Factor = new javax.swing.JTextField();
        jPanel9 = new javax.swing.JPanel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        tablaNotas = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();

        jTextField4.setText("jTextField4");

        jButton2.setText("jButton2");

        jLabel24.setText("jLabel24");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new java.awt.CardLayout());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jPanel5.setBackground(new java.awt.Color(224, 225, 255));
        jPanel5.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel1.setToolTipText("");
        jLabel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        combo_pais.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Argentina", "Bolivia", "Brasil", "Chile", "Colombia", "EEUU" }));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setText("Paises: ");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(combo_pais, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(59, 59, 59)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(combo_pais, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(31, Short.MAX_VALUE))
        );

        jPanel6.setBackground(new java.awt.Color(224, 225, 255));
        jPanel6.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel3.setText("MEDALLAS ");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel4.setText("ORO");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel5.setText("PLATA");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel6.setText("BRONCE");

        numOro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numOroActionPerformed(evt);
            }
        });

        jButton1.setText("Modificar");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jButton1MousePressed(evt);
            }
        });

        jButton4.setText("Agregar");
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jButton4MousePressed(evt);
            }
        });

        jButton3.setText("Eliminar");
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jButton3MousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3))
                        .addGap(126, 126, 126)
                        .addComponent(jLabel5)
                        .addGap(106, 106, 106)
                        .addComponent(jLabel6))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGap(66, 66, 66)
                                .addComponent(numOro, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(69, 69, 69)
                                .addComponent(numPlata, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGap(85, 85, 85)
                                .addComponent(jButton4)
                                .addGap(88, 88, 88)
                                .addComponent(jButton1)))
                        .addGap(73, 73, 73)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(numBronce, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(jButton3)))))
                .addContainerGap(50, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(72, 72, 72)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(jLabel5)
                            .addComponent(jLabel4)))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(jLabel3)))
                .addGap(21, 21, 21)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(numOro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(numPlata, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(numBronce, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton4)
                    .addComponent(jButton1)
                    .addComponent(jButton3))
                .addContainerGap(55, Short.MAX_VALUE))
        );

        tablaMedallas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4", "Title 5"
            }
        ));
        jScrollPane1.setViewportView(tablaMedallas);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Medallas", jPanel2);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jPanel8.setBackground(new java.awt.Color(224, 225, 255));
        jPanel8.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setText("Nombre");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel8.setText("País");

        textNombre.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textNombreActionPerformed(evt);
            }
        });

        Nota6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Nota6ActionPerformed(evt);
            }
        });

        Nota3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Nota3ActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel9.setText("Nota 1");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel10.setText("Nota 5");

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel11.setText("Nota 2");

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel12.setText("Nota 6");

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel13.setText("Nota 3");

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel14.setText("Nota 7");

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel15.setText("Nota 8");

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel16.setText("Nota 4");

        agregar_notas.setText("Agregar");
        agregar_notas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                agregar_notasMouseClicked(evt);
            }
        });

        jButton6.setText("Calcular");
        jButton6.
        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel17.setText("Factor");

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel18.setText("N.F");

        Factor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FactorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(124, 124, 124)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Nota1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Nota5, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Nota2, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel12))
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Nota3, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel14)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(Nota7, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(Nota6, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Nota4, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(29, 29, 29)
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Nota8, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(agregar_notas)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel18)
                        .addGap(18, 18, 18)
                        .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(78, 78, 78))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addGap(27, 27, 27)
                                .addComponent(jLabel7))
                            .addComponent(textNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addGap(33, 33, 33)
                                .addComponent(textPais, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addGap(65, 65, 65)
                                .addComponent(jLabel8)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 135, Short.MAX_VALUE)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel17)
                                .addGap(82, 82, 82))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                                .addComponent(Factor, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(62, 62, 62))))))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addComponent(jLabel17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textPais, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Factor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(Nota5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel10)
                        .addComponent(Nota1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(Nota6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(Nota2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel12)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Nota3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Nota7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Nota4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Nota8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 43, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(agregar_notas)
                    .addComponent(jButton6)
                    .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18))
                .addGap(62, 62, 62))
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        tablaNotas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4", "Title 5", "Title 6", "Title 7", "Title 8", "Title 9"
            }
        ));
        jScrollPane3.setViewportView(tablaNotas);

        jTabbedPane2.addTab("tab2", jScrollPane3);

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4", "Title 5", "Title 6", "Title 7", "Title 8", "Title 9", "Title 10", "Title 11", "Title 12", "Title 13"
            }
        ));
        jScrollPane2.setViewportView(jTable2);

        jTabbedPane2.addTab("tab1", jScrollPane2);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 558, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Natacion", jPanel3);

        jPanel10.setBackground(new java.awt.Color(224, 225, 255));
        jPanel10.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel19.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel19.setText("JUEGOS PANAMERICANOS 2023");

        jLabel20.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel20.setText("1.");

        jLabel21.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel21.setText("2.");

        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel22.setText("3.");

        jLabel23.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel23.setText("jLabel23");

        jLabel25.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel25.setText("jLabel25");

        jLabel26.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel26.setText("jLabel26");

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(jLabel19))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGap(125, 125, 125)
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel22, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(29, 29, 29)
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel23)
                            .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel26))))
                .addContainerGap(165, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(jLabel19)
                .addGap(83, 83, 83)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(jLabel25, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE))
                .addGap(9, 9, 9)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(jLabel26))
                .addContainerGap(253, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Final", jPanel4);

        jPanel1.add(jTabbedPane1, "card2");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MousePressed
        modificar();
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1MousePressed

    private void jButton3MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MousePressed
        borrarTodo();
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton3MousePressed

    private void jButton4MousePressed(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jButton4MousePressed
        agregar();
    }

    private void numOroActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_numOroActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_numOroActionPerformed

    private void Nota6ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_Nota6ActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_Nota6ActionPerformed

    private void textNombreActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_textNombreActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_textNombreActionPerformed

    private void Nota3ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_Nota3ActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_Nota3ActionPerformed


    private void FactorActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_FactorActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_FactorActionPerformed

    private void agregar_notasMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_agregar_notasMouseClicked
        agregarNotas();
    }// GEN-LAST:event_agregar_notasMouseClicked

    String validacionNumerica( String validacion1, String validacion2, String validacion3) {
        System.out.println(validacion1);
        System.out.println(validacion2);
        System.out.println(validacion3);
        if ( validacion1 == "" || validacion2 == "" || validacion3 == "") {

            return "Problemas en el codigo.";
        } else {
            return "Todo bien";
        }
    }

   

    // -----------------------------------------------Botones Medallas -------------------------------------------//

    // Funcion para el boton agregar de la ventana 1
    void agregar() {
        // System.out.println("Boton agregar precionado");
        System.out.println("Boton borrar precionado");
        System.out.println(con);
        System.out.println(this.con);
        try {
            String pais = combo_pais.getItemAt(combo_pais.getSelectedIndex());
            Conexion bd = new Conexion("panamericanos");
            this.con = bd.getConnection();
            ResultSet rs_view = read("medallas", this.con);

            while (rs_view.next()) {
                
                if (pais.equals(rs_view.getString("nombre"))) {

                    System.out.println("Pais repetido");
                    throw new Exception("Pais repetido");
                }
            } 

            int numero_oro = Integer.parseInt(numOro.getText());
            int numero_plata = Integer.parseInt(numPlata.getText());
            int numero_bronce = Integer.parseInt(numBronce.getText());
            int total = numero_oro + numero_plata + numero_bronce;

            Object[] valores = { pais, numero_oro, numero_plata, numero_bronce, total };
            create("medallas", valores);

            borrarTabla();

            ResultSet rs = read("medallas", this.con);

            while (rs.next()) {

                int id = rs.getInt(1);
                String nombre = rs.getString("nombre");
                int oro = rs.getInt("oro");
                int plata = rs.getInt("plata");
                int bronce = rs.getInt("bronce");
                int total_ = rs.getInt("total");

                this.dtm.addRow(new Object[] {
                        nombre, oro, plata, bronce, total_
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.out.println("Error de formato numérico: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error general: " + e.getMessage());
        }
    }
    void borrarTabla(){
        int rowCount = dtm.getRowCount();
        for (int i = rowCount - 1; i >= 0; i--) {
            dtm.removeRow(i);
        }
    }
    void borrarTodo() {
        
        // Borrar todas las filas de la interfaz
        int rowCount = dtm.getRowCount();
        for (int i = rowCount - 1; i >= 0; i--) {
            dtm.removeRow(i);
        }
        try {
            System.out.println("Se ejecuta");
            deleteAll("medallas");
            System.out.println("Se ejecuta");
            System.out.println(this.con);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Funcion para el boton modificar de la ventana 1
    void modificar() {
        // Obtener la fila seleccionada en la interfaz
        
        String numeroString = numOro.getText();
        String numeroString1 = numPlata.getText();
        String numeroString2 = numBronce.getText();
        String pais = combo_pais.getItemAt(combo_pais.getSelectedIndex());
        int fila = -1;
        int numero_filas = tablaMedallas.getRowCount();
        for (int i = 0; i < numero_filas; i++){
            if ( pais.equals(tablaMedallas.getValueAt(i, 0))){
                fila = i;
                break;
            }
        }
        
        if (validacionNumerica( numeroString, numeroString1, numeroString2) != "Problemas en el codigo.") {

            // Obtener los nuevos valores desde los campos de texto
            int numero_oro = Integer.parseInt(numeroString);
            int numero_plata = Integer.parseInt(numeroString1);
            int numero_bronce = Integer.parseInt(numeroString2);
            System.out.println("Numero_oro: " + numero_oro);
            System.out.println("Numero_plata: " + numero_plata);
            System.out.println("Numero_bronce: " + numero_bronce);
            System.out.println("Numero_FILA" + fila);

            // Actualizar las filas en la interfaz
            dtm.setValueAt(numero_oro, fila, 1);
            dtm.setValueAt(numero_plata, fila, 2);
            dtm.setValueAt(numero_bronce, fila, 3);
            int total = numero_oro + numero_plata + numero_bronce;
            dtm.setValueAt(total, fila, 4);

            // Actualizar los datos en la base de datos
            try {
                System.out.println("Cago 1");

              
                System.out.println("Cago 2");

                // Actualizar los datos en la base de datos
                System.out.println(pais);
                Object[] nuevosValores = { numero_oro, numero_plata, numero_bronce, total};
                update_medallas("medallas", nuevosValores, pais);
            } catch (NumberFormatException e) {
                System.out.println("Error de formato numérico: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error general: " + e.getMessage());
            }
        } else {
            System.out.println("Problemas en el codigo.");
        }
    }

    // ---------------------------------------------- Botones Notas -------------------------------------------//

    // Funcion para el boton para agregar notas de la ventana 2
    void agregarNotas() {

        String nombre_atleta = textNombre.getText();
        String pais_atleta = textPais.getText();

        float factor_notas = Float.parseFloat(Factor.getText().replace(',', '.'));
        float nota_1 = Float.parseFloat(Nota1.getText().replace(',', '.'));
        float nota_2 = Float.parseFloat(Nota2.getText().replace(',', '.'));
        float nota_3 = Float.parseFloat(Nota3.getText().replace(',', '.'));
        float nota_4 = Float.parseFloat(Nota4.getText().replace(',', '.'));
        float nota_5 = Float.parseFloat(Nota5.getText().replace(',', '.'));
        float nota_6 = Float.parseFloat(Nota6.getText().replace(',', '.'));
        float nota_7 = Float.parseFloat(Nota7.getText().replace(',', '.'));
        float nota_8 = Float.parseFloat(Nota8.getText().replace(',', '.'));
        float valor_nota_final = 0;

        try {
            Object[] notas_natacion = { nombre_atleta, pais_atleta };
            create("notas", notas_natacion);
            System.out.println(read_last_id("notas", "id"));
            ResultSet rs = read("notas", this.con);
            while (rs.next()) {
                int id = rs.getInt(1);
                String nombre_a = rs.getString("nomAtleta");
                String nombre_p = rs.getString("nomPais");

                float nota1 = rs.getFloat("nota1");
                float nota2 = rs.getFloat("nota2");
                float nota3 = rs.getFloat("nota3");
                float nota4 = rs.getFloat("nota4");
                float nota5 = rs.getFloat("nota5");
                float nota6 = rs.getFloat("nota6");
                float nota7 = rs.getFloat("nota7");
                float nota8 = rs.getFloat("nota8");
                float factor = rs.getFloat("factor");

                this.nat.addRow(new Object[] {
                        nombre_a, nombre_p, nota1, nota2, nota3, nota4, nota5, nota6, nota7, nota8, factor

                });

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    };

    // Funcion para el boton para calcular de la ventana 2 // Bien
    void calcular() {
        try {
            int fila = tablaNotas.getSelectedRow();

            float factor_notas = Float.parseFloat(tablaNotas.getValueAt(fila, 10).toString());

            float nota_1 = Float.parseFloat(tablaNotas.getValueAt(fila, 2).toString());
            float nota_2 = Float.parseFloat(tablaNotas.getValueAt(fila, 3).toString());
            float nota_3 = Float.parseFloat(tablaNotas.getValueAt(fila, 4).toString());
            float nota_4 = Float.parseFloat(tablaNotas.getValueAt(fila, 5).toString());
            float nota_5 = Float.parseFloat(tablaNotas.getValueAt(fila, 6).toString());
            float nota_6 = Float.parseFloat(tablaNotas.getValueAt(fila, 7).toString());
            float nota_7 = Float.parseFloat(tablaNotas.getValueAt(fila, 8).toString());
            float nota_8 = Float.parseFloat(tablaNotas.getValueAt(fila, 9).toString());

            List<Float> lista_notas = new ArrayList<>();
            lista_notas.add(nota_1);
            lista_notas.add(nota_2);
            lista_notas.add(nota_3);
            lista_notas.add(nota_4);
            lista_notas.add(nota_5);
            lista_notas.add(nota_6);
            lista_notas.add(nota_7);
            lista_notas.add(nota_8);

            // Eliminar las primeras dos notas y las últimas dos notas
            Collections.sort(lista_notas);

            // Eliminar las primeras dos notas y las últimas dos notas
            lista_notas.subList(0, 2).clear();
            lista_notas.subList(lista_notas.size() - 2, lista_notas.size()).clear();

            float suma = 0;
            for (Float nota : lista_notas) {
                suma += nota;
            }
            // Multiplica la suma por el factor_notas
            float resultado = suma * factor_notas;
            nat.setValueAt(resultado, fila, 11);

            // System.out.println(lista_notas);

        } catch (Exception e) {
            System.out.println("la entrada no es un numero valido");
        }
    }
    public static void main(String args[]) {
        // Object[] valores = {"Chile", 16, 20, 30, 66};
        // crud.create("medallas", valores);
        CRUD crud = new CRUD();

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                crud.setVisible(true);
                // ResulSet rs = read();

            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField Factor;
    private javax.swing.JTextField Nota1;
    private javax.swing.JTextField Nota2;
    private javax.swing.JTextField Nota3;
    private javax.swing.JTextField Nota4;
    private javax.swing.JTextField Nota5;
    private javax.swing.JTextField Nota6;
    private javax.swing.JTextField Nota7;
    private javax.swing.JTextField Nota8;
    private javax.swing.JButton agregar_notas;
    private javax.swing.JComboBox<String> combo_pais;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextField jTextField12;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField numBronce;
    private javax.swing.JTextField numOro;
    private javax.swing.JTextField numPlata;
    private javax.swing.JTable tablaMedallas;
    private javax.swing.JTable tablaNotas;
    private javax.swing.JTextField textNombre;
    private javax.swing.JTextField textPais;
    // End of variables declaration//GEN-END:variables
}
