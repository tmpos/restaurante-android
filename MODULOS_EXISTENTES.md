# Modulos existentes

Fecha de revision: 2026-06-15

## Alcance de la revision

Este inventario se obtuvo revisando la navegacion principal, pantallas Compose,
gestores de datos, persistencia local, integraciones de plataforma y servicios de
sincronizacion. No se basa solamente en los botones visibles.

Estados usados:

- **Operativo:** tiene pantalla, logica y persistencia o integracion funcional.
- **Parcial:** existe y funciona en parte, pero contiene acciones pendientes,
  datos simulados o integraciones incompletas.

## Operacion y ventas

| Modulo | Estado | Funciones encontradas |
|---|---|---|
| Inicio de sesion | Operativo | Acceso por usuario/contrasena o PIN, apertura y recuperacion de turno, logo y nombre de empresa. |
| Dashboard | Operativo | Ventas del dia, facturas, formas de pago, comandas, mesas, productos mas vendidos y ordenes recientes. |
| Punto de venta (POS) | Operativo | Busqueda, categorias, vista grid/lista, carrito, cantidades, escaner, venta libre y detalle de producto. |
| Carrito y promociones | Operativo | Totales, ITBIS, descuentos, propina, cliente, delivery, envio a cocina, mesa y cuenta dividida. |
| Cobros | Operativo | Efectivo, tarjeta, transferencia, credito, recargos, pago dividido, monto recibido y cambio. |
| Ordenes pausadas | Operativo | Guardar y recuperar ventas pendientes. |
| Facturacion | Operativo | Historial, NCF secuencial, impresion, anulacion/eliminacion protegida por OTP y devoluciones parciales/totales. |
| Reportes | Operativo | Hoy, 7 dias, 30 dias, historial y rango personalizado con date picker. Ventas, ITBIS, descuentos, pagos, productos y facturas. |
| Envio de reportes | Operativo | Generacion HTML y envio SMTP al correo configurado de la empresa. |

## Restaurante

| Modulo | Estado | Funciones encontradas |
|---|---|---|
| Mesas | Operativo | Crear, eliminar, posicionar, abrir, agregar productos, notas, cantidades, mover y unir mesas. |
| Plano de mesas | Operativo | Vista visual, formas y posiciones persistidas por turno. |
| Detalle de mesa | Operativo | Productos, camarero, impresion de precuenta, limpiar mesa y cobrar. |
| Comandas / cocina | Operativo | Envio desde POS, estados pendiente/preparacion/listo, filtros y cursos de plato. |
| Reservaciones | Operativo | Crear, editar, eliminar, asignar mesa y cambiar estado. |
| Menu web Wi-Fi | Operativo | Catalogo web, busqueda, categorias, consulta de mesas y envio de productos/notas a mesa. |

## Catalogo e inventario

| Modulo | Estado | Funciones encontradas |
|---|---|---|
| Productos | Operativo | CRUD, precio, costo, impuestos, codigo/barcode, imagen, stock, categoria y venta por peso. |
| Categorias | Operativo | Crear, editar, eliminar, icono/color y categoria virtual Todas en POS. |
| Extras / guarniciones | Operativo | CRUD, tipo, precio y asociacion al detalle del producto. |
| Modificadores | Operativo | Grupos, opciones, precios, seleccion minima/maxima y persistencia. |
| Inventario | Operativo | Existencias, bajo stock, agotados, ajustes manuales, motivo, usuario, historial y valorizacion. |
| Descuento automatico de stock | Operativo | Rebaja existencias al completar una venta. |
| Recetas | Parcial | CRUD de receta, ingredientes, cantidades y porciones. No esta conectado al consumo automatico del POS. |

## Compras y proveedores

| Modulo | Estado | Funciones encontradas |
|---|---|---|
| Proveedores | Operativo | CRUD con RNC, contacto, telefono, correo, direccion y rubro. |
| Ordenes de compra | Operativo | Crear, editar, cancelar, eliminar, agregar productos, costos y totales. |
| Recepcion de compra | Operativo | Marcar orden como recibida y aumentar automaticamente el stock. |

## Clientes y credito

| Modulo | Estado | Funciones encontradas |
|---|---|---|
| Clientes | Operativo | CRUD, contacto, RNC, direccion, tipo y limite de credito. |
| Seleccion de cliente en POS | Operativo | Asociacion de la venta y datos del cliente a la factura. |
| Cuentas por cobrar | Operativo | Consumos, abonos, saldo, limites, historial, filtros de fecha e impresion de estado. |

## Caja, usuarios y control

| Modulo | Estado | Funciones encontradas |
|---|---|---|
| Turnos | Operativo | Apertura, cierre, turno por usuario, recuperacion de turno activo e historial. |
| Control de caja | Parcial | Resumen, efectivo esperado, ventas por metodo, gastos, conteo fisico, diferencia, impresion y correo de cierre. |
| Gastos de turno | Operativo | Registro y uso en el cuadre de caja. |
| Entradas y retiros de efectivo | Parcial | Botones visibles, pero sus acciones estan marcadas como `TODO`. |
| Usuarios | Operativo | CRUD, administrador/cajero, PIN, contrasena y perfil. |
| Permisos | Parcial | Solo existen los roles ADMIN y CAJERO; no hay permisos granulares por accion. |

## Configuracion e integraciones

| Modulo | Estado | Funciones encontradas |
|---|---|---|
| Empresa | Operativo | Nombre, RNC, telefono, correo, direccion, moneda y logo. |
| Apariencia | Operativo | Tema claro/oscuro y color primario. |
| Configuracion de ventas | Operativo | Impuestos, descuentos, cliente requerido, stock y envio a cocina. |
| Metodos de pago | Operativo | Activar/desactivar, nombre y recargo porcentual. |
| Tickets e impresion | Operativo | Impresora, papel, copias, margenes, logo, tamanos, campos opcionales y vista previa. |
| Impresion por servidor | Operativo | Envio del ticket y logo a SDK Server, con configuracion visual desde POS. |
| Perifericos Elo | Operativo | Impresora, cajon, lector, display de cliente y paneles de prueba. |
| Display de cliente | Operativo | Mensajes de espera y actualizaciones durante la venta, local o por servidor. |
| Correo SMTP | Operativo | Configuracion, prueba, OTP, cierre de caja y reportes. |
| TM Cloud | Operativo | Conexion, creacion de tablas, push, pull, sincronizacion completa/cambios y auto-sync. |
| Servidor local Wi-Fi | Operativo | API y aplicacion web para catalogo y mesas. |
| Soporte de base de datos | Operativo | Lectura, edicion, respaldo y restauracion manual de tablas locales. |
| Backups generales | Parcial | La interfaz existe, pero la lista usa datos simulados y los botones generales no ejecutan acciones reales. |
| Cola de sincronizacion en Configuracion | Parcial | La pantalla usa una cola simulada, separada del modulo TM Cloud real. |

## Persistencia y plataformas

- Persistencia local mediante archivos estructurados para productos, categorias,
  ventas, mesas, turnos, usuarios, inventario, compras y demas entidades.
- Aplicacion Kotlin Multiplatform para Android y escritorio.
- Seleccion y cache de imagenes.
- Servicios multiplataforma de fecha, red, correo e impresion.
- Integracion con lector de codigo de barras y control de duplicados.
- Tema visual comun compatible con modo oscuro.

## Modulo visible sin implementar

- **Cotizaciones:** aparece en la navegacion, pero cae en `PlaceholderScreen`.

