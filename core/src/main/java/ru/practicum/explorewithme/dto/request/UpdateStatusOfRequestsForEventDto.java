package ru.practicum.explorewithme.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explorewithme.enums.RequestForEventStatus;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateStatusOfRequestsForEventDto {
    @NotNull(message = "Field: requestIds. Error: must not be null.")
    private List<Long> requestIds;
    @NotNull(message = "Field: status. Error: must not be null.")
    private RequestForEventStatus status;
}
