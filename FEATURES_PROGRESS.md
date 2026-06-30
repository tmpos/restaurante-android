# Progreso de Implementacion de Features

## Estado General

- **Proyecto**: TM Restaurant POS (Kotlin Multiplatform + Compose Desktop)
- **Ultima actualizacion**: 12 Jun 2026
- **Compilacion**: Sin errores de compilacion

---

## #1 PAGO DIVIDIDO (Split Payment) — COMPLETADO ✅

### Que hace
Permite dividir el total de una factura entre **multiples metodos de pago** (ej: mitad efectivo, mitad tarjeta).

### Archivos modificados

| Archivo | Cambios |
|---------|---------|
| `ui/components/PaymentModal.kt` | `PaymentSplit` data class, `PaymentResult.paymentSplits`, `SplitPaymentPanel` UI, `SplitOrderSummary` UI, toggle split mode en header |
| `ui/data/InvoiceHistory.kt` | Serializacion/deserializacion de `paymentSplits` en TSV (campo 23), funcion `getPaymentSplits()`, `getDisplayPaymentMethod()` |

### Como funciona
1. En `PaymentModal`, boton "Dividir" en el header activa `splitMode`
2. Panel izquierdo muestra cada metodo de pago con campo para ingresar monto
3. Panel derecho muestra resumen de los splits
4. Al completar, `PaymentSplit` se guarda en `PaymentResult.paymentSplits`
5. `paymentMethod` (texto) se construye como "EFECTIVO + TARJETA" en modo split
6. `InvoiceHistory` serializa los splits en campo 23 del TSV

### Datos
```kotlin
data class PaymentSplit(
    val method: String,
    val amount: Double,
    val percentage: String = "0"
)

// En PaymentResult:
val paymentSplits: List<PaymentSplit> = emptyList()
```

### Formato TSV (campo 23)
```
EFECTIVO:500.00:0|TARJETA:500.00:2.5
```
- Separador entre splits: `|`
- Campos dentro de cada split: `metodo:monto:porcentaje`
- Valores nulos/vacios se omiten

### Posibles mejoras pendientes
- Mostrar los metodos de pago divididos en el resumen de factura impresa
- Agregar split payment al reporte de cierre de caja
- Poder editar splits despues de creados

---

## #8 UNIR/MOVER MESAS (MERGE TABLES) — COMPLETADO ✅

### Que hace
Permite unir dos o mas mesas en una (combinando todos sus productos) y mover productos de una mesa a otra.

### Archivos modificados
| Archivo | Cambios |
|---------|---------|
| `data/MesasManager.kt` | `mergeTables(sourceIds, targetId)`, `moveItemsToMesa(sourceId, targetId)` |
| `screens/mesas/MesasScreen.kt` | Boton "Unir" en toolbar con modo seleccion; `MoveToMesaDialog` para seleccionar destino |
| `screens/mesas/MesaDetailModal.kt` | Boton "Mover a..." con callback `onMoveItems` |

### Como funciona
- **Unir**: Boton "Unir" en toolbar activa modo seleccion → click en mesas a unir → boton verde "Unir" combina todos los items en la ultima seleccionada
- **Mover**: Boton "Mover a..." en MesaDetailModal → seleccionar mesa destino → todos los productos se mueven

### Merge logic en MesasManager
```kotlin
mergeTables(sourceIds, targetId)  // combina items, vacia las fuentes
moveItemsToMesa(sourceId, targetId)  // mueve todo, vacia la fuente
```

## #7 MAPA VISUAL DE MESAS (FLOOR PLAN) — COMPLETADO ✅

### Que hace
Reemplaza la lista/grid simple de mesas con un mapa visual interactivo. Las mesas se muestran como formas (circulos/rectangulos) en posiciones configurables, con colores segun estado. Se pueden arrastrar para reposicionar.

### Archivos modificados
| Archivo | Cambios |
|---------|---------|
| `data/MesasManager.kt` | `Mesa.xPos`, `Mesa.yPos`, `Mesa.shape`, `Mesa.tableWidth`, `Mesa.tableHeight`; `addMesa(name, shape)` con auto-placement; `updateMesaPosition()` |
| `data/TurnoPersistence.kt` | Serializacion de nuevas coordenadas y forma |
| `screens/mesas/MesasScreen.kt` | Toggle grid/floor-plan; canvas con posicionamiento absoluto; drag con long-press; AddMesaDialog con selector de forma |

### Como funciona
1. **Toggle**: Boton en toolbar cambia entre vista grid (original) y vista mapa
2. **Floor plan**: Mesas posicionadas con `Modifier.offset(xPos.dp, yPos.dp)` sobre un canvas
3. **Colores**: Verde (libre), Naranja (ocupada), Rojo (>2h ocupada)
4. **Formas**: Circulo o rectangulo redondeado segun `mesa.shape`
5. **Drag**: Long-press + arrastrar reposiciona; guarda automaticamente al soltar
6. **Nuevas mesas**: Dialogo incluye selector de forma; posicion inicial auto-calculada

### Datos
```kotlin
data class Mesa(
    val id: Int,
    val name: String,
    val items: List<CartItem> = emptyList(),
    val isOccupied: Boolean = false,
    val openedAt: Long = 0L,
    val xPos: Float = -1f,      // -1 = auto-place
    val yPos: Float = -1f,
    val shape: String = "rectangle",
    val tableWidth: Int = 120,
    val tableHeight: Int = 80
)
```

### Pendiente / mejoras futuras
- Zoom y pan del canvas
- Fondo con imagen del local (plano arquitectonico)
- Zonas/salones separados
- Mesas en formas poligonales (mesa larga, L-shape)
- Modo edicion con rejilla de alineacion (snap to grid)

## #6 MODIFICADORES POR PRODUCTO — COMPLETADO ✅

### Que hace
Permite asignar grupos de modificadores a productos (ej: "Tipo de pan: Blanco/Integral/Ajo", "Extra queso: Si/No +RD\$30"). Los modificadores se muestran al agregar el producto al carrito.

### Archivos nuevos
| Archivo | Descripcion |
|---------|-------------|
| `data/ModifierManager.kt` | ModifierGroup, ModifierOption, ModifierSelection + persistencia `modifiers.v1.tsv` |
| `screens/ModifiersScreen.kt` | Pantalla admin para gestionar grupos de modificadores |

### Archivos modificados
| Archivo | Cambios |
|---------|---------|
| `data/Models.kt` | `Product.modifierGroupIds`, `CartItem.selectedModifiers` |
| `data/AppPersistence.kt` | Serializacion de `modifierGroupIds` (campo 21 en products) |
| `components/ProductDetailModal.kt` | UI de seleccion de modificadores (radio/checkbox) por grupo |
| `components/CartPanel.kt` | Muestra modificadores seleccionados debajo del nombre del item |
| `components/ProductFormModal.kt` | Seccion multi-select para asignar grupos de modificadores al producto |
| `screens/ProductsScreen.kt` | Pasa `modifierGroupIds` en create/update |
| `data/InvoiceHistory.kt` | Serializacion de `selectedModifiers` (campo 9 en items) |
| `data/HoldOrderManager.kt` | Serializacion de `selectedModifiers` (campo 9 en items) |
| Navegacion | AppTopBar, SidebarDrawer, MenuDashboardModal, App.kt |

### Como funciona
1. Admin crea grupos de modificadores en pantalla "Modificadores"
   - Grupo: nombre, requerido, max selecciones
   - Opciones: nombre, precio adicional
2. Admin asigna grupos a productos en el formulario de producto
3. En POS, al hacer clic en un producto con modificadores:
   - ProductDetailModal muestra cada grupo con sus opciones
   - Radio button si `maxSelections == 1`, checkbox si > 1
   - Costo adicional se suma al subtotal
   - Info de modificadores se incluye en `extrasNote`
4. En el carrito, los modificadores se muestran debajo del nombre del item
5. En la factura/historial, los modificadores persisten en TSV

### Datos
```kotlin
data class ModifierGroup(
    val id: String,
    val name: String,
    val options: List<ModifierOption>,
    val required: Boolean = false,
    val maxSelections: Int = 1
)
data class ModifierOption(
    val id: String, val name: String, val price: Double = 0.0
)
data class ModifierSelection(
    val groupId: String, val groupName: String,
    val optionId: String, val optionName: String,
    val price: Double = 0.0
)
```

### Pendiente / mejoras futuras
- Modificadores anidados (segun opcion A, mostrar opciones B)
- Precios fijos vs porcentuales
- Orden de modificadores drag-drop
- Copiar modificadores entre productos

## #5 FRACCIONES/PESO PARA PRODUCTOS POR LIBRA — COMPLETADO ✅

### Que hace
Permite vender productos por peso (libras, kilos) con cantidades decimales. Ej: 1.5 lbs de carne a RD\$ 200/lb = RD\$ 300.

### Archivos modificados
| Archivo | Cambios |
|---------|---------|
| `data/Models.kt` | `Product.sellByWeight`, `CartItem.weightQuantity`, `CartItem.effectiveQuantity` computed property |
| `data/PosState.kt` | `rawSubtotal` usa `effectiveQuantity` |
| `components/CartPanel.kt` | Subtotal usa `effectiveQuantity`, CartItemRow muestra lbs si `weightQuantity > 0` |
| `components/PaymentModal.kt` | Totals usan `effectiveQuantity`, display de lbs |
| `components/SplitBillModal.kt` | Calculos por comensal usan `effectiveQuantity` |
| `components/ProductDetailModal.kt` | Input decimal para peso cuando `sellByWeight=true` |
| `screens/pos/PosViewModel.kt` | `addProductToCart` acepta `weightQuantity`, propina usa `effectiveQuantity` |
| `screens/pos/PosScreen.kt` | Ticket items y CFD usan `effectiveQuantity` |
| `screens/pos/PosEvent.kt` | `AddProductToCart.weightQuantity` |
| `screens/pos/ReceiptBuilder.kt` | Calculos de ticket usan `effectiveQuantity` |
| `screens/FacturasScreen.kt` | Item totals y display usan `effectiveQuantity` |
| `screens/mesas/MesaCobrarModal.kt` | `effectiveQuantity` para mesas |
| `screens/mesas/MesaDetailModal.kt` | `effectiveQuantity` en calculos |
| `screens/mesas/MesasScreen.kt` | Total por mesa usa `effectiveQuantity` |
| `data/WebCheckoutManager.kt` | Total usa `effectiveQuantity` |
| `data/InvoiceHistory.kt` | Serializacion de `weightQuantity` (campo 8 en items) |
| `data/HoldOrderManager.kt` | Serializacion de `weightQuantity` (campo 8 en items) |

### Como funciona
1. Producto con `sellByWeight = true` → al agregar al carrito, se muestra input decimal en vez de +/- enteros
2. `weightQuantity` guarda el peso (ej: 1.5 para 1.5 lbs)
3. `effectiveQuantity` retorna `weightQuantity` si > 0, o `quantity.toDouble()` si no
4. Todos los calculos de precio usan `effectiveQuantity`
5. En pantalla, se muestra "1.5 lbs" en vez de "1x" para productos por peso

### Datos
```kotlin
data class Product(
    ...
    val sellByWeight: Boolean = false
)

data class CartItem(
    ...
    val weightQuantity: Double = 0.0
) {
    val effectiveQuantity: Double get() = if (weightQuantity > 0) weightQuantity else quantity.toDouble()
}
```

### Pendiente / mejoras futuras
- Selector de unidad (lbs, kg, oz, g) en producto
- Impresion de peso en ticket
- Bascula integrada (balanza)

## #4 HOLD/RECALL ORDER (SUSPENDER Y RECUPERAR ORDEN) — COMPLETADO ✅

### Que hace
Permite suspender (pausar) una orden en proceso para retomarla despues. Ideal cuando un cliente pide esperar o cuando hay que atender otra orden urgente.

### Archivos nuevos
| Archivo | Descripcion |
|---------|-------------|
| `ui/data/HoldOrderManager.kt` | Persistencia de ordenes suspendidas (`held_orders.v1.tsv`) |
| `ui/components/RecallOrdersModal.kt` | Modal para listar y recuperar ordenes suspendidas |

### Archivos modificados
| Archivo | Cambios |
|---------|---------|
| `ui/components/CartPanel.kt` | Boton Pause activado (`onHoldOrder`), callback `onRecallOrders`, nuevo boton en CartHeader |
| `ui/screens/pos/PosEvent.kt` | `RecallOrder` event |
| `ui/screens/pos/PosViewModel.kt` | Handler `RecallOrder` — restaura items, descuento, cliente |
| `ui/screens/pos/PosScreen.kt` | Wire de hold/recall, integracion de RecallOrdersModal |

### Como funciona
1. **Hold**: Boton Pause (⏸) en CartHeader → guarda items + descuento + cliente en `HoldOrderManager` → limpia carrito
2. **Recall**: Boton en CartHeader (junto al Pause) abre `RecallOrdersModal`
3. Modal lista ordenes suspendidas con label, items y cliente
4. Click "Recuperar" → carga items, descuento y cliente de vuelta al carrito
5. Click 🗑 → elimina orden suspendida

### Datos
```kotlin
data class HeldOrder(
    val id: String,                    // "HOLD-12345678"
    val label: String,                 // "3 items - RD$ 1500"
    val items: List<CartItem>,
    val discountLabel: String = "",
    val discountAmount: Double = 0.0,
    val clientId: String = "",
    val clientName: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
```

### Pendiente / mejoras futuras
- Indicador visual en cart cuando hay ordenes suspendidas
- Tiempo transcurrido desde que se suspendio
- Hold multiple simultaneo

## #3 DEVOLUCIONES/REEMBOLSOS CON MANEJO DE INVENTARIO — COMPLETADO ✅

### Que hace
Permite devolver productos de una factura existente, restaurar el inventario automaticamente y registrar la devolucion en el historial.

### Archivos nuevos
| Archivo | Descripcion |
|---------|-------------|
| `ui/components/ReturnModal.kt` | Modal para seleccionar items a devolver con cantidades y motivos |

### Archivos modificados
| Archivo | Cambios |
|---------|---------|
| `ui/components/PaymentModal.kt` | `ReturnedItem` data class, `PaymentResult.returnedItems` |
| `ui/data/InvoiceHistory.kt` | Serializacion de `returnedItems` (campo 25), `processReturn()` |
| `ui/data/ProductState.kt` | `adjustStock(productId, delta)` metodo generico |
| `ui/screens/pos/PosViewModel.kt` | `deductStockForSale()` — descuenta inventario al completar venta |
| `ui/screens/FacturasScreen.kt` | Boton "Devolver" en FacturaCard, modal ReturnModal |
| `ui/components/InvoiceDrawer.kt` | Boton "Devolver" en InvoiceCard, modal ReturnModal |

### Como funciona
1. **Al vender**: `PosViewModel.deductStockForSale()` descuenta stock de productos con `controlInventory = true`
2. **En FacturasScreen o InvoiceDrawer**: boton "Devolver" en facturas activas (status = "ACTIVA")
3. **ReturnModal**: lista items de la factura con campos +/- para cantidad a devolver + motivo opcional
4. **Al confirmar**:
   - `ProductState.adjustStock()` restaura inventario (+cantidad devuelta)
   - `InvoiceHistory.processReturn()` marca status como "DEVUELTA" o "DEVOLUCION_PARCIAL"
   - `returnedItems` se guarda en TSV campo 25

### Datos
```kotlin
data class ReturnedItem(
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val refundAmount: Double,
    val reason: String = ""
)

// PaymentResult nuevos campos:
val returnedItems: List<ReturnedItem> = emptyList()
```

### Estados de factura
- `"ACTIVA"` — Normal, permite devolucion
- `"ANULADA"` — Anulada (soft cancel), no permite devolucion
- `"DEVUELTA"` — Devuelta totalmente
- `"DEVOLUCION_PARCIAL"` — Devuelta parcialmente

### Pendiente / mejoras futuras
- Reembolso en efectivo o tarjeta desde el POS
- Reporte de devoluciones
- Regenerar inventario desde facturas existentes (migracion)

## #2 DIVIDIR CUENTA (Split Bill) — COMPLETADO ✅

### Diferencia con #1
- **#1 Pago Dividido**: Un solo total pagado con multiples metodos (ej: $1000 mitad efectivo, mitad tarjeta)
- **#2 Dividir Cuenta**: Varios comensales comparten items y cada uno paga su parte (ej: 3 personas, cada una paga lo que consumio)

### Que hace
Permite asignar los productos del carrito a diferentes comensales antes de cobrar. Cada comensal tiene su nombre y subtotal visible. El desglose por comensal se guarda en el historial de facturas.

### Archivos nuevos
| Archivo | Descripcion |
|---------|-------------|
| `ui/components/SplitBillModal.kt` | Modal para agregar comensales y asignar items |

### Archivos modificados
| Archivo | Cambios |
|---------|---------|
| `ui/data/Models.kt` | `CartItem.dinerIndex: Int = 0` |
| `ui/data/InvoiceHistory.kt` | Serializacion de `dinerIndex` en items (campo 7) y `dinerNames` (campo 24) |
| `ui/components/PaymentModal.kt` | `PaymentResult.dinerNames`, parametro `dinerNames`, `DinerBreakdownSection` UI |
| `ui/screens/pos/PosState.kt` | `splitBillDinerNames`, `showSplitBillModal` |
| `ui/screens/pos/PosEvent.kt` | `ShowSplitBillModal`, `DismissSplitBillModal`, `ApplySplitBill` |
| `ui/screens/pos/PosViewModel.kt` | Handlers para split bill, reset en new sale |
| `ui/screens/pos/PosScreen.kt` | Integracion de SplitBillModal, paso de dinerNames a PaymentModal |
| `ui/components/CartPanel.kt` | Boton "Dividir Cuenta"/"Cuenta Dividida", badge en items |

### Como funciona
1. En CartPanel, boton "Dividir Cuenta" abre `SplitBillModal`
2. Agregar comensales (default 2, hasta ~10)
3. Asignar items haciendo clic en los circulos numerados al lado de cada producto
4. Boton "Dividir en partes iguales" para distribuir equitativamente
5. Al aceptar, los items se marcan con `dinerIndex` y se almacenan los nombres
6. En PaymentModal, seccion "COMENSALES" muestra subtotal por persona
7. En CartPanel, items muestran badge circular con el numero de comensal
8. `PaymentResult.dinerNames` se persiste en TSV campo 24

### Datos
```kotlin
// CartItem.dinerIndex (default 0 = no asignado)
data class CartItem(
    ...
    val dinerIndex: Int = 0
)

// PaymentResult.dinerNames
data class PaymentResult(
    ...
    val dinerNames: List<String> = emptyList()
)
```

### Pendiente / mejoras futuras
- Pago individual por comensal (cada uno paga por separado)
- Editar asignacion despues de aplicada
- Mostrar desglose en factura impresa
- Reabrir split bill para modificar

---

## #12 TIEMPOS DE COMIDA (ENTRADA > FUERTE > POSTRE) — COMPLETADO ✅

### Que hace
Permite asignar un "tiempo de comida" (Entrada, Fuerte, Postre) a cada producto al agregarlo al carrito. Las comandas enviadas a cocina muestran y se filtran por tiempo de comida, permitiendo al personal de cocina saber el orden de preparacion.

### Archivos modificados
| Archivo | Cambios |
|---------|---------|
| `data/Models.kt` | `CartItem.courseType: String = ""` |
| `data/ComandaModel.kt` | `ComandaItem.courseType: String = ""` |
| `data/ComandasManager.kt` | Mapeo de `courseType` CartItem → ComandaItem; serializacion en TSV (4to campo en items) |
| `data/InvoiceHistory.kt` | Serializacion de `courseType` (sub-campo 10 en items, usa `getOrElse` para backward compat) |
| `data/HoldOrderManager.kt` | Serializacion de `courseType` (sub-campo 10 en items, usa `getOrElse`) |
| `data/TurnoPersistence.kt` | Serializacion de `courseType` en ITEM (campo 11, usa `getOrElse`) |
| `screens/pos/PosEvent.kt` | `AddProductToCart.courseType` |
| `screens/pos/PosViewModel.kt` | Pasa `courseType` a `CartItem`; no mergea si courseType difiere |
| `screens/pos/PosScreen.kt` | Pasa `courseType` desde `ProductDetailModal` al evento |
| `components/ProductDetailModal.kt` | Callback actualizado con `courseType`; UI con 3 chips: Entrada/Fuerte/Postre |
| `screens/comandas/ComandasScreen.kt` | Filtro por curso (Entrada/Fuerte/Postre); badge de curso en cada item |

### Como funciona
1. **En ProductDetailModal**: 3 chips "Entrada", "Fuerte", "Postre" debajo de modificadores
   - Colores: Cyan (Entrada), Naranja (Fuerte), Morado (Postre)
   - Click selecciona/deselecciona
   - Default: ninguno (sin tiempo de comida)
2. **Al agregar al carrito**: `courseType` se guarda en `CartItem`
   - Si tiene courseType, NO se mergea con items existentes (se crea nuevo line item)
3. **Al enviar a cocina**: `ComandasManager.enviarACocina()` mapea `courseType` de CartItem a ComandaItem
4. **En KDS (ComandasScreen)**:
   - Segunda fila de filtros: "Todas/Entrada/Fuerte/Postre" con conteo
   - Cada item muestra badge de color con el nombre del curso
5. **Serialización**: `courseType` se persiste en todos los formatos (InvoiceHistory, HoldOrderManager, TurnoPersistence, ComandasManager)

### Datos
```kotlin
// CartItem
data class CartItem(
    ...
    val courseType: String = ""  // "", "Entrada", "Fuerte", "Postre"
)

// ComandaItem
data class ComandaItem(
    ...
    val courseType: String = ""
)
```

### Formato TSV en ComandasManager (campo items)
```
productName|quantity|notes|courseType;productName|quantity|notes|courseType
```

### Formato TSV en InvoiceHistory (sub-campo 10)
```
...,courseType
```

### Pendiente / mejoras futuras
- Asignacion de tiempo de comida automatica por categoria de producto
- Envio por oleadas (solo Entradas primero, luego Fuertes, etc.)
- Tiempo estimado de preparacion por curso
- Reordenar items en KDS por curso

---

## #13 KDS PROFESIONAL (KANBAN 3 COLUMNAS) — COMPLETADO ✅

### Que hace
Reemplaza la vista de grilla simple con un layout Kanban de 3 columnas (Pendiente/En Preparacion/Listo). Incluye alerta visual de nuevas ordenes, barra de tiempo de urgencia y conteo de items por comanda.

### Archivos modificados
| Archivo | Cambios |
|---------|---------|
| `screens/comandas/ComandasScreen.kt` | Layout Kanban 3 columnas, flash en nuevas ordenes, barra de progreso temporal, badge de items, chips de columna con colores |

### Como funciona
1. **Kanban**: 3 columnas lado a lado — Pendiente (rojo), En Preparacion (naranja), Listo (verde)
2. **Flash alert**: Nueva orden pendiente parpadea 300ms al aparecer
3. **Timer bar**: Barra de 3px verde/amarillo/rojo segun tiempo transcurrido
4. **Chips**: Cada columna muestra su nombre + conteo con color de fondo distintivo

---

## #14 RECETAS (INGREDIENTES POR PRODUCTO) — COMPLETADO ✅

### Archivos nuevos
| Archivo | Descripcion |
|---------|-------------|
| `data/RecipeManager.kt` | Recipe, RecipeIngredient data classes + TSV persistence |
| `screens/recetas/RecetasScreen.kt` | CRUD de recetas con selector de productos e ingredientes |

### Archivos modificados
| Archivo | Cambios |
|---------|---------|
| `AppTopBar.kt` | Screen.Recetas enum + title |
| `App.kt` | Import + when branch |
| `SidebarDrawer.kt`, `MenuDashboardModal.kt` | Navigation entries |

---

## #15 DESCUENTO DE INVENTARIO AUTOMATICO POR VENTA — COMPLETADO ✅

### Que hace
Al completar una venta, descuenta automaticamente el stock de productos con `controlInventory = true`. Se implemento como parte de #3 Devoluciones/Reembolsos.

### Archivo
| Archivo | Funcion |
|---------|---------|
| `screens/pos/PosViewModel.kt` | `deductStockForSale()` — llama en PaymentComplete |
| `data/ProductState.kt` | `adjustStock(productId, delta)` — nucleo del ajuste |

---

## #16 ORDENES DE COMPRA — COMPLETADO ✅

### Archivos nuevos
| Archivo | Descripcion |
|---------|-------------|
| `data/PurchaseOrderManager.kt` | PurchaseOrder, PurchaseOrderItem + TSV persistence |
| `screens/compras/OrdenesCompraScreen.kt` | CRUD de ordenes de compra, filtros por estado |

### Archivos modificados
| Archivo | Cambios |
|---------|---------|
| `AppTopBar.kt` | Screen.OrdenesCompra enum + title |
| `App.kt` | Import + when branch |
| `SidebarDrawer.kt`, `MenuDashboardModal.kt` | Navigation entries |

---

## #17 AJUSTES DE INVENTARIO CON JUSTIFICACION — COMPLETADO ✅

### Archivos nuevos
| Archivo | Descripcion |
|---------|-------------|
| `data/InventoryAdjustmentManager.kt` | InventoryAdjustment data class + log persistence |

### Archivos modificados
| Archivo | Cambios |
|---------|---------|
| `data/ProductState.kt` | `adjustStock()` ahora acepta `reason`, llama a InventoryAdjustmentManager.log() |
| `screens/InventarioScreen.kt` | Dialog de ajuste con selector de motivo + historial de ajustes |

---

## #18 ALERTAS DE STOCK BAJO — COMPLETADO ✅

### Archivos modificados
| Archivo | Cambios |
|---------|---------|
| `screens/InventarioScreen.kt` | Badges "Bajo Stock" y "Sin Stock" con conteo; tarjetas con fondo de color (rojo/amarillo) y etiqueta "STOCK BAJO"/"SIN STOCK" |

---

## #19 VALORACION DE INVENTARIO — COMPLETADO ✅

### Archivos modificados
| Archivo | Cambios |
|---------|---------|
| `screens/InventarioScreen.kt` | Modal "Valoracion" con calculo de costo total, precio venta total y ganancia potencial |

---

### Implementadas (24 screens)
- POS (venta rapida)
- Gestion de productos
- Gestion de categorias
- Gestion de mesas
- Gestion de clientes
- Gestion de usuarios
- Gestion de turnos
- Gestion de caja
- Gestion de inventario
- Gestion de proveedores
- Gestion de facturas
- Gestion de creditos
- Gestion de comandas (KDS basico)
- Gestion de extras
- Configuracion
- Dashboard
- Login
- Splash
- Cloud sync
- Control de caja
- Base de datos de soporte
- Pantalla de comandas (cocina)

### Pendientes (orden priorizado)
1. ✅ **Pago dividido (split payment)**
2. ✅ **Dividir cuenta (split bill) entre comensales**
3. ✅ **Devoluciones/Reembolsos con manejo de inventario**
4. ✅ **Hold/Recall order (suspender y recuperar orden)**
5. ✅ **Fracciones/peso para productos por libra**
6. ✅ **Modificadores por producto**
7. ✅ **Mapa visual de mesas (floor plan)**
8. ✅ **Unir/mover mesas (merge tables)**
9. ✅ Tiempo de espera por mesa
10. ✅ Asignacion de mesero a mesas
11. ✅ Reservaciones
12. ✅ Tiempos de comida (entrada > fuerte > postre)
13. ✅ KDS profesional (Kanban 3 columnas, alertas visuales, timer por urgencia)
14. ✅ Recetas (ingredientes por producto)
15. ✅ Descuento de inventario automatico por venta
16. ✅ Ordenes de compra
17. ✅ Ajustes de inventario con justificacion
18. ✅ Alertas de stock bajo
19. ✅ Valoracion de inventario
20-50. ⬜ Reportes, multi-moneda, gift cards, cupones, lealtad, CRM, delivery, empleados, tecnico

---

## Arquitectura de la App

### Persistencia
- Archivos TSV planos (no base de datos)
- Cada manager tiene su propio archivo: `productos.v1.tsv`, `clientes.v1.tsv`, `invoices.v1.tsv`, etc.
- Formato: campos separados por tab, con escaping de caracteres especiales

### Managers (patron singleton)
- `ProductManager`, `ClientesManager`, `InvoiceHistory`, `TurnoManager`, etc.
- Usan `mutableStateListOf` o `mutableStateOf` para reactividad en Compose
- `loadXxx()` y `saveXxx()` para persistencia

### PaymentResult (campos actuales)
```
invoiceNumber, ncf, total, subtotalPreTax, taxAmount, paymentMethod,
receivedAmount, change, note, items, surchargeAmount, surchargePercent,
turnoId, timestamp, discountLabel, discountAmount, tipLabel, tipAmount,
customerId, customerName, customerRnc, customerPhone, status, paymentSplits
```

### Reglas de serializacion TSV en InvoiceHistory
- Los items se serializan como JSON en campo 10
- Los paymentSplits se serializan en campo 23
- Usar `getOrElse(index) { ... }` para backward compatibilidad
- No cambiar el orden de campos existentes

---

## Compilacion

### Errores pre-existentes (no relacionados con estos cambios)
- `DateUtils.kt:3` - Expected function 'formatDateTime' has no actual declaration for JVM
- `PrinterService.kt:15` - Expected function 'printWithSystemDialog' has no actual declaration for JVM
- `ServerPrintService.kt:65,71,78` - Expected functions sin actual declaration for JVM

### Comando para compilar
```
./gradlew.bat composeApp:compileKotlinDesktop
```
