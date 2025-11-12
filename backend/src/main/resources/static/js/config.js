// Global configuration for frontend
// Base URL provided by task requirements
const BACKEND_BASE_URL = 'http://localhost:8080';

// LocalStorage keys
const LS_TOKEN_KEY = 'accessToken';
const LS_USER_PROFILE = 'userProfile';

// Simple helpers
function getToken() {
  return window.localStorage.getItem(LS_TOKEN_KEY) || '';
}

function setToken(token) {
  window.localStorage.setItem(LS_TOKEN_KEY, token || '');
}

function clearToken() {
  window.localStorage.removeItem(LS_TOKEN_KEY);
}

function saveUserProfile(profile) {
  try {
    window.localStorage.setItem(LS_USER_PROFILE, JSON.stringify(profile || {}));
  } catch (_) {}
}

function getUserProfile() {
  try {
    const s = window.localStorage.getItem(LS_USER_PROFILE);
    return s ? JSON.parse(s) : null;
  } catch (_) {
    return null;
  }
}

