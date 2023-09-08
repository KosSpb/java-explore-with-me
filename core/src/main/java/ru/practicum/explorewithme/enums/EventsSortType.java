package ru.practicum.explorewithme.enums;

import ru.practicum.explorewithme.exception.NotFoundException;

import java.util.Arrays;

public enum EventsSortType {
    EVENT_DATE,
    VIEWS;

    public static EventsSortType findByType(String sortType) {
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(sortType))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Unknown sort type: " + sortType));
    }
}
