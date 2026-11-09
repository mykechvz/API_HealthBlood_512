/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.lasalle.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import org.lasalle.model.ConnectionMysql;
import org.lasalle.model.Usuario;
import java.sql.SQLException;
import java.sql.ResultSet;

/**
 *
 * @author mykec
 */
public class ControllerUsuario {

    public List<Usuario> getUsuarios() throws SQLException {
        List<Usuario> lista = new ArrayList<>();

        String sql = "SELECT idUsuario, nombre, primerApellido, segundoApellido, email "
                + "FROM usuario ORDER BY idUsuario";

        ConnectionMysql connMysql = new ConnectionMysql();
        Connection conn = connMysql.open();
        if (conn == null) {
            throw new SQLException("No se pudo conectar a la base de datos");
        }

        try (PreparedStatement pstm = conn.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery()) {
            while (rs.next()) {
                lista.add(fillUsuario(rs));
            }
        } finally {
            connMysql.close();
        }

        return lista;
    }

    public int insertUsuario(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuario (nombre, primerApellido, segundoApellido, email) "
                + "VALUES (?, ?, ?, ?)";

        ConnectionMysql connMysql = new ConnectionMysql();
        Connection conn = connMysql.open();
        if (conn == null) {
            throw new SQLException("No se pudo conectar a la base de datos");
        }

        PreparedStatement pstm = null;
        ResultSet rs = null;
        int idGenerado = 0;
        try {
            pstm = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            pstm.setString(1, usuario.getNombre());
            pstm.setString(2, usuario.getPrimerApellido());
            if (usuario.getSegundoApellido() == null || usuario.getSegundoApellido().isBlank()) {
                pstm.setNull(3, java.sql.Types.VARCHAR);
            } else {
                pstm.setString(3, usuario.getSegundoApellido());
            }
            pstm.setString(4, usuario.getEmail());

            if (pstm.executeUpdate() == 0) {
                throw new SQLException("No se pudo insertar el usuario");
            }

            rs = pstm.getGeneratedKeys();
            if (rs.next()) {
                idGenerado = rs.getInt(1);
            }
        } finally {
            if (rs != null) rs.close();
            if (pstm != null) pstm.close();
            connMysql.close();
        }
        return idGenerado;
    }

    public Usuario fillUsuario(ResultSet rs) throws SQLException {
        Usuario user = new Usuario();
        user.setIdUsuario(rs.getInt("idUsuario"));
        user.setNombre(rs.getString("nombre"));
        user.setPrimerApellido(rs.getString("primerApellido"));
        user.setSegundoApellido(rs.getString("segundoApellido"));
        user.setEmail(rs.getString("email"));
        return user;
    }

}
