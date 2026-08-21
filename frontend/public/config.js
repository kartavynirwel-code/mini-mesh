// Local-dev default only. In the built Docker image, your container
// entrypoint overwrites this file (generated from config.template.js)
// with the real GREETING_SERVICE_URL for that environment.
window.__APP_CONFIG__ = {
  GREETING_SERVICE_URL: "/api",
};
