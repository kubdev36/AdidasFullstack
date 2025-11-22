import { defineStore } from "pinia";
import axios from "axios";

const api = axios.create({
    baseURL: "http://localhost:8082/api",
    headers: { "Content-Type": "application/json" }
});

export const useAdminVoucherStore = defineStore("adminVoucher", {
    state: () => ({
        vouchers: [],
        loading: false,
        error: null,
    }),

    actions: {
        async fetchVouchers() {
            try {
                this.loading = true;
                this.error = null;
                const res = await api.get("/vouchers");
                this.vouchers = res.data;
            } finally {
                this.loading = false;
            }
        },

        async addVoucher(voucherData) {
            try {
                this.loading = true;
                this.error = null;
                const res = await api.post("/vouchers", voucherData);
                this.vouchers.push(res.data);
                return res.data;
            } finally {
                this.loading = false;
            }
        },

        async updateVoucher(id, voucherData) {
            try {
                this.loading = true;
                this.error = null;
                const res = await api.put(`/vouchers/${id}`, voucherData);
                const updated = res.data;
                const index = this.vouchers.findIndex(v => v.id === id);
                if (index !== -1) this.vouchers[index] = updated;
                return updated;
            } finally {
                this.loading = false;
            }
        },

        async deleteVoucher(id) {
            try {
                this.loading = true;
                this.error = null;
                await api.delete(`/vouchers/${id}`);
                this.vouchers = this.vouchers.filter(v => v.id !== id);
            } finally {
                this.loading = false;
            }
        }
    }
});
