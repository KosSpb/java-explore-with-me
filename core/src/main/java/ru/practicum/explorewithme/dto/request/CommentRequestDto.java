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
public class CommentRequestDto {
    @NotBlank(message = "Field: text. Error: must not be blank or null.")
    @Size(min = 3, max = 7000, message = "Field: text. Error: must be minimum 3 and maximum 7000 characters.")
    private String text;
}
