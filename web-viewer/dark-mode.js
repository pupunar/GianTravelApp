/**
 * Dark Mode Manager
 * Gestisce il tema scuro/chiaro del web viewer
 */

const DarkModeManager = (() => {
  const STORAGE_KEY = 'giantravelapp_theme';
  const DARK_CLASS = 'dark-mode';
  const HTML_ATTR = 'data-theme';
  
  // Detectionu preferenza sistema
  function getSystemPreference() {
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }
  
  // Ottieni tema salvato o preferenza sistema
  function getSavedTheme() {
    const saved = localStorage.getItem(STORAGE_KEY);
    return saved || getSystemPreference();
  }
  
  // Applica tema
  function applyTheme(theme) {
    const isDark = theme === 'dark';
    const html = document.documentElement;
    const body = document.body;
    
    if (isDark) {
      html.setAttribute(HTML_ATTR, 'dark');
      body.classList.add(DARK_CLASS);
    } else {
      html.setAttribute(HTML_ATTR, 'light');
      body.classList.remove(DARK_CLASS);
    }
    
    localStorage.setItem(STORAGE_KEY, theme);
    
    // Notifica app
    window.dispatchEvent(new CustomEvent('themeChanged', {
      detail: { theme }
    }));
    
    // Aggiorna Leaflet map colori
    updateMapTheme(isDark);
  }
  
  // Aggiorna colori mappa basato sul tema
  function updateMapTheme(isDark) {
    if (window.app && window.app.map) {
      // Leaflet OSM non cambia basato su CSS, usiamo un'alternativa
      const layerUrl = isDark
        ? 'https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png'
        : 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
      
      const attribution = isDark
        ? '© CartoDB contributors | © OpenStreetMap contributors'
        : '© OpenStreetMap contributors';
      
      // Rimuovi layer precedente
      window.app.map.eachLayer(layer => {
        if (layer instanceof L.TileLayer) {
          window.app.map.removeLayer(layer);
        }
      });
      
      // Aggiungi nuovo layer
      L.tileLayer(layerUrl, {
        attribution,
        maxZoom: 19
      }).addTo(window.app.map);
    }
  }
  
  // Toggle tema
  function toggle() {
    const current = localStorage.getItem(STORAGE_KEY) || getSystemPreference();
    const newTheme = current === 'dark' ? 'light' : 'dark';
    applyTheme(newTheme);
  }
  
  // Inizializza
  function init() {
    const theme = getSavedTheme();
    applyTheme(theme);
    
    // Monitora cambiamenti preferenza sistema
    const darkModeQuery = window.matchMedia('(prefers-color-scheme: dark)');
    darkModeQuery.addListener((e) => {
      if (!localStorage.getItem(STORAGE_KEY)) {
        applyTheme(e.matches ? 'dark' : 'light');
      }
    });
    
    // Crea bottone toggle
    createToggleButton();
  }
  
  // Crea bottone per togglare tema
  function createToggleButton() {
    const button = document.createElement('button');
    button.id = 'theme-toggle';
    button.className = 'theme-toggle';
    button.setAttribute('aria-label', 'Toggle dark mode');
    button.innerHTML = `
      <svg class="sun-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="5"></circle>
        <line x1="12" y1="1" x2="12" y2="3"></line>
        <line x1="12" y1="21" x2="12" y2="23"></line>
        <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line>
        <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line>
        <line x1="1" y1="12" x2="3" y2="12"></line>
        <line x1="21" y1="12" x2="23" y2="12"></line>
        <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line>
        <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line>
      </svg>
      <svg class="moon-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
      </svg>
    `;
    
    button.addEventListener('click', toggle);
    
    // Aggiungi al DOM
    const topBar = document.querySelector('.top-bar') || document.querySelector('header');
    if (topBar) {
      topBar.appendChild(button);
    } else {
      document.body.appendChild(button);
    }
  }
  
  return {
    init,
    toggle,
    applyTheme,
    getSavedTheme,
    getSystemPreference
  };
})();

// Inizializza quando DOM è pronto
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', () => DarkModeManager.init());
} else {
  DarkModeManager.init();
}
