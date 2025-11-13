function buildNav(profile){
  var role = profile && profile.role;
  var username = profile && profile.username;
  var links = ''+
    '<li class="nav-item"><a class="nav-link" href="/dashboard.html" data-path="/dashboard.html">仪表盘</a></li>'+
    (role==='EMPLOYEE' ? '<li class="nav-item"><a class="nav-link" href="/apply_leave.html" data-path="/apply_leave.html">提交请假</a></li>' : '')+
    (role==='EMPLOYEE' ? '<li class="nav-item"><a class="nav-link" href="/employee_requests.html" data-path="/employee_requests.html">我的请假<span id="badgeMyTotal" class="badge badge-pill badge-secondary ml-1" style="display:none"></span><span id="badgeMyPendingDot" class="badge badge-pill badge-danger ml-1" style="display:none">●</span></a></li>' : '')+
    (role==='MANAGER' ? '<li class="nav-item"><a class="nav-link" href="/pending_approvals.html" data-path="/pending_approvals.html">待审批<span id="badgePending" class="badge badge-pill badge-danger ml-1" style="display:none"></span></a></li>' : '')+
    (role==='MANAGER' ? '<li class="nav-item"><a class="nav-link" href="/manager_approvals_history.html" data-path="/manager_approvals_history.html">已处理审批</a></li>' : '')+
    (role==='HR' ? '<li class="nav-item"><a class="nav-link" href="/hr_report.html" data-path="/hr_report.html">HR 报表</a></li>' : '');
  var html = ''+
    '<div class="container">'+
      '<a class="navbar-brand" href="/dashboard.html">Leave System</a>'+
      '<button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navCollapse" aria-controls="navCollapse" aria-expanded="false" aria-label="Toggle navigation">'+
        '<span class="navbar-toggler-icon"></span>'+
      '</button>'+
      '<div class="collapse navbar-collapse" id="navCollapse">'+
        '<ul class="navbar-nav mr-auto">'+ links +'</ul>'+
        '<ul class="navbar-nav ml-auto">'+
          (username ? '<li class="nav-item"><span class="nav-link u-text-muted">'+username+'</span></li>' : '')+
          '<li class="nav-item"><a class="nav-link" href="/login.html">退出登录</a></li>'+
        '</ul>'+
      '</div>'+
    '</div>';
  return html;
}

function highlightActive(){
  var p = window.location.pathname;
  var as = document.querySelectorAll('#navMount a.nav-link');
  for(var i=0;i<as.length;i++){ var a=as[i]; var path=a.getAttribute('data-path'); if(path===p){ a.classList.add('active') } }
}

async function updateCounts(profile){
  try{
    if(profile && profile.role==='MANAGER'){
      var r = await listPendingApprovals(0,1);
      if(r && r.code===0){ var n=r.data && r.data.totalElements ? r.data.totalElements : 0; var b=document.getElementById('badgePending'); if(b){ if(n>0){ b.textContent=n; b.style.display='inline-block' } else { b.style.display='none' } } }
    }
    if(profile && profile.role==='EMPLOYEE'){
      var u = profile.username || '';
      var r2 = await listEmployeeLeavesByUsername(u,0,10);
      if(r2 && r2.code===0){
        var pd=r2.data||{}; var total=pd.totalElements||0; var items=pd.items||[]; var hasPending=false; for(var i=0;i<items.length;i++){ if(items[i].status==='PENDING'){ hasPending=true; break } }
        var bt=document.getElementById('badgeMyTotal'); if(bt){ if(total>0){ bt.textContent=total; bt.style.display='inline-block' } else { bt.style.display='none' } }
        var bd=document.getElementById('badgeMyPendingDot'); if(bd){ bd.style.display = hasPending ? 'inline-block' : 'none' }
      }
    }
  }catch(e){}
}

async function renderNavbar(){
  var mount = document.getElementById('navMount');
  if(!mount) return;
  try{
    var prof = getToken() ? await fetchProfile() : null;
    mount.innerHTML = buildNav(prof||{});
    highlightActive();
    await updateCounts(prof||{});
  }catch(e){ mount.innerHTML = buildNav({}); highlightActive() }
}

document.addEventListener('DOMContentLoaded', function(){ renderNavbar() });
