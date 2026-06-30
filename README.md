# TMRestaurant - POS Restaurante

Sistema POS para restaurantes desarrollado en **Kotlin Multiplatform** (Android + Desktop).

## Características

- **Punto de Venta (POS)** con gestión de mesas, comandas y pedidos
- **Menú digital** con acceso mediante código QR
- **Control de inventario** y recetas
- **Gestión de clientes, empleados y usuarios**
- **Múltiples métodos de pago** (efectivo, tarjeta, crédito)
- **Facturación electrónica DGII** vía Alanube (E31, E32, E33, E34, E45)
- **Impresión de tickets** (impresoras térmicas ELO)
- **TM Cloud** - Sincronización remota con respaldo en la nube
- **Control de caja y turnos**
- **Órdenes de compra** y proveedores
- **Reservaciones**
- **Reportes y dashboard**

## Tecnologías

- Kotlin Multiplatform + Compose Multiplatform
- SQLite (base de datos local)
- Compose Desktop + Android
- API DGII vía Alanube
- WiFi Menu Server (menú QR embebido)

## Descargas

[Download v2.2](https://github.com/tmpos/restaurante-android/releases/download/v2.2/TMRestaurant-v2.2.apk)

## Versiones

| Versión | Cambios |
|---------|---------|
| 2.2 | Corrección parser JSON TM Cloud (price/category), botón descarga directa en Productos, confirmación Vaciar tabla |
| 2.1 | Corrección multi-compañía Alanube (GET /company/{id}) |
| 2.0 | Integración Alanube DGII (E31-E45) |
| 1.0 | Versión inicial |
