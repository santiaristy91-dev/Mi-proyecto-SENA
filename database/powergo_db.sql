-- ====================================================================
-- SISTEMA DE GESTIÓN Y ALQUILER DE POWER BANKS - "POWERGO"
-- Script de Base de Datos Relacional para MySQL / MariaDB
-- Elaborado para: PowerGo - Ficha SENA ADSO
-- Autor: Santiago Aristizabal Escobar
-- Motor: InnoDB | Cotejamiento: utf8mb4_unicode_ci
-- ====================================================================

-- 1. CREACIÓN Y SELECCIÓN DE BASE DE DATOS
DROP DATABASE IF EXISTS powergo_db;
CREATE DATABASE powergo_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE powergo_db;

-- ====================================================================
-- 2. DEFINICIÓN DE TABLAS PRINCIPALES (DDL)
-- ====================================================================

-- --------------------------------------------------------------------
-- TABLA: usuarios
-- Administra las cuentas de clientes, administradores y personal técnico
-- --------------------------------------------------------------------
CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo VARCHAR(120) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    telefono VARCHAR(20) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    rol ENUM('cliente', 'administrador', 'tecnico') NOT NULL DEFAULT 'cliente',
    saldo_cuenta DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    estado ENUM('activo', 'inactivo', 'suspendido') NOT NULL DEFAULT 'activo',
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_usuarios_email (email),
    INDEX idx_usuarios_rol (rol)
) ENGINE=InnoDB COMMENT='Cuentas de usuario de la plataforma PowerGo';

-- --------------------------------------------------------------------
-- TABLA: planes
-- Catálogo de planes y tarifas ofertados en la landing page
-- --------------------------------------------------------------------
CREATE TABLE planes (
    id_plan INT AUTO_INCREMENT PRIMARY KEY,
    codigo_plan VARCHAR(30) NOT NULL UNIQUE,
    nombre_plan VARCHAR(80) NOT NULL,
    categoria ENUM('ocasional', 'popular', 'frecuente') NOT NULL DEFAULT 'ocasional',
    precio DECIMAL(10, 2) NOT NULL,
    tipo_cobro ENUM('hora', 'dia', 'mes') NOT NULL,
    horas_incluidas_dia INT DEFAULT 0,
    descripcion TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='Planes y paquetes tarifarios de PowerGo';

-- --------------------------------------------------------------------
-- TABLA: suscripciones_usuario
-- Registra las suscripciones mensuales o pases adquiridos por usuarios
-- --------------------------------------------------------------------
CREATE TABLE suscripciones_usuario (
    id_suscripcion INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    id_plan INT NOT NULL,
    fecha_inicio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_fin DATETIME NOT NULL,
    auto_renovacion BOOLEAN NOT NULL DEFAULT TRUE,
    estado ENUM('activa', 'vencida', 'cancelada') NOT NULL DEFAULT 'activa',
    CONSTRAINT fk_suscripcion_usuario FOREIGN KEY (id_usuario) 
        REFERENCES usuarios(id_usuario) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_suscripcion_plan FOREIGN KEY (id_plan) 
        REFERENCES planes(id_plan) ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_suscripcion_estado (id_usuario, estado)
) ENGINE=InnoDB COMMENT='Historial de membresías y suscripciones activas';

-- --------------------------------------------------------------------
-- TABLA: estaciones
-- Puntos físicos de dispensación de cargadores (centros comerciales, U. de A.)
-- --------------------------------------------------------------------
CREATE TABLE estaciones (
    id_estacion INT AUTO_INCREMENT PRIMARY KEY,
    codigo_estacion VARCHAR(20) NOT NULL UNIQUE,
    nombre_estacion VARCHAR(120) NOT NULL,
    ciudad VARCHAR(60) NOT NULL DEFAULT 'Medellín',
    direccion VARCHAR(180) NOT NULL,
    latitud DECIMAL(10, 8),
    longitud DECIMAL(11, 8),
    capacidad_total INT NOT NULL DEFAULT 20,
    estado ENUM('activa', 'mantenimiento', 'inactiva') NOT NULL DEFAULT 'activa',
    fecha_instalacion DATE NOT NULL,
    INDEX idx_estaciones_ciudad (ciudad),
    INDEX idx_estaciones_estado (estado)
) ENGINE=InnoDB COMMENT='Estaciones físicas dispensadoras de PowerGo';

-- --------------------------------------------------------------------
-- TABLA: cargadores (power banks)
-- Dispositivos físicos portátiles que se alquilan a los usuarios
-- --------------------------------------------------------------------
CREATE TABLE cargadores (
    id_cargador INT AUTO_INCREMENT PRIMARY KEY,
    numero_serie VARCHAR(40) NOT NULL UNIQUE,
    modelo VARCHAR(50) NOT NULL DEFAULT 'PowerGo Volt 5000mAh',
    nivel_bateria INT NOT NULL DEFAULT 100 CHECK (nivel_bateria BETWEEN 0 AND 100),
    ciclos_carga INT NOT NULL DEFAULT 0,
    tipo_conector VARCHAR(60) NOT NULL DEFAULT 'Lightning / USB-C / Micro-USB',
    estado ENUM('disponible', 'en_prestamo', 'en_mantenimiento', 'baja') NOT NULL DEFAULT 'disponible',
    id_estacion_actual INT NULL,
    CONSTRAINT fk_cargador_estacion FOREIGN KEY (id_estacion_actual) 
        REFERENCES estaciones(id_estacion) ON UPDATE CASCADE ON DELETE SET NULL,
    INDEX idx_cargador_estado (estado),
    INDEX idx_cargador_estacion (id_estacion_actual)
) ENGINE=InnoDB COMMENT='Dispositivos portátiles (power banks) disponibles para alquiler';

-- --------------------------------------------------------------------
-- TABLA: alquileres
-- Registro de cada transacción de préstamo y devolución de cargador
-- --------------------------------------------------------------------
CREATE TABLE alquileres (
    id_alquiler INT AUTO_INCREMENT PRIMARY KEY,
    codigo_alquiler VARCHAR(30) NOT NULL UNIQUE,
    id_usuario INT NOT NULL,
    id_cargador INT NOT NULL,
    id_estacion_origen INT NOT NULL,
    id_estacion_destino INT NULL,
    fecha_inicio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_fin DATETIME NULL,
    horas_duracion DECIMAL(6, 2) NULL DEFAULT 0.00,
    tarifa_aplicada DECIMAL(10, 2) NOT NULL DEFAULT 3000.00,
    costo_total DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    estado ENUM('activo', 'finalizado', 'cancelado', 'no_devuelto') NOT NULL DEFAULT 'activo',
    CONSTRAINT fk_alquiler_usuario FOREIGN KEY (id_usuario) 
        REFERENCES usuarios(id_usuario) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_alquiler_cargador FOREIGN KEY (id_cargador) 
        REFERENCES cargadores(id_cargador) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_alquiler_origen FOREIGN KEY (id_estacion_origen) 
        REFERENCES estaciones(id_estacion) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_alquiler_destino FOREIGN KEY (id_estacion_destino) 
        REFERENCES estaciones(id_estacion) ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_alquiler_estado (estado),
    INDEX idx_alquiler_usuario (id_usuario)
) ENGINE=InnoDB COMMENT='Registro histórico y activo de préstamos de cargadores';

-- --------------------------------------------------------------------
-- TABLA: transacciones_pagos
-- Detalle de los pagos realizados por alquileres o suscripciones
-- --------------------------------------------------------------------
CREATE TABLE transacciones_pagos (
    id_pago INT AUTO_INCREMENT PRIMARY KEY,
    referencia_pago VARCHAR(50) NOT NULL UNIQUE,
    id_usuario INT NOT NULL,
    id_alquiler INT NULL,
    id_suscripcion INT NULL,
    tipo_concepto ENUM('alquiler_hora', 'pase_diario', 'suscripcion_mensual', 'recarga_saldo', 'penalidad') NOT NULL,
    monto DECIMAL(10, 2) NOT NULL,
    metodo_pago ENUM('tarjeta_credito', 'pse', 'nequi', 'daviplata', 'saldo_cuenta', 'efectivo') NOT NULL,
    estado_pago ENUM('aprobado', 'pendiente', 'rechazado') NOT NULL DEFAULT 'aprobado',
    fecha_pago DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pago_usuario FOREIGN KEY (id_usuario) 
        REFERENCES usuarios(id_usuario) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_pago_alquiler FOREIGN KEY (id_alquiler) 
        REFERENCES alquileres(id_alquiler) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_pago_suscripcion FOREIGN KEY (id_suscripcion) 
        REFERENCES suscripciones_usuario(id_suscripcion) ON UPDATE CASCADE ON DELETE SET NULL,
    INDEX idx_pagos_fecha (fecha_pago),
    INDEX idx_pagos_estado (estado_pago)
) ENGINE=InnoDB COMMENT='Auditoría financiera y cobros de PowerGo';

-- --------------------------------------------------------------------
-- TABLA: mensajes_contacto
-- Recibe las solicitudes del formulario de contacto de la landing page
-- --------------------------------------------------------------------
CREATE TABLE mensajes_contacto (
    id_mensaje INT AUTO_INCREMENT PRIMARY KEY,
    nombre_remitente VARCHAR(100) NOT NULL,
    email_remitente VARCHAR(100) NOT NULL,
    telefono VARCHAR(20) NULL,
    asunto VARCHAR(150) NOT NULL,
    mensaje TEXT NOT NULL,
    leido BOOLEAN NOT NULL DEFAULT FALSE,
    respuesta TEXT NULL,
    fecha_envio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='Buzón de soporte y contacto de la landing page';

-- ====================================================================
-- 3. VISTAS DEL SISTEMA (VIEWS)
-- ====================================================================

-- Vista 1: Disponibilidad y slots libres en tiempo real por cada estación
CREATE OR REPLACE VIEW vw_estaciones_disponibilidad AS
SELECT 
    e.id_estacion,
    e.codigo_estacion,
    e.nombre_estacion,
    e.ciudad,
    e.direccion,
    e.capacidad_total,
    COUNT(CASE WHEN c.estado = 'disponible' THEN 1 END) AS cargadores_disponibles,
    (e.capacidad_total - COUNT(CASE WHEN c.estado = 'disponible' THEN 1 END)) AS slots_vacios_retorno,
    ROUND((COUNT(CASE WHEN c.estado = 'disponible' THEN 1 END) / e.capacidad_total) * 100, 1) AS porcentaje_disponibilidad,
    e.estado AS estado_estacion
FROM estaciones e
LEFT JOIN cargadores c ON e.id_estacion = c.id_estacion_actual
GROUP BY e.id_estacion, e.codigo_estacion, e.nombre_estacion, e.ciudad, e.direccion, e.capacidad_total, e.estado;

-- Vista 2: Alquileres activos con detalles consolidados para el Dashboard
CREATE OR REPLACE VIEW vw_alquileres_activos AS
SELECT 
    a.id_alquiler,
    a.codigo_alquiler,
    u.nombre_completo AS nombre_usuario,
    u.email AS email_usuario,
    c.numero_serie AS codigo_cargador,
    c.nivel_bateria,
    e.nombre_estacion AS estacion_origen,
    a.fecha_inicio,
    TIMESTAMPDIFF(MINUTE, a.fecha_inicio, NOW()) AS minutos_transcurridos,
    ROUND(TIMESTAMPDIFF(MINUTE, a.fecha_inicio, NOW()) / 60, 2) AS horas_estimadas,
    a.tarifa_aplicada,
    a.estado
FROM alquileres a
INNER JOIN usuarios u ON a.id_usuario = u.id_usuario
INNER JOIN cargadores c ON a.id_cargador = c.id_cargador
INNER JOIN estaciones e ON a.id_estacion_origen = e.id_estacion
WHERE a.estado = 'activo';

-- ====================================================================
-- 4. DISPARADORES AUTOMÁTICOS (TRIGGERS)
-- ====================================================================

DELIMITER $$

-- Trigger 1: Al iniciar alquiler, el cargador pasa a 'en_prestamo' y sale de la estación
CREATE TRIGGER trg_despues_iniciar_alquiler
AFTER INSERT ON alquileres
FOR EACH ROW
BEGIN
    IF NEW.estado = 'activo' THEN
        UPDATE cargadores 
        SET estado = 'en_prestamo',
            id_estacion_actual = NULL,
            ciclos_carga = ciclos_carga + 1
        WHERE id_cargador = NEW.id_cargador;
    END IF;
END$$

-- Trigger 2: Al finalizar el alquiler, el cargador queda disponible en la nueva estación
CREATE TRIGGER trg_despues_finalizar_alquiler
AFTER UPDATE ON alquileres
FOR EACH ROW
BEGIN
    IF OLD.estado = 'activo' AND NEW.estado = 'finalizado' THEN
        UPDATE cargadores 
        SET estado = 'disponible',
            id_estacion_actual = NEW.id_estacion_destino
        WHERE id_cargador = NEW.id_cargador;
    END IF;
END$$

DELIMITER ;

-- ====================================================================
-- 5. PROCEDIMIENTOS ALMACENADOS (STORED PROCEDURES)
-- ====================================================================

DELIMITER $$

-- Procedimiento para iniciar un préstamo validando disponibilidad
CREATE PROCEDURE sp_iniciar_alquiler(
    IN p_id_usuario INT,
    IN p_id_estacion_origen INT,
    OUT p_codigo_alquiler VARCHAR(30),
    OUT p_resultado VARCHAR(100)
)
BEGIN
    DECLARE v_id_cargador INT DEFAULT NULL;
    DECLARE v_codigo VARCHAR(30);

    -- Buscar un cargador disponible con más de 80% de batería en esa estación
    SELECT id_cargador INTO v_id_cargador
    FROM cargadores
    WHERE id_estacion_actual = p_id_estacion_origen 
      AND estado = 'disponible' 
      AND nivel_bateria >= 80
    LIMIT 1;

    IF v_id_cargador IS NULL THEN
        SET p_codigo_alquiler = NULL;
        SET p_resultado = 'ERROR: No hay cargadores disponibles en esta estación.';
    ELSE
        SET v_codigo = CONCAT('ALQ-', UNIX_TIMESTAMP(), '-', FLOOR(100 + (RAND() * 899)));
        
        INSERT INTO alquileres (codigo_alquiler, id_usuario, id_cargador, id_estacion_origen, fecha_inicio, tarifa_aplicada, estado)
        VALUES (v_codigo, p_id_usuario, v_id_cargador, p_id_estacion_origen, NOW(), 3000.00, 'activo');

        SET p_codigo_alquiler = v_codigo;
        SET p_resultado = 'EXITO: Alquiler iniciado correctamente.';
    END IF;
END$$

-- Procedimiento para devolver un cargador y liquidar costo
CREATE PROCEDURE sp_devolver_cargador(
    IN p_codigo_alquiler VARCHAR(30),
    IN p_id_estacion_destino INT,
    OUT p_costo_final DECIMAL(10, 2),
    OUT p_resultado VARCHAR(100)
)
BEGIN
    DECLARE v_id_alquiler INT DEFAULT NULL;
    DECLARE v_fecha_inicio DATETIME;
    DECLARE v_tarifa DECIMAL(10, 2);
    DECLARE v_minutos INT;
    DECLARE v_horas DECIMAL(6, 2);
    DECLARE v_slots_libres INT DEFAULT 0;

    -- Verificar que la estación de destino tenga slots libres
    SELECT slots_vacios_retorno INTO v_slots_libres
    FROM vw_estaciones_disponibilidad
    WHERE id_estacion = p_id_estacion_destino;

    IF v_slots_libres <= 0 THEN
        SET p_costo_final = 0.00;
        SET p_resultado = 'ERROR: La estación de destino está llena.';
    ELSE
        SELECT id_alquiler, fecha_inicio, tarifa_aplicada
        INTO v_id_alquiler, v_fecha_inicio, v_tarifa
        FROM alquileres
        WHERE codigo_alquiler = p_codigo_alquiler AND estado = 'activo';

        IF v_id_alquiler IS NULL THEN
            SET p_costo_final = 0.00;
            SET p_resultado = 'ERROR: El préstamo no existe o ya fue finalizado.';
        ELSE
            SET v_minutos = TIMESTAMPDIFF(MINUTE, v_fecha_inicio, NOW());
            IF v_minutos < 60 THEN
                SET v_horas = 1.00;
            ELSE
                SET v_horas = CEIL(v_minutos / 60);
            END IF;

            SET p_costo_final = v_horas * v_tarifa;

            UPDATE alquileres
            SET fecha_fin = NOW(),
                id_estacion_destino = p_id_estacion_destino,
                horas_duracion = v_horas,
                costo_total = p_costo_final,
                estado = 'finalizado'
            WHERE id_alquiler = v_id_alquiler;

            SET p_resultado = 'EXITO: Cargador devuelto y alquiler liquidado.';
        END IF;
    END IF;
END$$

DELIMITER ;

-- ====================================================================
-- 6. DATOS SEMILLA DE PRUEBA (SEED DATA - MOCK DATA DE LA LANDING)
-- ====================================================================

-- Usuarios por defecto de la aplicación
INSERT INTO usuarios (nombre_completo, email, telefono, password_hash, rol, saldo_cuenta) VALUES
('Santiago Aristizabal', 'cliente@powergo.com', '3001234567', SHA2('123', 256), 'cliente', 50000.00),
('Administrador PowerGo', 'admin@powergo.com', '3119876543', SHA2('admin', 256), 'administrador', 0.00),
('Camila Restrepo', 'camila.restrepo@udea.edu.co', '3145678901', SHA2('camila2026', 256), 'cliente', 15000.00),
('Mateo Gaviria', 'mateo.gaviria@gmail.com', '3206549870', SHA2('mateo2026', 256), 'cliente', 20000.00);

-- Planes de la landing page
INSERT INTO planes (codigo_plan, nombre_plan, categoria, precio, tipo_cobro, horas_incluidas_dia, descripcion) VALUES
('PLAN-HORA', 'Pago por Hora', 'ocasional', 3000.00, 'hora', 0, 'Ideal para emergencias donde solo necesitas una carga rápida con cables incorporados.'),
('PLAN-DIA', 'Pase Diario', 'popular', 10000.00, 'dia', 24, 'Uso ilimitado por 24 horas y devoluciones ilimitadas en cualquier estación.'),
('PLAN-MES', 'Suscripción Mensual', 'frecuente', 25000.00, 'mes', 3, 'Hasta 3 horas gratis por día y acceso a cargadores prioritarios para estudiantes y profesionales.');

-- Estaciones físicas reales en Medellín
INSERT INTO estaciones (codigo_estacion, nombre_estacion, ciudad, direccion, latitud, longitud, capacidad_total, fecha_instalacion) VALUES
('st-1', 'Centro Comercial Santa Fe', 'Medellín', 'Carrera 43A # 7 Sur-170, El Poblado', 6.19730000, -75.57390000, 20, '2026-01-15'),
('st-2', 'Universidad de Antioquia', 'Medellín', 'Calle 67 # 53-108, Bloque 22', 6.26740000, -75.56840000, 15, '2026-02-01');

-- Cargadores asignados a las estaciones (15 en Santa Fe, 8 en U de A)
INSERT INTO cargadores (numero_serie, nivel_bateria, ciclos_carga, estado, id_estacion_actual) VALUES
-- En Centro Comercial Santa Fe (15 cargadores)
('PB-001', 98, 12, 'disponible', 1),
('PB-002', 100, 8, 'disponible', 1),
('PB-003', 95, 14, 'disponible', 1),
('PB-004', 92, 19, 'disponible', 1),
('PB-005', 100, 5, 'disponible', 1),
('PB-006', 88, 22, 'disponible', 1),
('PB-007', 90, 16, 'disponible', 1),
('PB-008', 85, 30, 'disponible', 1),
('PB-009', 100, 4, 'disponible', 1),
('PB-010', 94, 11, 'disponible', 1),
('PB-011', 89, 25, 'disponible', 1),
('PB-012', 96, 15, 'disponible', 1),
('PB-013', 91, 18, 'disponible', 1),
('PB-014', 100, 2, 'disponible', 1),
('PB-015', 86, 28, 'disponible', 1),
-- En Universidad de Antioquia (8 cargadores)
('PB-016', 100, 9, 'disponible', 2),
('PB-017', 95, 13, 'disponible', 2),
('PB-018', 92, 17, 'disponible', 2),
('PB-019', 88, 21, 'disponible', 2),
('PB-020', 100, 3, 'disponible', 2),
('PB-021', 65, 34, 'en_prestamo', NULL), -- El préstamo activo del cliente demo
('PB-022', 97, 10, 'disponible', 2),
('PB-023', 93, 15, 'disponible', 2);

-- Préstamo activo inicial (Coincide con mock de app.js)
INSERT INTO alquileres (codigo_alquiler, id_usuario, id_cargador, id_estacion_origen, fecha_inicio, tarifa_aplicada, estado) VALUES
('loan-1', 1, 21, 1, '2026-07-16 10:30:00', 3000.00, 'activo');

-- Suscripción demo
INSERT INTO suscripciones_usuario (id_usuario, id_plan, fecha_inicio, fecha_fin, auto_renovacion, estado) VALUES
(1, 3, '2026-07-01 00:00:00', '2026-08-01 23:59:59', TRUE, 'activa');

-- Pagos simulados
INSERT INTO transacciones_pagos (referencia_pago, id_usuario, id_suscripcion, tipo_concepto, monto, metodo_pago, estado_pago) VALUES
('TX-20260701-001', 1, 1, 'suscripcion_mensual', 25000.00, 'tarjeta_credito', 'aprobado');

-- Mensajes de prueba del formulario de contacto
INSERT INTO mensajes_contacto (nombre_remitente, email_remitente, telefono, asunto, mensaje) VALUES
('Juan Perez', 'juan.perez@correo.com', '3109876543', 'Nueva estación en el Metro', 'Buenas tardes, me gustaría saber si planean instalar estaciones de PowerGo en las estaciones del Metro de Medellín.');
