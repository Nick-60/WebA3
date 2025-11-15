// Minimal static server with server-side redirect for root
// No external dependencies required
const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = process.env.PORT ? Number(process.env.PORT) : 5173;
const ROOT = path.resolve(__dirname);

const MIME = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.gif': 'image/gif',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon'
};

function sendRedirect(res, location) {
  res.statusCode = 302;
  res.setHeader('Location', location);
  res.end();
}

function safeJoin(base, target) {
  const targetPath = path.normalize(target).replace(/^([/\\])*\.+([/\\]|$)/, '');
  return path.join(base, targetPath);
}

const server = http.createServer((req, res) => {
  const urlPath = decodeURIComponent((req.url || '/').split('?')[0]);
  // Server-side redirect: root -> /frontend/login.html
  if (urlPath === '/' || urlPath === '/index.html') {
    return sendRedirect(res, '/frontend/login.html');
  }

  let filePath = safeJoin(ROOT, urlPath);
  try {
    const stat = fs.statSync(filePath);
    if (stat.isDirectory()) {
      const indexPath = path.join(filePath, 'index.html');
      if (fs.existsSync(indexPath)) {
        filePath = indexPath;
      } else {
        // No index.html in directory: redirect to login for UX
        return sendRedirect(res, '/frontend/login.html');
      }
    }

    const ext = path.extname(filePath).toLowerCase();
    res.setHeader('Content-Type', MIME[ext] || 'application/octet-stream');
    const stream = fs.createReadStream(filePath);
    stream.on('error', () => {
      res.statusCode = 500;
      res.end('Internal Server Error');
    });
    stream.pipe(res);
  } catch (err) {
    res.statusCode = 404;
    res.end('Not Found');
  }
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`Static server running: http://127.0.0.1:${PORT}/`);
});

