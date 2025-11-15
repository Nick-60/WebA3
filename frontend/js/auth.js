if (typeof axios === 'undefined') { console.error('Axios not found.'); }
const api = axios.create({ baseURL: BACKEND_BASE_URL });
api.interceptors.request.use((config)=>{ const t=getToken(); if(t){ config.headers=config.headers||{}; config.headers['Authorization']='Bearer '+t } return config });
api.interceptors.response.use((res)=>res,(err)=>{ const s=err?.response?.status; const m=err?.response?.data?.message||err.message||'Request error'; if(s===401){ try{ showError('Unauthorized. Please sign in again.'); setTimeout(function(){ window.location.href='/login.html' }, 800) }catch(_){ alert('Unauthorized. Please sign in again.') } } else if(s===403){ try{ showError('Forbidden. You do not have permission.'); }catch(_){ alert('Forbidden. You do not have permission.') } } else { try{ showError(m) }catch(_){ alert(m) } } return Promise.reject(err) });
async function login(username,password){ const r=await api.post('/api/auth/login',{username,password}); const t=r?.data?.accessToken; if(!t) throw new Error('Login response missing accessToken'); setToken(t); return r.data }
async function fetchProfile(){ const r=await api.get('/api/profile'); saveUserProfile(r?.data||{}); return r.data }
async function createLeaveRequest(dto){ const p={ leave_type:dto.leave_type, start_date:dto.start_date, end_date:dto.end_date, comment:dto.comment||'' }; const r=await api.post('/api/leave/request',p); return r.data }
async function listPendingApprovals(page=0,size=10){ const r=await api.get('/api/leave/pending',{ params:{page,size} }); return r.data }
async function approveLeave(id,comment=''){ const r=await api.patch(`/api/leave/${id}/approve`,{comment}); return r.data }
async function rejectLeave(id,comment=''){ const r=await api.patch(`/api/leave/${id}/reject`,{comment}); return r.data }
async function exportHrReport(params={}){ const c={ params, responseType:'blob' }; const r=await api.get('/api/leave/hr/export',c); return r.data }
async function listEmployeeLeavesByUsername(username,page=0,size=10){ const r=await api.get(`/api/leave/employee/${encodeURIComponent(username)}`,{ params:{page,size} }); return r.data }
async function listManagerApprovalsHistory(page=0,size=10){ const r=await api.get('/api/leave/approvals/history',{ params:{page,size} }); return r.data }
async function cancelLeave(id){ const r=await api.patch(`/api/leave/${id}/cancel`); return r.data }
async function register(username,password,email){ const r=await api.post('/api/auth/register',{username,password,email}); return r.data }
