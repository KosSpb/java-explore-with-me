package ru.practicum.explorewithme.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explorewithme.validation.OnCreate;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocationRequestDto {
    @NotNull(groups = OnCreate.class, message = "Field: lat. Error: must not be null.")
    private Double lat;
    @NotNull(groups = OnCreate.class, message = "Field: lon. Error: must not be null.")
    private Double lon;
}
