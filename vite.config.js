import { defineConfig } from 'vite';

// Vite configuration: treat `frontend` as project root with multi-page HTML entries
export default defineConfig({
  root: 'frontend',
  appType: 'mpa', // multi-page application
  server: {
    port: 5173,
    strictPort: true,
    open: '/login.html', // open login page by default
    headers: {
      'Content-Security-Policy': "default-src 'self' https:; script-src 'self' https: 'unsafe-inline' 'unsafe-eval'; style-src 'self' https: 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' https: data:; connect-src 'self' https: http://localhost:8080;"
    }
  },
  preview: {
    port: 5173,
    strictPort: true,
  },
  build: {
    outDir: '../dist',
    emptyOutDir: true,
  },
});

