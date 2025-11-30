package adiadas_backedn.backend.controller;

import adiadas_backedn.backend.dto.FavoriteDTO;
import adiadas_backedn.backend.service.FavoriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = "http://localhost:8080")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    // GET /api/favorites/{userId}
    @GetMapping("/{userId}")
    public ResponseEntity<List<FavoriteDTO>> getFavorites(@PathVariable String userId) {
        List<FavoriteDTO> list = favoriteService.getFavorites(userId);
        return ResponseEntity.ok(list);
    }

    // POST /api/favorites/{userId}?productId=xxx
    @PostMapping("/{userId}")
    public ResponseEntity<Void> addFavorite(
            @PathVariable String userId,
            @RequestParam String productId) {
        favoriteService.addFavorite(userId, productId);
        return ResponseEntity.ok().build();
    }

    // DELETE /api/favorites/{userId}?productId=xxx
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeFavorite(
            @PathVariable String userId,
            @RequestParam String productId) {
        favoriteService.removeFavorite(userId, productId);
        return ResponseEntity.ok().build();
    }
}
