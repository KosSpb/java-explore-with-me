package ru.practicum.explorewithme.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explorewithme.validation.OnCreate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompilationRequestDto {
    private Set<Long> events;
    private Boolean pinned;
    @NotBlank(groups = OnCreate.class, message = "Field: title. Error: must not be blank or null.")
    @Size(min = 1, max = 50, message = "Field: title. Error: must be minimum 1 and maximum 50 characters.")
    private String title;
}
