package adiadas_backedn.backend.repository;


import adiadas_backedn.backend.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndProductIdAndColorNameAndSizeValue(
            String cartId, String productId, String colorName, String sizeValue);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.productId = :productId AND ci.colorName = :colorName AND ci.sizeValue = :sizeValue")
    void deleteByCartIdAndProductIdAndColorNameAndSizeValue(
            @Param("cartId") String cartId,
            @Param("productId") String productId,
            @Param("colorName") String colorName,
            @Param("sizeValue") String sizeValue);
}