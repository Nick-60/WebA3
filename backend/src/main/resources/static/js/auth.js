// Axios instance and auth interceptors
// Requires: config.js loaded first

// Load Axios from CDN or environment; if not present, fail gracefully
if (typeof axios === 'undefined') {
  console.error('Axios not found. Please include axios via CDN.');
}

const api = axios.create({
  baseURL: BACKEND_BASE_URL,
});

// Attach Authorization header automatically
api.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers = config.headers || {};
    config.headers['Authorization'] = `Bearer ${token}`;
  }
  return config;
});

// Global response error handling
api.interceptors.response.use(
  (res) => res,
  (err) => {
    const status = err?.response?.status;
    const message = err?.response?.data?.message || err.message || 'Request error';
    // 401/403 -> notify and optionally redirect
    if (status === 401) {
      showError('Unauthorized. Please sign in again.');
    } else if (status === 403) {
      showError('Forbidden. You do not have permission.');
    } else {
      showError(message);
    }
    return Promise.reject(err);
  }
);

// Login API
async function login(username, password) {
  const res = await api.post('/api/auth/login', { username, password });
  const token = res?.data?.accessToken;
  if (!token) throw new Error('Login response missing accessToken');
  setToken(token);
  return res.data;
}

// Profile API
async function fetchProfile() {
  const res = await api.get('/api/profile');
  saveUserProfile(res?.data || {});
  return res.data;
}

// Leave request APIs
async function createLeaveRequest(dto) {
  // Backend expects snake_case keys per DTO mapping
  const payload = {
    leave_type: dto.leave_type,
    start_date: dto.start_date,
    end_date: dto.end_date,
    comment: dto.comment || '',
  };
  const res = await api.post('/api/leave/request', payload);
  return res.data;
}

async function listPendingApprovals(page = 0, size = 10) {
  const res = await api.get('/api/leave/pending', { params: { page, size } });
  return res.data; // ApiResponse with data = PagedData
}

async function approveLeave(id, comment = '') {
  const res = await api.patch(`/api/leave/${id}/approve`, { comment });
  return res.data;
}

async function rejectLeave(id, comment = '') {
  const res = await api.patch(`/api/leave/${id}/reject`, { comment });
  return res.data;
}

// HR export
async function exportHrReport(params = {}) {
  // params: { from, to, departmentId }
  const config = { params, responseType: 'blob' };
  const res = await api.get('/api/leave/hr/export', config);
  return res.data; // Blob (xlsx)
}

