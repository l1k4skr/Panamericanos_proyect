package Source_Packages.proyectobd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    // Parámetros de conexión
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/panamericanos?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "Andres123ful";

    // Método para obtener la conexión
    public static Connection getConnection() {
        Connection con = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Conexion exitosa");
        } catch (ClassNotFoundException e) {
            System.out.println("Error al cargar el driver: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error al conectar: " + e.getMessage());
        }

        return con;
    }

    // Método para cerrar la conexión (opcional pero recomendado)
    public static void closeConnection(Connection con) {
        if (con != null) {
            try {
                con.close();
                System.out.println("Conexion cerrada");
            } catch (SQLException e) {
                System.out.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }

    // public static void main(String[] args) {
    //     // Test de la conexión
    //     Connection testCon = getConnection();
    //     closeConnection(testCon);
    // }
}
