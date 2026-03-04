package com.chat.room.entity;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RoomMemberId implements Serializable {
    private Long room;
    private Long user;
}
