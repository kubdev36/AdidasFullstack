package adiadas_backedn.backend.repository;

import adiadas_backedn.backend.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    // Lấy tất cả favorite của 1 user
    List<Favorite> findByUser_Id(String userId);

    // Tìm 1 favorite theo user + product
    Optional<Favorite> findByUser_IdAndProduct_Id(String userId, String productId);

    // Xóa favorite theo user + product
    void deleteByUser_IdAndProduct_Id(String userId, String productId);
}
