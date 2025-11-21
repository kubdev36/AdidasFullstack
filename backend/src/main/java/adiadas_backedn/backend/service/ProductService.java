package adiadas_backedn.backend.service;

import adiadas_backedn.backend.model.Product;
import adiadas_backedn.backend.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        List<Product> products = productRepository.findAll();
        products.forEach(p -> {
            System.out.println("📦 Product: " + p.getName() +
                    ", Featured: " + p.getFeatured() +
                    ", isFeatured(): " + p.isFeatured());
        });
        return products;
    }

    public List<Product> getFeaturedProducts() {
        List<Product> featuredProducts = productRepository.findFeaturedProductsNative();
        System.out.println("🎯 Featured products found (Native): " + featuredProducts.size());
        featuredProducts.forEach(p -> {
            System.out.println("🎯 Featured Product: " + p.getName() + ", Featured: " + p.getFeatured());
        });
        return featuredProducts;
    }

    public Optional<Product> getProductById(String id) {
        return productRepository.findById(id);
    }

    public Optional<Product> getProductWithDetails(String id) {
        return productRepository.findByIdWithDetails(id);
    }

    // THÊM: Phương thức mới để lấy sản phẩm với videos
    public Optional<Product> getProductWithVideos(String id) {
        return productRepository.findByIdWithVideos(id);
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword);
    }

    public List<Product> getProductsByPriceRange(Double minPrice, Double maxPrice) {
        return productRepository.findByPriceRange(minPrice, maxPrice);
    }

    public Product createProduct(Product product) {
        if (product.getId() == null || product.getId().isEmpty()) {
            product.setId(generateProductId());
        }
        return productRepository.save(product);
    }

    public Product updateProduct(String id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setName(productDetails.getName());
        product.setPrice(productDetails.getPrice());
        product.setFeatured(productDetails.getFeatured());
        product.setCategory(productDetails.getCategory());
        product.setImage(productDetails.getImage());
        product.setHoverImage(productDetails.getHoverImage());
        product.setLink(productDetails.getLink());
        product.setRating(productDetails.getRating());
        product.setPromoCodes(productDetails.getPromoCodes());
        product.setPaymentOptions(productDetails.getPaymentOptions());
        product.setShipping(productDetails.getShipping());
        product.setReturnsExchanges(productDetails.getReturnsExchanges());
        product.setSizingNote(productDetails.getSizingNote());
        product.setDescription(productDetails.getDescription());

        return productRepository.save(product);
    }

    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }

    private String generateProductId() {
        return "prod_" + System.currentTimeMillis();
    }
}