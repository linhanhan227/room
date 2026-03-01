package com.chat.room.service.recommendation;

import com.chat.room.dto.ChatRoomDTO;
import com.chat.room.entity.ChatRoom;
import com.chat.room.entity.User;
import com.chat.room.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PopularityBasedRecommendationStrategy implements RoomRecommendationStrategy {

    private final ChatRoomRepository chatRoomRepository;

    @Override
    public String getName() {
        return "POPULARITY";
    }

    @Override
    public List<ChatRoomDTO> recommend(User user, int limit) {
        return recommend(limit);
    }

    @Override
    public List<ChatRoomDTO> recommend(int limit) {
        log.debug("Using POPULARITY recommendation strategy, limit: {}", limit);
        
        List<Object[]> roomsWithMembers = chatRoomRepository.findRoomsWithMemberCount(
                PageRequest.of(0, limit)
        );
        
        return roomsWithMembers.stream()
                .map(row -> {
                    ChatRoom room = (ChatRoom) row[0];
                    Long memberCount = (Long) row[1];
                    ChatRoomDTO dto = ChatRoomDTO.fromEntity(room);
                    dto.setMemberCount(memberCount.intValue());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
