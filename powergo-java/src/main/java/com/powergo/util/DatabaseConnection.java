package com.powergo.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestor de conexiones a la base de datos MySQL por medio de JDBC.
 * Implementa el patrón Singleton para la obtención de conexiones seguras.
 */
public class DatabaseConnection {

    // Parámetros de conexión al MySQL local de Laragon
    private static final String URL = "jdbc:mysql://localhost:3306/powergo_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Configuración sin contraseña estándar en Laragon

    private static Connection connection = null;

    // Constructor privado para evitar instanciación externa
    private DatabaseConnection() {
    }

    /**
     * Obtiene una conexión activa a la base de datos powergo_db.
     * @return Connection objeto de conexión JDBC
     * @throws SQLException si ocurre un error al conectar
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Cargar el driver JDBC de MySQL (com.mysql.cj.jdbc.Driver)
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Advertencia: Driver MySQL CJ no encontrado en classpath: " + e.getMessage());
        }

        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    /**
     * Cierra la conexión activa si existe.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }
}
