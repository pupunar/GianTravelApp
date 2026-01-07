const { createApp } = Vue;

const app = createApp({
    data() {
        return {
            tripName: 'My Amazing Trip',
            tripActive: true,
            locationsCount: 0,
            distanceKm: '0.00',
            avgSpeed: '0.00',
            duration: '--',
            maxAltitude: '0',
            currentLocation: null,
            locations: [],
            diaryEntries: [],
            comments: [],
            newComment: {
                name: '',
                message: ''
            },
            map: null,
            polyline: null,
            markers: [],
            shareCode: '',
            backendUrl: 'http://localhost:5000'
        };
    },
    
    mounted() {
        // Estrai share code dall'URL
        const params = new URLSearchParams(window.location.search);
        this.shareCode = params.get('code') || 'SHARE_12345678';
        
        // Inizializza mappa
        this.initMap();
        
        // Carica dati del viaggio
        this.loadTripData();
        
        // Aggiorna posizione ogni 5 secondi (real-time)
        setInterval(() => this.updateTripData(), 5000);
    },
    
    methods: {
        initMap() {
            this.map = L.map('map').setView([51.505, -0.09], 13);
            
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: '© OpenStreetMap contributors',
                maxZoom: 19
            }).addTo(this.map);
        },
        
        async loadTripData() {
            try {
                // Carica viaggio tramite share code
                const response = await fetch(`${this.backendUrl}/api/share/${this.shareCode}`);
                const trip = await response.json();
                
                this.tripName = trip.name;
                this.tripActive = trip.isActive;
                
                // Carica posizioni
                await this.loadLocations(trip.tripId);
                
                // Carica diario
                this.loadDiaryEntries(trip);
                
                // Carica commenti
                await this.loadComments(trip.tripId);
            } catch (error) {
                console.error('Errore nel caricamento del viaggio:', error);
            }
        },
        
        async loadLocations(tripId) {
            try {
                const response = await fetch(`${this.backendUrl}/api/trips/${tripId}/locations`);
                this.locations = await response.json();
                
                this.locationsCount = this.locations.length;
                
                // Disegna polyline
                this.drawPolyline();
                
                // Aggiorna statistiche
                this.updateStatistics();
                
                // Imposta posizione corrente
                if (this.locations.length > 0) {
                    this.currentLocation = this.locations[this.locations.length - 1];
                }
            } catch (error) {
                console.error('Errore nel caricamento delle posizioni:', error);
            }
        },
        
        drawPolyline() {
            // Rimuovi polyline precedente
            if (this.polyline) {
                this.map.removeLayer(this.polyline);
            }
            
            // Converti coordinate
            const latLngs = this.locations.map(loc => 
                L.latLng(loc.latitude, loc.longitude)
            );
            
            // Crea polyline
            this.polyline = L.polyline(latLngs, {
                color: '#667eea',
                weight: 3,
                opacity: 0.8
            }).addTo(this.map);
            
            // Aggiungi marcatori start/end
            if (latLngs.length > 0) {
                // Start marker
                L.circleMarker(latLngs[0], {
                    radius: 6,
                    color: '#28a745',
                    fillColor: '#28a745',
                    fillOpacity: 1
                }).bindPopup('Start').addTo(this.map);
                
                // End marker
                L.circleMarker(latLngs[latLngs.length - 1], {
                    radius: 6,
                    color: '#dc3545',
                    fillColor: '#dc3545',
                    fillOpacity: 1
                }).bindPopup('Current Location').addTo(this.map);
                
                // Centra mappa
                this.map.fitBounds(L.latLngBounds(latLngs));
            }
        },
        
        updateStatistics() {
            if (this.locations.length < 2) return;
            
            // Calcola distanza
            let totalDistance = 0;
            let speedSum = 0;
            let maxAlt = 0;
            
            for (let i = 1; i < this.locations.length; i++) {
                const from = this.locations[i - 1];
                const to = this.locations[i];
                totalDistance += this.haversineDistance(from, to);
                speedSum += to.speed || 0;
                maxAlt = Math.max(maxAlt, to.altitude || 0);
            }
            
            this.distanceKm = totalDistance.toFixed(2);
            this.avgSpeed = (speedSum / this.locations.length * 3.6).toFixed(1);
            this.maxAltitude = Math.round(maxAlt).toString();
            
            // Calcola durata
            const duration = this.locations[this.locations.length - 1].timestamp - 
                           this.locations[0].timestamp;
            const hours = Math.floor(duration / (1000 * 60 * 60));
            const minutes = Math.floor((duration % (1000 * 60 * 60)) / (1000 * 60));
            this.duration = `${hours}h ${minutes}m`;
        },
        
        haversineDistance(from, to) {
            const R = 6371; // km
            const dLat = this.toRad(to.latitude - from.latitude);
            const dLon = this.toRad(to.longitude - from.longitude);
            const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                    Math.cos(this.toRad(from.latitude)) * Math.cos(this.toRad(to.latitude)) *
                    Math.sin(dLon/2) * Math.sin(dLon/2);
            const c = 2 * Math.asin(Math.sqrt(a));
            return R * c;
        },
        
        toRad(deg) {
            return deg * (Math.PI / 180);
        },
        
        loadDiaryEntries(trip) {
            this.diaryEntries = trip.diaryEntries ? 
                Object.values(trip.diaryEntries).sort((a, b) => b.timestamp - a.timestamp) : 
                [];
        },
        
        async loadComments(tripId) {
            try {
                const response = await fetch(`${this.backendUrl}/api/trips/${tripId}/comments`);
                this.comments = await response.json();
            } catch (error) {
                console.error('Errore nel caricamento dei commenti:', error);
            }
        },
        
        async updateTripData() {
            try {
                const response = await fetch(`${this.backendUrl}/api/share/${this.shareCode}`);
                const trip = await response.json();
                
                // Aggiorna solo se ci sono nuove posizioni
                await this.loadLocations(trip.tripId);
                this.loadDiaryEntries(trip);
                await this.loadComments(trip.tripId);
            } catch (error) {
                console.error('Errore nell\'aggiornamento:', error);
            }
        },
        
        async addComment() {
            if (!this.newComment.name || !this.newComment.message) {
                alert('Per favore, compila tutti i campi');
                return;
            }
            
            try {
                const trip = await (await fetch(`${this.backendUrl}/api/share/${this.shareCode}`)).json();
                
                const response = await fetch(
                    `${this.backendUrl}/api/trips/${trip.tripId}/comments`,
                    {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            friendName: this.newComment.name,
                            message: this.newComment.message,
                            latitude: this.currentLocation?.latitude,
                            longitude: this.currentLocation?.longitude
                        })
                    }
                );
                
                if (response.ok) {
                    this.newComment = { name: '', message: '' };
                    await this.loadComments(trip.tripId);
                }
            } catch (error) {
                console.error('Errore nell\'invio del commento:', error);
            }
        },
        
        formatTime(timestamp) {
            const date = new Date(timestamp);
            return date.toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' });
        }
    }
});

app.mount('#app');
