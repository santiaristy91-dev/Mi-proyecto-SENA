# PowerGo - Módulo de Codificación en Java con JDBC
**Evidencia SENA:** GA7-220501096-AA2-EV01  
**Componente Formativo:** Construcción de aplicaciones con JAVA  
**Aprendiz:** Santiago Aristizabal Escobar - Ficha 3336153  

---

## 1. Arquitectura del Módulo
El proyecto se diseñó bajo una **arquitectura por capas desacoplada** con alta cohesión y bajo acoplamiento:
- `com.powergo.model`: Clases de entidad (POJO / JavaBeans) que representan las tablas de la base de datos (`Usuario`, `Estacion`).
- `com.powergo.util`: Gestor de conexión segura a la base de datos MySQL mediante JDBC (`DatabaseConnection`).
- `com.powergo.dao`: Clases de acceso a datos (*Data Access Object*) que encapsulan las sentencias SQL preparadas (`PreparedStatement`) para el CRUD (`UsuarioDAO`, `EstacionDAO`).
- `com.powergo.MainApp`: Clase principal con interfaz de consola interactiva y rutina automatizada de pruebas CRUD.

## 2. Requisitos Previos
1. Base de datos MySQL en ejecución (Laragon o servicio local en puerto 3306).
2. Base de datos `powergo_db` creada con el script SQL adjunto.
3. Conector JDBC de MySQL (`mysql-connector-j-8.x.jar`).

## 3. Operaciones CRUD Implementadas
- **Inserción (Create):** `usuarioDAO.insertar(usuario)` con obtención de claves autogeneradas.
- **Consulta (Read):** `usuarioDAO.obtenerPorId(id)`, `obtenerPorEmail(email)` y `listarTodos()`.
- **Actualización (Update):** `usuarioDAO.actualizar(usuario)` para cambios de perfil y saldo.
- **Eliminación (Delete):** `usuarioDAO.eliminar(id)` para descarte seguro de registros.

## 4. Repositorio Remoto en GitHub
- Repositorio oficial: https://github.com/santiarist91-dev/Mi-proyecto-SENA
