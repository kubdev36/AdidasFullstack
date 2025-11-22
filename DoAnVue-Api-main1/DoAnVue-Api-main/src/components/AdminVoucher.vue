<template>
  <AppHeader/>

  <div class="admin-products-container">
    <div class="header-section">
      <h1>Quản Lý Voucher</h1>
      <button @click="openModal('add')" class="add-btn">
        <i class="fas fa-plus"></i> Thêm Voucher Mới
      </button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading">
      <i class="fas fa-spinner fa-spin"></i> Đang tải dữ liệu...
    </div>

    <!-- Table -->
    <div v-else class="product-table-wrapper">
      <table class="product-table">
        <thead>
        <tr>
          <th>Code</th>
          <th>Mô tả</th>
          <th>Loại</th>
          <th>Giá trị</th>
          <th>Giới hạn</th>
          <th>Bắt đầu</th>
          <th>Kết thúc</th>
          <th>Kích hoạt</th>
          <th>Hành động</th>
        </tr>
        </thead>

        <tbody>
        <tr v-for="v in vouchers" :key="v.id">
          <td>{{ v.code }}</td>
          <td class="desc-cell">{{ v.description }}</td>
          <td>{{ v.discountType }}</td>
          <td>
            {{ v.discountType === "PERCENT"
              ? v.discountValue + "%"
              : formatPrice(v.discountValue) }}
          </td>
          <td>{{ v.usageLimit || "∞" }}</td>
          <td>{{ formatDate(v.startDate) }}</td>
          <td>{{ formatDate(v.endDate) }}</td>
          <td>{{ v.active ? "✓" : "✗" }}</td>

          <td class="actions">
            <button class="action-btn edit" @click="openModal('edit', v)">
              <i class="fas fa-edit"></i> Sửa
            </button>

            <button class="action-btn delete" @click="deleteVoucher(v.id)">
              <i class="fas fa-trash-alt"></i> Xóa
            </button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal -->
    <div v-if="isModalOpen" class="modal-overlay">
      <div class="modal-content">

        <span @click="closeModal" class="close-btn">&times;</span>

        <h2>
          {{ modalMode === "add" ? "Thêm Voucher" : "Chỉnh Sửa Voucher" }}
        </h2>

        <form @submit.prevent="saveVoucher" class="product-form">

          <!-- Code + Type -->
          <div class="form-row">
            <div class="form-group">
              <label>Mã voucher*</label>
              <input v-model="form.code" required />
            </div>

            <div class="form-group">
              <label>Loại giảm giá*</label>
              <select v-model="form.discountType" required>
                <option value="PERCENT">PERCENT (%)</option>
                <option value="FIXED">FIXED (VNĐ)</option>
              </select>
            </div>
          </div>

          <!-- ✅ MÔ TẢ -->
          <div class="form-group">
            <label>Mô tả</label>
            <textarea
                v-model="form.description"
                rows="2"
                placeholder="Mô tả ngắn cho voucher (ví dụ: Giảm 20% tối đa 50k cho mọi đơn hàng)"
            ></textarea>
          </div>

          <!-- Value -->
          <div class="form-row">
            <div class="form-group">
              <label>Giá trị giảm*</label>
              <input type="number" v-model.number="form.discountValue" required />
            </div>

            <div class="form-group">
              <label>Giảm tối đa</label>
              <input type="number" v-model.number="form.maxDiscount" />
            </div>
          </div>

          <!-- Min, Limit -->
          <div class="form-row">
            <div class="form-group">
              <label>Đơn tối thiểu</label>
              <input type="number" v-model.number="form.minOrderValue" />
            </div>

            <div class="form-group">
              <label>Giới hạn lượt dùng</label>
              <input type="number" v-model.number="form.usageLimit" />
            </div>
          </div>

          <!-- Dates -->
          <div class="form-row">
            <div class="form-group">
              <label>Ngày bắt đầu</label>
              <input type="datetime-local" v-model="form.startDate" />
            </div>

            <div class="form-group">
              <label>Ngày kết thúc</label>
              <input type="datetime-local" v-model="form.endDate" />
            </div>
          </div>

          <div class="form-group">
            <label>Kích hoạt</label>
            <input type="checkbox" v-model="form.active" />
          </div>

          <div class="form-actions">
            <button type="button" class="cancel-btn" @click="closeModal">Hủy</button>
            <button type="submit" class="save-btn" :disabled="saving">
              {{ saving ? "Đang lưu..." : "Lưu" }}
            </button>
          </div>
        </form>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import { useAdminVoucherStore } from "@/stores/adminVoucherStore";
import AppHeader from "./AppHeader.vue";

const voucherStore = useAdminVoucherStore();

const vouchers = computed(() => voucherStore.vouchers);
const loading = computed(() => voucherStore.loading);
const saving = computed(() => voucherStore.loading);

const isModalOpen = ref(false);
const modalMode = ref("add");

const form = ref({});

const emptyForm = {
  id: null,
  code: "",
  description: "",          // ✅ thêm description vào form mặc định
  discountType: "PERCENT",
  discountValue: 0,
  minOrderValue: null,
  maxDiscount: null,
  startDate: "",
  endDate: "",
  usageLimit: null,
  active: true
};

onMounted(async () => {
  console.log("🔄 Fetch vouchers on mount");
  await voucherStore.fetchVouchers();
});

const openModal = (mode, voucher = null) => {
  modalMode.value = mode;

  if (mode === "edit" && voucher) {
    console.log("✏️ Edit voucher:", voucher);

    form.value = {
      id: voucher.id,
      code: voucher.code,
      description: voucher.description || "",   // ✅ map description
      discountType: voucher.discountType || "PERCENT",
      discountValue: voucher.discountValue,
      minOrderValue: voucher.minOrderValue,
      maxDiscount: voucher.maxDiscount,
      usageLimit: voucher.usageLimit,
      active: voucher.active,
      startDate: voucher.startDate ? voucher.startDate.substring(0, 16) : "",
      endDate: voucher.endDate ? voucher.endDate.substring(0, 16) : ""
    };
  } else {
    console.log("➕ Add new voucher");
    form.value = { ...emptyForm };
  }

  isModalOpen.value = true;
};

const closeModal = () => {
  isModalOpen.value = false;
};

const saveVoucher = async () => {
  try {
    console.log("💾 Save voucher, mode =", modalMode.value, "form =", form.value);

    const payload = {
      ...form.value,
      startDate: form.value.startDate || null,
      endDate: form.value.endDate || null
      // description đã nằm trong form nên tự đi kèm payload
    };

    if (modalMode.value === "add") {
      await voucherStore.addVoucher(payload);
    } else {
      if (!payload.id) {
        alert("Không có ID voucher để cập nhật");
        return;
      }
      await voucherStore.updateVoucher(payload.id, payload);
    }

    await voucherStore.fetchVouchers();
    closeModal();
  } catch (err) {
    console.error("❌ Lỗi saveVoucher:", err);
    alert("Có lỗi khi lưu voucher (xem console để biết chi tiết).");
  }
};

const deleteVoucher = async (id) => {
  if (!confirm("Bạn chắc chắn muốn xóa voucher này?")) return;

  try {
    console.log("🗑️ Delete voucher id =", id);
    await voucherStore.deleteVoucher(id);
    await voucherStore.fetchVouchers();
  } catch (err) {
    console.error("❌ Lỗi deleteVoucher:", err);
    alert("Không thể xóa voucher (xem console để biết chi tiết).");
  }
};

const formatDate = (v) =>
    v ? new Date(v).toLocaleDateString("vi-VN") : "";

const formatPrice = (v) =>
    v ? v.toLocaleString("vi-VN") + "₫" : "0₫";
</script>
<style scoped>
/* ====== WRAPPER ====== */
.admin-products-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

/* ====== HEADER ====== */
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid #e0e0e0;
}

.header-section h1 {
  font-size: 1.8rem;
  font-weight: 700;
  color: #333;
}

.add-btn {
  padding: 10px 18px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 8px;
}

.add-btn:hover {
  transform: translateY(-2px);
  opacity: 0.92;
}

/* ====== TABLE ====== */
.product-table-wrapper {
  margin-top: 10px;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.product-table {
  width: 100%;
  border-collapse: collapse;
}

.product-table th {
  background: #f3f4f6;
  padding: 14px;
  font-weight: 600;
  text-align: left;
  color: #333;
  font-size: 0.9rem;
  border-bottom: 1px solid #e5e7eb;
}

.product-table td {
  padding: 12px;
  border-bottom: 1px solid #ececec;
  font-size: 0.9rem;
  color: #444;
}

.product-table tr:hover td {
  background: #f9fafb;
}

/* ====== ACTION BUTTONS ====== */
.actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 6px 12px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  font-size: 0.85rem;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: 0.2s ease;
}

.action-btn.edit {
  background: #4299e1;
  color: white;
}

.action-btn.edit:hover {
  background: #3182ce;
}

.action-btn.delete {
  background: #e53e3e;
  color: white;
}

.action-btn.delete:hover {
  background: #c53030;
}

/* ====== MODAL ====== */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.55);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.modal-content {
  background: white;
  width: 92%;
  max-width: 600px;
  padding: 25px;
  border-radius: 14px;
  position: relative;
  animation: popIn 0.25s ease-out;
}

@keyframes popIn {
  from { transform: scale(0.92); opacity: 0; }
  to   { transform: scale(1); opacity: 1; }
}

.close-btn {
  position: absolute;
  top: 12px;
  right: 15px;
  font-size: 28px;
  cursor: pointer;
  color: #777;
}

.close-btn:hover {
  color: #333;
}

/* ====== FORM ====== */
.product-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-weight: 600;
  margin-bottom: 6px;
  color: #333;
  font-size: 0.9rem;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 10px 12px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.95rem;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-group textarea {
  resize: vertical;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.25);
  outline: none;
}

/* ====== FORM BUTTONS ====== */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 10px;
}

.save-btn,
.cancel-btn {
  padding: 10px 22px;
  border-radius: 8px;
  font-size: 0.9rem;
  cursor: pointer;
  border: none;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: 0.2s ease;
}

.save-btn {
  background: #38a169;
  color: white;
}

.save-btn:hover:not(:disabled) {
  background: #2f855a;
}

.cancel-btn {
  background: #a0aec0;
  color: white;
}

.cancel-btn:hover {
  background: #718096;
}

.save-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* ====== RESPONSIVE ====== */
@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }

  .modal-content {
    width: 96%;
  }

  .actions {
    flex-direction: column;
  }
}
</style>