<template>
  <div class="checkout-page">
    <h2>Thanh toán</h2>

    <div v-if="cartStore.items.length === 0" class="empty-cart">
      <p>Giỏ hàng trống, không thể thanh toán</p>
      <router-link to="/product" class="btn-primary">Tiếp tục mua sắm</router-link>
    </div>

    <div v-else class="checkout-container">
      <div class="order-summary">
        <h3>Đơn hàng của bạn</h3>
        <div class="order-items">
          <div v-for="item in cartStore.items" :key="item.id + '-' + item.sizeValue + '-' + item.colorName" class="order-item">
            <div class="item-info">
              <span class="item-name">{{ item.productName }}</span>
              <div class="item-details">
                <span v-if="item.colorName">Màu: {{ item.colorName }}</span>
                <span v-if="item.sizeValue">Size: {{ item.sizeValue }}</span>
                <span class="item-quantity">x {{ item.quantity }}</span>
              </div>
            </div>
            <span class="item-price">{{ formatPrice(item.price * item.quantity) }}</span>
          </div>
        </div>
        <div class="order-total">
          <strong>Tổng cộng: {{ formatPrice(cartStore.totalPrice) }}</strong>
        </div>
      </div>

      <form @submit.prevent="processOrder" class="checkout-form">
        <h3>Thông tin giao hàng</h3>

        <div class="form-group">
          <label>Họ và tên:</label>
          <input v-model="orderForm.fullName" type="text" required>
        </div>

        <div class="form-group">
          <label>Email:</label>
          <input v-model="orderForm.email" type="email" required>
        </div>

        <div class="form-group">
          <label>Số điện thoại:</label>
          <input v-model="orderForm.phone" type="tel" required>
        </div>

        <div class="form-group">
          <label>Địa chỉ:</label>
          <textarea v-model="orderForm.address" required></textarea>
        </div>

        <div class="form-group">
          <label>Ghi chú:</label>
          <textarea v-model="orderForm.notes" placeholder="Ghi chú thêm (không bắt buộc)"></textarea>
        </div>

        <div class="review">
          <h3>Đánh giá sản phẩm</h3>
          <div class="stars">
            <span
              v-for="n in 5"
              :key="n"
              class="star"
              :class="{ active: n <= stars }"
              @click="stars = n"
            >★</span>
          </div>
          <textarea v-model="comment" placeholder="Nhập bình luận..." rows="3" />
        </div>

        <div class="payment-methods">
          <h4>Phương thức thanh toán:</h4>
          <div class="payment-option">
            <input v-model="orderForm.paymentMethod" type="radio" id="cod" value="cod">
            <label for="cod">Thanh toán khi nhận hàng (COD)</label>
          </div>
          <div class="payment-option">
            <input v-model="orderForm.paymentMethod" type="radio" id="qr" value="qr">
            <label for="qr">Thanh toán QR Code</label>
          </div>
          <div class="payment-option">
            <input v-model="orderForm.paymentMethod" type="radio" id="card" value="card">
            <label for="card">Thẻ tín dụng/Ghi nợ</label>
          </div>
        </div>

        <div class="form-actions">
          <router-link to="/cart" class="btn-back">Quay lại giỏ hàng</router-link>
          <button type="submit" class="btn-order" :disabled="processing">
            {{ processing ? 'Đang xử lý...' : 'Đặt hàng' }}
          </button>
        </div>
      </form>
    </div>

    <QRPayment
      v-if="showQRPayment"
      :show="showQRPayment"
      :amount="cartStore.totalPrice"
      :order-id="currentOrderId" 
      @close="handleQRClose"
      @payment-success="handlePaymentSuccess"
      @payment-failed="handlePaymentFailed"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useCartStore } from '@/stores/cartStore';
import { useRouter } from 'vue-router';
import axios from 'axios'; 
import QRPayment from './QRPayment.vue'; 

const cartStore = useCartStore();
const router = useRouter();

const processing = ref(false);
const showQRPayment = ref(false);
const currentOrderId = ref(''); // ID đơn hàng thật từ Database
const stars = ref(0); 
const comment = ref('');

// Lấy thông tin user
const currentUser = computed(() => {
  try {
    const userStr = localStorage.getItem('userLogin') || localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  } catch (error) {
    console.error('Error parsing user:', error);
    return null;
  }
});

const orderForm = ref({
  fullName: '',
  email: '',
  phone: '',
  address: '',
  notes: '',
  paymentMethod: 'cod', // Mặc định là cod
});

const formatPrice = (price) => {
  if (typeof price !== 'number') return '0 đ';
  return price.toLocaleString('vi-VN') + ' đ';
};

const checkLoginStatus = () => {
  if (currentUser.value) {
    orderForm.value.fullName = currentUser.value.name || '';
    orderForm.value.email = currentUser.value.email || '';
    orderForm.value.phone = currentUser.value.phone || ''; 
    orderForm.value.address = currentUser.value.address || '';
  }
};

// ------------------------------------------------------------------
// 1. HÀM TẠO DỮ LIỆU CHUẨN
// ------------------------------------------------------------------
const getCommonOrderData = () => {
  return {
    fullName: orderForm.value.fullName,
    email: orderForm.value.email,
    phone: orderForm.value.phone,
    address: orderForm.value.address,
    notes: orderForm.value.notes,
    
    userId: currentUser.value ? (currentUser.value.id || currentUser.value.userId) : null,
    
    // Status mặc định luôn là PENDING khi mới tạo
    status: 'PENDING', 
    total: cartStore.totalPrice,
    
    items: cartStore.items.map(item => ({
      productId: String(item.productId || item.id),
      quantity: item.quantity,
      price: item.price,
      colorName: item.colorName || "", 
      sizeValue: item.sizeValue || ""
    })),

    rating: stars.value,
    comment: comment.value
  };
};

// ------------------------------------------------------------------
// 2. LOGIC XỬ LÝ ĐẶT HÀNG (QUAN TRỌNG ĐÃ SỬA ĐỔI)
// ------------------------------------------------------------------
const processOrder = async () => {
  // Validate đăng nhập
  if (!currentUser.value) {
    alert('Vui lòng đăng nhập để thanh toán!');
    router.push('/login'); 
    return;
  }

  // Validate giỏ hàng
  if (cartStore.items.length === 0) {
      alert('Giỏ hàng đang trống!');
      return;
  }

  processing.value = true;

  try {
    // A. Chuẩn bị dữ liệu
    const orderData = getCommonOrderData();
    // Gán phương thức thanh toán chuẩn
    orderData.paymentMethod = orderForm.value.paymentMethod === 'qr' ? 'QR_CODE' : 'COD';

    // B. Gửi API tạo đơn hàng (Dù là QR hay COD đều phải lưu trước)
    const token = localStorage.getItem('authToken');
    const config = {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    };

    console.log('📤 Đang tạo đơn hàng...', orderData);
    // Gọi Backend để lưu đơn hàng trạng thái PENDING
    const response = await axios.post('http://localhost:8082/api/orders', orderData, config);

    if (response.status === 200 || response.status === 201) {
        const savedOrder = response.data; // Đơn hàng đã được lưu trong DB
        console.log('✅ Đơn hàng đã tạo:', savedOrder);

        // C. Phân chia luồng xử lý
        if (orderForm.value.paymentMethod === 'qr') {
            // == TRƯỜNG HỢP QR ==
            // Lấy ID thật từ DB gán vào currentOrderId để popup QR sử dụng check status
            currentOrderId.value = String(savedOrder.id); 
            
            // Mở popup QR -> Popup sẽ tự lo việc hiển thị mã và polling check status
            showQRPayment.value = true;
            
            // Lưu ý: Không clearCart hay redirect ở đây, đợi user thanh toán xong
        } else {
            // == TRƯỜNG HỢP COD ==
            // Thông báo và kết thúc luôn
            alert(`🎉 Đặt hàng thành công!\nMã đơn: ${savedOrder.id}\nTổng tiền: ${formatPrice(cartStore.totalPrice)}`);
            await cartStore.clearCart();
            router.push('/');
        }
    }

  } catch (error) {
    console.error('❌ Lỗi tạo đơn hàng:', error);
    if (error.response && error.response.status === 401) {
        alert('Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.');
        router.push('/login');
    } else {
        alert('Có lỗi xảy ra khi tạo đơn hàng. Vui lòng thử lại.');
    }
  } finally {
    // Nếu là COD thì tắt loading, nếu là QR thì giữ loading background hoặc tắt tùy ý
    if (orderForm.value.paymentMethod !== 'qr') {
        processing.value = false;
    }
  }
};

// ------------------------------------------------------------------
// 3. CALLBACK KHI QR BÁO THANH TOÁN THÀNH CÔNG
// ------------------------------------------------------------------
const handlePaymentSuccess = async () => {
  // Khi hàm này được gọi, có nghĩa là Polling bên QRPayment đã thấy status = PAID
  console.log('✅ QR Payment confirmed by Backend!');
  
  showQRPayment.value = false;
  processing.value = false;
  
  // Thông báo người dùng
  alert(`🎉 Thanh toán thành công! Đơn hàng #${currentOrderId.value} đã được xác nhận.`);
  
  // Xóa giỏ hàng và về trang chủ
  await cartStore.clearCart();
  router.push('/');
};

// User tắt popup mà chưa thanh toán xong
const handleQRClose = () => {
  showQRPayment.value = false;
  processing.value = false;
  // Có thể nhắc nhở: "Đơn hàng của bạn đã được tạo nhưng chưa thanh toán. Vui lòng kiểm tra lại."
};

const handlePaymentFailed = () => {
  alert('Thanh toán thất bại. Vui lòng thử lại.');
  showQRPayment.value = false;
  processing.value = false;
};

onMounted(() => {
  checkLoginStatus();
  if (cartStore.items.length === 0) {
    cartStore.fetchCart();
  }
});
</script>

<style scoped>
/* (Giữ nguyên toàn bộ CSS của bạn, không cần thay đổi) */
.checkout-page {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}
/* ... các style khác giữ nguyên ... */
.checkout-form {
  background: white;
  padding: 20px;
  border-radius: 10px;
  border: 1px solid #ddd;
}
.form-actions {
  display: flex;
  gap: 15px;
  justify-content: space-between;
}
.btn-order {
  background-color: #007bff;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 5px;
  cursor: pointer;
}
.btn-order:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}
.checkout-page {

  padding: 20px;

  max-width: 1000px;

  margin: 0 auto;

}



.checkout-page h2 {

  margin-bottom: 30px;

  color: #333;

}



.empty-cart {

  text-align: center;

  padding: 50px;

  color: #666;

}



.checkout-container {

  display: grid;

  grid-template-columns: 1fr 2fr;

  gap: 40px;

}



.order-summary {

  background: #f8f9fa;

  padding: 20px;

  border-radius: 10px;

  height: fit-content;

}



.order-summary h3 {

  margin-bottom: 20px;

  color: #333;

}



.order-items {

  margin-bottom: 20px;

}



.order-item {

  display: flex;

  justify-content: space-between;

  align-items: flex-start;

  padding: 10px 0;

  border-bottom: 1px solid #ddd;

}



.item-info {

  flex: 1;

}



.item-name {

  display: block;

  font-weight: 500;

  margin-bottom: 4px;

}



.item-details {

  font-size: 0.9em;

  color: #666;

}



.item-details span {

  margin-right: 10px;

}



.item-quantity {

  font-weight: bold;

}



.item-price {

  font-weight: bold;

  color: #b30404;

}



.order-total {

  padding-top: 15px;

  border-top: 2px solid #333;

  font-size: 1.2rem;

  color: #333;

}



.checkout-form {

  background: white;

  padding: 20px;

  border-radius: 10px;

  border: 1px solid #ddd;

}



.checkout-form h3 {

  margin-bottom: 20px;

  color: #333;

}



.form-group {

  margin-bottom: 20px;

}



.form-group label {

  display: block;

  margin-bottom: 5px;

  font-weight: 500;

  color: #333;

}



.form-group input,

.form-group textarea {

  width: 100%;

  padding: 10px;

  border: 1px solid #ddd;

  border-radius: 5px;

  font-size: 16px;

  box-sizing: border-box;

}



.form-group textarea {

  resize: vertical;

  height: 80px;

}



.payment-methods {

  margin-bottom: 30px;

}



.payment-methods h4 {

  margin-bottom: 15px;

  color: #333;

}



.payment-option {

  display: flex;

  align-items: center;

  margin-bottom: 10px;

}



.payment-option input {

  margin-right: 10px;

  width: auto;

}



.form-actions {

  display: flex;

  gap: 15px;

  justify-content: space-between;

}



.btn-primary,

.btn-order {

  background-color: #007bff;

  color: white;

  border: none;

  padding: 12px 24px;

  border-radius: 5px;

  cursor: pointer;

  text-decoration: none;

  transition: background-color 0.3s;

  display: inline-block;

  text-align: center;

}



.btn-primary:hover,

.btn-order:hover:not(:disabled) {

  background-color: #0056b3;

}



.btn-order:disabled {

  background-color: #6c757d;

  cursor: not-allowed;

}



.btn-back {

  background-color: #6c757d;

  color: white;

  border: none;

  padding: 12px 24px;

  border-radius: 5px;

  cursor: pointer;

  text-decoration: none;

  transition: background-color 0.3s;

  text-align: center;

}



.btn-back:hover {

  background-color: #545b62;

}



.review {

  margin: 20px 0;

  padding: 20px;

  border: 1px solid #ddd;

  border-radius: 5px;

  background: #f8f9fa;

}



.review h3 {

  margin-bottom: 15px;

  color: #333;

}



.stars {

  margin-bottom: 10px;

}



.stars .star {

  font-size: 24px;

  cursor: pointer;

  color: #ccc;

  margin-right: 5px;

  transition: color 0.2s;

}



.stars .star:hover {

  color: #ffd700;

}



.stars .star.active {

  color: #ffd700;

}



.review textarea {

  width: 100%;

  margin-top: 10px;

  resize: vertical;

  border: 1px solid #ddd;

  border-radius: 5px;

  padding: 10px;

  font-size: 14px;

}



@media (max-width: 768px) {

  .checkout-container {

    grid-template-columns: 1fr;

  }



  .form-actions {

    flex-direction: column;

  }



  .btn-primary,

  .btn-order,

  .btn-back {

    width: 100%;

  }

}
</style>