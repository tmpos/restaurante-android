# Análisis de Módulos - TM-RESTAURANTE POS

> **Fecha**: Junio 2026 | **Versión**: 1.0.0 | **Plataforma**: Kotlin Multiplatform (Android + Desktop)

---

## Resumen Ejecutivo

TM-RESTAURANTE es un POS funcional con el flujo de venta completo (productos → carrito → cobro → factura → impresión). Está bien construido pero tiene **varios módulos críticos sin implementar** que limitan su uso profesional. Este documento lista lo que falta por prioridad.

---

## Módulos Existentes

| Módulo | Estado | Calidad |
|--------|--------|---------|
| Punto de Venta (POS) | Completo | Alta |
| Gestión de Mesas | Completo | Alta |
| Caja / Cierre de Turno | Completo | Alta |
| Gestión de Productos | Completo | Media |
| Gestión de Categorías | Completo | Media |
| Facturación Electrónica (NCF) | Completo | Media |
| Control de Turnos | Completo | Alta |
| Impresión (ELO/USB/Bluetooth) | Completo | Alta |
| Configuración (10 secciones) | Completo | Alta |
| Login (PIN + Credenciales) | Completo | Baja |
| Dashboard | Parcial | Baja |
| Escáner de Código de Barras | Completo | Alta |
| Modo Oscuro | Completo | - |
| Envío de Correo (SMTP) | Completo | Media |
| Periféricos ELO | Completo | Alta |

---

## Módulos Faltantes (Prioridad Alta)

### 1. Comandas / Pantalla de Cocina
**Archivo**: `PlaceholderScreen` (no implementado)
**Impacto**: **Crítico** - Sin esto el restaurante no puede operar.

- [ ] Pantalla de comandas pendientes en tiempo real (cocina/bar)
- [ ] Tarjetas de orden con: mesa, items, hora, tiempo transcurrido
- [ ] Estados: Pendiente → En Preparación → Listo
- [ ] Filtro por área (Cocina / Bar / Parrilla)
- [ ] Impresión automática a impresora de cocina al crear orden
- [ ] Alerta sonora/visual para nuevas órdenes
- [ ] Botón "Cocina" en el POS (actualmente no-op `{}`) debe enviar items con flag `sendToKitchen`

### 2. Clientes / CRM Básico
**Archivo**: `PlaceholderScreen` (no implementado)
**Impacto**: **Alto** - Necesario para facturación con crédito fiscal y fidelización.

- [ ] CRUD de clientes: nombre, RNC/cédula, teléfono, email, dirección
- [ ] Historial de compras por cliente
- [ ] Búsqueda rápida en POS por nombre/RNC/teléfono
- [ ] Asignar cliente a factura en el modal de pago
- [ ] Límite de crédito y cuenta por cobrar
- [ ] Selector de cliente en toolbar del POS (actualmente no-op `{}`)

### 3. Reportes / Analítica
**Archivo**: Dashboard usa `MockData` (datos falsos)
**Impacto**: **Alto** - El dueño no puede ver el rendimiento real del negocio.

- [ ] Dashboard con datos reales (ventas del día, turno activo, mesas ocupadas)
- [ ] Reporte de ventas por período (día/semana/mes/año)
- [ ] Ventas por categoría y producto (ranking)
- [ ] Ventas por método de pago
- [ ] Ventas por cajero/turno
- [ ] Reporte de gastos del período
- [ ] Reporte de cierres de caja históricos
- [ ] Gráficos simples (barras/torta) - al menos texto con formato
- [ ] Exportación a PDF/CSV

### 4. Gestión de Usuarios
**Archivo**: `TurnoModel.kt` (3 usuarios hardcodeados)
**Impacto**: **Alto** - No se pueden agregar/editar/eliminar cajeros.

- [ ] CRUD de usuarios: nombre, rol (Admin/Cajero/Mesero), PIN, contraseña
- [ ] Persistencia de usuarios (archivo o DB)
- [ ] Historial de acciones por usuario (auditoría básica)
- [ ] Permisos granulares (ej: cajero no puede ver costos ni modificar productos)

### 5. Inventario / Control de Stock
**Archivo**: Modelo `Product` tiene campos `stock`, `stockAlert`, `controlInventory` pero sin lógica real
**Impacto**: **Alto** - Sin esto no hay control de insumos.

- [ ] Descontar stock automáticamente al vender
- [ ] Alertas de stock bajo en dashboard
- [ ] Registrar entradas de inventario (compras a proveedores)
- [ ] Ajustes de inventario manuales
- [ ] Vista de inventario actual con filtros
- [ ] Historial de movimientos de inventario

---

## Módulos Faltantes (Prioridad Media)

### 6. Proveedores
**Archivo**: `PlaceholderScreen` (no implementado)
- [ ] CRUD de proveedores: nombre, RNC, contacto, teléfono, email
- [ ] Relación con entradas de inventario
- [ ] Historial de compras por proveedor

### 7. Descuentos y Propinas
- [ ] Botón "Aplicar Descuento" en carrito POS actualmente es no-op `{}`
- [ ] Descuento por porcentaje o monto fijo
- [ ] Descuento por producto o sobre el total
- [ ] Códigos de descuento / promociones
- [ ] Agregar propina en modal de pago (sugerida: 10%, 15%, 20%, personalizada)
- [ ] La propina `suggestedTipPercent` ya está en settings pero sin UI

### 8. Delivery / Para Llevar
- [ ] Modo delivery en POS con campos: dirección, teléfono, repartidor
- [ ] Tarifa de envío configurable
- [ ] Seguimiento de órdenes delivery
- [ ] Impresión con datos de entrega

### 9. Sincronización con Servidor
**Archivo**: Settings de servidor existen pero botones son no-ops `{}`
- [ ] Sincronización real de facturas al servidor
- [ ] Sincronización de productos y categorías
- [ ] Cola de sincronización funcional (actualmente datos mock)
- [ ] Reintentos automáticos con backoff
- [ ] Modo offline → cola → sincronizar al reconectar

### 10. Respaldos / Backup
**Archivo**: Settings muestran `mockBackups`, botones son no-ops `{}`
- [ ] Backup automático al cerrar turno
- [ ] Backup manual de toda la data
- [ ] Restaurar desde backup
- [ ] Exportar/importar backup a archivo ZIP
- [ ] Backup a USB o servidor

---

## Módulos Faltantes (Prioridad Baja / Futuro)

### 11. Extras / Modificadores
**Archivo**: `PlaceholderScreen` (no implementado)
Productos pueden tener extras (ej: "sin cebolla", "extra queso", "término medio")
- [ ] CRUD de grupos de modificadores (obligatorios/opcionales)
- [ ] Asignar modificadores a productos/categorías
- [ ] Selección de modificadores al agregar producto al carrito

### 12. Cotizaciones
**Archivo**: `PlaceholderScreen` (no implementado)
- [ ] Crear cotización para eventos/grandes órdenes
- [ ] Convertir cotización a orden/factura
- [ ] Envío de cotización por email/WhatsApp

### 13. Multi-idioma
Todas las strings están hardcodeadas en español.
- [ ] Sistema de traducción (i18n)
- [ ] Inglés como segundo idioma mínimo
- [ ] Selector de idioma en settings

### 14. Pasarela de Pago
- [ ] Integración con tarjeta física (el lector MagTek ya está detectado pero no procesa pagos)
- [ ] Procesamiento de pago con tarjeta (API de banco local)
- [ ] Pago con QR / transferencia bancaria automatizada

### 15. Licencias
**Archivo**: Settings muestra hardcodeado `"Prueba (7 días)"` con botones no-ops `{}`
- [ ] Sistema de licencias real con activación por código
- [ ] Verificación periódica contra servidor
- [ ] Modo offline con días de gracia

---

## Mejoras Técnicas Recomendadas

### Arquitectura
- [ ] Migrar de archivos TSV planos a **SQLite/Room** para mejor rendimiento y consultas
- [ ] Usar **navegación tipada** (Voyager o Decompose) en vez de `when()` con enum
- [ ] Separar UI de lógica con ViewModel pattern consistente (solo POS lo tiene)
- [ ] Inyección de dependencias (Koin) para managers/singletons

### UX/UI
- [ ] Teclado numérico en pantalla para cantidades en POS (no solo en pago)
- [ ] Feedback táctil (haptic) en botones
- [ ] Animaciones de transición entre pantallas
- [ ] Swipe gestures en listas (deslizar para eliminar/editar)
- [ ] Sonidos de alerta (nueva comanda, stock bajo)

### Rendimiento
- [ ] Paginación en listas grandes (productos, facturas)
- [ ] Caché de imágenes de productos
- [ ] Lazy loading en dashboard y reportes

### Seguridad
- [ ] Hash de contraseñas (actualmente texto plano)
- [ ] Bloqueo de pantalla por inactividad (PIN)
- [ ] Registro de auditoría (quién hizo qué y cuándo)
- [ ] Cifrado de datos sensibles (config SMTP, licencia)

### Desktop
- [ ] Implementar envío de email real en desktop (actualmente es stub)
- [ ] Soportar periféricos en desktop (impresora, scanner, lector de tarjetas)

---

## Plan de Acción Recomendado (por dónde empezar)

1. **Comandas / Pantalla Cocina** → Sin esto el restaurante no funciona profesionalmente
2. **Reportes / Dashboard real** → El dueño necesita ver números reales
3. **Gestión de Usuarios** → Agregar/editar cajeros es esencial
4. **Inventario básico** → Descontar stock al vender + alertas
5. **Clientes** → Necesario para facturación fiscal
6. **Proveedores + Entradas de inventario** → Cierra el ciclo de inventario
7. **Sincronización real** → Backup y multi-dispositivo
8. **Descuentos y Propinas** → Completar lo que ya está a medias
9. **Delivery** → Expansión del negocio
10. **Extras/Modificadores** → Personalización de órdenes

---

*Análisis generado automáticamente revisando 83 archivos Kotlin del proyecto.*
