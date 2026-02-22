import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    https: false, // opzionale, per far partire dev server https (consigliato)
    proxy: {
      '/api': {
        target: 'https://localhost:4567',
        changeOrigin: true,
        secure: false, // disabilita controllo certificato self-signed nel proxy
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
});
