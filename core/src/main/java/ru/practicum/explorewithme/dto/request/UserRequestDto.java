package ru.practicum.explorewithme.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequestDto {
    @Email(regexp = ".+[@].+[\\.].+", message = "Field: email. Error: must be this pattern: user@example.ru")
    @NotBlank(message = "Field: email. Error: must not be blank or null.")
    @Size(min = 6, max = 254, message = "Field: title. Error: must be minimum 6 and maximum 254 characters.")
    private String email;
    @NotBlank(message = "Field: name. Error: must not be blank or null.")
    @Size(min = 2, max = 250, message = "Field: title. Error: must be minimum 2 and maximum 250 characters.")
    private String name;
}
