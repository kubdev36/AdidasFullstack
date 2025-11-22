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
          <div class="order-line">
            <span>Tạm tính:</span>
            <span>{{ formatPrice(cartStore.finalTotal) }}</span>
          </div>

          <div
              v-if="cartStore.discount > 0"
              class="order-line order-discount"
          >
            <span>Giảm giá</span>
            <span>-{{ formatPrice(cartStore.discount) }}</span>
          </div>

          <div class="order-line order-final">
            <strong>Thành tiền:</strong>
            <strong>{{ formatPrice(cartStore.totalPrice) }}</strong>
          </div>

          <div
              v-if="cartStore.voucherCode"
              class="order-voucher"
          >
            <small>Mã áp dụng: {{ cartStore.voucherCode }}</small>
          </div>
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
        :amount="cartStore.finalTotal"
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
import QRPayment from './QRPayment.vue';

const cartStore = useCartStore();
const router = useRouter();

const processing = ref(false);
const showQRPayment = ref(false);
const currentOrderId = ref(null);
const stars = ref(0);
const comment = ref('');

const currentUser = computed(() => {
  try {
    const userStr = localStorage.getItem('user') || localStorage.getItem('userLogin') || localStorage.getItem('currentUser');
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
  paymentMethod: 'cod',
});

const formatPrice = (price) => {
  if (typeof price !== 'number') return '0 đ';
  return price.toLocaleString('vi-VN') + ' đ';
};

const checkLoginStatus = () => {
  console.log('🔍 Checking login status...');
  console.log('👤 Current user:', currentUser.value);

  if (currentUser.value) {
    // Tự động điền thông tin người dùng vào form
    orderForm.value.fullName = currentUser.value.name || '';
    orderForm.value.email = currentUser.value.email || '';
  }
};

const processOrder = async () => {
  // Kiểm tra đăng nhập
  if (!currentUser.value) {
    alert('Vui lòng đăng nhập để thanh toán!');
    router.push('/auth');
    return;
  }

  processing.value = true;

  try {
    console.log('🛒 Processing order...');
    console.log('📦 Cart items:', cartStore.items);
    console.log('💰 Total price:', cartStore.totalPrice);

    // Tạo đối tượng đơn hàng
    const orderData = {
      ...orderForm.value,
      items: cartStore.items.map(item => ({
        productId: item.productId,
        productName: item.productName,
        price: item.price,
        quantity: item.quantity,
        colorName: item.colorName,
        sizeValue: item.sizeValue,
        image: item.image
      })),
      subtotal: cartStore.totalPrice,
      discount: cartStore.discount,
      voucherCode: cartStore.voucherCode || null,
      total: cartStore.finalTotal,
      date: new Date().toISOString(),
      status: 'pending',
      userId: currentUser.value.id,
      userEmail: currentUser.value.email,
      rating: stars.value,
      comment: comment.value
    };

    console.log('📋 Order data:', orderData);

    // Xử lý thanh toán QR nếu được chọn
    if (orderForm.value.paymentMethod === 'qr') {
      currentOrderId.value = 'ORD-' + Date.now();
      showQRPayment.value = true;
    } else {
      // Logic cho COD và các phương thức khác
      await handleOrderSuccess(orderData);
    }
  } catch (error) {
    console.error('❌ Lỗi xử lý đơn hàng:', error);
    alert('Đã xảy ra lỗi khi đặt hàng. Vui lòng thử lại.');
  } finally {
    processing.value = false;
  }
};

const handleOrderSuccess = async (orderData) => {
  try {
    // TODO: Gửi orderData đến backend API
    // const response = await axios.post('http://localhost:8082/api/orders', orderData);

    console.log('✅ Order created successfully:', orderData);

    // Hiển thị thông báo thành công
    alert(
        `Đặt hàng thành công! Tổng tiền: ${formatPrice(cartStore.finalTotal)}\n` +
        `Mã đơn hàng: ${currentOrderId.value || 'COD-' + Date.now()}`
    );

    // Xóa giỏ hàng
    await cartStore.clearCart();

    // Chuyển hướng về trang chủ
    router.push('/');
  } catch (error) {
    console.error('❌ Error saving order:', error);
    alert('Lỗi khi lưu đơn hàng. Vui lòng thử lại.');
  }
};

const handleQRClose = () => {
  showQRPayment.value = false;
  console.log('❌ QR payment closed');
};

const handlePaymentSuccess = (paymentData) => {
  console.log('✅ QR Payment successful:', paymentData);
  showQRPayment.value = false;

  // Tạo order data cho QR payment
  const orderData = {
    ...orderForm.value,
    items: cartStore.items,
    subtotal: cartStore.totalPrice,
    discount: cartStore.discount,
    voucherCode: cartStore.voucherCode || null,
    total: cartStore.finalTotal,       // ✅ sau giảm
    date: new Date().toISOString(),
    status: 'paid',
    userId: currentUser.value.id,
    paymentMethod: 'qr',
    paymentId: paymentData.paymentId,
    orderId: currentOrderId.value
  };


  handleOrderSuccess(orderData);
};

const handlePaymentFailed = (error) => {
  console.error('❌ QR Payment failed:', error);
  alert('Thanh toán QR thất bại. Vui lòng thử lại hoặc chọn phương thức khác.');
  showQRPayment.value = false;
};

onMounted(() => {
  console.log('🛒 Checkout page mounted');
  checkLoginStatus();

  // Kiểm tra nếu giỏ hàng trống
  if (cartStore.items.length === 0) {
    console.log('🛒 Cart is empty, fetching cart...');
    cartStore.fetchCart();
  }
});
</script>

<style scoped>
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

.order-total {
  padding-top: 15px;
  border-top: 2px solid #333;
  font-size: 1rem;
  color: #333;
}

.order-line {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.order-discount span:last-child {
  color: #b30404;
}

.order-final strong:last-child {
  color: #b30404;
  font-size: 1.1rem;
}

.order-voucher {
  margin-top: 4px;
  color: #555;
  font-size: 0.85rem;
}

</style>