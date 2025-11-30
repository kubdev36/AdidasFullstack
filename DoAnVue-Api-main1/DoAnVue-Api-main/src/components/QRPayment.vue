<template>
  <div class="qr-payment" v-if="show">
    <div class="qr-overlay" @click="closeQR">
      <div class="qr-modal" @click.stop>
        <div class="qr-header">
          <h3>Thanh toán QR Code</h3>
          <button @click="closeQR" class="close-btn">&times;</button>
        </div>
        
        <div class="qr-body">
          <div class="payment-info">
            <p><strong>Tổng tiền:</strong> {{ formatPrice(amount) }}</p>
            <p><strong>Mã đơn hàng:</strong> #{{ orderId }}</p>
            <div v-if="currentUser" class="user-info">
              <p><strong>Người thanh toán:</strong> {{ currentUser.name }}</p>
            </div>
          </div>
          
          <div class="qr-container">
            <div v-if="showQRCode && paymentStatus === 'waiting'" class="qr-display">
              <p>Quét mã QR bên dưới để thanh toán</p>
              
              <div class="countdown-timer">
                 <span :class="{ 'text-red': timeLeft < 60 }">
                    Thời gian còn lại: {{ formattedTime }}
                 </span>
              </div>

              <img :src="qrCodeDataUrl" alt="Payment QR Code" class="qr-image" />
              
              <div class="bank-info">
                <p><strong>Ngân hàng:</strong> BIDV</p>
                <p><strong>Số tài khoản:</strong> 8857120992</p>
                <p><strong>Chủ tài khoản:</strong> DANG TRUNG HAI</p>
                <p><strong>Nội dung CK:</strong> <span style="color:red; font-weight:bold">{{ qrContent }}</span></p>
              </div>

              <div class="auto-check-status">
                  <div class="spinner-small"></div>
                  <div class="status-text">
                      <p><strong>Đang chờ xác nhận thanh toán...</strong></p>
                      <p class="sub-text">Hệ thống sẽ tự động xử lý khi nhận được tiền.</p>
                      <p class="sub-text">(Vui lòng không tắt màn hình này)</p>
                  </div>
              </div>

              <button @click="closeQR" class="btn-back">Hủy bỏ</button>
            </div>
            
            <div v-else-if="paymentStatus === 'success'" class="status-success">
               <div class="success-icon">🎉</div>
               <h3>Thanh toán thành công!</h3>
               <p>Hệ thống đã nhận được tiền.</p>
               <p>Đang chuyển hướng...</p>
            </div>
            
             <div v-else class="qr-placeholder">
               <div class="spinner"></div>
               <p>Đang tạo mã QR...</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onUnmounted, onMounted, computed, watch } from 'vue'
import axios from 'axios' // 🔥 Bắt buộc import axios

// eslint-disable-next-line no-undef, no-unused-vars
const props = defineProps({
  show: { type: Boolean, default: false },
  amount: { type: Number, required: true },
  orderId: { type: String, required: true }
})

// eslint-disable-next-line no-undef
const emit = defineEmits(['close', 'payment-success'])

// State variables
const paymentStatus = ref('waiting') // waiting, success
const currentUser = ref(null)
const showQRCode = ref(false)
const qrCodeDataUrl = ref('')
const qrContent = ref('')

// Variables cho Polling và Timer
const timeLeft = ref(300) // 5 phút
let timerInterval = null
let pollingInterval = null // Biến để lưu vòng lặp kiểm tra API

// Formatter
const formattedTime = computed(() => {
  const minutes = Math.floor(timeLeft.value / 60)
  const seconds = timeLeft.value % 60
  return `${minutes}:${seconds < 10 ? '0' : ''}${seconds}`
})

const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price)
}

// Check login
onMounted(() => {
  const user = localStorage.getItem('userLogin') 
  if (user) {
    currentUser.value = JSON.parse(user)
  }
})

// Dọn dẹp khi tắt component
onUnmounted(() => {
  stopAllIntervals()
})

const stopAllIntervals = () => {
    if (timerInterval) clearInterval(timerInterval)
    if (pollingInterval) clearInterval(pollingInterval)
}

// --- 1. HÀM TẠO QR ---
// Trong file QRPayment.vue

const generateQRCode = () => {
    // Reset
    paymentStatus.value = 'waiting'
    timeLeft.value = 300
    
    const description = `DH ${props.orderId}`
    qrContent.value = description 

    // 🔥 SỬA LẠI THÔNG TIN NGÂN HÀNG CHO KHỚP VỚI SEPAY (BIDV)
    const bankCode = '970418' // Mã BIDV (theo VietQR)
    const accountNumber = '962470867447890' // Số tài khoản BIDV của bạn (như trong ảnh)
    
    // Link tạo QR
    const qrUrl = `https://img.vietqr.io/image/${bankCode}-${accountNumber}-compact2.png` +
                  `?amount=${props.amount}&addInfo=${encodeURIComponent(description)}`

    qrCodeDataUrl.value = qrUrl
    showQRCode.value = true

    startTimer()
    startPolling()
}

// --- 2. HÀM ĐẾM NGƯỢC ---
const startTimer = () => {
    if (timerInterval) clearInterval(timerInterval)
    timerInterval = setInterval(() => {
        if (timeLeft.value > 0) {
            timeLeft.value--
        } else {
            stopAllIntervals()
            alert('Hết thời gian thanh toán. Vui lòng thử lại.')
            emit('close')
        }
    }, 1000)
}

// --- 3. HÀM CHECK TRẠNG THÁI TỰ ĐỘNG (QUAN TRỌNG NHẤT) ---
const startPolling = () => {
    if (pollingInterval) clearInterval(pollingInterval)
    
    // Cứ 2 giây gọi API 1 lần
    pollingInterval = setInterval(async () => {
        try {
            console.log("Checking payment status for:", props.orderId)
            
            // Gọi API Backend của bạn
            const response = await axios.get(`http://localhost:8082/api/payment/check-status/${props.orderId}`)
            
            const status = response.data // Backend trả về "PENDING" hoặc "PAID"
            
            if (status === 'PAID') {
                // Đã thanh toán thành công!
                handleSuccess()
            }
        } catch (error) {
            // Lỗi mạng hoặc 404 (chưa có đơn) thì cứ lờ đi và check tiếp
            console.warn("Chưa thấy thanh toán hoặc lỗi mạng:", error.message)
        }
    }, 2000) // 2000ms = 2 giây
}

const handleSuccess = () => {
    stopAllIntervals() // Dừng hỏi API
    paymentStatus.value = 'success' // Chuyển giao diện sang thành công
    
    // Đợi 2 giây cho người dùng xem thông báo rồi tắt
    setTimeout(() => {
        emit('payment-success')
    }, 2000)
}

const closeQR = () => {
  stopAllIntervals()
  emit('close')
}

// Watch: Khi popup mở lên thì tự chạy logic
watch(() => props.show, (newVal) => {
  if (newVal) {
     generateQRCode()
  } else {
     stopAllIntervals()
  }
}, { immediate: true })

</script>

<style scoped>
/* CSS cho phần Auto Check */
.auto-check-status {
    margin-top: 15px;
    margin-bottom: 15px;
    padding: 15px;
    background-color: #f0f8ff;
    border: 1px solid #bce0fd;
    border-radius: 8px;
    display: flex;
    align-items: center;
    gap: 15px;
    text-align: left;
}

.spinner-small {
    width: 25px;
    height: 25px;
    border: 3px solid #ccc;
    border-top: 3px solid #007bff;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    flex-shrink: 0;
}

.status-text p {
    margin: 0;
    color: #007bff;
}

.status-text .sub-text {
    font-size: 12px;
    color: #666;
    margin-top: 2px;
}

/* Các CSS cũ */
.countdown-timer {
  font-size: 16px;
  font-weight: bold;
  color: #dc3545;
  margin-bottom: 10px;
  background: #fff5f5;
  padding: 5px 10px;
  border-radius: 15px;
  display: inline-block;
}

.qr-payment {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0; z-index: 9999;
}
.qr-overlay {
  position: absolute; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.7); display: flex; align-items: center; justify-content: center; padding: 20px;
}
.qr-modal {
  background: white; border-radius: 15px; max-width: 90vw; width: 100%; max-height: 90vh;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3); overflow-y: auto; display: flex; flex-direction: column;
}
@media (min-width: 576px) { .qr-modal { max-width: 400px; } }

.qr-header {
  padding: 20px; background: #007bff; color: white; display: flex; justify-content: space-between; align-items: center;
}
.close-btn { background: none; border: none; color: white; font-size: 24px; cursor: pointer; }
.qr-body { padding: 20px; text-align: center; flex: 1; }
.payment-info { margin-bottom: 20px; padding: 15px; background: #f8f9fa; border-radius: 8px; text-align: left; }
.qr-display { display: flex; flex-direction: column; align-items: center; gap: 15px; }
.qr-image { max-width: 250px; width: 100%; height: auto; border: 1px solid #ddd; border-radius: 8px; padding: 10px; }
.bank-info { background: #f8f9fa; border-radius: 8px; padding: 15px; width: 100%; text-align: left; }
.btn-back { padding: 10px 20px; border: none; border-radius: 5px; cursor: pointer; font-weight: 500; background: #6c757d; color: white; width: 100%; }
.status-success { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 30px; gap: 15px; }
.success-icon { font-size: 50px; }
.spinner { width: 40px; height: 40px; border: 4px solid #f3f3f3; border-top: 4px solid #007bff; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
</style>
Đang hiển thị 5586663866879172676.