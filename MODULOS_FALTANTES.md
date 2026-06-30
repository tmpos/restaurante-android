# Modulos faltantes y mejoras pendientes

Fecha de revision: 2026-06-15

## Criterio

Este documento separa:

1. **Pendientes comprobados:** funciones visibles o declaradas que no estan
   terminadas en el codigo actual.
2. **Brechas funcionales:** capacidades necesarias para cerrar flujos ya
   existentes.
3. **Modulos recomendados:** no son errores, pero suelen ser necesarios en un
   POS de restaurante comercial.

## Prioridad critica

### 1. Entradas y retiros de caja

**Estado actual:** faltante.

Los botones existen en Control de Caja, pero ambos callbacks contienen `TODO`.

Debe incluir:

- Registro de entrada o retiro.
- Monto, motivo, usuario y fecha.
- Historial por turno.
- Impacto en efectivo esperado y cierre.
- Impresion o comprobante opcional.

### 2. Seguridad de credenciales

**Estado actual:** parcial y de alto riesgo.

Usuarios, PIN y contrasenas se guardan directamente en archivos locales. Tambien
se crean credenciales iniciales conocidas.

Debe incluir:

- Hash seguro de contrasenas y PIN.
- Cambio obligatorio de credenciales iniciales.
- Bloqueo por intentos fallidos.
- Cierre automatico de sesion.
- Proteccion o cifrado de secretos SMTP y claves cloud.

### 3. Permisos granulares y auditoria

**Estado actual:** parcial.

Solo existen ADMIN y CAJERO. El historial de ajustes de inventario guarda
usuario, pero no hay una auditoria general.

Debe incluir:

- Permisos por modulo y accion.
- Roles configurables: administrador, supervisor, cajero, camarero, cocina.
- Bitacora inmutable de anulaciones, descuentos, cambios de precio, devoluciones,
  borrados, aperturas de cajon y cambios de configuracion.
- Consulta y exportacion de auditoria.

### 4. Backups reales y recuperacion integral

**Estado actual:** parcial.

La seccion general de backups usa `mockBackups`; los botones Crear backup y
Actualizar no tienen implementacion. El editor de soporte solo respalda tablas
individuales.

Debe incluir:

- Backup completo de todos los archivos y logo.
- Restauracion validada y versionada.
- Backup automatico al cerrar turno o sesion.
- Rotacion y eliminacion segura.
- Copia local, USB y nube.

### 5. Pruebas automatizadas y migraciones

**Estado actual:** faltante.

No se encontraron fuentes de pruebas unitarias o de interfaz en el repositorio.

Debe incluir:

- Pruebas de totales, impuestos, descuentos y devoluciones.
- Pruebas de stock, recetas y recepcion de compras.
- Pruebas de persistencia y recuperacion.
- Pruebas de turnos, caja y pagos divididos.
- Versionado y migracion de formatos de datos.

## Prioridad alta

### 6. Cotizaciones

**Estado actual:** faltante.

Existe en el menu, pero abre el modulo generico en construccion.

Debe incluir:

- Crear y editar cotizacion.
- Cliente, productos, impuestos, descuentos y vigencia.
- Estados borrador/enviada/aceptada/rechazada.
- Impresion y correo.
- Convertir cotizacion en venta o factura.

### 7. Consumo de inventario por recetas

**Estado actual:** parcial.

Las recetas se guardan, pero el POS solo descuenta el stock del producto vendido.
No se encontro uso de `RecipeManager` durante la venta.

Debe incluir:

- Rebajar ingredientes segun receta y cantidad vendida.
- Considerar porciones, extras y modificadores.
- Revertir inventario en anulaciones/devoluciones.
- Calcular costo real y margen del plato.
- Alertar ingredientes insuficientes.

### 8. Movimientos completos de inventario

**Estado actual:** parcial.

Hay stock y ajustes, pero faltan operaciones avanzadas.

Debe incluir:

- Conteo fisico y conciliacion.
- Transferencias entre almacenes o sucursales.
- Unidades y conversiones: unidad, libra, kilogramo, caja.
- Lotes, vencimientos y mermas.
- Kardex por producto.
- Punto de reorden y sugerencia de compra.

### 9. Compras y cuentas por pagar

**Estado actual:** parcial.

Las ordenes aumentan stock al marcarse recibidas, pero no manejan el ciclo
financiero completo.

Debe incluir:

- Seleccionar proveedor por ID, no solo escribir el nombre.
- Recepciones parciales.
- Factura del proveedor, NCF, impuestos y vencimiento.
- Cuentas por pagar, abonos y saldo.
- Devoluciones a proveedor.
- Comparacion de costos y actualizacion controlada del costo del producto.

### 10. Fiscalidad NCF completa

**Estado actual:** parcial.

Existe una secuencia NCF simple.

Debe incluir:

- Tipos de comprobante y secuencias separadas.
- Rango autorizado, fecha de vencimiento y alertas.
- Notas de credito/debito vinculadas.
- Reportes fiscales 606, 607, 608 y 609, si aplican al negocio.
- Validaciones de RNC y comprobantes.

### 11. Reportes avanzados y exportacion

**Estado actual:** parcial.

El reporte actual cubre ventas y correo, pero no exporta archivos ni analiza
costos completos.

Debe incluir:

- Exportar PDF, CSV y Excel.
- Ventas por hora, cajero, camarero, mesa, categoria y producto.
- Rentabilidad, costo, margen y merma.
- Impuestos, propinas, descuentos, anulaciones y devoluciones.
- Comparacion entre periodos.
- Cierre Z y consolidado por turno/sucursal.

## Prioridad media

### 12. Delivery completo

**Estado actual:** parcial.

El POS permite capturar datos basicos de delivery, pero no existe un modulo de
despacho.

Debe incluir:

- Pedidos en preparacion/listos/asignados/entregados.
- Repartidores y zonas.
- Direccion geolocalizada.
- Costo de envio y tiempo estimado.
- Seguimiento y cierre de entrega.

### 13. Fidelizacion y promociones

**Estado actual:** faltante.

Debe incluir:

- Puntos o saldo por cliente.
- Cupones y codigos promocionales.
- Promociones por horario, categoria, cantidad o combo.
- Historial de consumo y segmentacion.
- Tarjetas de regalo.

### 14. Gestion de empleados

**Estado actual:** faltante.

Usuarios no equivale a gestion laboral.

Debe incluir:

- Empleados y puestos.
- Marcacion de entrada/salida.
- Horarios y asistencia.
- Ventas y propinas por empleado.
- Comisiones o nomina, si aplica.

### 15. Cocina avanzada

**Estado actual:** parcial.

Comandas funciona, pero puede ampliarse con:

- Estaciones de cocina por producto.
- Impresoras por area.
- Tiempos objetivo y alertas de demora.
- Priorizacion, reimpresion y trazabilidad.
- Pantalla dedicada KDS y llamada de orden lista.

### 16. Reservaciones con calendario

**Estado actual:** parcial.

Debe incluir:

- Calendario visual por dia/semana.
- Date picker y selector de hora.
- Disponibilidad real de mesas.
- Prevencion de conflictos.
- Recordatorios por correo o mensajeria.
- Lista de espera y no-show.

### 17. Multi-sucursal y multi-almacen

**Estado actual:** faltante.

La nube sincroniza datos, pero no existe un modelo funcional de sucursales.

Debe incluir:

- Sucursal y almacen en ventas, stock, compras y usuarios.
- Numeracion fiscal por sucursal.
- Transferencias y consolidacion central.
- Configuracion y permisos por ubicacion.

### 18. Integracion de pagos real

**Estado actual:** parcial.

El POS registra metodos y existe infraestructura de lector/perifericos, pero no
se encontro procesamiento transaccional con un adquirente.

Debe incluir:

- Solicitud de cobro al terminal.
- Respuesta aprobada/rechazada.
- Numero de autorizacion y referencia.
- Reversion, anulacion y conciliacion.
- Evitar marcar pagada una venta sin confirmacion del proveedor.

## Mejoras tecnicas recomendadas

### 19. Persistencia transaccional

Los datos se guardan principalmente en archivos TSV/propiedades. Para mayor
volumen y concurrencia conviene:

- Base local SQLite.
- Transacciones atomicas.
- Indices y consultas.
- Integridad referencial.
- Migraciones de esquema.

### 20. Sincronizacion y conflictos

TM Cloud es funcional, pero faltan politicas visibles para:

- Conflictos de edicion.
- Borrados sincronizados.
- Reintentos durables.
- Cola real de sincronizacion en Configuracion.
- Indicador por registro y diagnostico de errores.

### 21. Monitoreo y diagnostico

Debe incluir:

- Registro estructurado de errores.
- Exportacion de diagnostico.
- Estado de impresora, servidor, correo y nube.
- Alertas de almacenamiento y archivos corruptos.
- Reporte de fallos sin exponer datos sensibles.

## Orden sugerido de implementacion

1. Entradas/retiros de caja.
2. Seguridad, permisos y auditoria.
3. Backups reales y pruebas automatizadas.
4. Consumo de recetas e inventario avanzado.
5. Cotizaciones.
6. Compras con cuentas por pagar.
7. Fiscalidad NCF completa.
8. Reportes/exportaciones.
9. Delivery, fidelizacion y reservas avanzadas.
10. Multi-sucursal e integracion de pagos.

