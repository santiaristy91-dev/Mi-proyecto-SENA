package com.powergo.dao;

import com.powergo.model.Usuario;
import com.powergo.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) para la entidad Usuario.
 * Contiene todas las operaciones CRUD (Create, Read, Update, Delete)
 * utilizando JDBC con PreparedStatement para prevenir inyecciones SQL.
 */
public class UsuarioDAO {

    /**
     * OPERACIÓN C: INSERTAR (Crear un nuevo usuario)
     * @param usuario Datos del usuario a registrar
     * @return boolean true si fue exitoso, false en caso contrario
     */
    public boolean insertar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nombre_completo, email, telefono, password_hash, rol, saldo_cuenta, estado) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, usuario.getNombreCompleto());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getTelefono());
            ps.setString(4, usuario.getPasswordHash());
            ps.setString(5, usuario.getRol());
            ps.setDouble(6, usuario.getSaldoCuenta());
            ps.setString(7, usuario.getEstado());

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        usuario.setIdUsuario(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar usuario: " + e.getMessage());
        }
        return false;
    }

    /**
     * OPERACIÓN R: CONSULTAR POR ID
     * @param idUsuario Identificador único del usuario
     * @return Usuario encontrado o null
     */
    public Usuario obtenerPorId(int idUsuario) {
        String sql = "SELECT * FROM usuarios WHERE id_usuario = ?";
        Usuario usuario = null;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    usuario = mapearUsuario(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar usuario por ID: " + e.getMessage());
        }
        return usuario;
    }

    /**
     * OPERACIÓN R: CONSULTAR POR EMAIL
     * @param email Correo electrónico a buscar
     * @return Usuario encontrado o null
     */
    public Usuario obtenerPorEmail(String email) {
        String sql = "SELECT * FROM usuarios WHERE email = ?";
        Usuario usuario = null;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    usuario = mapearUsuario(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar usuario por email: " + e.getMessage());
        }
        return usuario;
    }

    /**
     * OPERACIÓN R: LISTAR TODOS LOS USUARIOS
     * @return Lista de todos los usuarios registrados
     */
    public List<Usuario> listarTodos() {
        String sql = "SELECT * FROM usuarios ORDER BY id_usuario ASC";
        List<Usuario> lista = new ArrayList<>();

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearUsuario(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
        }
        return lista;
    }

    /**
     * OPERACIÓN U: ACTUALIZAR DATOS DE UN USUARIO
     * @param usuario Objeto con la información modificada
     * @return boolean true si se actualizó al menos un registro
     */
    public boolean actualizar(Usuario usuario) {
        String sql = "UPDATE usuarios SET nombre_completo = ?, email = ?, telefono = ?, " +
                     "rol = ?, saldo_cuenta = ?, estado = ? WHERE id_usuario = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario.getNombreCompleto());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getTelefono());
            ps.setString(4, usuario.getRol());
            ps.setDouble(5, usuario.getSaldoCuenta());
            ps.setString(6, usuario.getEstado());
            ps.setInt(7, usuario.getIdUsuario());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
        }
        return false;
    }

    /**
     * OPERACIÓN D: ELIMINAR UN USUARIO
     * @param idUsuario Identificador del usuario a eliminar
     * @return boolean true si se eliminó con éxito
     */
    public boolean eliminar(int idUsuario) {
        String sql = "DELETE FROM usuarios WHERE id_usuario = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
        }
        return false;
    }

    // Método auxiliar privado para convertir fila de ResultSet a objeto Usuario
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setNombreCompleto(rs.getString("nombre_completo"));
        u.setEmail(rs.getString("email"));
        u.setTelefono(rs.getString("telefono"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRol(rs.getString("rol"));
        u.setSaldoCuenta(rs.getDouble("saldo_cuenta"));
        u.setEstado(rs.getString("estado"));
        u.setFechaRegistro(rs.getTimestamp("fecha_registro"));
        u.setFechaActualizacion(rs.getTimestamp("fecha_actualizacion"));
        return u;
    }
}
