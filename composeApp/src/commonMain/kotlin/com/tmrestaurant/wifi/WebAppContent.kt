package com.tmrestaurant.wifi

object WebAppContent {
    fun generate(): String = """
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
<title>Menu Digital - TM Restaurant</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;background:linear-gradient(180deg,#f8fafc 0,#eef2f7 100%);color:#1f2937;min-height:100vh}
.header{background:linear-gradient(135deg,#4c1d95,#6d28d9);color:#fff;padding:12px 22px;position:sticky;top:0;z-index:100;box-shadow:0 4px 18px #4c1d9526;display:flex;align-items:center;justify-content:space-between;gap:16px}
.header h1{font-size:19px;font-weight:750;letter-spacing:-.2px}
.header-nav{display:flex;gap:8px}
.nav-btn{height:42px;padding:0 16px;border:1px solid #ffffff38;border-radius:13px;background:#ffffff14;color:#fff;font-size:14px;font-weight:700;cursor:pointer}
.nav-btn.active{background:#fff;color:#5b21b6;box-shadow:0 5px 12px #2e106533}
.nav-btn:active{transform:scale(.96)}
.catalog-page{max-width:1400px;margin:0 auto;padding:26px 24px 42px}
.catalog-hero{background:linear-gradient(135deg,#ffffff,#f5f3ff);border:1px solid #e9d5ff;border-radius:24px;padding:24px;display:flex;align-items:center;justify-content:space-between;gap:20px;box-shadow:0 10px 28px #4c1d950d}
.catalog-title{font-size:26px;font-weight:850;color:#312e81;letter-spacing:-.5px}
.catalog-subtitle{font-size:14px;color:#64748b;margin-top:6px}
.catalog-count{min-width:82px;height:82px;border-radius:22px;background:#6d28d9;color:#fff;display:flex;flex-direction:column;align-items:center;justify-content:center;box-shadow:0 10px 20px #6d28d938}
.catalog-count strong{font-size:25px}.catalog-count span{font-size:11px;text-transform:uppercase;letter-spacing:.8px}
.catalog-tools{display:flex;gap:12px;align-items:center;margin:20px 0 12px}
.catalog-search{flex:1;height:48px;border:1px solid #dbe1ea;border-radius:15px;background:#fff;padding:0 16px;font-size:15px;outline:none;box-shadow:0 3px 10px #3341550a}
.catalog-search:focus{border-color:#8b5cf6;box-shadow:0 0 0 3px #8b5cf61f}
.catalog-tabs{display:flex;gap:8px;overflow-x:auto;padding:4px 0 14px;scrollbar-width:none}
.catalog-tabs::-webkit-scrollbar{display:none}
.catalog-tab{border:1px solid #e2e8f0;background:#fff;color:#64748b;padding:9px 16px;border-radius:999px;font-size:13px;font-weight:700;white-space:nowrap;cursor:pointer}
.catalog-tab.active{background:#6d28d9;border-color:#6d28d9;color:#fff}
.catalog-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(215px,1fr));gap:16px}
.catalog-card{background:#fff;border:1px solid #e5e7eb;border-radius:20px;overflow:hidden;box-shadow:0 5px 16px #3341550d;transition:transform .18s ease,box-shadow .18s ease}
.catalog-card:hover{transform:translateY(-4px);box-shadow:0 13px 24px #3341551a}
.catalog-visual{height:118px;display:flex;align-items:center;justify-content:center;position:relative;background:linear-gradient(135deg,#ede9fe,#ddd6fe)}
.catalog-visual.bebidas{background:linear-gradient(135deg,#dbeafe,#bfdbfe)}
.catalog-visual.extras{background:linear-gradient(135deg,#ffedd5,#fed7aa)}
.catalog-visual.entrada{background:linear-gradient(135deg,#dcfce7,#bbf7d0)}
.catalog-icon{font-size:48px;filter:drop-shadow(0 5px 5px #33415522)}
.catalog-category{position:absolute;left:12px;top:12px;background:#ffffffdb;color:#475569;border-radius:999px;padding:5px 9px;font-size:10px;font-weight:800;text-transform:uppercase}
.catalog-info{padding:15px}
.catalog-name{font-size:15px;font-weight:800;color:#1e293b;line-height:1.3;min-height:39px}
.catalog-description{font-size:12px;color:#94a3b8;line-height:1.4;height:34px;overflow:hidden;margin-top:5px}
.catalog-price{font-size:20px;font-weight:850;color:#6d28d9;margin-top:10px}
.comandas-page{max-width:1200px;margin:0 auto;padding:26px 24px 42px}
.comandas-header{display:flex;align-items:center;justify-content:space-between;gap:16px;margin-bottom:18px}
.comandas-title{font-size:26px;font-weight:850;color:#312e81}
.comandas-subtitle{font-size:14px;color:#64748b;margin-top:5px}
.comandas-refresh{border:0;border-radius:13px;background:#6d28d9;color:#fff;padding:12px 16px;font-weight:750;cursor:pointer}
.comandas-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(280px,1fr));gap:16px}
.comanda-card{background:#fff;border:1px solid #e5e7eb;border-radius:20px;padding:17px;box-shadow:0 6px 18px #3341550d}
.comanda-top{display:flex;align-items:flex-start;justify-content:space-between;gap:12px;padding-bottom:12px;border-bottom:1px solid #eef2f7}
.comanda-mesa{font-size:18px;font-weight:850;color:#1e293b}
.comanda-area{font-size:12px;color:#64748b;margin-top:3px}
.comanda-status{padding:6px 10px;border-radius:999px;font-size:11px;font-weight:800;white-space:nowrap}
.status-pendiente{background:#fef3c7;color:#92400e}
.status-en-preparacion{background:#dbeafe;color:#1d4ed8}
.status-listo{background:#dcfce7;color:#166534}
.comanda-products{display:flex;flex-direction:column;gap:10px;padding-top:13px}
.comanda-product{display:flex;gap:9px;align-items:flex-start;font-size:13px;color:#334155}
.comanda-qty{min-width:31px;height:25px;border-radius:8px;background:#ede9fe;color:#6d28d9;display:flex;align-items:center;justify-content:center;font-weight:850}
.comanda-product-name{font-weight:700}
.comanda-note{font-size:12px;color:#b45309;margin-top:3px}
.kitchen-state{position:absolute;z-index:5;bottom:22px;left:50%;transform:translateX(-50%);border-radius:999px;padding:5px 9px;background:#fff;font-size:10px;font-weight:850;white-space:nowrap;box-shadow:0 3px 8px #3341552b}
.kitchen-state.pendiente{color:#92400e;background:#fef3c7}
.kitchen-state.en-preparacion{color:#1d4ed8;background:#dbeafe}
.kitchen-state.listo{color:#166534;background:#dcfce7}
.mesas-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(245px,1fr));gap:26px 20px;padding:30px 24px 38px;max-width:1500px;margin:0 auto}
.mesa-card{height:205px;border-radius:24px;text-align:center;cursor:pointer;position:relative;display:flex;align-items:center;justify-content:center;transition:transform .18s ease,filter .18s ease;isolation:isolate}
.mesa-card:hover{transform:translateY(-4px);filter:drop-shadow(0 12px 12px #3341551f)}
.mesa-card:active{transform:scale(.97)}
.table-top{width:132px;height:132px;border-radius:50%;position:relative;z-index:2;display:flex;flex-direction:column;align-items:center;justify-content:center;border:7px solid;box-shadow:inset 0 5px 0 #ffffff80,inset 0 -8px 12px #00000012,0 9px 14px #33415525}
.libre .table-top{background:linear-gradient(145deg,#dcfce7,#bbf7d0);border-color:#4ade80;color:#166534}
.ocupada .table-top{background:linear-gradient(145deg,#fef3c7,#fde68a);border-color:#f59e0b;color:#92400e}
.table-name{font-size:18px;font-weight:800;line-height:1.1;max-width:105px}
.status{font-size:12px;font-weight:650;margin-top:7px;padding:4px 10px;border-radius:999px;background:#ffffffa6}
.chair{width:60px;height:25px;position:absolute;z-index:1;border-radius:9px 9px 13px 13px;border:3px solid;box-shadow:inset 0 -5px 8px #00000012,0 4px 6px #3341551f}
.libre .chair{background:#86efac;border-color:#22c55e}
.ocupada .chair{background:#fcd34d;border-color:#f59e0b}
.chair.top{top:9px;left:50%;transform:translateX(-50%)}
.chair.bottom{bottom:9px;left:50%;transform:translateX(-50%) rotate(180deg)}
.chair.left{left:29px;top:50%;transform:translateY(-50%) rotate(-90deg)}
.chair.right{right:29px;top:50%;transform:translateY(-50%) rotate(90deg)}
.badge-items{position:absolute;z-index:4;top:28px;right:calc(50% - 88px);min-width:31px;height:31px;border-radius:16px;padding:0 8px;background:#ef4444;color:#fff;border:3px solid #fff;display:none;align-items:center;justify-content:center;font-size:11px;font-weight:800;box-shadow:0 5px 10px #ef44444d}
.ocupada .badge-items{display:flex}
@media(max-width:600px){.header{padding:10px 12px}.header h1{font-size:16px}.nav-btn{height:40px;padding:0 11px;font-size:12px}.mesas-grid{grid-template-columns:repeat(2,minmax(145px,1fr));gap:18px 8px;padding:22px 8px}.mesa-card{height:180px}.table-top{width:116px;height:116px}.chair.left{left:7px}.chair.right{right:7px}.chair{width:52px;height:23px}.table-name{font-size:16px}.catalog-page{padding:16px 12px 30px}.catalog-hero{padding:18px}.catalog-title{font-size:21px}.catalog-count{min-width:68px;height:68px;border-radius:18px}.catalog-tools{margin-top:14px}.catalog-grid{grid-template-columns:repeat(2,minmax(0,1fr));gap:10px}.catalog-visual{height:95px}.catalog-info{padding:12px}.catalog-name{font-size:13px}.catalog-price{font-size:17px}}
.modal-overlay{position:fixed;inset:0;background:#00000080;z-index:300;display:flex;align-items:flex-end;justify-content:center}
.modal{background:#fff;border-radius:22px 22px 0 0;width:100%;max-width:500px;max-height:92vh;display:flex;flex-direction:column;animation:slideUp .25s;overflow:hidden}
@keyframes slideUp{from{transform:translateY(100%)}to{transform:translateY(0)}}
.modal-header{padding:14px 18px;border-bottom:1px solid #e5e7eb;display:flex;align-items:center;justify-content:space-between;flex-shrink:0}
.modal-header h2{font-size:16px;font-weight:700}
.modal-close{width:44px;height:44px;border:0;border-radius:14px;background:#f1f5f9;color:#334155;font-size:24px;line-height:1;display:flex;align-items:center;justify-content:center;cursor:pointer;flex-shrink:0}
.modal-close:active{background:#e2e8f0;transform:scale(.94)}
.modal-scroll{flex:1;overflow-y:auto;-webkit-overflow-scrolling:touch}
.modal-section{padding:0 0 12px 0}
.section-header{display:flex;justify-content:space-between;align-items:center;padding:12px 18px 6px;font-size:13px;font-weight:700;color:#6b7280;text-transform:uppercase;letter-spacing:.5px}
.section-divider{border-top:1px solid #e5e7eb;margin:0 18px}
.item-row{display:flex;align-items:center;gap:10px;padding:10px 18px;border-bottom:1px solid #f3f4f6}
.item-info{flex:1;min-width:0}
.item-name{font-size:14px;font-weight:600;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.item-detail{font-size:11px;color:#9ca3af;margin-top:2px}
.item-note{font-size:12px;color:#b45309;margin-top:5px;font-weight:650;white-space:normal}
.item-price{font-size:14px;font-weight:700;color:#7c3aed;text-align:right;white-space:nowrap}
.item-note-btn{width:42px;height:42px;border:0;border-radius:12px;background:#ede9fe;color:#6d28d9;font-size:18px;display:flex;align-items:center;justify-content:center;cursor:pointer;flex-shrink:0}
.item-note-btn:active{background:#ddd6fe;transform:scale(.94)}
.item-remove{width:42px;height:42px;border:0;border-radius:12px;background:#fee2e2;color:#dc2626;font-size:18px;display:flex;align-items:center;justify-content:center;cursor:pointer;flex-shrink:0}
.item-remove:active{background:#fecaca;transform:scale(.94)}
.total-row{display:flex;justify-content:space-between;align-items:center;padding:12px 18px;background:#f8f5ff}
.total-label{font-size:15px;font-weight:700}
.total-value{font-size:20px;font-weight:700;color:#7c3aed}
.tabs-h{display:flex;gap:4px;padding:8px 18px;overflow-x:auto}
.tab{padding:6px 14px;border-radius:16px;font-size:12px;font-weight:600;white-space:nowrap;cursor:pointer;background:#f3f4f6;color:#6b7280;border:none;transition:all .2s;flex-shrink:0}
.tab.active{background:#7c3aed;color:#fff}
.products-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(130px,1fr));gap:8px;padding:0 18px 12px}
.product-card{background:#fff;border-radius:12px;padding:10px;cursor:pointer;box-shadow:0 1px 3px #0000000d;transition:transform .15s;display:flex;flex-direction:column;gap:2px;border:1px solid #e5e7eb}
.product-card:active{transform:scale(.96);background:#f8f5ff;border-color:#c4b5fd}
.product-card .pname{font-size:12px;font-weight:600;line-height:1.3}
.product-card .pprice{font-size:14px;font-weight:700;color:#7c3aed}
.product-card.added{background:#f0fdf4;border-color:#86efac;animation:pulse .5s}
@keyframes pulse{0%{transform:scale(1)}50%{transform:scale(1.05)}100%{transform:scale(1)}}
.btn{width:100%;padding:14px;border:none;border-radius:14px;font-size:15px;font-weight:700;cursor:pointer;transition:background .2s}
.btn-primary{background:#7c3aed;color:#fff;margin-top:8px}
.btn-primary:active{background:#6d28d9}
.btn-danger{background:#fee2e2;color:#ef4444;margin-top:4px}
.btn-danger:active{background:#fecaca}
.btn-cobrar{background:#16a34a;color:#fff}
.btn-cobrar:active{background:#15803d}
.btn-kitchen{background:#f59e0b;color:#fff}
.btn-kitchen:active{background:#d97706}
.btn-close{background:#e2e8f0;color:#334155}
.btn-close:active{background:#cbd5e1}
.modal-footer{padding:12px 18px calc(18px + env(safe-area-inset-bottom));border-top:1px solid #e5e7eb;flex-shrink:0;background:#fff}
.btn-row{display:flex;gap:8px;flex-wrap:wrap}
.btn-row .btn{flex:1 1 135px;margin-top:0}
.empty{text-align:center;padding:24px;color:#9ca3af;font-size:13px}
.toast{position:fixed;top:20px;left:50%;transform:translateX(-50%);background:#1f2937;color:#fff;padding:10px 22px;border-radius:12px;font-size:13px;z-index:500;animation:fadeIn .3s;pointer-events:none}
@keyframes fadeIn{from{opacity:0;transform:translateX(-50%) translateY(-10px)}to{opacity:1;transform:translateX(-50%) translateY(0)}}
.spinner{border:3px solid #e5e7eb;border-top:3px solid #7c3aed;border-radius:50%;width:24px;height:24px;animation:spin .7s linear infinite;margin:20px auto}
@keyframes spin{to{transform:rotate(360deg)}}
</style>
</head>
<body>

<div class="header">
  <h1>🍽️ TM Restaurant</h1>
  <div class="header-nav">
    <button id="navMesas" class="nav-btn active" onclick="showView('mesas')">Mesas</button>
    <button id="navCatalogo" class="nav-btn" onclick="showView('catalogo')">Catálogo</button>
    <button id="navComandas" class="nav-btn" onclick="showView('comandas')">Comandas</button>
  </div>
</div>

<div id="content"></div>
<div id="modalContainer"></div>
<div id="toastContainer"></div>

<script>
var mesas = [];
var productos = [];
var categorias = [];
var catActual = 'todas';
var mesaItems = [];
var loadingItemId = null;
var menuLoaded = false;
var menuError = false;
var activeMesa = null;
var currentView = 'mesas';
var catalogCategory = 'todas';
var catalogSearch = '';
var comandas = [];

function refreshActiveMesa(){
  if(activeMesa) renderMesaModal(activeMesa.id,activeMesa.nombre);
}

function showView(view){
  currentView=view;
  closeModal();
  document.getElementById('navMesas').classList.toggle('active',view==='mesas');
  document.getElementById('navCatalogo').classList.toggle('active',view==='catalogo');
  document.getElementById('navComandas').classList.toggle('active',view==='comandas');
  if(view==='catalogo') renderCatalog();
  else if(view==='comandas') loadComandas();
  else renderHome();
}

function loadData(){
  fetch('/api/mesas').then(function(r){return r.json()}).then(function(m){
    if(Array.isArray(m)){mesas=m} else {mesas=[]}
    renderHome();
  }).catch(function(){mesas=[];renderHome()});

  fetch('/api/menu').then(function(r){
    if(!r.ok) throw new Error('HTTP '+r.status);
    return r.text();
  }).then(function(t){
    try{
      var menu=JSON.parse(t);
      productos=menu.productos||[];
      categorias=menu.categorias||[];
      menuLoaded=true;
      menuError=false;
      refreshActiveMesa();
      if(currentView==='catalogo') renderCatalog();
    }catch(e){
      productos=[];
      categorias=[];
      menuLoaded=true;
      menuError=true;
      refreshActiveMesa();
      if(currentView==='catalogo') renderCatalog();
    }
  }).catch(function(){
    productos=[];
    categorias=[];
    menuLoaded=true;
    menuError=true;
    refreshActiveMesa();
    if(currentView==='catalogo') renderCatalog();
  });

  setInterval(function(){
    fetch('/api/mesas').then(function(r){return r.json()}).then(function(m){
      if(Array.isArray(m)) mesas=m;
      if(currentView==='mesas') renderHome();
    }).catch(function(){})
    if(currentView==='comandas') loadComandas();
  },5000);
}

function loadComandas(){
  fetch('/api/comandas').then(function(r){
    if(!r.ok)throw new Error('HTTP '+r.status);
    return r.json();
  }).then(function(data){
    comandas=Array.isArray(data)?data:[];
    renderComandas();
  }).catch(function(){
    document.getElementById('content').innerHTML='<div class="empty">No se pudieron cargar las comandas</div>';
  });
}

function statusClass(status){
  return (status||'').toLowerCase().replace(/\s+/g,'-');
}

function renderComandas(){
  var c=document.getElementById('content');
  var cards=comandas.map(function(comanda){
    var products=(comanda.productos||[]).map(function(item){
      return '<div class="comanda-product"><span class="comanda-qty">'+item.cantidad+'x</span><div><div class="comanda-product-name">'+item.producto+'</div>'+(item.nota?'<div class="comanda-note">Nota: '+item.nota+'</div>':'')+'</div></div>';
    }).join('');
    return '<article class="comanda-card"><div class="comanda-top"><div><div class="comanda-mesa">'+comanda.mesa+'</div><div class="comanda-area">'+comanda.area+'</div></div><span class="comanda-status status-'+statusClass(comanda.estado)+'">'+comanda.estado+'</span></div><div class="comanda-products">'+products+'</div></article>';
  }).join('');
  if(!cards)cards='<div class="empty">No hay comandas activas</div>';
  c.innerHTML='<main class="comandas-page"><div class="comandas-header"><div><div class="comandas-title">Comandas de cocina</div><div class="comandas-subtitle">Estado actualizado de las ordenes enviadas</div></div><button class="comandas-refresh" onclick="loadComandas()">Actualizar</button></div><section class="comandas-grid">'+cards+'</section></main>';
}

function catalogIcon(category){
  var value=(category||'').toLowerCase();
  if(value.indexOf('bebida')>=0)return '🥤';
  if(value.indexOf('extra')>=0)return '🍗';
  if(value.indexOf('entrada')>=0)return '🥗';
  if(value.indexOf('guarn')>=0)return '🍟';
  return '🍽️';
}

function catalogClass(category){
  var value=(category||'').toLowerCase();
  if(value.indexOf('bebida')>=0)return 'bebidas';
  if(value.indexOf('extra')>=0)return 'extras';
  if(value.indexOf('entrada')>=0)return 'entrada';
  return '';
}

function setCatalogCategory(category){
  catalogCategory=category;
  renderCatalog();
}

function updateCatalogSearch(value){
  catalogSearch=value.toLowerCase().trim();
  renderCatalog();
  var input=document.getElementById('catalogSearch');
  if(input){input.focus();input.setSelectionRange(input.value.length,input.value.length)}
}

function renderCatalog(){
  var c=document.getElementById('content');
  if(!menuLoaded){
    c.innerHTML='<div class="spinner"></div><div class="empty">Cargando catálogo...</div>';
    return;
  }
  if(menuError){
    c.innerHTML='<div class="empty">No se pudo cargar el catálogo</div>';
    return;
  }
  var filtered=productos.filter(function(p){
    var categoryOk=catalogCategory==='todas'||p.categoria===catalogCategory;
    var text=((p.nombre||'')+' '+(p.descripcion||'')+' '+(p.categoria||'')).toLowerCase();
    return categoryOk&&(!catalogSearch||text.indexOf(catalogSearch)>=0);
  });
  var tabs='<button class="catalog-tab '+(catalogCategory==='todas'?'active':'')+'" onclick="setCatalogCategory(\'todas\')">Todo</button>';
  categorias.forEach(function(cat){
    tabs+='<button class="catalog-tab '+(catalogCategory===cat.nombre?'active':'')+'" onclick="setCatalogCategory(\''+cat.nombre.replace(/'/g,"\\'")+'\')">'+cat.nombre+'</button>';
  });
  var cards=filtered.map(function(p){
    return '<article class="catalog-card">'+
      '<div class="catalog-visual '+catalogClass(p.categoria)+'"><span class="catalog-category">'+p.categoria+'</span><span class="catalog-icon">'+catalogIcon(p.categoria)+'</span></div>'+
      '<div class="catalog-info"><div class="catalog-name">'+p.nombre+'</div><div class="catalog-description">'+(p.descripcion||'Disponible en nuestro menú')+'</div><div class="catalog-price">RD$ '+p.precio.toFixed(2)+'</div></div>'+
    '</article>';
  }).join('');
  if(!cards)cards='<div class="empty">No hay productos que coincidan con la búsqueda</div>';
  c.innerHTML='<main class="catalog-page">'+
    '<section class="catalog-hero"><div><div class="catalog-title">Nuestro menú</div><div class="catalog-subtitle">Combos, bebidas y productos disponibles</div></div><div class="catalog-count"><strong>'+productos.length+'</strong><span>productos</span></div></section>'+
    '<div class="catalog-tools"><input id="catalogSearch" class="catalog-search" type="search" value="'+catalogSearch.replace(/"/g,'&quot;')+'" placeholder="Buscar productos..." oninput="updateCatalogSearch(this.value)"></div>'+
    '<div class="catalog-tabs">'+tabs+'</div>'+
    '<section class="catalog-grid">'+cards+'</section>'+
  '</main>';
}

function renderHome(){
  var c=document.getElementById('content');
  if(!mesas.length){c.innerHTML='<div class="empty">📋 No hay mesas disponibles</div>';return}
  c.innerHTML='<div class="mesas-grid">'+
    mesas.map(function(m){
      var cls=m.ocupada?'ocupada':'libre';
      var status=m.estadoComanda||(m.ocupada?(m.items+' items'):'Libre');
      var badge=m.ocupada?'<span class="badge-items">'+m.items+'</span>':'';
      var kitchen=m.estadoComanda?'<span class="kitchen-state '+statusClass(m.estadoComanda)+'">'+m.estadoComanda+'</span>':'';
      return '<div class="mesa-card '+cls+'" onclick="openMesa('+m.id+',\''+m.nombre.replace(/'/g,"\\'")+'\')">'+
        '<span class="chair top"></span><span class="chair right"></span><span class="chair bottom"></span><span class="chair left"></span>'+
        badge+
        kitchen+
        '<div class="table-top"><div class="table-name">'+m.nombre+'</div><div class="status">'+status+'</div></div>'+
      '</div>'
    }).join('')+
  '</div>';
}

function openMesa(id,nombre){
  var m=document.getElementById('modalContainer');
  catActual='todas';
  mesaItems=[];
  loadingItemId=null;
  activeMesa={id:id,nombre:nombre};

  m.innerHTML='<div class="modal-overlay" onclick="closeModal()"><div class="modal" onclick="event.stopPropagation()"><div class="modal-header"><h2>🪑 '+nombre+'</h2><button type="button" class="modal-close" aria-label="Cerrar" onclick="closeModal()">×</button></div><div class="modal-scroll"><div class="spinner"></div><div class="empty">Cargando...</div></div></div></div>';

  fetch('/api/mesa/'+id+'/items').then(function(r){return r.json()}).then(function(data){
    mesaItems=data.items||[];
    activeMesa.estadoComanda=data.estadoComanda||null;
    activeMesa.areaComanda=data.areaComanda||null;
    renderMesaModal(id,nombre);
  }).catch(function(){mesaItems=[];renderMesaModal(id,nombre)});
}

function renderMesaModal(id,nombre){
  var m=document.getElementById('modalContainer');
  var total=0;
  mesaItems.forEach(function(i){total+=i.total||0});
  var kitchenStatus=activeMesa&&activeMesa.estadoComanda
    ?'<div class="section-header">Estado en '+(activeMesa.areaComanda||'Cocina')+': '+activeMesa.estadoComanda+'</div>'
    :'';

  var itemsHtml='';
  if(mesaItems.length===0){
    itemsHtml='<div class="empty">Mesa vacia - Agregue productos</div>';
  }else{
    itemsHtml=mesaItems.map(function(i,idx){
      var noteHtml=i.nota?'<div class="item-note">📝 '+i.nota+'</div>':'';
      var encodedNote=encodeURIComponent(i.nota||'').replace(/'/g,'%27');
      return '<div class="item-row"><div class="item-info"><div class="item-name">'+i.producto+'</div><div class="item-detail">RD$ '+(i.precio||0).toFixed(2)+' x '+i.cantidad+'</div>'+noteHtml+'</div><div class="item-price">RD$ '+i.total.toFixed(2)+'</div><button type="button" class="item-note-btn" aria-label="Agregar nota" onclick="editProductNote('+id+','+i.productoId+',\''+nombre.replace(/'/g,"\\'")+'\',\''+encodedNote+'\')">📝</button><button type="button" class="item-remove" aria-label="Eliminar '+i.producto+'" onclick="removeProductFromMesa('+id+','+i.productoId+',\''+nombre.replace(/'/g,"\\'")+'\',\''+i.producto.replace(/'/g,"\\'")+'\')">🗑</button></div>';
    }).join('');
  }

  var tabsHtml='<button class="tab '+(catActual==='todas'?'active':'')+'" onclick="catActual=\'todas\';renderMesaModal('+id+',\''+nombre.replace(/'/g,"\\'")+'\')">Todo</button>';
  categorias.forEach(function(c){
    tabsHtml+='<button class="tab '+(catActual===c.nombre?'active':'')+'" onclick="catActual=\''+c.nombre.replace(/'/g,"\\'")+'\';renderMesaModal('+id+',\''+nombre.replace(/'/g,"\\'")+'\')">'+c.nombre+'</button>';
  });

  var filtered=catActual==='todas'?productos:productos.filter(function(p){return p.categoria===catActual});
  var productsHtml='';
  if(!menuLoaded){
    productsHtml='<div class="spinner"></div><div class="empty">Cargando productos...</div>';
  }else if(menuError){
    productsHtml='<div class="empty">No se pudo cargar el menu</div>';
  }else if(!filtered.length){
    productsHtml='<div class="empty">No hay productos en esta categoria</div>';
  }else{
    productsHtml='<div class="products-grid">'+filtered.map(function(p){
      var isLoading=loadingItemId===p.id;
      var cls=isLoading?'product-card added':'product-card';
      return '<div class="'+cls+'" id="pc'+p.id+'" onclick="addProductToMesa('+id+','+p.id+',\''+nombre.replace(/'/g,"\\'")+'\')"><div class="pname">'+p.nombre+'</div><div class="pprice">RD$ '+p.precio.toFixed(2)+'</div></div>';
    }).join('')+'</div>';
  }

  m.innerHTML='<div class="modal-overlay" onclick="closeModal()"><div class="modal" onclick="event.stopPropagation()"><div class="modal-header"><h2>🪑 '+nombre+'</h2><button type="button" class="modal-close" aria-label="Cerrar" onclick="closeModal()">×</button></div><div class="modal-scroll">'+
    '<div class="section-header">📋 Productos en la mesa</div>'+
    kitchenStatus+
    itemsHtml+
    (mesaItems.length>0?'<div class="total-row"><span class="total-label">Total</span><span class="total-value">RD$ '+total.toFixed(2)+'</span></div>':'')+
    '<div class="section-divider"></div>'+
    '<div class="section-header">➕ Agregar productos</div>'+
    '<div class="tabs-h">'+tabsHtml+'</div>'+
    productsHtml+
    '</div>'+
    '<div class="modal-footer"><div class="btn-row">'+
    (mesaItems.length>0?'<button class="btn btn-kitchen" onclick="sendMesaToKitchen('+id+',\''+nombre.replace(/'/g,"\\'")+'\')">🍳 Enviar a cocina</button>':'')+
    (mesaItems.length>0?'<button class="btn btn-cobrar" onclick="cobrarMesa('+id+',\''+nombre.replace(/'/g,"\\'")+'\')">💳 Cobrar RD$ '+total.toFixed(2)+'</button>':'')+
    '<button type="button" class="btn btn-close" onclick="closeModal()">Cerrar</button>'+
    '</div></div>'+
    '</div></div>';
}

function addProductToMesa(mesaId,productId,nombre){
  if(loadingItemId)return;
  loadingItemId=productId;
  var el=document.getElementById('pc'+productId);
  if(el){el.classList.add('added');el.style.pointerEvents='none'}

  fetch('/api/mesa/'+mesaId+'/add',{
    method:'POST',
    headers:{'Content-Type':'application/json'},
    body:JSON.stringify({productoId:productId,cantidad:1})
  }).then(function(r){
    if(!r.ok) return r.json().then(function(body){throw new Error(body.error||('HTTP '+r.status))});
    return r.json();
  }).then(function(res){
    showToast('✅ Agregado a '+nombre);
    if(el){el.classList.remove('added');el.style.pointerEvents='auto'}
    loadingItemId=null;
    fetch('/api/mesa/'+mesaId+'/items').then(function(r){return r.json()}).then(function(data){
      mesaItems=data.items||[];
      renderMesaModal(mesaId,nombre);
      fetch('/api/mesas').then(function(r){return r.json()}).then(function(m){
        if(Array.isArray(m)){mesas=m;renderHome()}
      }).catch(function(){})
    }).catch(function(){renderMesaModal(mesaId,nombre)});
  }).catch(function(e){
    showToast('Error al agregar');
    if(el){el.classList.remove('added');el.style.pointerEvents='auto'}
    loadingItemId=null;
  });
}

function removeProductFromMesa(mesaId,productId,nombre,producto){
  if(loadingItemId)return;
  if(!confirm('¿Eliminar '+producto+' de '+nombre+'?'))return;
  loadingItemId=productId;
  fetch('/api/mesa/'+mesaId+'/remove',{
    method:'POST',
    headers:{'Content-Type':'application/json'},
    body:JSON.stringify({productoId:productId})
  }).then(function(r){
    if(!r.ok) return r.json().then(function(body){throw new Error(body.error||('HTTP '+r.status))});
    return r.json();
  }).then(function(){
    showToast('Producto eliminado');
    loadingItemId=null;
    return fetch('/api/mesa/'+mesaId+'/items');
  }).then(function(r){return r.json()}).then(function(data){
    mesaItems=data.items||[];
    renderMesaModal(mesaId,nombre);
    return fetch('/api/mesas');
  }).then(function(r){return r.json()}).then(function(data){
    if(Array.isArray(data)){mesas=data;renderHome()}
  }).catch(function(){
    loadingItemId=null;
    showToast('Error al eliminar el producto');
  });
}

function editProductNote(mesaId,productId,nombre,encodedNote){
  if(loadingItemId)return;
  var currentNote=decodeURIComponent(encodedNote||'');
  var note=prompt('Nota para cocina o factura (ejemplo: sin cebolla):',currentNote||'');
  if(note===null)return;
  note=note.trim();
  if(note.length>200){showToast('La nota no puede superar 200 caracteres');return}
  loadingItemId=productId;
  fetch('/api/mesa/'+mesaId+'/note',{
    method:'POST',
    headers:{'Content-Type':'application/json'},
    body:JSON.stringify({productoId:productId,nota:note})
  }).then(function(r){
    if(!r.ok) return r.json().then(function(body){throw new Error(body.error||('HTTP '+r.status))});
    return r.json();
  }).then(function(){
    showToast(note?'Nota guardada':'Nota eliminada');
    loadingItemId=null;
    return fetch('/api/mesa/'+mesaId+'/items');
  }).then(function(r){return r.json()}).then(function(data){
    mesaItems=data.items||[];
    renderMesaModal(mesaId,nombre);
  }).catch(function(){
    loadingItemId=null;
    showToast('Error al guardar la nota');
  });
}

function sendMesaToKitchen(id,nombre){
  if(!confirm('¿Enviar los productos de '+nombre+' a cocina?'))return;
  fetch('/api/mesa/'+id+'/cocina',{method:'POST'}).then(function(r){
    if(!r.ok) return r.json().then(function(body){throw new Error(body.error||('HTTP '+r.status))});
    return r.json();
  }).then(function(res){
    showToast('🍳 '+res.mensaje);
    return fetch('/api/mesa/'+id+'/items');
  }).then(function(r){return r.json()}).then(function(data){
    mesaItems=data.items||[];
    if(activeMesa){
      activeMesa.estadoComanda=data.estadoComanda||null;
      activeMesa.areaComanda=data.areaComanda||null;
    }
    renderMesaModal(id,nombre);
    return fetch('/api/mesas');
  }).then(function(r){return r.json()}).then(function(data){
    if(Array.isArray(data)){mesas=data;renderHome()}
  }).catch(function(){
    showToast('Error al enviar la comanda');
  });
}

function cobrarMesa(id,nombre){
  if(!confirm('¿Enviar '+nombre+' a caja para realizar la factura?'))return;
  fetch('/api/mesa/'+id+'/cobrar',{method:'POST'}).then(function(r){
    if(!r.ok) return r.json().then(function(body){throw new Error(body.error||('HTTP '+r.status))});
    return r.json();
  }).then(function(res){
    showToast('🔔 Solicitud enviada a caja');
    closeModal();
    fetch('/api/mesas').then(function(r){return r.json()}).then(function(m){
      if(Array.isArray(m)){mesas=m;renderHome()}
    }).catch(function(){})
  }).catch(function(){showToast('Error al enviar la solicitud')});
}

function closeModal(){
  activeMesa=null;
  document.getElementById('modalContainer').innerHTML='';
}

function showToast(msg){
  var t=document.getElementById('toastContainer');
  t.innerHTML='<div class="toast">'+msg+'</div>';
  setTimeout(function(){t.innerHTML=''},2000);
}

loadData();
</script>
</body>
</html>
""".trimIndent()
}
