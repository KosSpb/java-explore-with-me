package ru.practicum.explorewithme.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryRequestDto {
    @NotBlank(message = "Field: name. Error: must not be blank or null.")
    @Size(min = 1, max = 50, message = "Field: name. Error: must be minimum 1 and maximum 50 characters.")
    private String name;
}
