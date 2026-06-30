package com.tmrestaurant.wifi

object WebAdminContent {
    fun generate(): String = """
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Admin - TM Restaurant</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;background:#f1f5f9;color:#1e293b;display:flex;min-height:100vh}
.sidebar{width:220px;background:#1e293b;color:#fff;display:flex;flex-direction:column;flex-shrink:0;position:sticky;top:0;height:100vh;overflow-y:auto}
.sidebar h1{font-size:15px;font-weight:750;padding:18px 16px;border-bottom:1px solid #334155;letter-spacing:-.2px}
.sidebar nav{flex:1;padding:8px 0}
.sidebar a{display:flex;align-items:center;gap:10px;padding:10px 16px;color:#94a3b8;text-decoration:none;font-size:13px;font-weight:600;cursor:pointer;border-left:3px solid transparent}
.sidebar a:hover{background:#334155;color:#e2e8f0}
.sidebar a.active{background:#334155;color:#fff;border-left-color:#8b5cf6}
.main{flex:1;display:flex;flex-direction:column;min-width:0}
.header{background:#fff;border-bottom:1px solid #e2e8f0;padding:14px 24px;display:flex;align-items:center;justify-content:space-between;gap:12px}
.header h2{font-size:18px;font-weight:750}
.header .status{font-size:12px;color:#94a3b8;display:flex;align-items:center;gap:6px}
.content{padding:20px 24px;flex:1;overflow-y:auto}
.card{background:#fff;border-radius:14px;padding:20px;margin-bottom:16px;box-shadow:0 1px 3px #0000000d}
.stats-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(180px,1fr));gap:12px;margin-bottom:20px}
.stat-card{background:#fff;border-radius:14px;padding:18px;box-shadow:0 1px 3px #0000000d;border:1px solid #e5e7eb}
.stat-card .label{font-size:11px;color:#94a3b8;font-weight:700;text-transform:uppercase;letter-spacing:.5px}
.stat-card .value{font-size:26px;font-weight:800;color:#1e293b;margin-top:6px}
.stat-card .sub{font-size:12px;color:#64748b;margin-top:4px}
.table-wrap{overflow-x:auto}
table{width:100%;border-collapse:collapse;font-size:13px}
th{text-align:left;padding:10px 12px;border-bottom:2px solid #e2e8f0;color:#64748b;font-size:11px;text-transform:uppercase;letter-spacing:.5px;white-space:nowrap}
td{padding:10px 12px;border-bottom:1px solid #f1f5f9;vertical-align:middle}
tr:hover td{background:#f8fafc}
.badge{display:inline-block;padding:3px 8px;border-radius:999px;font-size:11px;font-weight:700}
.badge-green{background:#dcfce7;color:#166534}
.badge-red{background:#fee2e2;color:#991b1b}
.badge-yellow{background:#fef3c7;color:#92400e}
.badge-blue{background:#dbeafe;color:#1d4ed8}
.badge-gray{background:#f1f5f9;color:#475569}
.btn{display:inline-flex;align-items:center;gap:5px;padding:7px 14px;border:none;border-radius:8px;font-size:12px;font-weight:700;cursor:pointer;transition:background .15s}
.btn-primary{background:#6d28d9;color:#fff}
.btn-primary:hover{background:#5b21b6}
.btn-success{background:#16a34a;color:#fff}
.btn-success:hover{background:#15803d}
.btn-danger{background:#ef4444;color:#fff}
.btn-danger:hover{background:#dc2626}
.btn-warning{background:#f59e0b;color:#fff}
.btn-warning:hover{background:#d97706}
.btn-sm{padding:5px 10px;font-size:11px}
.btn-ghost{background:transparent;color:#64748b;padding:5px 8px}
.btn-ghost:hover{background:#f1f5f9}
.btn-group{display:flex;gap:6px;flex-wrap:wrap}
.toolbar{display:flex;gap:10px;align-items:center;margin-bottom:14px;flex-wrap:wrap}
.toolbar input,.toolbar select{height:38px;border:1px solid #dbe1ea;border-radius:8px;padding:0 12px;font-size:13px;outline:none}
.toolbar input:focus,.toolbar select:focus{border-color:#8b5cf6;box-shadow:0 0 0 3px #8b5cf61f}
.toolbar input{flex:1;min-width:160px}
.modal-overlay{position:fixed;inset:0;background:#00000080;z-index:200;display:flex;align-items:center;justify-content:center}
.modal{background:#fff;border-radius:16px;width:90%;max-width:560px;max-height:90vh;overflow-y:auto;padding:24px}
.modal h3{font-size:17px;font-weight:700;margin-bottom:16px}
.form-group{margin-bottom:12px}
.form-group label{display:block;font-size:12px;font-weight:700;color:#475569;margin-bottom:4px}
.form-group input,.form-group select{width:100%;height:40px;border:1px solid #dbe1ea;border-radius:8px;padding:0 12px;font-size:13px;outline:none}
.form-group input:focus,.form-group select:focus{border-color:#8b5cf6}
.form-row{display:grid;grid-template-columns:1fr 1fr;gap:12px}
.form-actions{display:flex;gap:10px;justify-content:flex-end;margin-top:16px}
.form-actions .btn{padding:9px 20px;font-size:13px}
.tabs{display:flex;gap:6px;margin-bottom:14px;flex-wrap:wrap}
.tab-btn{padding:7px 14px;border:1px solid #e2e8f0;border-radius:8px;background:#fff;color:#64748b;font-size:12px;font-weight:700;cursor:pointer}
.tab-btn.active{background:#6d28d9;border-color:#6d28d9;color:#fff}
.empty-state{text-align:center;padding:40px 20px;color:#94a3b8;font-size:14px}
.spinner{border:3px solid #e5e7eb;border-top:3px solid #6d28d9;border-radius:50%;width:24px;height:24px;animation:spin .7s linear infinite;margin:20px auto}
@keyframes spin{to{transform:rotate(360deg)}}
.toast{position:fixed;top:20px;left:50%;transform:translateX(-50%);background:#1f2937;color:#fff;padding:10px 22px;border-radius:12px;font-size:13px;z-index:300;animation:fadeIn .3s;pointer-events:none}
@keyframes fadeIn{from{opacity:0;transform:translateX(-50%) translateY(-10px)}to{opacity:1;transform:translateX(-50%) translateY(0)}}
.inline-flex{display:inline-flex;align-items:center;gap:6px}
.ml-auto{margin-left:auto}
.mt-8{margin-top:8px}
.text-right{text-align:right}
.w-full{width:100%}
@media(max-width:768px){.sidebar{width:56px}.sidebar h1{font-size:0;padding:14px}.sidebar h1::after{content:'⚙';font-size:20px}.sidebar a span{display:none}.sidebar a{justify-content:center;padding:12px}.content{padding:14px}.stats-grid{grid-template-columns:repeat(2,1fr)}.modal{width:95%;padding:16px}.form-row{grid-template-columns:1fr}}
@media(max-width:480px){.sidebar{width:48px}.header{padding:10px 14px}.header h2{font-size:15px}.stats-grid{grid-template-columns:1fr}}
</style>
</head>
<body>
<aside class="sidebar">
  <h1>TM Admin</h1>
  <nav>
    <a onclick="showSection('dashboard')" id="nav-dashboard" class="active">📊 <span>Dashboard</span></a>
    <a onclick="showSection('products')" id="nav-products">📦 <span>Productos</span></a>
    <a onclick="showSection('categories')" id="nav-categories">🏷️ <span>Categorías</span></a>
    <a onclick="showSection('inventory')" id="nav-inventory">📋 <span>Inventario</span></a>
    <a onclick="showSection('invoices')" id="nav-invoices">🧾 <span>Facturas</span></a>
    <a onclick="showSection('comandas')" id="nav-comandas">🍳 <span>Comandas</span></a>
    <a onclick="showSection('clients')" id="nav-clients">👥 <span>Clientes</span></a>
    <a onclick="showSection('users')" id="nav-users">🔐 <span>Usuarios</span></a>
    <a onclick="showSection('turnos')" id="nav-turnos">🔄 <span>Turnos</span></a>
    <a onclick="showSection('settings')" id="nav-settings">⚙️ <span>Configuración</span></a>
  </nav>
</aside>
<div class="main">
  <div class="header">
    <h2 id="sectionTitle">Dashboard</h2>
    <div class="status" id="statusBar">📶 Conectando...</div>
  </div>
  <div class="content" id="mainContent"><div class="spinner"></div></div>
</div>
<div id="modalContainer"></div>
<div id="toastContainer"></div>

<script>
var currentSection = 'dashboard';
var productsCache = [];
var categoriesCache = [];
var clientsCache = [];
var usersCache = [];

function showSection(name){
  currentSection = name;
  document.querySelectorAll('.sidebar a').forEach(function(a){a.classList.remove('active')});
  var el = document.getElementById('nav-'+name);
  if(el) el.classList.add('active');
  document.getElementById('sectionTitle').textContent = name==='dashboard'?'Dashboard'
    :name==='products'?'Productos'
    :name==='categories'?'Categorías'
    :name==='inventory'?'Inventario'
    :name==='invoices'?'Facturas'
    :name==='comandas'?'Comandas'
    :name==='clients'?'Clientes'
    :name==='users'?'Usuarios'
    :name==='turnos'?'Turnos'
    :'Configuración';
  renderSection(name);
}

function renderSection(name){
  var c = document.getElementById('mainContent');
  if(name==='dashboard') loadDashboard(c);
  else if(name==='products') loadProducts(c);
  else if(name==='categories') loadCategories(c);
  else if(name==='inventory') loadInventory(c);
  else if(name==='invoices') loadInvoices(c);
  else if(name==='comandas') loadComandas(c);
  else if(name==='clients') loadClients(c);
  else if(name==='users') loadUsers(c);
  else if(name==='turnos') loadTurnos(c);
  else if(name==='settings') loadSettings(c);
}

function api(path, method, body){
  var opts = {method: method||'GET', headers:{}};
  if(body){opts.headers['Content-Type']='application/json';opts.body=JSON.stringify(body)}
  return fetch(path, opts).then(function(r){return r.json()});
}

function showToast(msg){
  var t=document.getElementById('toastContainer');
  t.innerHTML='<div class="toast">'+msg+'</div>';
  setTimeout(function(){t.innerHTML=''},2500);
}

function esc(str){return (str||'').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;')}
function money(v){return 'RD$ '+(v||0).toFixed(2)}
function dateStr(ts){var d=new Date(ts);return d.toLocaleDateString()+' '+d.toLocaleTimeString()}

// ===================== DASHBOARD =====================
function loadDashboard(c){
  c.innerHTML = '<div class="spinner"></div>';
  api('/api/admin/dashboard').then(function(d){
    if(!d.ok){c.innerHTML='<div class="empty-state">Error al cargar dashboard</div>';return}
    var maxVal = Math.max(d.totalProductos, d.totalFacturas, d.totalClientes, 1);
    c.innerHTML = '<div class="stats-grid">'+
      '<div class="stat-card"><div class="label">Productos</div><div class="value">'+d.totalProductos+'</div><div class="sub">'+d.totalActivos+' activos</div></div>'+
      '<div class="stat-card"><div class="label">Stock Bajo</div><div class="value" style="color:'+(d.bajosStock>0?'#ef4444':'#16a34a')+'">'+d.bajosStock+'</div><div class="sub">requieren atención</div></div>'+
      '<div class="stat-card"><div class="label">Facturas</div><div class="value">'+d.totalFacturas+'</div><div class="sub">'+money(d.totalVendido)+' vendido</div></div>'+
      '<div class="stat-card"><div class="label">Clientes</div><div class="value">'+d.totalClientes+'</div><div class="sub">registrados</div></div>'+
      '<div class="stat-card"><div class="label">Turno</div><div class="value" style="color:'+(d.turnoActivo?'#16a34a':'#94a3b8')+'">'+(d.turnoActivo?'Activo':'Inactivo')+'</div><div class="sub">'+(d.turnoActivo?esc(d.turnoNombre)+' - '+esc(d.turnoCajero):'Sin turno abierto')+'</div></div>'+
    '</div>'+
    '<div class="card"><h3 style="font-size:14px;margin-bottom:12px;color:#475569;font-weight:700">📊 Estadísticas</h3>'+
    '<div style="display:flex;flex-direction:column;gap:10px">'+
      '<div><div style="display:flex;justify-content:space-between;font-size:12px;margin-bottom:4px"><span>Productos ('+d.totalProductos+')</span><span>'+Math.round(d.totalProductos/maxVal*100)+'%</span></div><div style="height:20px;background:#f1f5f9;border-radius:10px;overflow:hidden"><div style="height:100%;width:'+Math.round(d.totalProductos/maxVal*100)+'%;background:linear-gradient(90deg,#6d28d9,#8b5cf6);border-radius:10px;transition:width .5s"></div></div></div>'+
      '<div><div style="display:flex;justify-content:space-between;font-size:12px;margin-bottom:4px"><span>Facturas ('+d.totalFacturas+')</span><span>'+Math.round(d.totalFacturas/maxVal*100)+'%</span></div><div style="height:20px;background:#f1f5f9;border-radius:10px;overflow:hidden"><div style="height:100%;width:'+Math.round(d.totalFacturas/maxVal*100)+'%;background:linear-gradient(90deg,#16a34a,#22c55e);border-radius:10px;transition:width .5s"></div></div></div>'+
      '<div><div style="display:flex;justify-content:space-between;font-size:12px;margin-bottom:4px"><span>Clientes ('+d.totalClientes+')</span><span>'+Math.round(d.totalClientes/maxVal*100)+'%</span></div><div style="height:20px;background:#f1f5f9;border-radius:10px;overflow:hidden"><div style="height:100%;width:'+Math.round(d.totalClientes/maxVal*100)+'%;background:linear-gradient(90deg,#2563eb,#3b82f6);border-radius:10px;transition:width .5s"></div></div></div>'+
      '<div><div style="display:flex;justify-content:space-between;font-size:12px;margin-bottom:4px"><span>Vendido: '+money(d.totalVendido)+'</span></div></div>'+
    '</div></div>'+
    '<div class="card"><h3 style="font-size:14px;margin-bottom:10px;color:#475569;font-weight:700">⚡ Acciones rápidas</h3>'+
    '<div style="display:flex;gap:8px;flex-wrap:wrap">'+
      '<button class="btn btn-primary" onclick="showSection(\'products\')">📦 Gestionar Productos</button>'+
      '<button class="btn btn-success" onclick="showSection(\'invoices\')">🧾 Ver Facturas</button>'+
      '<button class="btn btn-warning" onclick="showSection(\'inventory\')">📋 Revisar Inventario</button>'+
      '<button class="btn" style="background:#f1f5f9;color:#475569" onclick="showSection(\'settings\')">⚙️ Configuración</button>'+
    '</div></div>';
    document.getElementById('statusBar').textContent = '📶 '+d.totalProductos+' productos | '+d.totalFacturas+' facturas';
  }).catch(function(){c.innerHTML='<div class="empty-state">Error de conexión con el servidor</div>'});
}

// ===================== PRODUCTS =====================
var productSearch = '';
var productCat = '';
var productActive = '';

function loadProducts(c){
  var params = '?q='+encodeURIComponent(productSearch);
  if(productCat) params += '&cat='+encodeURIComponent(productCat);
  if(productActive) params += '&active='+productActive;
  c.innerHTML = '<div class="toolbar"><input placeholder="Buscar producto..." oninput="productSearch=this.value;loadProducts(document.getElementById(\'mainContent\'))"><select onchange="productCat=this.value;loadProducts(document.getElementById(\'mainContent\'))"><option value="">Todas las categorías</option></select><select onchange="productActive=this.value;loadProducts(document.getElementById(\'mainContent\'))"><option value="">Todos</option><option value="true">Activos</option><option value="false">Inactivos</option></select><button class="btn btn-primary" onclick="showProductForm()">+ Nuevo</button></div><div class="spinner"></div>';
  api('/api/admin/categories').then(function(cats){
    if(Array.isArray(cats)){categoriesCache=cats;
      var sel = c.querySelector('select');
      if(sel){sel.innerHTML='<option value="">Todas las categorías</option>'+cats.map(function(ca){return '<option value="'+esc(ca.nombre)+'" '+(productCat===ca.nombre?'selected':'')+'>'+esc(ca.nombre)+'</option>'}).join('')}
    }
  }).catch(function(){});
  api('/api/admin/products'+params).then(function(prods){
    if(!Array.isArray(prods)){c.innerHTML='<div class="empty-state">Error al cargar productos</div>';return}
    productsCache=prods;
    if(!prods.length){c.querySelector('.spinner').outerHTML='<div class="empty-state">No hay productos</div>';return}
    var tbl = '<div class="table-wrap"><table><thead><tr><th>ID</th><th>Nombre</th><th>Código</th><th>Categoría</th><th>Precio</th><th>Stock</th><th>Estado</th><th>Acciones</th></tr></thead><tbody>'+
      prods.map(function(p){
        return '<tr><td>'+p.id+'</td><td><strong>'+esc(p.nombre)+'</strong></td><td>'+esc(p.codigo)+'</td><td>'+esc(p.categoria)+'</td><td>'+money(p.precio)+'</td><td>'+
          (p.controlInventario?'<span style="color:'+(p.stock<=p.alertaStock?'#ef4444':'#16a34a')+'">'+p.stock+'</span>':'—')+'</td><td>'+
          (p.activo?'<span class="badge badge-green">Activo</span>':'<span class="badge badge-red">Inactivo</span>')+'</td><td>'+
          '<div class="btn-group"><button class="btn btn-sm btn-primary" onclick="showProductForm('+p.id+')">✏️</button><button class="btn btn-sm btn-danger" onclick="deleteProduct('+p.id+')">🗑</button></div></td></tr>';
      }).join('')+'</tbody></table></div>';
    c.querySelector('.spinner').outerHTML = tbl;
  }).catch(function(){c.innerHTML='<div class="empty-state">Error de conexión</div>'});
}

function showProductForm(id){
  var p = id ? productsCache.find(function(x){return x.id===id}) : null;
  var title = p ? 'Editar Producto' : 'Nuevo Producto';
  var cats = categoriesCache;
  var modal = document.getElementById('modalContainer');
  modal.innerHTML = '<div class="modal-overlay" onclick="closeModal(event)"><div class="modal" onclick="event.stopPropagation()"><h3>'+title+'</h3>'+
    '<div class="form-row"><div class="form-group"><label>Nombre</label><input id="pf-name" value="'+esc(p?p.nombre:'')+'"></div>'+
    '<div class="form-group"><label>Código</label><input id="pf-code" value="'+esc(p?p.codigo:'')+'"></div></div>'+
    '<div class="form-row"><div class="form-group"><label>Precio</label><input id="pf-price" type="number" step="0.01" value="'+(p?p.precio:'')+'"></div>'+
    '<div class="form-group"><label>Costo</label><input id="pf-cost" type="number" step="0.01" value="'+(p?p.costo:'0')+'"></div></div>'+
    '<div class="form-row"><div class="form-group"><label>Categoría</label><select id="pf-cat">'+(cats.length?cats.map(function(c){return '<option value="'+esc(c.nombre)+'" '+(p&&p.categoria===c.nombre?'selected':'')+'>'+esc(c.nombre)+'</option>'}).join(''):'')+'</select></div>'+
    '<div class="form-group"><label>ITBIS %</label><input id="pf-tax" type="number" step="0.1" value="'+(p?p.itbis:'18')+'"></div></div>'+
    '<div class="form-row"><div class="form-group"><label>Stock</label><input id="pf-stock" type="number" value="'+(p?p.stock:'0')+'"></div>'+
    '<div class="form-group"><label>Alerta Stock</label><input id="pf-alert" type="number" value="'+(p?p.alertaStock:'1')+'"></div></div>'+
    '<div class="form-row"><div class="form-group"><label>Descripción</label><input id="pf-desc" value="'+esc(p?p.descripcion:'')+'"></div>'+
    '<div class="form-group"><label>Código de Barra</label><input id="pf-barcode" value="'+esc(p?p.codigoBarra:'')+'"></div></div>'+
    '<div class="form-row"><div class="form-group"><label><input id="pf-active" type="checkbox" '+(p&&!p.activo?'':'checked')+'> Activo</label></div>'+
    '<div class="form-group"><label><input id="pf-pos" type="checkbox" '+(p&&!p.venderPos?'':'checked')+'> Vender en POS</label></div></div>'+
    '<div class="form-row"><div class="form-group"><label><input id="pf-cocina" type="checkbox" '+(p&&!p.cocina?'':'checked')+'> Enviar a cocina</label></div>'+
    '<div class="form-group"><label><input id="pf-bar" type="checkbox" '+(p&&p.bar?'checked':'')+'> Enviar a bar</label></div></div>'+
    '<div class="form-row"><div class="form-group"><label><input id="pf-inv" type="checkbox" '+(p&&!p.controlInventario?'':'checked')+'> Control inventario</label></div>'+
    '<div class="form-group"><label><input id="pf-peso" type="checkbox" '+(p&&p.venderPorPeso?'checked':'')+'> Vender por peso</label></div></div>'+
    '<div class="form-actions"><button class="btn btn-ghost" onclick="closeModal()">Cancelar</button><button class="btn btn-primary" onclick="saveProduct('+(id||'null')+')">'+(p?'Actualizar':'Crear')+'</button></div></div></div>';
}

function saveProduct(id){
  var body = {
    nombre: document.getElementById('pf-name').value,
    codigo: document.getElementById('pf-code').value,
    precio: parseFloat(document.getElementById('pf-price').value)||0,
    costo: parseFloat(document.getElementById('pf-cost').value)||0,
    categoria: document.getElementById('pf-cat').value,
    itbis: parseFloat(document.getElementById('pf-tax').value)||18,
    stock: parseInt(document.getElementById('pf-stock').value)||0,
    alertaStock: parseInt(document.getElementById('pf-alert').value)||1,
    descripcion: document.getElementById('pf-desc').value,
    codigoBarra: document.getElementById('pf-barcode').value,
    activo: document.getElementById('pf-active').checked,
    venderPos: document.getElementById('pf-pos').checked,
    cocina: document.getElementById('pf-cocina').checked,
    bar: document.getElementById('pf-bar').checked,
    controlInventario: document.getElementById('pf-inv').checked,
    venderPorPeso: document.getElementById('pf-peso').checked
  };
  var url = id ? '/api/admin/products/'+id : '/api/admin/products';
  var method = id ? 'PUT' : 'POST';
  api(url, method, body).then(function(r){
    if(r.ok){showToast(id?'Producto actualizado':'Producto creado');closeModal();renderSection('products')}
    else showToast('Error: '+(r.error||'desconocido'));
  }).catch(function(){showToast('Error de conexión')});
}

function deleteProduct(id){
  if(!confirm('¿Eliminar producto #'+id+'?'))return;
  api('/api/admin/products/'+id, 'DELETE').then(function(r){
    if(r.ok){showToast('Producto eliminado');renderSection('products')}
    else showToast('Error: '+(r.error||''));
  }).catch(function(){showToast('Error de conexión')});
}

// ===================== CATEGORIES =====================
function loadCategories(c){
  c.innerHTML = '<div class="toolbar"><button class="btn btn-primary" onclick="showCategoryForm()">+ Nueva Categoría</button></div><div class="spinner"></div>';
  api('/api/admin/categories').then(function(cats){
    if(!Array.isArray(cats)){c.innerHTML='<div class="empty-state">Error</div>';return}
    categoriesCache=cats;
    if(!cats.length){c.querySelector('.spinner').outerHTML='<div class="empty-state">No hay categorías</div>';return}
    var tbl = '<div class="table-wrap"><table><thead><tr><th>ID</th><th>Nombre</th><th>Descripción</th><th>Color</th><th>Orden</th><th>Estado</th><th>Visible POS</th><th>Acciones</th></tr></thead><tbody>'+
      cats.map(function(ca){
        return '<tr><td>'+ca.id+'</td><td><strong>'+esc(ca.nombre)+'</strong></td><td>'+esc(ca.descripcion)+'</td><td><span class="badge" style="background:'+ca.color.toLowerCase()+'22;color:'+ca.color.toLowerCase()+'">'+ca.color+'</span></td><td>'+ca.orden+'</td><td>'+(ca.activo?'<span class="badge badge-green">Activo</span>':'<span class="badge badge-red">Inactivo</span>')+'</td><td>'+(ca.visiblePos?'<span class="badge badge-green">Sí</span>':'<span class="badge badge-gray">No</span>')+'</td><td><div class="btn-group"><button class="btn btn-sm btn-primary" onclick="showCategoryForm('+ca.id+')">✏️</button><button class="btn btn-sm btn-danger" onclick="deleteCategory('+ca.id+')">🗑</button></div></td></tr>';
      }).join('')+'</tbody></table></div>';
    c.querySelector('.spinner').outerHTML = tbl;
  }).catch(function(){c.innerHTML='<div class="empty-state">Error de conexión</div>'});
}

function showCategoryForm(id){
  var ca = id ? categoriesCache.find(function(x){return x.id===id}) : null;
  var modal = document.getElementById('modalContainer');
  modal.innerHTML = '<div class="modal-overlay" onclick="closeModal(event)"><div class="modal" onclick="event.stopPropagation()"><h3>'+(ca?'Editar Categoría':'Nueva Categoría')+'</h3>'+
    '<div class="form-group"><label>Nombre</label><input id="cf-name" value="'+esc(ca?ca.nombre:'')+'"></div>'+
    '<div class="form-group"><label>Descripción</label><input id="cf-desc" value="'+esc(ca?ca.descripcion:'')+'"></div>'+
    '<div class="form-row"><div class="form-group"><label>Color</label><select id="cf-color"><option value="Gray" '+(ca&&ca.color==='Gray'?'selected':'')+'>Gris</option><option value="Red" '+(ca&&ca.color==='Red'?'selected':'')+'>Rojo</option><option value="Green" '+(ca&&ca.color==='Green'?'selected':'')+'>Verde</option><option value="Blue" '+(ca&&ca.color==='Blue'?'selected':'')+'>Azul</option><option value="Yellow" '+(ca&&ca.color==='Yellow'?'selected':'')+'>Amarillo</option><option value="Purple" '+(ca&&ca.color==='Purple'?'selected':'')+'>Púrpura</option><option value="Orange" '+(ca&&ca.color==='Orange'?'selected':'')+'>Naranja</option></select></div>'+
    '<div class="form-group"><label>Orden</label><input id="cf-order" type="number" value="'+(ca?ca.orden:'0')+'"></div></div>'+
    '<div class="form-row"><div class="form-group"><label><input id="cf-active" type="checkbox" '+(ca&&!ca.activo?'':'checked')+'> Activo</label></div>'+
    '<div class="form-group"><label><input id="cf-visible" type="checkbox" '+(ca&&!ca.visiblePos?'':'checked')+'> Visible en POS</label></div></div>'+
    '<div class="form-actions"><button class="btn btn-ghost" onclick="closeModal()">Cancelar</button><button class="btn btn-primary" onclick="saveCategory('+(id||'null')+')">'+(ca?'Actualizar':'Crear')+'</button></div></div></div>';
}

function saveCategory(id){
  var body = {
    nombre: document.getElementById('cf-name').value,
    descripcion: document.getElementById('cf-desc').value,
    color: document.getElementById('cf-color').value,
    orden: parseInt(document.getElementById('cf-order').value)||0,
    activo: document.getElementById('cf-active').checked,
    visiblePos: document.getElementById('cf-visible').checked
  };
  var url = id ? '/api/admin/categories/'+id : '/api/admin/categories';
  var method = id ? 'PUT' : 'POST';
  api(url, method, body).then(function(r){
    if(r.ok){showToast(id?'Categoría actualizada':'Categoría creada');closeModal();renderSection('categories')}
    else showToast('Error: '+(r.error||''));
  }).catch(function(){showToast('Error de conexión')});
}

function deleteCategory(id){
  if(!confirm('¿Eliminar categoría #'+id+'?'))return;
  api('/api/admin/categories/'+id, 'DELETE').then(function(r){
    if(r.ok){showToast('Categoría eliminada');renderSection('categories')}
    else showToast('Error: '+(r.error||''));
  }).catch(function(){showToast('Error de conexión')});
}

// ===================== INVENTORY =====================
function loadInventory(c){
  c.innerHTML = '<div class="spinner"></div>';
  api('/api/admin/inventory').then(function(items){
    if(!Array.isArray(items)){c.innerHTML='<div class="empty-state">Error</div>';return}
    if(!items.length){c.innerHTML='<div class="empty-state">No hay productos con control de inventario</div>';return}
    c.innerHTML = '<div class="table-wrap"><table><thead><tr><th>Producto</th><th>Código</th><th>Stock</th><th>Alerta</th><th>Estado</th><th>Costo</th><th>Acción</th></tr></thead><tbody>'+
      items.map(function(it){
        var cls = it.bajo ? 'badge-red' : 'badge-green';
        return '<tr><td><strong>'+esc(it.nombre)+'</strong></td><td>'+esc(it.codigo)+'</td><td style="color:'+(it.bajo?'#ef4444':'#16a34a')+';font-weight:700">'+it.stock+'</td><td>'+it.alerta+'</td><td><span class="badge '+cls+'">'+(it.bajo?'Bajo':'Normal')+'</span></td><td>'+money(it.costo)+'</td><td><button class="btn btn-sm btn-warning" onclick="showAdjustForm('+it.id+',\''+esc(it.nombre)+'\','+it.stock+')">Ajustar</button></td></tr>';
      }).join('')+'</tbody></table></div>';
  }).catch(function(){c.innerHTML='<div class="empty-state">Error de conexión</div>'});
}

function showAdjustForm(id,name,currentStock){
  var modal = document.getElementById('modalContainer');
  modal.innerHTML = '<div class="modal-overlay" onclick="closeModal(event)"><div class="modal" onclick="event.stopPropagation()"><h3>Ajustar Stock: '+esc(name)+'</h3>'+
    '<div class="form-group"><label>Stock actual: <strong>'+currentStock+'</strong></label></div>'+
    '<div class="form-group"><label>Cambio (positivo=entrada, negativo=salida)</label><input id="af-delta" type="number" value="0"></div>'+
    '<div class="form-group"><label>Razón</label><input id="af-reason" placeholder="Ej: Ajuste de inventario"></div>'+
    '<div class="form-actions"><button class="btn btn-ghost" onclick="closeModal()">Cancelar</button><button class="btn btn-primary" onclick="adjustInventory('+id+')">Guardar</button></div></div></div>';
}

function adjustInventory(id){
  var delta = parseInt(document.getElementById('af-delta').value)||0;
  var razon = document.getElementById('af-reason').value || 'Ajuste web';
  if(delta===0){showToast('El cambio no puede ser 0');return}
  api('/api/admin/inventory/adjust', 'POST', {id:id, delta:delta, razon:razon}).then(function(r){
    if(r.ok){showToast('Stock ajustado: '+r.nuevoStock);closeModal();renderSection('inventory')}
    else showToast('Error: '+(r.error||''));
  }).catch(function(){showToast('Error de conexión')});
}

// ===================== INVOICES =====================
var invoiceSearch = '';
var invoicePage = 1;

function loadInvoices(c){
  var params = '?page='+invoicePage+'&limit=50';
  if(invoiceSearch) params += '&q='+encodeURIComponent(invoiceSearch);
  c.innerHTML = '<div class="toolbar"><input placeholder="Buscar factura (número, NCF, cliente)..." oninput="invoiceSearch=this.value;invoicePage=1;loadInvoices(document.getElementById(\'mainContent\'))">'+
    '<button class="btn btn-ghost" onclick="invoicePage=Math.max(1,invoicePage-1);loadInvoices(document.getElementById(\'mainContent\'))" id="prevPageBtn" style="display:none">← Anterior</button>'+
    '<button class="btn btn-ghost" onclick="invoicePage++;loadInvoices(document.getElementById(\'mainContent\'))" id="nextPageBtn">Siguiente →</button></div><div class="spinner"></div>';
  api('/api/admin/invoices'+params).then(function(data){
    if(!data || !data.facturas){c.innerHTML='<div class="empty-state">Error al cargar facturas</div>';return}
    var facturas = data.facturas;
    if(!facturas.length){c.querySelector('.spinner').outerHTML='<div class="empty-state">No hay facturas'+(invoiceSearch?' que coincidan con la búsqueda':'')+'</div>';return}
    document.getElementById('prevPageBtn').style.display = invoicePage>1?'inline-flex':'none';
    document.getElementById('nextPageBtn').style.display = data.total > invoicePage*50?'inline-flex':'none';
    var tbl = '<div class="table-wrap"><table><thead><tr><th># Factura</th><th>NCF</th><th>Cliente</th><th>Total</th><th>Pago</th><th>Items</th><th>Fecha</th><th>Acción</th></tr></thead><tbody>'+
      facturas.map(function(f){
        var statusClass = (f.estado||'ACTIVA')==='ANULADA'?'badge-red':'badge-green';
        return '<tr><td><strong>'+esc(f.numero)+'</strong></td><td style="font-size:11px">'+esc(f.ncf)+'</td><td>'+esc(f.cliente||'Consumidor Final')+'</td><td>'+money(f.total)+'</td><td>'+esc(f.pago)+'</td><td>'+f.items+'</td><td style="font-size:12px">'+dateStr(f.fecha)+'</td><td><button class="btn btn-sm btn-danger" onclick="deleteInvoice(\''+esc(f.numero)+'\')">🗑</button></td></tr>';
      }).join('')+'</tbody></table></div>'+
      '<div style="text-align:center;margin-top:10px;font-size:12px;color:#94a3b8">Página '+data.pagina+' — Total: '+data.total+' facturas</div>';
    c.querySelector('.spinner').outerHTML = tbl;
  }).catch(function(){c.innerHTML='<div class="empty-state">Error de conexión</div>'});
}

function deleteInvoice(num){
  if(!confirm('¿Eliminar factura '+num+'?'))return;
  api('/api/admin/invoices/'+encodeURIComponent(num), 'DELETE').then(function(r){
    if(r.ok){showToast('Factura eliminada');renderSection('invoices')}
    else showToast('Error: '+(r.error||''));
  }).catch(function(){showToast('Error de conexión')});
}

// ===================== COMANDA =====================
function loadComandas(c){
  c.innerHTML = '<div class="toolbar"><button class="btn btn-primary" onclick="loadComandas(document.getElementById(\'mainContent\'))">🔄 Refrescar</button></div><div class="spinner"></div>';
  api('/api/admin/comandas').then(function(coms){
    if(!Array.isArray(coms)){c.innerHTML='<div class="empty-state">Error</div>';return}
    if(!coms.length){c.querySelector('.spinner').outerHTML='<div class="empty-state">No hay comandas activas</div>';return}
    var html = '<div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(280px,1fr));gap:12px">'+
      coms.map(function(cmd){
        var statusClass = cmd.estado==='Pendiente'?'badge-yellow':cmd.estado==='EnPreparacion'?'badge-blue':'badge-green';
        return '<div class="card" style="margin:0">'+
          '<div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:10px">'+
          '<div><strong>'+esc(cmd.mesa)+'</strong><br><span style="font-size:12px;color:#94a3b8">'+esc(cmd.area)+'</span></div>'+
          '<span class="badge '+statusClass+'">'+cmd.estado+'</span></div>'+
          '<div style="font-size:12px;color:#64748b;margin-bottom:10px">'+(cmd.productos||[]).map(function(it){return '<div style="padding:3px 0">'+it.cantidad+'x '+esc(it.nombre)+(it.nota?' <span style="color:#b45309;font-size:11px">('+esc(it.nota)+')</span>':'')+'</div>'}).join('')+'</div>'+
          '<div class="btn-group">'+
          (cmd.estado==='Pendiente'?'<button class="btn btn-sm btn-warning" onclick="updateComanda(\''+esc(cmd.id)+'\',\'EnPreparacion\')">Iniciar Preparación</button>':'')+
          (cmd.estado==='EnPreparacion'?'<button class="btn btn-sm btn-success" onclick="updateComanda(\''+esc(cmd.id)+'\',\'Listo\')">Marcar Listo</button>':'')+
          '</div></div>';
      }).join('')+'</div>';
    c.querySelector('.spinner').outerHTML = html;
  }).catch(function(){c.innerHTML='<div class="empty-state">Error de conexión</div>'});
}

function updateComanda(id,estado){
  api('/api/admin/comandas/'+encodeURIComponent(id), 'PUT', {estado:estado}).then(function(r){
    if(r.ok){showToast('Comanda actualizada a '+estado);renderSection('comandas')}
    else showToast('Error: '+(r.error||''));
  }).catch(function(){showToast('Error de conexión')});
}

// ===================== CLIENTS =====================
function loadClients(c){
  c.innerHTML = '<div class="toolbar"><button class="btn btn-primary" onclick="showClientForm()">+ Nuevo Cliente</button></div><div class="spinner"></div>';
  api('/api/admin/clientes').then(function(cls){
    if(!Array.isArray(cls)){c.innerHTML='<div class="empty-state">Error</div>';return}
    clientsCache=cls;
    if(!cls.length){c.querySelector('.spinner').outerHTML='<div class="empty-state">No hay clientes</div>';return}
    var tbl = '<div class="table-wrap"><table><thead><tr><th>ID</th><th>Nombre</th><th>RNC</th><th>Teléfono</th><th>Email</th><th>Tipo</th><th>Límite Crédito</th><th>Acción</th></tr></thead><tbody>'+
      cls.map(function(cl){
        return '<tr><td>'+esc(cl.id)+'</td><td><strong>'+esc(cl.nombre)+'</strong></td><td>'+esc(cl.rnc)+'</td><td>'+esc(cl.telefono)+'</td><td>'+esc(cl.email)+'</td><td>'+esc(cl.tipo)+'</td><td>'+money(cl.limiteCredito)+'</td><td><button class="btn btn-sm btn-danger" onclick="deleteClient(\''+esc(cl.id)+'\')">🗑</button></td></tr>';
      }).join('')+'</tbody></table></div>';
    c.querySelector('.spinner').outerHTML = tbl;
  }).catch(function(){c.innerHTML='<div class="empty-state">Error de conexión</div>'});
}

function showClientForm(){
  var modal = document.getElementById('modalContainer');
  modal.innerHTML = '<div class="modal-overlay" onclick="closeModal(event)"><div class="modal" onclick="event.stopPropagation()"><h3>Nuevo Cliente</h3>'+
    '<div class="form-row"><div class="form-group"><label>Nombre</label><input id="clf-name"></div>'+
    '<div class="form-group"><label>RNC/Cédula</label><input id="clf-rnc"></div></div>'+
    '<div class="form-row"><div class="form-group"><label>Teléfono</label><input id="clf-phone"></div>'+
    '<div class="form-group"><label>Email</label><input id="clf-email" type="email"></div></div>'+
    '<div class="form-group"><label>Dirección</label><input id="clf-address"></div>'+
    '<div class="form-row"><div class="form-group"><label>Tipo</label><select id="clf-type"><option>Consumidor Final</option><option>Contribuyente</option><option>Crédito</option></select></div>'+
    '<div class="form-group"><label>Límite Crédito</label><input id="clf-credit" type="number" value="0"></div></div>'+
    '<div class="form-actions"><button class="btn btn-ghost" onclick="closeModal()">Cancelar</button><button class="btn btn-primary" onclick="saveClient()">Crear</button></div></div></div>';
}

function saveClient(){
  var body = {
    nombre: document.getElementById('clf-name').value,
    rnc: document.getElementById('clf-rnc').value,
    telefono: document.getElementById('clf-phone').value,
    email: document.getElementById('clf-email').value,
    direccion: document.getElementById('clf-address').value,
    tipo: document.getElementById('clf-type').value,
    limiteCredito: parseFloat(document.getElementById('clf-credit').value)||0
  };
  if(!body.nombre){showToast('El nombre es requerido');return}
  api('/api/admin/clientes', 'POST', body).then(function(r){
    if(r.ok){showToast('Cliente creado');closeModal();renderSection('clients')}
    else showToast('Error: '+(r.error||''));
  }).catch(function(){showToast('Error de conexión')});
}

function deleteClient(id){
  if(!confirm('¿Eliminar cliente '+id+'?'))return;
  api('/api/admin/clientes/'+encodeURIComponent(id), 'DELETE').then(function(r){
    if(r.ok){showToast('Cliente eliminado');renderSection('clients')}
    else showToast('Error: '+(r.error||''));
  }).catch(function(){showToast('Error de conexión')});
}

// ===================== USERS =====================
function loadUsers(c){
  c.innerHTML = '<div class="toolbar"><button class="btn btn-primary" onclick="showUserForm()">+ Nuevo Usuario</button></div><div class="spinner"></div>';
  api('/api/admin/users').then(function(us){
    if(!Array.isArray(us)){c.innerHTML='<div class="empty-state">Error</div>';return}
    usersCache=us;
    if(!us.length){c.querySelector('.spinner').outerHTML='<div class="empty-state">No hay usuarios</div>';return}
    var tbl = '<div class="table-wrap"><table><thead><tr><th>ID</th><th>Nombre</th><th>Rol</th><th>Acciones</th></tr></thead><tbody>'+
      us.map(function(u){
        return '<tr><td>'+esc(u.id)+'</td><td><strong>'+esc(u.nombre)+'</strong></td><td><span class="badge badge-blue">'+esc(u.rol)+'</span></td><td><div class="btn-group"><button class="btn btn-sm btn-primary" onclick="showUserForm(\''+esc(u.id)+'\')">✏️</button><button class="btn btn-sm btn-danger" onclick="deleteUser(\''+esc(u.id)+'\')">🗑</button></div></td></tr>';
      }).join('')+'</tbody></table></div>';
    c.querySelector('.spinner').outerHTML = tbl;
  }).catch(function(){c.innerHTML='<div class="empty-state">Error de conexión</div>'});
}

function showUserForm(id){
  var u = id ? usersCache.find(function(x){return x.id===id}) : null;
  var modal = document.getElementById('modalContainer');
  modal.innerHTML = '<div class="modal-overlay" onclick="closeModal(event)"><div class="modal" onclick="event.stopPropagation()"><h3>'+(u?'Editar Usuario':'Nuevo Usuario')+'</h3>'+
    '<div class="form-group"><label>Nombre</label><input id="uf-name" value="'+esc(u?u.nombre:'')+'"></div>'+
    '<div class="form-row"><div class="form-group"><label>Rol</label><select id="uf-role"><option value="Mesero" '+(u&&u.rol==='Mesero'?'selected':'')+'>Mesero</option><option value="Cajero" '+(u&&u.rol==='Cajero'?'selected':'')+'>Cajero</option><option value="Admin" '+(u&&u.rol==='Admin'?'selected':'')+'>Admin</option><option value="Cocina" '+(u&&u.rol==='Cocina'?'selected':'')+'>Cocina</option></select></div>'+
    '<div class="form-group"><label>PIN</label><input id="uf-pin" type="password" placeholder="'+(u?'Dejar vacío para mantener':'')+'" value=""></div></div>'+
    '<div class="form-actions"><button class="btn btn-ghost" onclick="closeModal()">Cancelar</button><button class="btn btn-primary" onclick="saveUser(\''+(u?esc(u.id):'')+'\')">'+(u?'Actualizar':'Crear')+'</button></div></div></div>';
}

function saveUser(id){
  var body = {nombre: document.getElementById('uf-name').value, rol: document.getElementById('uf-role').value};
  var pin = document.getElementById('uf-pin').value;
  if(pin) body.pin = pin;
  if(!body.nombre){showToast('El nombre es requerido');return}
  var url = id ? '/api/admin/users/'+encodeURIComponent(id) : '/api/admin/users';
  var method = id ? 'PUT' : 'POST';
  api(url, method, body).then(function(r){
    if(r.ok){showToast(id?'Usuario actualizado':'Usuario creado');closeModal();renderSection('users')}
    else showToast('Error: '+(r.error||''));
  }).catch(function(){showToast('Error de conexión')});
}

function deleteUser(id){
  if(!confirm('¿Eliminar usuario '+id+'?'))return;
  api('/api/admin/users/'+encodeURIComponent(id), 'DELETE').then(function(r){
    if(r.ok){showToast('Usuario eliminado');renderSection('users')}
    else showToast('Error: '+(r.error||''));
  }).catch(function(){showToast('Error de conexión')});
}

// ===================== TURNOS =====================
function loadTurnos(c){
  c.innerHTML = '<div class="toolbar"><button class="btn btn-primary" onclick="showTurnoForm()">+ Abrir Turno</button></div><div class="spinner"></div>';
  api('/api/admin/turnos').then(function(ts){
    if(!Array.isArray(ts)){c.innerHTML='<div class="empty-state">Error</div>';return}
    if(!ts.length){c.querySelector('.spinner').outerHTML='<div class="empty-state">No hay turnos registrados</div>';return}
    var tbl = '<div class="table-wrap"><table><thead><tr><th>Nombre</th><th>Cajero</th><th>Inicio</th><th>Cierre</th><th>Estado</th><th>Ventas</th><th>Efectivo</th><th>Tarjeta</th></tr></thead><tbody>'+
      ts.map(function(t){
        return '<tr><td><strong>'+esc(t.nombre)+'</strong></td><td>'+esc(t.cajero)+'</td><td>'+dateStr(t.inicio)+'</td><td>'+(t.cierre?dateStr(t.cierre):'—')+'</td><td>'+(t.activo?'<span class="badge badge-green">Activo</span>':'<span class="badge badge-gray">Cerrado</span>')+'</td><td>'+money(t.totalVentas)+'</td><td>'+money(t.totalEfectivo)+'</td><td>'+money(t.totalTarjeta)+'</td></tr>';
      }).join('')+'</tbody></table></div>';
    c.querySelector('.spinner').outerHTML = tbl;
  }).catch(function(){c.innerHTML='<div class="empty-state">Error de conexión</div>'});
}

function showTurnoForm(){
  var modal = document.getElementById('modalContainer');
  modal.innerHTML = '<div class="modal-overlay" onclick="closeModal(event)"><div class="modal" onclick="event.stopPropagation()"><h3>Abrir Nuevo Turno</h3>'+
    '<div class="form-group"><label>Nombre del Turno</label><input id="tf-name" placeholder="Ej: Turno Mañana"></div>'+
    '<div class="form-group"><label>Cajero</label><input id="tf-cajero" placeholder="Nombre del cajero"></div>'+
    '<div class="form-actions"><button class="btn btn-ghost" onclick="closeModal()">Cancelar</button><button class="btn btn-primary" onclick="saveTurno()">Abrir Turno</button></div></div></div>';
}

function saveTurno(){
  var body = {
    nombre: document.getElementById('tf-name').value || 'Turno '+new Date().toLocaleDateString(),
    cajero: document.getElementById('tf-cajero').value
  };
  api('/api/admin/turnos', 'POST', body).then(function(r){
    if(r.ok){showToast('Turno abierto');closeModal();renderSection('turnos')}
    else showToast('Error: '+(r.error||''));
  }).catch(function(){showToast('Error de conexión')});
}

// ===================== SETTINGS =====================
function loadSettings(c){
  c.innerHTML = '<div class="spinner"></div>';
  api('/api/admin/settings').then(function(s){
    if(!s.ok){c.innerHTML='<div class="empty-state">Error</div>';return}
    c.innerHTML = '<div class="card"><h3 style="font-size:15px;font-weight:700;margin-bottom:16px">Configuración de la Empresa</h3>'+
      '<div class="form-row"><div class="form-group"><label>Nombre de la Empresa</label><input id="sf-name" value="'+esc(s.nombreEmpresa)+'"></div>'+
      '<div class="form-group"><label>RNC</label><input id="sf-rnc" value="'+esc(s.rnc)+'"></div></div>'+
      '<div class="form-row"><div class="form-group"><label>Teléfono</label><input id="sf-phone" value="'+esc(s.telefono)+'"></div>'+
      '<div class="form-group"><label>Email</label><input id="sf-email" value="'+esc(s.email)+'"></div></div>'+
      '<div class="form-group"><label>Dirección</label><input id="sf-address" value="'+esc(s.direccion)+'"></div>'+
      '<div class="form-row"><div class="form-group"><label>Moneda</label><input id="sf-currency" value="'+esc(s.moneda)+'"></div>'+
      '<div class="form-group"><label>ITBIS %</label><input id="sf-tax" type="number" step="0.1" value="'+s.itbis+'"></div></div>'+
      '<div class="form-row"><div class="form-group"><label>Propina sugerida %</label><input id="sf-tip" type="number" step="0.1" value="'+s.propinaSugerida+'"></div>'+
      '<div class="form-group"><label><input id="sf-dark" type="checkbox" '+(s.modoOscuro?'checked':'')+'> Modo oscuro</label></div></div>'+
      '<div class="form-group"><label>Color primario</label><input id="sf-color" value="'+esc(s.colorPrimario)+'"></div>'+
      '<div class="form-actions"><button class="btn btn-primary" onclick="saveSettings()">Guardar Configuración</button></div></div>';
  }).catch(function(){c.innerHTML='<div class="empty-state">Error de conexión</div>'});
}

function saveSettings(){
  var body = {
    nombreEmpresa: document.getElementById('sf-name').value,
    rnc: document.getElementById('sf-rnc').value,
    telefono: document.getElementById('sf-phone').value,
    email: document.getElementById('sf-email').value,
    direccion: document.getElementById('sf-address').value,
    moneda: document.getElementById('sf-currency').value,
    itbis: parseFloat(document.getElementById('sf-tax').value)||18,
    propinaSugerida: parseFloat(document.getElementById('sf-tip').value)||0,
    modoOscuro: document.getElementById('sf-dark').checked,
    colorPrimario: document.getElementById('sf-color').value
  };
  api('/api/admin/settings', 'POST', body).then(function(r){
    if(r.ok){showToast('Configuración guardada')}
    else showToast('Error: '+(r.error||''));
  }).catch(function(){showToast('Error de conexión')});
}

// ===================== CLOSE MODAL =====================
function closeModal(e){
  if(e && e.target !== e.currentTarget) return;
  document.getElementById('modalContainer').innerHTML = '';
}

// ===================== INIT =====================
document.getElementById('statusBar').textContent = '📶 Conectando...';
renderSection('dashboard');
setInterval(function(){
  api('/api/admin/dashboard').then(function(d){
    if(d.ok) document.getElementById('statusBar').textContent = '📶 '+d.totalProductos+' productos'+(d.turnoActivo?' · Turno activo':' · Sin turno');
  }).catch(function(){});
}, 10000);
</script>
</body>
</html>
""".trimIndent()
}
