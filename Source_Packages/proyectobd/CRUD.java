package Source_Packages.proyectobd;

import java.sql.*;

public class CRUD {
    private Connection con;

    public CRUD() {
        this.con = Conexion.getConnection();
    }

    private static String removeIdColumn(String columns) {
        String[] parts = columns.split(",\\s*");
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

    public ResultSet read(String tableName) throws SQLException {
        String sql = "SELECT * FROM " + tableName;
        Statement st = con.createStatement();
        return st.executeQuery(sql);
    }

    public void create(String tableName, Object[] values) {
        try {
            String columns = ColumnNameFetcher.getColumnNames(con, tableName);
            columns = removeIdColumn(columns);
            String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (";

            for (int i = 0; i < values.length; i++) {
                sql += (i == 0) ? "?" : ", ?";
            }
            sql += ")";

            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                for (int i = 0; i < values.length; i++) {
                    pstmt.setObject(i + 1, values[i]);
                }
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Los métodos delete y update se pueden implementar de manera similar
    public void delete(String tableName, int id){
        String sql = String.format("SELECT * FROM %s where id = ?");
        try (PreparedStatement preparedStatement = con.prepareStatement(sql)){
            preparedStatement.setInt(1,id);

        } catch (SQLException e){
            // e.printStackTrace();
        } finally {

        }

    }
    public static void main(String[] args) {
        CRUD crud = new CRUD();
        Object[] valores = {"Argentina", 10, 5, 2, 17};

        try {
            crud.read("tabla_medallas");
            crud.create("tabla_medallas", valores);
            ResultSet rs = crud.read("tabla_medallas");

            while (rs.next()) {
                // Procesar y mostrar los resultados como sea necesario
                int id = rs.getInt(1);
                String nombre = rs.getString(2);
                int oro = rs.getInt(3);
                int plata = rs.getInt(4);
                int bronce = rs.getInt(5);
                int total = rs.getInt(6);

                String format = String.format("ID: %d ", id);
                String format1 = String.format("Nombre: %s ", nombre);
                String format2 = String.format("oro: %d ", oro);
                String format3 = String.format("plata: %d ", plata);
                String format4 = String.format("bronce: %d ", bronce);
                String format5 = String.format("total: %d ", total);

                System.out.println();
                System.out.print(format);
                System.out.println(format1);
                System.out.print(format2);
                System.out.print(format3);
                System.out.print(format4);
                System.out.print(format5);
                System.out.println();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Conexion.closeConnection(crud.con);
        }
    }
}
