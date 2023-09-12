package ru.practicum.explorewithme.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.explorewithme.enums.EventModerationState;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderMethodName = "eventFullInfoResponseDtoBuilder")
@EqualsAndHashCode(callSuper = true)
public class EventFullInfoResponseDto extends EventResponseDto {
    private String annotation;
    private CategoryResponseDto category;
    private Long confirmedRequests;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;
    private String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private Long id;
    private UserResponseDto initiator;
    private LocationResponseDto location;
    private Boolean paid;
    private Long participantLimit;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;
    private Boolean requestModeration;
    private EventModerationState state;
    private String title;
    private Long views;
    private List<CommentResponseDto> comments;
}
