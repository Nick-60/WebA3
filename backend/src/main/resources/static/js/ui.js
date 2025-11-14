// Minimal UI helpers for alerts and loading

function showSuccess(msg) {
  alert(msg || 'Operation succeeded');
}

function showError(msg) {
  alert(msg || 'An error occurred');
}

function setText(id, text) {
  const el = document.getElementById(id);
  if (el) el.textContent = text;
}

function byId(id) {
  return document.getElementById(id);
}

