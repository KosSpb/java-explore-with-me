package ru.practicum.explorewithme;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatsResponseDto {
    private String app;
    private String uri;
    private Long hits;
}
