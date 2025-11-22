import { defineStore } from 'pinia';
import axios from 'axios';

export const useMegaMenuStore = defineStore('megaMenu', {
    state: () => ({
        data: null,
        loading: false,
        error: null,
    }),
    actions: {
        async fetchMegaMenu() {
            if (this.data) return;
            this.loading = true;
            this.error = null;
            try {
                const res = await axios.get('http://localhost:8082/api/products/mega-menu');

                console.log('Mega menu data:', res.data);
                this.data = res.data;
            } catch (err) {
                console.error('Fetch mega menu error:', err);
                this.error = 'Không tải được mega menu';
            } finally {
                this.loading = false;
            }
        },
    },
});
