<template>
  <header class="hero">
    <nav class="nav-bar">
      <!-- LOGO -->
      <div class="logo-container">
        <router-link to="/">
          <img
              src="https://hienlaptop.com/wp-content/uploads/2024/12/logo-adidas-vector-4.jpg"
              alt="Adidas Logo"
              class="adidas-logo-img"
          />
        </router-link>
      </div>

      <!-- NAV MENU -->
      <div class="nav-menu-wrapper">
        <div class="hamburger-menu" @click="toggleMenu">
          <span class="bar"></span>
          <span class="bar"></span>
          <span class="bar"></span>
        </div>

        <nav :class="['nav-links', { active: isMenuActive }]">
          <ul>
            <li><router-link to="/">HOME PAGE</router-link></li>

            <!-- PRODUCT + MEGA MENU -->
            <li
                class="nav-item has-mega"
                @mouseenter="openProductMega"
                @mouseleave="closeProductMega"
            >
              <router-link to="/product">PRODUCT</router-link>

              <div
                  v-if="showProductMega && megaMenuData"
                  class="mega-menu"
              >
                <!-- Cột text bên trái -->
                <div
                    v-for="category in megaMenuData.categories"
                    :key="category.title"
                    class="mega-column"
                >
                  <h4>{{ category.title }}</h4>
                  <router-link
                      v-for="item in category.items"
                      :key="item.id"
                      :to="`/product/${item.id}`"
                  >
                    {{ item.name }}
                  </router-link>
                </div>

                <!-- Cột có hình giày -->
                <div
                    v-for="shoe in megaMenuData.shoes"
                    :key="shoe.id"
                    class="mega-column mega-shoe"
                >
                  <h4>{{ shoe.name }}</h4>
                  <div
                      class="mega-product-card"
                      @click="goToCollection(shoe.id)"
                  >
                    <img :src="shoe.image" :alt="shoe.name" />
                    <span>Shop {{ shoe.name }}</span>
                  </div>
                </div>
              </div>
            </li>

            <li><router-link to="/about">ABOUT</router-link></li>
            <li><router-link to="/contact">CONTACT</router-link></li>
            <li><router-link to="/blog">BLOG</router-link></li>
            <li><router-link to="/help">HELP</router-link></li>
          </ul>
        </nav>
      </div>

      <!-- RIGHT SECTION: SEARCH + CART + USER -->
      <div class="right-section-container">
        <!-- SEARCH -->
        <div class="search-wrapper">
          <div class="header-search">
            <input
                v-model="searchQuery"
                @focus="showSearchDropdown = true"
                @input="onInput"
                @blur="onSearchBlur"
                @keyup.enter="onSearchClick"
                placeholder="Tìm kiếm sản phẩm..."
                type="text"
            />
            <div v-if="showSearchDropdown" class="search-dropdown">
              <div v-if="filteredProducts.length">
                <div
                    v-for="product in filteredProducts"
                    :key="product.id"
                    class="search-item"
                    @mousedown.prevent="goToProduct(product.id)"
                >
                  <img
                      :src="product.image"
                      alt="Product Image"
                      class="search-item-img"
                  />
                  <span class="search-item-name">{{ product.name }}</span>
                </div>
              </div>
              <div v-else class="search-item not-found">
                Không tìm thấy sản phẩm.
              </div>
            </div>
          </div>
        </div>

        <!-- CART -->
        <div class="cart-icon-wrapper" @click="showCart = true">
          <i class="fa-solid fa-cart-shopping cart-icon"></i>
          <span v-if="totalQuantity > 0" class="cart-badge">
            {{ totalQuantity }}
          </span>
        </div>

        <!-- USER + FAVORITE -->
        <ul class="user-action-icons">
          <li v-if="!currentUser">
            <router-link to="/auth" class="icon-link">
              <i class="fa-solid fa-user"></i>
            </router-link>
          </li>

          <li
              v-else
              class="user-info-dropdown-wrapper"
              ref="userDropdownRef"
          >
            <div
                @click.stop="toggleUserMenu"
                class="user-name-display"
            >
              <span class="user-name-text">
                {{ currentUser.name || currentUser.email || 'Người dùng' }}
              </span>
              <i
                  class="fa-solid fa-chevron-down chevron-icon"
                  :class="{ 'rotate-chevron': showUserMenu }"
              ></i>
            </div>

            <div
                v-if="showUserMenu"
                :class="['logout-dropdown', { 'show-dropdown': showUserMenu }]"
            >
              <button
                  @click="logout"
                  class="logout-button"
              >
                Đăng xuất
              </button>
              <button v-if="currentUser?.isAdmin" class="admin_css">
                <router-link to="/admin">Admin</router-link>
              </button>
            </div>
          </li>

          <li class="favorite-icon-item">
            <router-link to="/favorites" class="icon-link">
              <i class="fa-solid fa-heart heart-icon"></i>
            </router-link>
          </li>
        </ul>
      </div>
    </nav>

    <!-- PROMO BAR -->
    <div class="box">
      <p>Use code DEAL for an extra 30% off. Exclusions apply.</p>
    </div>

    <!-- CART MODAL -->
    <CartModal
        :show="showCart"
        :cartItems="cartStore.items"
        @close="showCart = false"
        @update:cartItems="cartStore.items = $event"
    />
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';

import { useProductStore } from '@/stores/productStore';
import { useCartStore } from '@/stores/cartStore';
import { useMegaMenuStore } from '@/stores/megaMenuStore';

import CartModal from './CartModal.vue';

const router = useRouter();

/* --- Menu Hamburger --- */
const isMenuActive = ref(false);
const toggleMenu = () => {
  isMenuActive.value = !isMenuActive.value;
};

/* --- Mega menu PRODUCT --- */
const showProductMega = ref(false);
const openProductMega = () => { showProductMega.value = true; };
const closeProductMega = () => { showProductMega.value = false; };

const megaMenuStore = useMegaMenuStore();
const megaMenuData = computed(
    () => megaMenuStore.data || { categories: [], shoes: [] }
);

const goToCollection = (id) => {
  router.push(`/product/${id}`);
  showProductMega.value = false;
};

/* --- Xác thực người dùng (localStorage) --- */
const currentUser = ref(null);
const showUserMenu = ref(false);
const userDropdownRef = ref(null);

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value;
};

const logout = () => {
  localStorage.removeItem('userLogin');
  currentUser.value = null;
  showUserMenu.value = false;
  router.push('/');
};

const handleClickOutside = (event) => {
  if (userDropdownRef.value && !userDropdownRef.value.contains(event.target)) {
    showUserMenu.value = false;
  }
};

/* --- Giỏ hàng --- */
const cartStore = useCartStore();
const showCart = ref(false);
const totalQuantity = computed(() => cartStore.totalQuantity);

/* --- Tìm kiếm --- */
const productsStore = useProductStore();
const searchQuery = ref('');
const showSearchDropdown = ref(false);

const filteredProducts = computed(() => {
  const q = searchQuery.value.trim().toLowerCase();
  if (!q) return [];
  return productsStore.products.filter(p =>
      p.name && p.name.toLowerCase().includes(q)
  );
});

const onInput = () => {
  showSearchDropdown.value = !!searchQuery.value.trim();
};

const onSearchClick = () => {
  if (searchQuery.value.trim()) {
    if (filteredProducts.value.length > 0) {
      goToProduct(filteredProducts.value[0].id);
    } else {
      router.push({ name: 'SearchResults', query: { q: searchQuery.value } });
    }
    showSearchDropdown.value = false;
    searchQuery.value = '';
  }
};

const goToProduct = (id) => {
  const targetPath = `/product/${id}`;
  router.push(targetPath);
  showSearchDropdown.value = false;
  searchQuery.value = '';
};

const onSearchBlur = () => {
  setTimeout(() => {
    showSearchDropdown.value = false;
  }, 200);
};

/* --- Lifecycle --- */
onMounted(() => {

  megaMenuStore.fetchMegaMenu();

  // user
  const user = localStorage.getItem('userLogin');
  if (user) {
    currentUser.value = JSON.parse(user);
  }

  document.addEventListener('click', handleClickOutside);

  // products cho search
  if (!productsStore.products.length) {
    productsStore.fetchProducts();
  }

  // mega menu data
  megaMenuStore.fetchMegaMenu();
});

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside);
});
</script>

<style scoped>
/* GENERAL STYLES - Các style chung cho header */
.hero {
  width: 100%;
}
.nav-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;    /* Căn giữa các phần tử theo chiều dọc */
  padding: 10px 20px;
  flex-wrap: wrap;        /* Giúp responsive tốt hơn */
  background-color: white; /* Đảm bảo nền trắng cho nav-bar */
  position: relative;      /* Quan trọng cho mega-menu */
}
.logo-container {
  display: flex;
  align-items: center;
}
.adidas-logo-img {
  display: flex;
  align-items: center;
  width: 100px; /* Điều chỉnh kích thước logo */
}

/* NAVIGATION LINKS */
.nav-menu-wrapper {
  display: flex;
  align-items: center;
}
.nav-links ul {
  list-style: none;
  display: flex;
  margin: 0;
  padding: 0; /* Bỏ padding mặc định của ul */
}
.nav-links li a {
  text-decoration: none;
  padding: 10px 15px; /* Điều chỉnh padding cho link */
  display: block;
  color: black;
  font-weight: 500;
  transition: background-color 0.3s ease, color 0.3s ease;
  border-radius: 4px;
}
.nav-links li a:hover {
  color: white;
  background: black;
}

/* HAMBURGER MENU - Ẩn trên desktop */
.hamburger-menu {
  display: none; /* Mặc định ẩn trên màn hình lớn */
  width: 30px;
  height: 24px;
  cursor: pointer;
  position: relative;
  flex-direction: column;
  justify-content: space-between;
}
.hamburger-menu .bar {
  display: block;
  width: 100%;
  height: 3px;
  background-color: black;
  margin: 3px 0; /* Điều chỉnh margin giữa các bar */
  transition: 0.4s;
  border-radius: 2px;
}

/* MEGA MENU PRODUCT */
.nav-links .nav-item.has-mega {
  position: static;
}

.mega-menu {
  position: absolute;
  top: calc(68% + 0px);
  left: 50%;
  transform: translateX(-50%);

  width: 100%;
  max-width: 1400px;
  background-color: #fff;

  border-top: 1px solid #eee;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);

  padding: 20px 40px;

  display: flex;
  justify-content: space-between;
  gap: 40px;

  z-index: 999;
  box-sizing: border-box;
}



.mega-column {
  flex: 1;
  min-width: 180px;
}

.mega-column h4 {
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 10px;
  text-transform: uppercase;
}

.mega-column a {
  display: block;
  font-size: 14px;
  color: #111;
  padding: 3px 0;
  text-decoration: none;
}

.mega-column a:hover {
  text-decoration: underline;
}

.mega-column.mega-shoe {
  max-width: 240px;
}

.mega-product-card {
  margin-top: 10px;
  cursor: pointer;
}

.mega-product-card img {
  width: 100%;
  height: auto;
  display: block;
}

.mega-product-card span {
  display: block;
  margin-top: 6px;
  font-size: 13px;
  font-weight: 500;
}

/* PROMOTIONAL BOX */
.box {
  text-align: center;
  background: linear-gradient(to right, #ebeff2 0%, #ebeff2 100%);
  height: 50px;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  font-size: 15px;
  color: #333;
  font-weight: 600;
}

/* RIGHT SECTION CONTAINER (Search, Cart, User/Favorite) */
.right-section-container {
  display: flex;
  align-items: center;
  gap: 20px; /* Khoảng cách giữa các item: Search, Cart, User/Favorite */
}

/* SEARCH BAR */
.search-wrapper {
  position: relative; /* Cho dropdown tìm kiếm */
  display: flex;
  align-items: center;
}
.header-search {
  display: flex;
  align-items: center;
  border: 1px solid #ccc;
  border-radius: 6px;
  background-color: #fff;
  padding: 5px 10px;
  width: 250px;
}
.header-search input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  background-color: transparent;
  padding: 0;
}

/* SEARCH DROPDOWN */
.search-dropdown {
  position: absolute;
  top: calc(100% + 2px);
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #ccc;
  z-index: 9999;
  max-height: 200px;
  overflow-y: auto;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
  border-radius: 4px;
  animation: fadeIn 0.25s ease-in-out;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}
.search-item {
  display: flex;
  align-items: center;
  padding: 10px 14px;
  cursor: pointer;
  border-bottom: 1px solid #f2f2f2;
  transition: background-color 0.2s ease;
}
.search-item:hover {
  background-color: #f5f5f5;
}
.search-item-img {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 12px;
}
.search-item-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}
.not-found {
  color: #999;
  font-style: italic;
  text-align: center;
  padding: 10px;
}

/* CART ICON */
.cart-icon-wrapper {
  position: relative; /* Cho badge số lượng */
  cursor: pointer;
  padding: 5px; /* Thêm padding để dễ click hơn */
  border-radius: 50%; /* Bo tròn nếu muốn */
  transition: background-color 0.2s ease;
}
.cart-icon-wrapper:hover {
  background-color: #f0f0f0; /* Hiệu ứng hover */
}
.cart-icon {
  font-size: 24px;
  color: #333;
}
.cart-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background-color: #ff4500; /* Màu cam nổi bật */
  color: white;
  border-radius: 50%;
  padding: 2px 6px;
  font-size: 10px;
  font-weight: bold;
  line-height: 1;
}

/* USER ACTION ICONS (User/Favorite) */
.user-action-icons {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  align-items: center;
}
.icon-link {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  transition: background-color 0.2s ease;
}
.icon-link:hover {
  background-color: #f0f0f0;
}
.icon-link .fa-solid {
  font-size: 20px;
  color: #666;
}
.icon-link .heart-icon {
  color: #e74c3c;
}

/* USER INFO & DROPDOWN */
.user-info-dropdown-wrapper {
  position: relative;
}
.user-name-display {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  cursor: pointer;
  border-radius: 20px;
  transition: background-color 0.2s ease;
}
.user-name-display:hover {
  background-color: #f0f0f0;
}
.user-name-text {
  font-weight: 600;
  color: #333;
  font-size: 15px;
  margin-right: 5px;
}
.chevron-icon {
  font-size: 12px;
  color: #666;
  transition: transform 0.2s ease-in-out;
}
.chevron-icon.rotate-chevron {
  transform: rotate(180deg);
}

.logout-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 5px;
  background-color: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  width: 160px;
  z-index: 100;
  overflow: hidden;
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.2s ease, visibility 0.2s ease;
}
.logout-dropdown.show-dropdown {
  opacity: 1;
  visibility: visible;
}
.logout-dropdown .logout-button {
  display: block;
  width: 100%;
  padding: 10px 15px;
  text-align: left;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 14px;
  color: #333;
  transition: background-color 0.2s ease;
}
.logout-dropdown .logout-button:hover {
  background-color: #f5f5f5;
}

/* Khoảng cách giữa tên user và icon tim */
.user-action-icons .favorite-icon-item {
  margin-left: 30px;
}

.admin_css {
  padding: 10px 20px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
  font-weight: bold;
  transition: background-color 0.3s ease, transform 0.2s ease;
  text-transform: uppercase;
}
.admin_css:hover {
  background-color: #2980b9;
  transform: scale(1.05);
}
.admin_css a {
  text-decoration: none;
  color: inherit;
}

/* Ẩn mega menu trên mobile */
@media (max-width: 992px) {
  .mega-menu {
    display: none !important;
  }
}
</style>
