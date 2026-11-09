-- ===================================================
-- Esquema de base de datos para GlucoAPI
-- Base de datos: gluco_api
--
-- Uso local:
--   mysql -u root -p < schema.sql
--
-- Uso en la nube (Render + MySQL administrado):
--   ejecuta este script desde la consola del proveedor
--   (TiDB Cloud, Aiven, Clever Cloud, etc.). Si el
--   proveedor ya creo la base de datos, puedes omitir
--   las dos primeras lineas.
-- ===================================================

CREATE DATABASE IF NOT EXISTS gluco_api
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE gluco_api;

-- Tabla: usuario
CREATE TABLE IF NOT EXISTS usuario (
  idUsuario INT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(40) NOT NULL,
  primerApellido VARCHAR(40) NOT NULL,
  segundoApellido VARCHAR(40) DEFAULT NULL,
  email VARCHAR(50) NOT NULL,
  PRIMARY KEY (idUsuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabla: medida
CREATE TABLE IF NOT EXISTS medida (
  idMedida INT NOT NULL AUTO_INCREMENT,
  idUsuario INT NOT NULL,
  glucosa INT NOT NULL,
  fechaHora DATETIME DEFAULT CURRENT_TIMESTAMP,
  contexto VARCHAR(40) DEFAULT NULL,
  notas VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (idMedida),
  KEY idx_usuario (idUsuario),
  CONSTRAINT fk_medida_usuario FOREIGN KEY (idUsuario)
    REFERENCES usuario (idUsuario)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Datos de prueba
INSERT IGNORE INTO usuario (idUsuario, nombre, primerApellido, segundoApellido, email) VALUES
  (1, 'Juan', 'Perez', 'Lopez', 'juan@correo.com'),
  (2, 'Maria', 'Garcia', 'Sanchez', 'maria@correo.com');

INSERT IGNORE INTO medida (idMedida, idUsuario, glucosa, fechaHora, contexto, notas) VALUES
  (1, 1, 95,  NOW(), 'Ayunas', 'Medicion matutina de prueba'),
  (2, 1, 135, NOW(), 'Postprandial', 'Despues de comer'),
  (3, 2, 100, NOW(), 'Antes de dormir', 'Lectura nocturna');
