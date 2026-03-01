package com.chat.room.service.recommendation;

import com.chat.room.dto.ChatRoomDTO;
import com.chat.room.entity.ChatRoom;
import com.chat.room.entity.User;
import com.chat.room.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RandomRecommendationStrategy implements RoomRecommendationStrategy {

    private final ChatRoomRepository chatRoomRepository;

    @Override
    public String getName() {
        return "RANDOM";
    }

    @Override
    public List<ChatRoomDTO> recommend(User user, int limit) {
        return recommend(limit);
    }

    @Override
    public List<ChatRoomDTO> recommend(int limit) {
        log.debug("Using RANDOM recommendation strategy, limit: {}", limit);
        
        List<ChatRoom> rooms = chatRoomRepository.findPublicRooms(
                PageRequest.of(0, limit * 3)
        ).getContent();
        
        Collections.shuffle(rooms);
        
        return rooms.stream()
                .limit(limit)
                .map(room -> {
                    ChatRoomDTO dto = ChatRoomDTO.fromEntity(room);
                    dto.setMemberCount(chatRoomRepository.countMembersByRoomId(room.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
