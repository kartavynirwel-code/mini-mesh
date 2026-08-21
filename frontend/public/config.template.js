// Template for runtime config injection.
//
// This file is NOT used directly by the app. Your container's entrypoint
// script should run something like:
//
//   envsubst '${GREETING_SERVICE_URL}' < config.template.js > /usr/share/nginx/html/config.js
//
// at container startup, reading GREETING_SERVICE_URL from the pod's env
// (which your Deployment sources from a ConfigMap). That produces a
// real config.js with the placeholder replaced, served as a static file
// nginx picks up before the React bundle loads. This is the standard
// pattern for making one built SPA image environment-portable without
// rebaking the JS bundle per environment.
window.__APP_CONFIG__ = {
  GREETING_SERVICE_URL: "${GREETING_SERVICE_URL}",
};
