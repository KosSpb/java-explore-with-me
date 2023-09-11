package ru.practicum.explorewithme.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.practicum.explorewithme.enums.RequestForEventStatus;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "requests_for_events")
public class RequestForEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime created;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @ToString.Exclude
    private Event event;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    @ToString.Exclude
    private User requester;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestForEventStatus status;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestForEvent requestForEvent = (RequestForEvent) o;
        return Objects.equals(id, requestForEvent.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
