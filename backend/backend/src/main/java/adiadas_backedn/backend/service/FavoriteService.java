package adiadas_backedn.backend.service;

import adiadas_backedn.backend.dto.FavoriteDTO;

import java.util.List;

public interface FavoriteService {

    List<FavoriteDTO> getFavorites(String userId);

    void addFavorite(String userId, String productId);

    void removeFavorite(String userId, String productId);
}
