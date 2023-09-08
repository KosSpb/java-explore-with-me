package ru.practicum.explorewithme.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompilationResponseDto {
    private Set<EventResponseDto> events;
    private Long id;
    private Boolean pinned;
    private String title;
}
