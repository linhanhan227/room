package com.chat.room.service.recommendation;

import com.chat.room.dto.ChatRoomDTO;
import com.chat.room.entity.User;

import java.util.List;

public interface RoomRecommendationStrategy {

    String getName();

    List<ChatRoomDTO> recommend(User user, int limit);

    List<ChatRoomDTO> recommend(int limit);
}
