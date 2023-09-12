package ru.practicum.explorewithme.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentFullInfoResponseDto {
    private Long id;
    private String text;
    private EventResponseDto event;
    private UserResponseDto author;
    private Boolean isAuthorInitiatorOfEvent;
    private Boolean isEdited;
    private LocalDateTime created;
}
