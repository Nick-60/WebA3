function ensureToastContainer(){
  var c=document.getElementById('toastContainer');
  if(!c){
    c=document.createElement('div');
    c.id='toastContainer';
    c.style.position='fixed';
    c.style.top='16px';
    c.style.right='16px';
    c.style.zIndex='9999';
    document.body.appendChild(c);
  }
  return c;
}
function notify(type,msg){
  var c=ensureToastContainer();
  var el=document.createElement('div');
  el.className='alert alert-'+(type||'info')+' u-card-elevated';
  el.textContent=msg||'';
  c.appendChild(el);
  setTimeout(function(){ if(el && el.parentNode){ el.parentNode.removeChild(el) } },3000);
}
function showSuccess(msg){ notify('success',msg||'Operation succeeded') }
function showError(msg){ notify('danger',msg||'An error occurred') }
function setText(id,text){ var el=document.getElementById(id); if(el) el.textContent=text }
function byId(id){ return document.getElementById(id) }
function withButtonLoading(btn,fn){
  if(!btn||typeof fn!=='function') return;
  btn.classList.add('is-loading');
  btn.disabled=true;
  var done=function(){ btn.classList.remove('is-loading'); btn.disabled=false };
  var p;
  try{ p=fn() }catch(e){ done(); throw e }
  if(p&&typeof p.then==='function'){ return p.then(function(r){ done(); return r }).catch(function(e){ done(); throw e }) } else { done(); return p }
}
