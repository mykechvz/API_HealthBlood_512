/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.lasalle.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.lasalle.model.ConnectionMysql;
import org.lasalle.model.Medida;

/**
 * Operaciones de base de datos para la entidad Medida.
 *
 * @author mykec
 */
public class ControllerMedida {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Devuelve todas las medidas, de la mas reciente a la mas antigua.
     */
    public List<Medida> getAllMedidas() throws SQLException {
        String sql = "SELECT idMedida, idUsuario, glucosa, "
                + "DATE_FORMAT(fechaHora, '%Y-%m-%d %H:%i:%s') AS fechaHora, contexto, notas "
                + "FROM medida ORDER BY fechaHora DESC, idMedida DESC";

        List<Medida> lista = new ArrayList<>();

        ConnectionMysql connMysql = new ConnectionMysql();
        Connection conn = connMysql.open();
        if (conn == null) {
            throw new SQLException("No se pudo conectar a la base de datos");
        }

        try (PreparedStatement pstm = conn.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery()) {
            while (rs.next()) {
                lista.add(fillMedida(rs));
            }
        } finally {
            connMysql.close();
        }

        return lista;
    }

    /**
     * Devuelve las medidas de un usuario, de la mas reciente a la mas antigua.
     */
    public List<Medida> getMedidasByUsuarioId(int idUsuario) throws SQLException {
        String sql = "SELECT idMedida, idUsuario, glucosa, "
                + "DATE_FORMAT(fechaHora, '%Y-%m-%d %H:%i:%s') AS fechaHora, contexto, notas "
                + "FROM medida WHERE idUsuario = ? ORDER BY fechaHora DESC, idMedida DESC";

        List<Medida> lista = new ArrayList<>();

        ConnectionMysql connMysql = new ConnectionMysql();
        Connection conn = connMysql.open();
        if (conn == null) {
            throw new SQLException("No se pudo conectar a la base de datos");
        }

        try (PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setInt(1, idUsuario);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    lista.add(fillMedida(rs));
                }
            }
        } finally {
            connMysql.close();
        }

        return lista;
    }

    /**
     * Inserta una medida. Si no se envia fechaHora se usa la fecha y hora actual.
     *
     * @return id autogenerado de la medida insertada.
     */
    public int insertMedida(Medida medida) throws SQLException {
        String fechaHora = medida.getFechaHora();
        if (fechaHora == null || fechaHora.trim().isEmpty()) {
            fechaHora = LocalDateTime.now().format(FORMATO_FECHA);
            medida.setFechaHora(fechaHora);
        }

        String sql = "INSERT INTO medida (idUsuario, glucosa, fechaHora, contexto, notas) "
                + "VALUES (?, ?, ?, ?, ?)";

        ConnectionMysql connMysql = new ConnectionMysql();
        Connection conn = connMysql.open();
        if (conn == null) {
            throw new SQLException("No se pudo conectar a la base de datos");
        }

        try (PreparedStatement pstm = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstm.setInt(1, medida.getIdUsuario());
            pstm.setInt(2, medida.getGlucosa());
            pstm.setString(3, fechaHora);
            pstm.setString(4, medida.getContexto());
            pstm.setString(5, medida.getNotas());

            if (pstm.executeUpdate() == 0) {
                throw new SQLException("No se pudo insertar la medida");
            }

            int idGenerado = 0;
            try (ResultSet rs = pstm.getGeneratedKeys()) {
                if (rs.next()) {
                    idGenerado = rs.getInt(1);
                    medida.setIdMedida(idGenerado);
                }
            }
            return idGenerado;
        } finally {
            connMysql.close();
        }
    }

    /**
     * Convierte una fila del ResultSet en un objeto Medida.
     */
    public Medida fillMedida(ResultSet rs) throws SQLException {
        Medida m = new Medida();
        m.setIdMedida(rs.getInt("idMedida"));
        m.setIdUsuario(rs.getInt("idUsuario"));
        m.setGlucosa(rs.getInt("glucosa"));
        m.setFechaHora(rs.getString("fechaHora"));
        m.setContexto(rs.getString("contexto"));
        m.setNotas(rs.getString("notas"));
        return m;
    }
}
