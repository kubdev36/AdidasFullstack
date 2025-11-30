package adiadas_backedn.backend.service;

import adiadas_backedn.backend.dto.FavoriteDTO;
import adiadas_backedn.backend.model.Favorite;
import adiadas_backedn.backend.model.Product;
import adiadas_backedn.backend.model.User;
import adiadas_backedn.backend.repository.FavoriteRepository;
import adiadas_backedn.backend.repository.ProductRepository;
import adiadas_backedn.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepo;
    private final UserRepository userRepo;
    private final ProductRepository productRepo;

    public FavoriteServiceImpl(FavoriteRepository favoriteRepo,
                               UserRepository userRepo,
                               ProductRepository productRepo) {
        this.favoriteRepo = favoriteRepo;
        this.userRepo = userRepo;
        this.productRepo = productRepo;
    }

    @Override
    public List<FavoriteDTO> getFavorites(String userId) {
        return favoriteRepo.findByUser_Id(userId)
                .stream()
                .map(FavoriteDTO::new)
                .toList();
    }

    @Override
    public void addFavorite(String userId, String productId) {

        // Nếu đã tồn tại thì bỏ qua
        if (favoriteRepo.findByUser_IdAndProduct_Id(userId, productId).isPresent()) {
            return;
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        Favorite f = new Favorite();
        f.setUser(user);
        f.setProduct(product);
        f.setAddedDate(LocalDateTime.now());

        favoriteRepo.save(f);
    }

    @Override
    public void removeFavorite(String userId, String productId) {
        favoriteRepo.deleteByUser_IdAndProduct_Id(userId, productId);
    }
}
