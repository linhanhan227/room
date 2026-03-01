package com.chat.room.service.recommendation;

import com.chat.room.config.AppProperties;
import com.chat.room.dto.ChatRoomDTO;
import com.chat.room.entity.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomRecommendationService {

    private final List<RoomRecommendationStrategy> strategies;
    private final AppProperties appProperties;
    
    private final Map<String, RoomRecommendationStrategy> strategyMap = new HashMap<>();
    private RoomRecommendationStrategy defaultStrategy;

    @PostConstruct
    public void init() {
        for (RoomRecommendationStrategy strategy : strategies) {
            strategyMap.put(strategy.getName().toUpperCase(), strategy);
            log.info("Registered recommendation strategy: {}", strategy.getName());
        }
        
        String defaultStrategyName = appProperties.getRoomRecommendation().getDefaultStrategy();
        defaultStrategy = strategyMap.get(defaultStrategyName.toUpperCase());
        
        if (defaultStrategy == null) {
            log.warn("Default strategy '{}' not found, falling back to HYBRID", defaultStrategyName);
            defaultStrategy = strategyMap.get("HYBRID");
        }
        
        if (defaultStrategy == null && !strategies.isEmpty()) {
            defaultStrategy = strategies.get(0);
        }
        
        log.info("Default recommendation strategy: {}", 
                defaultStrategy != null ? defaultStrategy.getName() : "NONE");
    }

    public List<ChatRoomDTO> getRecommendations(User user) {
        int limit = appProperties.getRoomRecommendation().getRecommendationLimit();
        return getRecommendations(user, limit);
    }

    public List<ChatRoomDTO> getRecommendations(User user, int limit) {
        log.debug("Getting recommendations for user: {}, limit: {}", 
                user != null ? user.getId() : "anonymous", limit);
        return defaultStrategy.recommend(user, limit);
    }

    public List<ChatRoomDTO> getRecommendations(String strategyName, User user, int limit) {
        RoomRecommendationStrategy strategy = strategyMap.get(strategyName.toUpperCase());
        
        if (strategy == null) {
            log.warn("Strategy '{}' not found, using default strategy", strategyName);
            return getRecommendations(user, limit);
        }
        
        log.debug("Using strategy: {} for recommendations", strategyName);
        return strategy.recommend(user, limit);
    }

    public List<ChatRoomDTO> getRecommendationsForAnonymous() {
        int limit = appProperties.getRoomRecommendation().getRecommendationLimit();
        return getRecommendationsForAnonymous(limit);
    }

    public List<ChatRoomDTO> getRecommendationsForAnonymous(int limit) {
        log.debug("Getting recommendations for anonymous user, limit: {}", limit);
        return defaultStrategy.recommend(limit);
    }

    public List<String> getAvailableStrategies() {
        return strategyMap.keySet().stream()
                .sorted()
                .toList();
    }

    public String getDefaultStrategyName() {
        return defaultStrategy != null ? defaultStrategy.getName() : null;
    }

    public void setDefaultStrategy(String strategyName) {
        RoomRecommendationStrategy strategy = strategyMap.get(strategyName.toUpperCase());
        if (strategy != null) {
            this.defaultStrategy = strategy;
            log.info("Default strategy changed to: {}", strategyName);
        } else {
            log.warn("Cannot set default strategy: '{}' not found", strategyName);
        }
    }

    public boolean hasStrategy(String strategyName) {
        return strategyMap.containsKey(strategyName.toUpperCase());
    }
}
