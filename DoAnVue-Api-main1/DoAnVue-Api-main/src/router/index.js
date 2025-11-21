import { createRouter, createWebHistory } from "vue-router";
import HomePage from '@/components/HomePage.vue';
import ProductDetail from '@/components/ProductDetail.vue';
import ProductList from '@/components/ProductList.vue';
import AuthPage from "@/components/AuthPage.vue";
import BlogList from "@/components/BlogList.vue";
import BlogDetail from '@/components/BlogDetail.vue';
import CartModal from '@/components/CartModal.vue';
import CheckoutPage from '@/components/CheckoutPage.vue';
import FavoritesPage from '@/components/FavoritesPage.vue';
import AdminProductPage from "../components/AdminProductPage.vue";
import ResetPassword from '../components/ResetPassword.vue';
import VerifyEmail from '../components/VerifyEmail.vue'; // Import component VerifyEmail

const routes = [
  {
    path: '/',
    name: 'Home',
    component: HomePage,
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: ProductDetail,
  },
  {
    path: '/product',
    name: 'ProductList',
    component: ProductList,
  },
  {
    path: '/auth',
    name: 'Auth',
    component: AuthPage,
  },
  {
    path: '/blog',
    name: 'Blog',
    component: BlogList,
  },
  {
    path: '/blog/:id',
    name: 'BlogDetail',
    component: BlogDetail,
  },
  {
    path: '/cart',
    name: 'Cart',
    component: CartModal,
  },
  {
    path: '/checkout',
    name: 'Checkout',
    component: CheckoutPage,
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: FavoritesPage,
  },
  {
    path: '/admin',
    name: 'AdminProducts',
    component: AdminProductPage,
  },
  {
    path: '/reset-password',
    name: 'ResetPassword',
    component: ResetPassword,
  },
  // Route mới cho xác thực email
  {
    path: '/verify-email',
    name: 'VerifyEmail',
    component: VerifyEmail,
    meta: {
      title: 'Xác Thực Email'
    }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// Update document title
router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = to.meta.title + ' | Adidas';
  } else {
    document.title = 'Adidas';
  }
  next();
});

export default router;