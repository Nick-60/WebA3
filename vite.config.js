import { defineConfig } from 'vite';

// Vite configuration: treat `frontend` as project root with multi-page HTML entries
export default defineConfig({
  root: 'frontend',
  appType: 'mpa', // multi-page application
  server: {
    port: 5173,
    strictPort: true,
    open: '/login.html', // open login page by default
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

