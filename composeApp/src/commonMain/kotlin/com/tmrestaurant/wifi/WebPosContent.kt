package com.tmrestaurant.wifi

object WebPosContent {
    fun generate(): String = """
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
<title>POS Web - TM Restaurant</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;background:#f1f5f9;color:#1e293b;overflow:hidden;height:100dvh;display:flex;flex-direction:column}
.header{background:linear-gradient(135deg,#4c1d95,#6d28d9);color:#fff;padding:10px 16px;display:flex;align-items:center;justify-content:space-between;gap:12px;flex-shrink:0;box-shadow:0 2px 12px #4c1d9533}
.header h1{font-size:17px;font-weight:750;letter-spacing:-.2px}
.header-right{display:flex;align-items:center;gap:10px;font-size:13px}
.pos-layout{display:flex;flex:1;overflow:hidden}
.sidebar{width:160px;background:#fff;border-right:1px solid #e2e8f0;overflow-y:auto;flex-shrink:0;padding:8px 0}
.sidebar-btn{display:block;width:100%;border:none;background:transparent;padding:10px 14px;text-align:left;font-size:13px;font-weight:600;color:#64748b;cursor:pointer;border-right:3px solid transparent}
.sidebar-btn.active{background:#f5f3ff;color:#6d28d9;border-right-color:#6d28d9}
.sidebar-btn:active{background:#ede9fe}
.main-area{flex:1;display:flex;flex-direction:column;overflow:hidden}
.toolbar{padding:10px 16px;background:#fff;border-bottom:1px solid #e2e8f0;display:flex;gap:10px;align-items:center;flex-shrink:0}
.toolbar input{flex:1;height:42px;border:1px solid #dbe1ea;border-radius:10px;padding:0 12px;font-size:14px;outline:none}
.toolbar input:focus{border-color:#8b5cf6;box-shadow:0 0 0 3px #8b5cf61f}
.toolbar .totals{white-space:nowrap;font-weight:700;font-size:15px;color:#6d28d9}
.category-tabs{display:flex;gap:6px;overflow-x:auto;padding:8px 16px;flex-shrink:0;background:#fff;border-bottom:1px solid #e2e8f0;scrollbar-width:none}
.category-tabs::-webkit-scrollbar{display:none}
.category-tab{border:1px solid #e2e8f0;background:#fff;color:#64748b;padding:7px 14px;border-radius:999px;font-size:12px;font-weight:700;white-space:nowrap;cursor:pointer;flex-shrink:0}
.category-tab.active{background:#6d28d9;border-color:#6d28d9;color:#fff}
.product-grid{flex:1;overflow-y:auto;padding:12px;display:grid;grid-template-columns:repeat(auto-fill,minmax(160px,1fr));gap:10px;align-content:start}
.product-card{background:#fff;border:1px solid #e5e7eb;border-radius:14px;padding:12px;cursor:pointer;display:flex;flex-direction:column;gap:4px;transition:transform .12s,box-shadow .12s;box-shadow:0 2px 6px #3341550a}
.product-card:active{transform:scale(.96);border-color:#c4b5fd;background:#faf5ff}
.product-card .pname{font-size:13px;font-weight:600;line-height:1.3;min-height:34px}
.product-card .pcode{font-size:11px;color:#94a3b8}
.product-card .pprice{font-size:17px;font-weight:750;color:#6d28d9;margin-top:4px}
.product-card .pstock{font-size:11px;color:#22c55e}
.product-card.out{opacity:.5}
.product-card.added{animation:pop .3s}
@keyframes pop{0%{transform:scale(1)}50%{transform:scale(1.06)}100%{transform:scale(1)}}
.empty-state{display:flex;align-items:center;justify-content:center;height:100%;color:#94a3b8;font-size:14px;grid-column:1/-1}
.cart-panel{width:380px;background:#fff;border-left:1px solid #e2e8f0;display:flex;flex-direction:column;flex-shrink:0}
.cart-header{padding:14px 16px;border-bottom:1px solid #e2e8f0;display:flex;align-items:center;justify-content:space-between}
.cart-header h2{font-size:15px;font-weight:700}
.cart-header span{font-size:12px;color:#94a3b8}
.cart-items{flex:1;overflow-y:auto;padding:8px 12px}
.cart-item{background:#f8fafc;border-radius:10px;padding:10px;margin-bottom:8px}
.cart-item-top{display:flex;justify-content:space-between;align-items:flex-start;gap:8px}
.cart-item-name{font-size:13px;font-weight:600;line-height:1.3;flex:1}
.cart-item-price{font-size:13px;font-weight:700;color:#6d28d9;white-space:nowrap}
.cart-item-qty{display:flex;align-items:center;gap:8px;margin-top:8px}
.qty-btn{width:36px;height:36px;border:none;border-radius:8px;background:#ede9fe;color:#6d28d9;font-size:18px;font-weight:700;cursor:pointer;display:flex;align-items:center;justify-content:center}
.qty-btn:active{background:#ddd6fe}
.qty-value{font-size:17px;font-weight:700;min-width:28px;text-align:center}
.remove-btn{margin-left:auto;width:36px;height:36px;border:none;border-radius:8px;background:#fee2e2;color:#ef4444;font-size:16px;cursor:pointer}
.remove-btn:active{background:#fecaca}
.cart-empty{display:flex;flex-direction:column;align-items:center;justify-content:center;height:100%;color:#cbd5e1;font-size:13px;gap:8px}
.cart-empty .icon{font-size:48px}
.cart-footer{padding:14px 16px;border-top:1px solid #e2e8f0}
.totals-row{display:flex;justify-content:space-between;font-size:13px;color:#64748b;margin-bottom:6px}
.totals-total{display:flex;justify-content:space-between;font-size:17px;font-weight:750;color:#1e293b;margin:8px 0 12px;padding-top:8px;border-top:1px solid #e2e8f0}
.checkout-btn{width:100%;height:50px;border:none;border-radius:12px;background:#16a34a;color:#fff;font-size:16px;font-weight:750;cursor:pointer}
.checkout-btn:active{background:#15803d}
.checkout-btn:disabled{background:#cbd5e1;cursor:default}
.pay-modal{position:fixed;inset:0;background:#00000080;z-index:200;display:flex;align-items:center;justify-content:center}
.pay-modal-content{background:#fff;border-radius:20px;width:90%;max-width:420px;padding:24px;max-height:90vh;overflow-y:auto}
.pay-modal-content h2{font-size:18px;font-weight:700;margin-bottom:16px}
.pay-option{padding:14px;border:2px solid #e5e7eb;border-radius:12px;cursor:pointer;margin-bottom:8px;font-weight:600;text-align:center;font-size:15px}
.pay-option.active{border-color:#6d28d9;background:#f5f3ff;color:#6d28d9}
.pay-btn{width:100%;height:50px;border:none;border-radius:12px;background:#6d28d9;color:#fff;font-size:16px;font-weight:750;cursor:pointer;margin-top:14px}
.pay-btn:active{background:#5b21b6}
.pay-close{width:100%;height:42px;border:none;border-radius:10px;background:#e2e8f0;color:#64748b;font-size:14px;cursor:pointer;margin-top:8px}
.toast{position:fixed;top:20px;left:50%;transform:translateX(-50%);background:#1f2937;color:#fff;padding:10px 22px;border-radius:12px;font-size:13px;z-index:300;animation:fadeIn .3s;pointer-events:none}
@keyframes fadeIn{from{opacity:0;transform:translateX(-50%) translateY(-10px)}to{opacity:1;transform:translateX(-50%) translateY(0)}}
.spinner{border:3px solid #e5e7eb;border-top:3px solid #6d28d9;border-radius:50%;width:28px;height:28px;animation:spin .7s linear infinite;margin:40px auto}
@keyframes spin{to{transform:rotate(360deg)}}
@media(max-width:900px){.sidebar{width:50px}.sidebar-btn{padding:10px 6px;font-size:11px;text-align:center}.cart-panel{width:280px}.product-grid{grid-template-columns:repeat(auto-fill,minmax(130px,1fr))}}
@media(max-width:600px){.pos-layout{flex-direction:column}.sidebar{width:100%;height:auto;display:flex;overflow-x:auto;border-right:none;border-bottom:1px solid #e2e8f0;padding:4px}.sidebar-btn{border-right:none;border-bottom:3px solid transparent;white-space:nowrap;padding:8px 12px;flex-shrink:0}.sidebar-btn.active{border-bottom-color:#6d28d9}.cart-panel{width:100%;max-height:45vh;border-left:none;border-top:1px solid #e2e8f0}}
</style>
</head>
<body>
<div class="header">
  <h1>🛒 TM POS Web</h1>
  <div class="header-right"><span id="statusConexion">Conectando...</span></div>
</div>
<div class="pos-layout">
  <div class="sidebar" id="sidebar"></div>
  <div class="main-area">
    <div class="toolbar">
      <input id="searchInput" type="search" placeholder="Buscar producto por nombre, código..." oninput="onSearch(this.value)">
      <span class="totals" id="toolbarTotal">RD$ 0.00</span>
    </div>
    <div class="category-tabs" id="categoryTabs"></div>
    <div class="product-grid" id="productGrid"><div class="spinner"></div></div>
  </div>
  <div class="cart-panel" id="cartPanel">
    <div class="cart-header">
      <h2>🛒 Orden Actual</h2>
      <span id="cartCount">0 productos</span>
    </div>
    <div class="cart-items" id="cartItems"><div class="cart-empty"><div class="icon">🛒</div><div>Carrito vacío</div></div></div>
    <div class="cart-footer" id="cartFooter" style="display:none">
      <div class="totals-row"><span>Subtotal:</span><span id="subtotalVal">RD$ 0.00</span></div>
      <div class="totals-row"><span>ITBIS (18%):</span><span id="taxVal">RD$ 0.00</span></div>
      <div class="totals-total"><span>Total:</span><span id="totalVal">RD$ 0.00</span></div>
      <button class="checkout-btn" onclick="showPayment()">💳 Cobrar</button>
    </div>
  </div>
</div>
<div id="modalContainer"></div>
<div id="toastContainer"></div>

<script>
var productos = [];
var categorias = [];
var cart = [];
var selectedCat = 'todas';
var searchQuery = '';
var loading = true;

function loadData(){
  fetch('/api/pos/categories').then(function(r){return r.json()}).then(function(c){
    categorias = Array.isArray(c)?c:[];
    renderCategories();
  }).catch(function(){});
  fetch('/api/pos/products').then(function(r){return r.json()}).then(function(p){
    productos = Array.isArray(p)?p:[];
    loading = false;
    document.getElementById('statusConexion').textContent = '📶 '+productos.length+' productos';
    renderProducts();
  }).catch(function(){loading=false;renderProducts()});
}

function renderCategories(){
  var el = document.getElementById('categoryTabs');
  var html = '<button class="category-tab '+(selectedCat==='todas'?'active':'')+'" onclick="selectCat(\'todas\')">Todo</button>';
  categorias.forEach(function(c){
    html += '<button class="category-tab '+(selectedCat===c.nombre?'active':'')+'" onclick="selectCat(\''+c.nombre.replace(/'/g,"\\'")+'\')">'+c.nombre+'</button>';
  });
  el.innerHTML = html;
}

function selectCat(cat){
  selectedCat = cat;
  renderCategories();
  renderProducts();
}

function onSearch(val){
  searchQuery = val.toLowerCase().trim();
  renderProducts();
}

function renderProducts(){
  var el = document.getElementById('productGrid');
  if(loading){el.innerHTML = '<div class="spinner"></div>';return}
  var filtered = productos.filter(function(p){
    var ok = selectedCat === 'todas' || p.categoria === selectedCat;
    if(searchQuery) ok = ok && (p.nombre.toLowerCase().indexOf(searchQuery)>=0 || p.codigo.toLowerCase().indexOf(searchQuery)>=0);
    return ok;
  });
  if(!filtered.length){el.innerHTML = '<div class="empty-state">No hay productos que coincidan</div>';return}
  el.innerHTML = filtered.map(function(p){
    var inCart = cart.find(function(c){return c.id===p.id});
    var cls = 'product-card'+(inCart?' added':'')+(p.stock<1?' out':'');
    return '<div class="'+cls+'" onclick="addToCart('+p.id+')"><div class="pname">'+p.nombre+'</div><div class="pcode">'+(p.codigo||'')+'</div><div class="pprice">RD$ '+p.precio.toFixed(2)+'</div>'+(p.stock<1?'<div class="pstock">Sin stock</div>':'<div class="pstock">Stock: '+p.stock+'</div>')+'</div>';
  }).join('');
}

function addToCart(id){
  var p = productos.find(function(x){return x.id===id});
  if(!p || p.stock<1) return;
  var existing = cart.find(function(c){return c.id===id});
  if(existing){
    existing.qty = Math.min(existing.qty + 1, 99);
  } else {
    cart.push({id:p.id, nombre:p.nombre, precio:p.precio, qty:1, stock:p.stock});
  }
  renderCart();
  renderProducts();
  showToast('✅ '+p.nombre);
}

function removeFromCart(id){
  cart = cart.filter(function(c){return c.id!==id});
  renderCart();
  renderProducts();
}

function changeQty(id,delta){
  var item = cart.find(function(c){return c.id===id});
  if(!item) return;
  item.qty = Math.max(1, Math.min(item.qty + delta, 99));
  renderCart();
}

function renderCart(){
  var countEl = document.getElementById('cartCount');
  var itemsEl = document.getElementById('cartItems');
  var footerEl = document.getElementById('cartFooter');
  countEl.textContent = cart.reduce(function(s,c){return s+c.qty},0)+' productos';
  if(!cart.length){
    itemsEl.innerHTML = '<div class="cart-empty"><div class="icon">🛒</div><div>Carrito vacío</div></div>';
    footerEl.style.display = 'none';
    document.getElementById('toolbarTotal').textContent = 'RD$ 0.00';
    return;
  }
  var subtotal = cart.reduce(function(s,c){return s + c.precio * c.qty},0);
  var tax = subtotal - subtotal/1.18;
  var total = subtotal;
  itemsEl.innerHTML = cart.map(function(c){
    return '<div class="cart-item"><div class="cart-item-top"><div class="cart-item-name">'+c.nombre+'</div><div class="cart-item-price">RD$ '+(c.precio*c.qty).toFixed(2)+'</div></div><div class="cart-item-qty"><button class="qty-btn" onclick="changeQty('+c.id+',-1)">−</button><span class="qty-value">'+c.qty+'</span><button class="qty-btn" onclick="changeQty('+c.id+',1)">+</button><button class="remove-btn" onclick="removeFromCart('+c.id+')">🗑</button></div></div>';
  }).join('');
  footerEl.style.display = 'block';
  document.getElementById('subtotalVal').textContent = 'RD$ '+subtotal.toFixed(2);
  document.getElementById('taxVal').textContent = 'RD$ '+tax.toFixed(2);
  document.getElementById('totalVal').textContent = 'RD$ '+total.toFixed(2);
  document.getElementById('toolbarTotal').textContent = 'RD$ '+total.toFixed(2);
}

function showPayment(){
  var total = cart.reduce(function(s,c){return s + c.precio * c.qty},0);
  var m = document.getElementById('modalContainer');
  m.innerHTML = '<div class="pay-modal" onclick="closeModal(event)"><div class="pay-modal-content" onclick="event.stopPropagation()"><h2>💳 Cobrar</h2><div style="font-size:24px;font-weight:750;text-align:center;margin-bottom:16px;color:#6d28d9">RD$ '+total.toFixed(2)+'</div><div style="margin-bottom:12px;font-weight:600;font-size:14px">Método de pago:</div><div class="pay-option active" id="payEfectivo" onclick="selectPay(\'efectivo\')">💵 Efectivo</div><div class="pay-option" id="payTarjeta" onclick="selectPay(\'tarjeta\')">💳 Tarjeta</div><div class="pay-option" id="payTransferencia" onclick="selectPay(\'transferencia\')">🏦 Transferencia</div><button class="pay-btn" onclick="doCheckout()">✅ Cobrar RD$ '+total.toFixed(2)+'</button><button class="pay-close" onclick="closeModal()">Cancelar</button></div></div>';
  window.selectedPay = 'efectivo';
}

function selectPay(method){
  window.selectedPay = method;
  document.querySelectorAll('.pay-option').forEach(function(el){el.classList.remove('active')});
  var id = method==='efectivo'?'payEfectivo':method==='tarjeta'?'payTarjeta':'payTransferencia';
  document.getElementById(id).classList.add('active');
}

function doCheckout(){
  if(!cart.length) return;
  var items = cart.map(function(c){return {id:c.id, cantidad:c.qty}});
  var btn = document.querySelector('.pay-btn');
  btn.disabled = true;
  btn.textContent = 'Procesando...';
  fetch('/api/pos/checkout', {
    method:'POST',
    headers:{'Content-Type':'application/json'},
    body:JSON.stringify({items:items, pago:window.selectedPay||'EFECTIVO'})
  }).then(function(r){return r.json()}).then(function(res){
    if(res.ok){
      showToast('✅ Factura '+res.factura+' creada - RD$ '+res.total.toFixed(2));
      cart = [];
      renderCart();
      renderProducts();
      closeModal();
      loadData();
    } else {
      showToast('Error: '+(res.error||'desconocido'));
      btn.disabled = false;
      btn.textContent = '✅ Cobrar';
    }
  }).catch(function(){
    showToast('Error de conexión');
    btn.disabled = false;
    btn.textContent = '✅ Cobrar';
  });
}

function closeModal(e){
  if(e && e.target !== e.currentTarget) return;
  document.getElementById('modalContainer').innerHTML = '';
}

function showToast(msg){
  var t = document.getElementById('toastContainer');
  t.innerHTML = '<div class="toast">'+msg+'</div>';
  setTimeout(function(){t.innerHTML=''},2500);
}

loadData();
setInterval(loadData, 15000);
</script>
</body>
</html>
""".trimIndent()
}
