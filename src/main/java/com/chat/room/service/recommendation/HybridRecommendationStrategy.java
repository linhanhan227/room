package com.chat.room.service.recommendation;

import com.chat.room.config.AppProperties;
import com.chat.room.dto.ChatRoomDTO;
import com.chat.room.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class HybridRecommendationStrategy implements RoomRecommendationStrategy {

    private final ActivityBasedRecommendationStrategy activityStrategy;
    private final PopularityBasedRecommendationStrategy popularityStrategy;
    private final NewestRecommendationStrategy newestStrategy;
    private final RandomRecommendationStrategy randomStrategy;
    private final AppProperties appProperties;

    @Override
    public String getName() {
        return "HYBRID";
    }

    @Override
    public List<ChatRoomDTO> recommend(User user, int limit) {
        return recommend(limit);
    }

    @Override
    public List<ChatRoomDTO> recommend(int limit) {
        log.debug("Using HYBRID recommendation strategy, limit: {}", limit);
        
        AppProperties.RoomRecommendation.HybridWeights weights = appProperties.getRoomRecommendation()
                .getHybridWeights();
        
        int activityCount = (int) Math.ceil(limit * weights.getActivityWeight());
        int popularityCount = (int) Math.ceil(limit * weights.getPopularityWeight());
        int newestCount = (int) Math.ceil(limit * weights.getNewestWeight());
        int randomCount = (int) Math.ceil(limit * weights.getRandomWeight());
        
        Map<Long, ChatRoomDTO> roomMap = new LinkedHashMap<>();
        
        List<ChatRoomDTO> activityRooms = activityStrategy.recommend(activityCount);
        activityRooms.forEach(room -> roomMap.put(room.getId(), room));
        
        List<ChatRoomDTO> popularityRooms = popularityStrategy.recommend(popularityCount);
        popularityRooms.forEach(room -> roomMap.putIfAbsent(room.getId(), room));
        
        List<ChatRoomDTO> newestRooms = newestStrategy.recommend(newestCount);
        newestRooms.forEach(room -> roomMap.putIfAbsent(room.getId(), room));
        
        List<ChatRoomDTO> randomRooms = randomStrategy.recommend(randomCount);
        randomRooms.forEach(room -> roomMap.putIfAbsent(room.getId(), room));
        
        List<ChatRoomDTO> result = new ArrayList<>(roomMap.values());
        
        if (result.size() > limit) {
            result = result.subList(0, limit);
        }
        
        return result;
    }
}
