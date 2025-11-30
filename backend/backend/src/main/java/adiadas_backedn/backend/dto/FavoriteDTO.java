package adiadas_backedn.backend.dto;

import adiadas_backedn.backend.model.Favorite;
import adiadas_backedn.backend.model.Product;
import adiadas_backedn.backend.model.ProductColor;
import adiadas_backedn.backend.model.ProductSize;
import adiadas_backedn.backend.model.ProductImage;

import java.util.List;
import java.util.stream.Collectors;

public class FavoriteDTO {

    private Long id;           // id của favorite (int trong DB)
    private String productId;  // id sản phẩm (varchar)
    private String name;
    private String image;
    private Double price;

    // ✅ mới thêm: danh sách màu, size, ảnh phụ của product
    private List<ProductColorDTO> colors;
    private List<ProductSizeDTO> sizes;
    private List<ProductImageDTO> images;

    public FavoriteDTO() {
    }

    // Constructor full (nếu chỗ khác cần dùng tay)
    public FavoriteDTO(Long id,
                       String productId,
                       String name,
                       String image,
                       Double price,
                       List<ProductColorDTO> colors,
                       List<ProductSizeDTO> sizes,
                       List<ProductImageDTO> images) {
        this.id = id;
        this.productId = productId;
        this.name = name;
        this.image = image;
        this.price = price;
        this.colors = colors;
        this.sizes = sizes;
        this.images = images;
    }

    // 🔥 Constructor tiện nhất: map trực tiếp từ Favorite
    public FavoriteDTO(Favorite favorite) {
        Product p = favorite.getProduct();

        this.id = favorite.getId();
        this.productId = p.getId();
        this.name = p.getName();
        this.image = p.getImage();
        this.price = p.getPrice();

        // ⚠️ phần này KHÔNG sửa Product, chỉ gọi getter có sẵn
        // Nếu Product của bạn đặt tên khác (ví dụ getProductColors()),
        // chỉ cần đổi đúng tên hàm ở đây.

        // Màu
        if (p.getColors() != null) {
            this.colors = p.getColors()
                    .stream()
                    .map(ProductColorDTO::new)
                    .collect(Collectors.toList());
        }

        // Size
        if (p.getSizes() != null) {
            this.sizes = p.getSizes()
                    .stream()
                    .map(ProductSizeDTO::new)
                    .collect(Collectors.toList());
        }

        // Ảnh phụ (nếu Product có list ảnh)
        if (p.getImages() != null) {
            this.images = p.getImages()
                    .stream()
                    .map(ProductImageDTO::new)
                    .collect(Collectors.toList());
        }
    }

    // Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public List<ProductColorDTO> getColors() { return colors; }
    public void setColors(List<ProductColorDTO> colors) { this.colors = colors; }

    public List<ProductSizeDTO> getSizes() { return sizes; }
    public void setSizes(List<ProductSizeDTO> sizes) { this.sizes = sizes; }

    public List<ProductImageDTO> getImages() { return images; }
    public void setImages(List<ProductImageDTO> images) { this.images = images; }
}
