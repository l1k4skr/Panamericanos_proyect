/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package juegospanamericanos;

//import java.lang.System.Logger;
//import java.lang.System.Logger.Level;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class Conexion {
    String bd ="panamericanos";
    String url="jdbc:mysql://localhost:3306/ ";
    String user="root";
    String password="";
    String driver="com.mysql.cj.jdbc.Driver";
    Connection con;

    public Conexion(String bd){
        this.bd = bd;
        try{
            Class.forName(driver);
            con=DriverManager.getConnection(url+this.bd,user,password);
            System.out.println("Se conecto a "+bd);
        } catch (Exception ex){
            //java.util.logging.Logger.getLogger(Conexion.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            System.out.println("No se conecto a "+bd+ " " + ex);
        }
    }
    public Connection getConnection(){
        return con;
    }
    public void desconectar(){
        try {
            con.close();
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(Conexion.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }
    
}


    

