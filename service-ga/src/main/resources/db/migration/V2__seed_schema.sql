-------------------------------------------------------------------------------
-- LIMPIEZA BÁSICA (primero hijos, luego padres)
-------------------------------------------------------------------------------
BEGIN
  EXECUTE IMMEDIATE 'TRUNCATE TABLE PRESTAMO';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'TRUNCATE TABLE GA_OUTBOX';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'TRUNCATE TABLE GA_IDEMPOTENCY';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'TRUNCATE TABLE LIBRO';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'TRUNCATE TABLE USUARIO';
EXCEPTION WHEN OTHERS THEN NULL;
END;
/

-------------------------------------------------------------------------------
-- LIBRO
-- STOCK_SEDE_A = S1, STOCK_SEDE_B = S2 (sumando stock_total por código)
-------------------------------------------------------------------------------
INSERT INTO LIBRO (LIBRO_ID, TITULO, STOCK_SEDE_A, STOCK_SEDE_B) VALUES
('9780439785969', 'Sistemas distribuidos modernos',                 3, 0);

INSERT INTO LIBRO (LIBRO_ID, TITULO, STOCK_SEDE_A, STOCK_SEDE_B) VALUES
('9780439358071', 'Diseño de microservicios en Java',               2, 0);

INSERT INTO LIBRO (LIBRO_ID, TITULO, STOCK_SEDE_A, STOCK_SEDE_B) VALUES
('9780439554893', 'Patrones de integración de datos',               1, 0);

INSERT INTO LIBRO (LIBRO_ID, TITULO, STOCK_SEDE_A, STOCK_SEDE_B) VALUES
('9780439655484', 'Introducción a la programación concurrente',     0, 2);

INSERT INTO LIBRO (LIBRO_ID, TITULO, STOCK_SEDE_A, STOCK_SEDE_B) VALUES
('9780439682589', 'Arquitecturas orientadas a eventos',             0, 1);

INSERT INTO LIBRO (LIBRO_ID, TITULO, STOCK_SEDE_A, STOCK_SEDE_B) VALUES
('9780976540601', 'Fundamentos de redes de computadores',           1, 0);

INSERT INTO LIBRO (LIBRO_ID, TITULO, STOCK_SEDE_A, STOCK_SEDE_B) VALUES
('9780439827607', 'Teoría de colas aplicada',                       0, 2);

INSERT INTO LIBRO (LIBRO_ID, TITULO, STOCK_SEDE_A, STOCK_SEDE_B) VALUES
('9780517226957', 'Algoritmos y estructuras de datos',              1, 0);

INSERT INTO LIBRO (LIBRO_ID, TITULO, STOCK_SEDE_A, STOCK_SEDE_B) VALUES
('9780345453747', 'Docker y contenedores para desarrolladores',     1, 0);

-------------------------------------------------------------------------------
-- USUARIO (inventados)
-------------------------------------------------------------------------------
INSERT INTO USUARIO (USUARIO_ID, NOMBRE) VALUES ('U001', 'Ana Torres');
INSERT INTO USUARIO (USUARIO_ID, NOMBRE) VALUES ('U002', 'Bruno García');
INSERT INTO USUARIO (USUARIO_ID, NOMBRE) VALUES ('U003', 'Carlos López');
INSERT INTO USUARIO (USUARIO_ID, NOMBRE) VALUES ('U004', 'Diana Martínez');
INSERT INTO USUARIO (USUARIO_ID, NOMBRE) VALUES ('U005', 'Elena Rodríguez');
INSERT INTO USUARIO (USUARIO_ID, NOMBRE) VALUES ('U006', 'Fernando Castillo');
INSERT INTO USUARIO (USUARIO_ID, NOMBRE) VALUES ('U007', 'Gabriela Ruiz');
INSERT INTO USUARIO (USUARIO_ID, NOMBRE) VALUES ('U008', 'Héctor Jiménez');

-------------------------------------------------------------------------------
-- PRESTAMO
-- NOTA: PRESTAMO_ID es IDENTITY → no lo incluimos en el INSERT
-------------------------------------------------------------------------------
INSERT INTO PRESTAMO
  (LIBRO_ID,        USUARIO_ID, FECHA_INICIO,          FECHA_ENTREGA,          RENOVACIONES, ESTADO)
VALUES
  ('9780439785969', 'U001',     DATE '2025-11-10',     DATE '2025-11-24',      1,            'ACTIVO');

INSERT INTO PRESTAMO
  (LIBRO_ID,        USUARIO_ID, FECHA_INICIO,          FECHA_ENTREGA,          RENOVACIONES, ESTADO)
VALUES
  ('9780439785969', 'U002',     DATE '2025-10-01',     DATE '2025-10-15',      0,            'DEV');

INSERT INTO PRESTAMO
  (LIBRO_ID,        USUARIO_ID, FECHA_INICIO,          FECHA_ENTREGA,          RENOVACIONES, ESTADO)
VALUES
  ('9780439358071', 'U003',     DATE '2025-11-05',     DATE '2025-11-19',      0,            'ACTIVO');

INSERT INTO PRESTAMO
  (LIBRO_ID,        USUARIO_ID, FECHA_INICIO,          FECHA_ENTREGA,          RENOVACIONES, ESTADO)
VALUES
  ('9780439554893', 'U004',     DATE '2025-09-10',     DATE '2025-09-24',      2,            'DEV');

INSERT INTO PRESTAMO
  (LIBRO_ID,        USUARIO_ID, FECHA_INICIO,          FECHA_ENTREGA,          RENOVACIONES, ESTADO)
VALUES
  ('9780439655484', 'U005',     DATE '2025-11-01',     DATE '2025-11-15',      0,            'ACTIVO');

INSERT INTO PRESTAMO
  (LIBRO_ID,        USUARIO_ID, FECHA_INICIO,          FECHA_ENTREGA,          RENOVACIONES, ESTADO)
VALUES
  ('9780439682589', 'U006',     DATE '2025-08-20',     DATE '2025-09-03',      1,            'DEV');

INSERT INTO PRESTAMO
  (LIBRO_ID,        USUARIO_ID, FECHA_INICIO,          FECHA_ENTREGA,          RENOVACIONES, ESTADO)
VALUES
  ('9780976540601', 'U007',     DATE '2025-11-12',     DATE '2025-11-26',      0,            'ACTIVO');

INSERT INTO PRESTAMO
  (LIBRO_ID,        USUARIO_ID, FECHA_INICIO,          FECHA_ENTREGA,          RENOVACIONES, ESTADO)
VALUES
  ('9780439827607', 'U008',     DATE '2025-10-15',     DATE '2025-10-29',      1,            'DEV');

INSERT INTO PRESTAMO
  (LIBRO_ID,        USUARIO_ID, FECHA_INICIO,          FECHA_ENTREGA,          RENOVACIONES, ESTADO)
VALUES
  ('9780517226957', 'U001',     DATE '2025-11-13',     DATE '2025-11-27',      0,            'ACTIVO');

INSERT INTO PRESTAMO
  (LIBRO_ID,        USUARIO_ID, FECHA_INICIO,          FECHA_ENTREGA,          RENOVACIONES, ESTADO)
VALUES
  ('9780345453747', 'U002',     DATE '2025-09-01',     DATE '2025-09-15',      0,            'DEV');

-------------------------------------------------------------------------------
-- GA_IDEMPOTENCY (ejemplos)
-------------------------------------------------------------------------------
INSERT INTO GA_IDEMPOTENCY (IDEMPOTENCY_KEY, CREATED_AT, OP_SUMMARY) VALUES
('LOAN-9780439785969-U001-20251110',
 DATE '2025-11-10',
 'Crear préstamo libro 9780439785969 para U001');

INSERT INTO GA_IDEMPOTENCY (IDEMPOTENCY_KEY, CREATED_AT, OP_SUMMARY) VALUES
('RETURN-9780976540601-U007-20251126',
 DATE '2025-11-26',
 'Registrar devolución libro 9780976540601 de U007');

INSERT INTO GA_IDEMPOTENCY (IDEMPOTENCY_KEY, CREATED_AT, OP_SUMMARY) VALUES
('SYNC-CATALOG-20251101',
 DATE '2025-11-01',
 'Sincronización completa del catálogo con sistema externo');

-------------------------------------------------------------------------------
-- GA_OUTBOX (eventos de dominio de ejemplo)
-------------------------------------------------------------------------------
INSERT INTO GA_OUTBOX (EVENT_TYPE, PAYLOAD, CREATED_AT, PROCESSED_AT) VALUES (
  'BOOK_LOAN_CREATED',
  TO_CLOB('{"libroId":"9780439785969","usuarioId":"U001","estado":"ACTIVO"}'),
  DATE '2025-11-10',
  NULL
);

INSERT INTO GA_OUTBOX (EVENT_TYPE, PAYLOAD, CREATED_AT, PROCESSED_AT) VALUES (
  'BOOK_LOAN_RETURNED',
  TO_CLOB('{"libroId":"9780976540601","usuarioId":"U007","estado":"DEV"}'),
  DATE '2025-11-26',
  DATE '2025-11-26'
);

INSERT INTO GA_OUTBOX (EVENT_TYPE, PAYLOAD, CREATED_AT, PROCESSED_AT) VALUES (
  'BOOK_STOCK_RECALCULATED',
  TO_CLOB('{"fecha":"2025-11-01","librosProcesados":9}'),
  DATE '2025-11-01',
  DATE '2025-11-01'
);

COMMIT;
