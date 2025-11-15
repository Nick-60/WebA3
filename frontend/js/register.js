const f=document.getElementById('registerForm');
f.addEventListener('submit',async(e)=>{
  e.preventDefault();
  const u=document.getElementById('username').value.trim();
  const em=document.getElementById('email').value.trim();
  const p=document.getElementById('password').value.trim();
  try{
    const d=await register(u,p,em);
    showSuccess('Registered successfully. Please sign in.');
    window.location.href='/login.html';
  }catch(err){
    showError('Register failed: '+(err?.response?.data?.message||err.message));
  }
});