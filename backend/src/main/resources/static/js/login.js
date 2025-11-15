const form = document.getElementById('loginForm');
form.addEventListener('submit', async (e) => {
  e.preventDefault();
  const username = document.getElementById('username').value.trim();
  const password = document.getElementById('password').value.trim();
  try {
    const data = await login(username, password);
    showSuccess('Signed in successfully');
    try { await fetchProfile(); } catch (_) {}
    window.location.href = '/dashboard.html';
  } catch (err) {
    showError('Sign in failed: ' + (err?.response?.data?.message || err.message));
  }
});