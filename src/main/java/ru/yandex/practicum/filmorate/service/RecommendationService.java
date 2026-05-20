package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.recommendation.RecommendationsStorage;

import java.util.List;

@Slf4j
@Service
public class RecommendationService {
    private final RecommendationsStorage recommendationsStorage;
    private final UserService userService;
    private final FilmService filmService;

    public RecommendationService(RecommendationsStorage recommendationsStorage, UserService userService, FilmService filmService) {
        this.recommendationsStorage = recommendationsStorage;
        this.userService = userService;
        this.filmService = filmService;
    }

    public List<Film> getRecommendations(Long userId) {
        userService.findById(userId);

        List<Long> recommendedFilmsId = recommendationsStorage.getRecommendations(userId);
        if (recommendedFilmsId.isEmpty()) {
            return List.of();
        }

        return recommendedFilmsId.stream()
                .map(filmService::getFilm)
                .toList();
    }
}
