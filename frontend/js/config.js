const BACKEND_BASE_URL = 'http://localhost:8080';
const LS_TOKEN_KEY = 'accessToken';
const LS_USER_PROFILE = 'userProfile';
function getToken(){ return window.localStorage.getItem(LS_TOKEN_KEY) || '' }
function setToken(t){ window.localStorage.setItem(LS_TOKEN_KEY, t || '') }
function clearToken(){ window.localStorage.removeItem(LS_TOKEN_KEY) }
function saveUserProfile(p){ try{ window.localStorage.setItem(LS_USER_PROFILE, JSON.stringify(p||{})) }catch(_){} }
function getUserProfile(){ try{ const s=window.localStorage.getItem(LS_USER_PROFILE); return s?JSON.parse(s):null }catch(_){ return null } }

