package ru.practicum.explorewithme.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explorewithme.enums.EventModerationAction;
import ru.practicum.explorewithme.exception.IncorrectRequestException;
import ru.practicum.explorewithme.validation.OnCreate;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventRequestDto {
    @NotBlank(groups = OnCreate.class, message = "Field: annotation. Error: must not be blank or null.")
    @Size(min = 20, max = 2000, message = "Field: annotation. Error: must be minimum 20 and maximum 2000 characters.")
    private String annotation;
    @NotNull(groups = OnCreate.class, message = "Field: category. Error: must not be null.")
    @Min(1)
    private Long category;
    @NotBlank(groups = OnCreate.class, message = "Field: description. Error: must not be blank or null.")
    @Size(min = 20, max = 7000, message = "Field: description. Error: must be minimum 20 and maximum 7000 characters.")
    private String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(groups = OnCreate.class, message = "Field: event date. Error: must not be null.")
    private LocalDateTime eventDate;
    @NotNull(groups = OnCreate.class, message = "Field: location. Error: must not be null.")
    private LocationRequestDto location;
    private Boolean paid;
    @Min(0)
    private Long participantLimit;
    private Boolean requestModeration;
    private EventModerationAction stateAction;
    @NotBlank(groups = OnCreate.class, message = "Field: title. Error: must not be blank or null.")
    @Size(min = 3, max = 120, message = "Field: title. Error: must be minimum 3 and maximum 120 characters.")
    private String title;

    public static void checkEventDate(LocalDateTime eventDate) {
        if (eventDate == null) {
            return;
        }
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new IncorrectRequestException("Field: event date. Error: должно содержать дату и время, " +
                    "которые наступят минимум через 2 часа от текущего момента. Value: " + eventDate);
        }
    }
}
