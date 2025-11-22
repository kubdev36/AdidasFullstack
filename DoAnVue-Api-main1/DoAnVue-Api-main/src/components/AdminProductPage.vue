<template>
  <AppHeader/>
  <div class="admin-products-container">
    <div class="header-section">
      <h1>Quản Lý Sản Phẩm</h1>
      <button @click="openModal('add')" class="add-btn">
        <i class="fas fa-plus"></i> Thêm Sản Phẩm Mới
      </button>

      <button @click="goToVoucher" class="add-btn">
        <i class="fas fa-ticket-alt"></i> Quản Lý Voucher
      </button>
    </div>

    <div v-if="loading" class="loading">
      <i class="fas fa-spinner fa-spin"></i> Đang tải dữ liệu...
    </div>

    <div v-else class="product-table-wrapper">
      <table class="product-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Tên Sản Phẩm</th>
            <th>Ảnh</th>
            <th>Giá</th>
            <th>Danh Mục</th>
            <th>Nổi Bật</th>
            <th>Hành Động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="product in products" :key="product.id">
            <td>{{ product.id }}</td>
            <td class="product-name">{{ product.name }}</td>
            <td>
              <img
                :src="getProductImage(product)"
                :alt="product.name"
                class="product-image"
              />
            </td>
            <td>${{ product.price }}</td>
            <td>
              <span class="category-badge">{{ product.category || 'Chưa phân loại' }}</span>
            </td>
            <td>
              <span :class="['featured-badge', { 'featured': product.featured }]">
                {{ product.featured ? '✓' : '✗' }}
              </span>
            </td>
            <td class="actions">
              <button @click="openModal('edit', product)" class="action-btn edit">
                <i class="fas fa-edit"></i> Sửa
              </button>
              <button @click="viewProductDetails(product.id)" class="action-btn view">
                <i class="fas fa-eye"></i> Chi tiết
              </button>
              <button @click="deleteProduct(product.id)" class="action-btn delete">
                <i class="fas fa-trash-alt"></i> Xóa
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="!loading && products.length === 0" class="empty-state">
      <i class="fas fa-box-open"></i>
      <p>Chưa có sản phẩm nào</p>
    </div>

    <div v-if="isModalOpen" class="modal-overlay">
      <div class="modal-content">
        <span @click="closeModal" class="close-btn">&times;</span>
        <h2>{{ modalMode === 'add' ? 'Thêm Sản Phẩm Mới' : 'Sửa Thông Tin Sản Phẩm' }}</h2>
        
        <form @submit.prevent="saveProduct" class="product-form">
          <div class="form-row">
            <div class="form-group">
              <label for="id">ID Sản Phẩm:</label>
              <input 
                type="text" 
                id="id" 
                v-model="productForm.id" 
                :disabled="modalMode === 'edit'"
                required 
              />
              <small v-if="modalMode === 'add'">Để trống để tự động generate ID</small>
            </div>
            
            <div class="form-group">
              <label for="name">Tên Sản Phẩm:*</label>
              <input type="text" id="name" v-model="productForm.name" required />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="price">Giá ($):*</label>
              <input type="number" id="price" v-model.number="productForm.price" step="0.01" required />
            </div>
            
            <div class="form-group">
              <label for="category">Danh Mục:</label>
              <select id="category" v-model="productForm.category">
                <option value="">Chọn danh mục</option>
                <option value="Giày thể thao">Giày thể thao</option>
                <option value="Giày thời trang">Giày thời trang</option>
              </select>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="image">Ảnh Chính:*</label>
              <input type="text" id="image" v-model="productForm.image" required />
            </div>
            
            <div class="form-group">
              <label for="hoverImage">Ảnh Hover:</label>
              <input type="text" id="hoverImage" v-model="productForm.hoverImage" />
            </div>
          </div>

          <div class="form-group full-width">
            <label for="description">Mô Tả:</label>
            <textarea 
              id="description" 
              v-model="productForm.description" 
              rows="3"
              placeholder="Mô tả sản phẩm..."
            ></textarea>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="rating">Đánh Giá:</label>
              <input type="text" id="rating" v-model="productForm.rating" placeholder="6 out of 6 stars" />
            </div>
            
            <div class="form-group">
              <label class="checkbox-label">
                <input type="checkbox" v-model="productForm.featured" />
                <span class="checkmark"></span>
                Sản phẩm nổi bật
              </label>
            </div>
          </div>

          <div class="form-group full-width">
            <label for="link">Link Chi Tiết:</label>
            <input type="text" id="link" v-model="productForm.link" placeholder="samba.html" />
          </div>

          <div class="form-actions">
            <button type="button" @click="closeModal" class="cancel-btn">
              <i class="fas fa-times"></i> Hủy
            </button>
            <button type="submit" class="save-btn" :disabled="saving">
              <i class="fas fa-save"></i> 
              {{ saving ? 'Đang lưu...' : 'Lưu Sản Phẩm' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useProductStore } from "@/stores/productStore";
import AppHeader from "./AppHeader.vue";
import { useRouter } from "vue-router";

const productStore = useProductStore();
const isModalOpen = ref(false);
const modalMode = ref("add");
const loading = ref(false);
const saving = ref(false);

const productForm = ref({
  id: "",
  name: "",
  price: 0,
  category: "",
  image: "",
  hoverImage: "",
  description: "",
  rating: "",
  featured: false,
  link: "",
  promoCodes: "",
  paymentOptions: "",
  shipping: "",
  returnsExchanges: "",
  sizingNote: ""
});

onMounted(async () => {
  await loadProducts();
});

const products = computed(() => productStore.products || []);

const loadProducts = async () => {
  loading.value = true;
  try {
    await productStore.fetchProducts();
  } catch (error) {
    console.error('Lỗi khi tải sản phẩm:', error);
    alert('Lỗi khi tải danh sách sản phẩm');
  } finally {
    loading.value = false;
  }
};

const getProductImage = (product) => {
  return product.image || (product.images && product.images[0]?.imageUrl) || '/placeholder-image.jpg';
};

const openModal = async (mode, product = null) => {
  modalMode.value = mode;
  
  if (mode === "edit" && product) {
    try {
      const fullProduct = await productStore.fetchProductById(product.id);
      productForm.value = { 
        ...fullProduct,
        category: fullProduct.category || "",
        image: fullProduct.image || "",
        hoverImage: fullProduct.hoverImage || "",
        description: fullProduct.description || "",
        rating: fullProduct.rating || "6 out of 6 stars",
        featured: Boolean(fullProduct.featured),
        link: fullProduct.link || "",
        promoCodes: fullProduct.promoCodes || "Use code DEAL for an extra 30% off. Exclusions apply.",
        paymentOptions: fullProduct.paymentOptions || "4 interest-free payments available with Klarna. Learn More",
        shipping: fullProduct.shipping || "Free shipping with Adiclun",
        returnsExchanges: fullProduct.returnsExchanges || "30 days returns and exchanges",
        sizingNote: fullProduct.sizingNote || "True to size. We recommend ordering your usual size."
      };
    } catch (error) {
      console.error('Lỗi khi tải chi tiết sản phẩm:', error);
      productForm.value = { 
        ...product,
        featured: Boolean(product.featured)
      };
    }
  } else {
    productForm.value = {
      id: "",
      name: "",
      price: 0,
      category: "",
      image: "",
      hoverImage: "",
      description: "",
      rating: "6 out of 6 stars",
      featured: false,
      link: "",
      promoCodes: "Use code DEAL for an extra 30% off. Exclusions apply.",
      paymentOptions: "4 interest-free payments available with Klarna. Learn More",
      shipping: "Free shipping with Adiclun",
      returnsExchanges: "30 days returns and exchanges",
      sizingNote: "True to size. We recommend ordering your usual size."
    };
  }
  
  isModalOpen.value = true;
};

const closeModal = () => {
  isModalOpen.value = false;
};

const saveProduct = async () => {
  saving.value = true;
  try {
    const productData = {
      ...productForm.value,
      featured: Boolean(productForm.value.featured),
      price: Number(productForm.value.price)
    };

    if (modalMode.value === "add" && !productData.id) {
      delete productData.id;
    }

    console.log('Sending product data:', productData);

    if (modalMode.value === "add") {
      await productStore.addProduct(productData);
    } else {
      await productStore.updateProduct(productData);
    }
    
    closeModal();
    await loadProducts();
  } catch (error) {
    console.error('Lỗi khi lưu sản phẩm:', error);
    
    let errorMessage = 'Lỗi khi lưu sản phẩm';
    if (error.response) {
      errorMessage += `: ${error.response.data?.message || error.response.statusText}`;
    } else if (error.request) {
      errorMessage += ': Không thể kết nối đến server';
    } else {
      errorMessage += `: ${error.message}`;
    }
    
    alert(errorMessage);
  } finally {
    saving.value = false;
  }
};

const deleteProduct = async (id) => {
  if (confirm("Bạn có chắc chắn muốn xóa sản phẩm này?")) {
    try {
      await productStore.deleteProduct(id);
      await loadProducts();
    } catch (error) {
      console.error('Lỗi khi xóa sản phẩm:', error);
      
      let errorMessage = 'Lỗi khi xóa sản phẩm';
      if (error.response) {
        errorMessage += `: ${error.response.data?.message || error.response.statusText}`;
      } else if (error.request) {
        errorMessage += ': Không thể kết nối đến server';
      } else {
        errorMessage += `: ${error.message}`;
      }
      
      alert(errorMessage);
    }
  }
};
const router = useRouter();

const goToVoucher = () => {
  router.push('/admin/vouchers');
};

const viewProductDetails = (id) => {
  window.open(`/product/${id}`, '_blank');
};
</script>

<style scoped>
.admin-products-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
  min-height: 100vh;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #e0e0e0;
}

.header-section h1 {
  color: #333;
  margin: 0;
  font-size: 2rem;
}

.add-btn {
  padding: 12px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: bold;
  font-size: 1rem;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.loading {
  text-align: center;
  padding: 60px 20px;
  font-size: 1.2rem;
  color: #666;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #666;
}

.empty-state i {
  font-size: 3rem;
  margin-bottom: 15px;
  opacity: 0.5;
}

.product-table-wrapper {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.product-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.95rem;
}

.product-table th {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 16px 12px;
  text-align: left;
  font-weight: 600;
  color: #2d3748;
  border-bottom: 2px solid #e2e8f0;
}

.product-table td {
  padding: 12px;
  border-bottom: 1px solid #e2e8f0;
  vertical-align: middle;
}

.product-table tbody tr:hover {
  background-color: #f8fafc;
}

.product-name {
  font-weight: 500;
  color: #2d3748;
  max-width: 200px;
}

.product-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
  border: 2px solid #e2e8f0;
}

.category-badge {
  background: #e2e8f0;
  color: #4a5568;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
}

.featured-badge {
  display: inline-block;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  text-align: center;
  line-height: 24px;
  font-size: 0.8rem;
  font-weight: bold;
}

.featured-badge.featured {
  background: #48bb78;
  color: white;
}

.featured-badge:not(.featured) {
  background: #e53e3e;
  color: white;
}

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85rem;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s ease;
}

.action-btn.edit {
  background: #4299e1;
  color: white;
}

.action-btn.view {
  background: #38b2ac;
  color: white;
}

.action-btn.delete {
  background: #f56565;
  color: white;
}

.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 16px;
  width: 90%;
  max-width: 700px;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.close-btn {
  position: absolute;
  top: 15px;
  right: 20px;
  font-size: 28px;
  cursor: pointer;
  color: #666;
  transition: color 0.2s;
}

.close-btn:hover {
  color: #333;
}

.modal-content h2 {
  margin-bottom: 25px;
  color: #2d3748;
  font-size: 1.5rem;
}

.product-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-group label {
  font-weight: 600;
  margin-bottom: 6px;
  color: #4a5568;
  font-size: 0.9rem;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 10px 12px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.95rem;
  transition: border-color 0.2s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #4299e1;
  box-shadow: 0 0 0 3px rgba(66, 153, 225, 0.1);
}

.form-group small {
  margin-top: 4px;
  color: #718096;
  font-size: 0.8rem;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  margin-top: 8px;
}

.checkbox-label input[type="checkbox"] {
  margin: 0;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
}

.save-btn,
.cancel-btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  font-size: 0.95rem;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s ease;
}

.save-btn {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  color: white;
}

.save-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.cancel-btn {
  background: #a0aec0;
  color: white;
}

.save-btn:hover:not(:disabled),
.cancel-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

@media (max-width: 768px) {
  .header-section {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .product-table-wrapper {
    overflow-x: auto;
  }
  
  .actions {
    flex-direction: column;
  }
  
  .modal-content {
    padding: 20px;
    width: 95%;
  }
}
</style>