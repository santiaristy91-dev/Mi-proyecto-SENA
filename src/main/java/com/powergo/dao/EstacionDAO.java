package com.powergo.dao;

import com.powergo.model.Estacion;
import com.powergo.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) para la entidad Estación dispensadora.
 * Implementa las operaciones CRUD mediante JDBC.
 */
public class EstacionDAO {

    public boolean insertar(Estacion estacion) {
        String sql = "INSERT INTO estaciones (codigo_estacion, nombre_estacion, direccion, capacidad_total, cargadores_disponibles, estado_operativo) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, estacion.getCodigoEstacion());
            ps.setString(2, estacion.getNombreEstacion());
            ps.setString(3, estacion.getDireccion());
            ps.setInt(4, estacion.getCapacidadTotal());
            ps.setInt(5, estacion.getCargadoresDisponibles());
            ps.setString(6, estacion.getEstadoOperativo());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        estacion.setIdEstacion(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar estación: " + e.getMessage());
        }
        return false;
    }

    public Estacion obtenerPorId(int idEstacion) {
        String sql = "SELECT * FROM estaciones WHERE id_estacion = ?";
        Estacion estacion = null;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idEstacion);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    estacion = mapearEstacion(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener estación: " + e.getMessage());
        }
        return estacion;
    }

    public List<Estacion> listarTodas() {
        String sql = "SELECT * FROM estaciones ORDER BY id_estacion ASC";
        List<Estacion> lista = new ArrayList<>();

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearEstacion(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar estaciones: " + e.getMessage());
        }
        return lista;
    }

    public boolean actualizar(Estacion estacion) {
        String sql = "UPDATE estaciones SET codigo_estacion = ?, nombre_estacion = ?, direccion = ?, " +
                     "capacidad_total = ?, cargadores_disponibles = ?, estado_operativo = ? WHERE id_estacion = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, estacion.getCodigoEstacion());
            ps.setString(2, estacion.getNombreEstacion());
            ps.setString(3, estacion.getDireccion());
            ps.setInt(4, estacion.getCapacidadTotal());
            ps.setInt(5, estacion.getCargadoresDisponibles());
            ps.setString(6, estacion.getEstadoOperativo());
            ps.setInt(7, estacion.getIdEstacion());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar estación: " + e.getMessage());
        }
        return false;
    }

    public boolean eliminar(int idEstacion) {
        String sql = "DELETE FROM estaciones WHERE id_estacion = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idEstacion);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar estación: " + e.getMessage());
        }
        return false;
    }

    private Estacion mapearEstacion(ResultSet rs) throws SQLException {
        Estacion est = new Estacion();
        est.setIdEstacion(rs.getInt("id_estacion"));
        est.setCodigoEstacion(rs.getString("codigo_estacion"));
        est.setNombreEstacion(rs.getString("nombre_estacion"));
        est.setDireccion(rs.getString("direccion"));
        est.setCapacidadTotal(rs.getInt("capacidad_total"));
        est.setCargadoresDisponibles(rs.getInt("cargadores_disponibles"));
        est.setEstadoOperativo(rs.getString("estado_operativo"));
        return est;
    }
}
