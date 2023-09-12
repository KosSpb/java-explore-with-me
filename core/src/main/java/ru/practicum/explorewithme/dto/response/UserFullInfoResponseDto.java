package ru.practicum.explorewithme.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserFullInfoResponseDto {
    private String email;
    private Long id;
    private String name;
}
