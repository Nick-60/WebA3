const form=document.getElementById('loginForm');
form.addEventListener('submit',async(e)=>{
  e.preventDefault();
  const u=document.getElementById('username').value.trim();
  const p=document.getElementById('password').value.trim();
  try{
    const d=await login(u,p);
    showSuccess('Signed in successfully');
    try{ await fetchProfile() }catch(_){}
    window.location.href='/dashboard.html';
  }catch(err){
    showError('Sign in failed: '+(err?.response?.data?.message||err.message));
  }
});