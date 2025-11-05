const CACHE_NAME = 'fitness-app-v4';
// Only precache truly static assets with correct paths.
// Avoid precaching HTML routes like '/' so authenticated views aren't served stale from cache.
const urlsToCache = [
  '/manifest.json'
];

// Install event - cache resources
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => {
        console.log('Opened cache');
        return cache.addAll(urlsToCache);
      })
  );
  // Activate updated SW immediately
  self.skipWaiting();
});

// Fetch event - serve from cache when offline
self.addEventListener('fetch', event => {
  const request = event.request;

  // Network-first strategy for navigations/HTML to ensure fresh authenticated content
  const isNavigation = request.mode === 'navigate' || (request.headers.get('accept') || '').includes('text/html');
  const isCss = request.destination === 'style' || (request.url && request.url.endsWith('.css'));

  if (isNavigation) {
    event.respondWith(
      fetch(request)
        .then(networkResponse => {
          // Optionally put a copy into cache for offline fallback
          const copy = networkResponse.clone();
          caches.open(CACHE_NAME).then(cache => cache.put(request, copy)).catch(() => {});
          return networkResponse;
        })
        .catch(() => caches.match(request))
    );
    return;
  }

  // Network-first for CSS to ensure latest styles, cache-first for others
  if (isCss) {
    event.respondWith(
      fetch(request)
        .then(networkResponse => {
          const copy = networkResponse.clone();
          caches.open(CACHE_NAME).then(cache => cache.put(request, copy)).catch(() => {});
          return networkResponse;
        })
        .catch(() => caches.match(request))
    );
    return;
  }

  // Cache-first for other requests (JS/images/etc.)
  event.respondWith(
    caches.match(request).then(cached => {
      return cached || fetch(request).then(networkResponse => {
        const copy = networkResponse.clone();
        caches.open(CACHE_NAME).then(cache => cache.put(request, copy)).catch(() => {});
        return networkResponse;
      });
    })
  );
});

// Activate event - clean up old caches
self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(cacheNames => {
      return Promise.all(
        cacheNames.map(cacheName => {
          if (cacheName !== CACHE_NAME) {
            console.log('Deleting old cache:', cacheName);
            return caches.delete(cacheName);
          }
        })
      );
    })
  );
  // Take control of uncontrolled clients ASAP
  self.clients.claim();
});

// --- Web Push: display notification ---
self.addEventListener('push', event => {
  try {
    const data = event.data ? event.data.json() : {};
    const title = data.title || 'FitPower';
    const body = data.body || '';
    const url = data.url || '/';
    const options = {
      body,
      icon: '/icons/icon-192x192.png',
      badge: '/icons/icon-192x192.png',
      data: { url },
      vibrate: [100, 50, 100]
    };
    event.waitUntil(self.registration.showNotification(title, options));
  } catch (e) {
    event.waitUntil(self.registration.showNotification('FitPower', { body: 'New notification' }));
  }
});

// Focus existing tab or open new on click
self.addEventListener('notificationclick', event => {
  event.notification.close();
  const url = (event.notification && event.notification.data && event.notification.data.url) || '/';
  event.waitUntil((async () => {
    const allClients = await clients.matchAll({ type: 'window', includeUncontrolled: true });
    for (const client of allClients) {
      try {
        await client.navigate(url);
      } catch(_) {}
      if ('focus' in client) {
        return client.focus();
      }
    }
    if (clients.openWindow) {
      return clients.openWindow(url);
    }
  })());
});
