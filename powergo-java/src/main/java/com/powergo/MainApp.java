package com.powergo;

import com.powergo.dao.EstacionDAO;
import com.powergo.dao.UsuarioDAO;
import com.powergo.model.Estacion;
import com.powergo.model.Usuario;

import java.util.List;
import java.util.Scanner;

/**
 * Clase principal del módulo de software PowerGo en Java.
 * Presenta una interfaz de consola interactiva para probar y verificar
 * las operaciones de Base de Datos mediante JDBC (CRUD completo).
 */
public class MainApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        EstacionDAO estacionDAO = new EstacionDAO();

        System.out.println("====================================================");
        System.out.println("   SISTEMA DE GESTION POWERGO - MODULO JAVA (JDBC)   ");
        System.out.println("      Evidencia GA7-220501096-AA2-EV01 (SENA ADSO)   ");
        System.out.println("====================================================");

        boolean salir = false;

        while (!salir) {
            System.out.println("\n--- MENU DE GESTION MODULAR ---");
            System.out.println("1. [C] Crear / Registrar nuevo usuario");
            System.out.println("2. [R] Consultar todos los usuarios");
            System.out.println("3. [R] Consultar usuario por ID");
            System.out.println("4. [U] Actualizar saldo y datos de usuario");
            System.out.println("5. [D] Eliminar un usuario");
            System.out.println("6. [R] Consultar estaciones dispensadoras");
            System.out.println("7. [TEST] Ejecutar prueba integral CRUD automática");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opción: ");

            String opcion = scanner.nextLine().trim();

            switch (opcion) {
                case "1":
                    registrarUsuarioInteractivo(scanner, usuarioDAO);
                    break;
                case "2":
                    listarUsuarios(usuarioDAO);
                    break;
                case "3":
                    consultarUsuarioPorId(scanner, usuarioDAO);
                    break;
                case "4":
                    actualizarUsuarioInteractivo(scanner, usuarioDAO);
                    break;
                case "5":
                    eliminarUsuarioInteractivo(scanner, usuarioDAO);
                    break;
                case "6":
                    listarEstaciones(estacionDAO);
                    break;
                case "7":
                    ejecutarPruebaAutomatica(usuarioDAO, estacionDAO);
                    break;
                case "0":
                    salir = true;
                    System.out.println("Saliendo del sistema PowerGo. ¡Hasta pronto!");
                    break;
                default:
                    System.out.println("Opción no válida. Intente de nuevo.");
            }
        }
        scanner.close();
    }

    private static void registrarUsuarioInteractivo(Scanner scanner, UsuarioDAO dao) {
        System.out.println("\n--- REGISTRAR NUEVO USUARIO ---");
        System.out.print("Nombre completo: ");
        String nombre = scanner.nextLine();
        System.out.print("Correo electrónico: ");
        String email = scanner.nextLine();
        System.out.print("Teléfono: ");
        String tel = scanner.nextLine();
        System.out.print("Contraseña: ");
        String pass = scanner.nextLine();

        Usuario nuevo = new Usuario(nombre, email, tel, pass, "cliente", 10000.0, "activo");
        boolean exito = dao.insertar(nuevo);
        if (exito) {
            System.out.println("✅ Usuario registrado exitosamente con ID: " + nuevo.getIdUsuario());
        } else {
            System.out.println("❌ Error al registrar usuario (verifique si el correo ya existe o la BD está activa).");
        }
    }

    private static void listarUsuarios(UsuarioDAO dao) {
        System.out.println("\n--- LISTADO DE USUARIOS REGISTRADOS ---");
        List<Usuario> lista = dao.listarTodos();
        if (lista.isEmpty()) {
            System.out.println("No hay usuarios registrados en el sistema.");
        } else {
            for (Usuario u : lista) {
                System.out.println(u);
            }
        }
    }

    private static void consultarUsuarioPorId(Scanner scanner, UsuarioDAO dao) {
        System.out.print("Ingrese el ID del usuario: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            Usuario u = dao.obtenerPorId(id);
            if (u != null) {
                System.out.println("✅ Usuario encontrado:\n" + u);
            } else {
                System.out.println("⚠️ No existe ningún usuario con el ID especificado.");
            }
        } catch (NumberFormatException e) {
            System.out.println("ID inválido.");
        }
    }

    private static void actualizarUsuarioInteractivo(Scanner scanner, UsuarioDAO dao) {
        System.out.print("Ingrese el ID del usuario a modificar: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            Usuario u = dao.obtenerPorId(id);
            if (u == null) {
                System.out.println("⚠️ Usuario no encontrado.");
                return;
            }
            System.out.println("Modificando a: " + u.getNombreCompleto());
            System.out.print("Nuevo teléfono (Enter para conservar actual): ");
            String tel = scanner.nextLine();
            if (!tel.trim().isEmpty()) u.setTelefono(tel);

            System.out.print("Nuevo saldo en cuenta (Enter para conservar actual): ");
            String saldoStr = scanner.nextLine();
            if (!saldoStr.trim().isEmpty()) u.setSaldoCuenta(Double.parseDouble(saldoStr));

            if (dao.actualizar(u)) {
                System.out.println("✅ Usuario actualizado correctamente.");
            } else {
                System.out.println("❌ Fallo al actualizar el usuario.");
            }
        } catch (Exception e) {
            System.out.println("Error en datos de entrada: " + e.getMessage());
        }
    }

    private static void eliminarUsuarioInteractivo(Scanner scanner, UsuarioDAO dao) {
        System.out.print("Ingrese el ID del usuario a eliminar: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            System.out.print("¿Está seguro de eliminar al usuario " + id + "? (s/n): ");
            String conf = scanner.nextLine();
            if (conf.equalsIgnoreCase("s")) {
                if (dao.eliminar(id)) {
                    System.out.println("✅ Usuario eliminado exitosamente.");
                } else {
                    System.out.println("❌ No se pudo eliminar (verifique ID o dependencias en alquileres).");
                }
            } else {
                System.out.println("Operación cancelada.");
            }
        } catch (NumberFormatException e) {
            System.out.println("ID inválido.");
        }
    }

    private static void listarEstaciones(EstacionDAO dao) {
        System.out.println("\n--- LISTADO DE ESTACIONES DISPENSADORAS ---");
        List<Estacion> estaciones = dao.listarTodas();
        if (estaciones.isEmpty()) {
            System.out.println("No hay estaciones registradas.");
        } else {
            for (Estacion est : estaciones) {
                System.out.println(est);
            }
        }
    }

    private static void ejecutarPruebaAutomatica(UsuarioDAO uDao, EstacionDAO eDao) {
        System.out.println("\n=== INICIANDO PRUEBA INTEGRAL CRUD AUTOMATICA ===");
        
        // 1. Inserción
        System.out.println("1. Probando Inserción (Create)...");
        long timestamp = System.currentTimeMillis();
        Usuario testUser = new Usuario("Aprendiz SENA Test", "test_" + timestamp + "@powergo.com", 
                                       "3009998877", "hash123", "cliente", 25000.0, "activo");
        boolean insertado = uDao.insertar(testUser);
        System.out.println("   Resultado inserción: " + (insertado ? "EXITO (ID=" + testUser.getIdUsuario() + ")" : "FALLO"));

        if (!insertado) return;

        // 2. Consulta
        System.out.println("2. Probando Consulta (Read)...");
        Usuario buscado = uDao.obtenerPorId(testUser.getIdUsuario());
        System.out.println("   Usuario recuperado: " + (buscado != null ? buscado.getNombreCompleto() : "NO ENCONTRADO"));

        // 3. Actualización
        System.out.println("3. Probando Actualización (Update)...");
        testUser.setSaldoCuenta(50000.0);
        testUser.setNombreCompleto("Aprendiz SENA Test - Modificado");
        boolean actualizado = uDao.actualizar(testUser);
        System.out.println("   Resultado actualización: " + (actualizado ? "EXITO (Nuevo saldo=$50000)" : "FALLO"));

        // 4. Eliminación
        System.out.println("4. Probando Eliminación (Delete)...");
        boolean eliminado = uDao.eliminar(testUser.getIdUsuario());
        System.out.println("   Resultado eliminación: " + (eliminado ? "EXITO" : "FALLO"));

        System.out.println("=== FIN DE PRUEBA CRUD: TODAS LAS OPERACIONES COMPLETADAS ===");
    }
}
