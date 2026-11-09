/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.lasalle.model;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Gestor de conexion a MySQL.
 *
 * En local usa los valores por defecto (127.0.0.1:3306 / root / root).
 * En la nube (Render) toma la configuracion de las variables de entorno:
 *
 *   DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
 *   DB_URL  (opcional: cadena JDBC completa, tiene prioridad)
 *   DB_SSL  (opcional: DISABLED, PREFERRED, REQUIRED, VERIFY_IDENTITY)
 *
 * @author mykec
 */
public class ConnectionMysql {

    Connection conn;

    public Connection open() {
        String host = env("DB_HOST", "127.0.0.1");
        String port = env("DB_PORT", "3306");
        String dbName = env("DB_NAME", "gluco_api");
        String user = env("DB_USER", "root");
        String password = env("DB_PASSWORD", "root");

        String customUrl = System.getenv("DB_URL");
        String url;

        if (customUrl != null && !customUrl.trim().isEmpty()) {
            url = customUrl.trim();
        } else {
            boolean isLocal = host.equals("127.0.0.1") || host.equalsIgnoreCase("localhost");
            String sslMode = env("DB_SSL", isLocal ? "DISABLED" : "REQUIRED");
            url = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                    + "?sslMode=" + sslMode
                    + "&allowPublicKeyRetrieval=true"
                    + "&useUnicode=true"
                    + "&characterEncoding=UTF-8"
                    + "&serverTimezone=UTC"
                    + "&connectTimeout=10000";
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, user, password);
            return conn;
        } catch (Exception e) {
            System.err.println("Error al conectar con MySQL: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value == null || value.trim().isEmpty()) ? defaultValue : value.trim();
    }
}
