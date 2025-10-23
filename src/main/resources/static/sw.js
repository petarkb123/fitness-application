const CACHE_NAME = 'fitness-app-v1';
const urlsToCache = [
  '/',
  '/css/dashboard.css',
  '/css/exercises.css',
  '/css/workouts.css',
  '/css/templates.css',
  '/css/create.css',
  '/css/edit.css',
  '/css/details.css',
  '/css/history.css',
  '/css/session.css',
  '/css/stats-weekly.css',
  '/css/advanced-stats.css',
  '/css/settings.css',
  '/css/login.css',
  '/css/register.css',
  '/css/index.css',
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
});

// Fetch event - serve from cache when offline
self.addEventListener('fetch', event => {
  event.respondWith(
    caches.match(event.request)
      .then(response => {
        // Return cached version or fetch from network
        return response || fetch(event.request);
      }
    )
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
});
