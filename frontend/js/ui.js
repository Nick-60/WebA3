function showSuccess(msg){ alert(msg||'操作成功') }
function showError(msg){ alert(msg||'发生错误') }
function setText(id,text){ const el=document.getElementById(id); if(el) el.textContent=text }
function byId(id){ return document.getElementById(id) }

