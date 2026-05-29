package ru.yandex.practicum.filmorate.storage.recommendation;

import java.util.List;

public interface RecommendationsStorage {
    List<Long> getRecommendations(Long userId);
}

