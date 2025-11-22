  <template>
    <router-view :key="$route.fullPath"/>
  </template>

  <script>
  import { onMounted } from 'vue';
  import { useCartStore } from '@/stores/cartStore';

  export default {
    name: 'App',
    setup() {
      const cartStore = useCartStore();

      onMounted(() => {
        cartStore.clearVoucher();
      });

      return {};
    },
  };
  </script>

  <style>
  #app {
    font-family: Avenir, Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-align: left;
    color: #2c3e50;
    margin: 0;
    padding: 0;
  }
  </style>
