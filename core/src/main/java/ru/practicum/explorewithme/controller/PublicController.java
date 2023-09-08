package ru.practicum.explorewithme.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.dto.response.CategoryResponseDto;
import ru.practicum.explorewithme.dto.response.CompilationResponseDto;
import ru.practicum.explorewithme.dto.response.EventFullInfoResponseDto;
import ru.practicum.explorewithme.dto.response.EventResponseDto;
import ru.practicum.explorewithme.enums.EventsSortType;
import ru.practicum.explorewithme.service.CategoryService;
import ru.practicum.explorewithme.service.CompilationService;
import ru.practicum.explorewithme.service.EventService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RestController
public class PublicController {
    private final CompilationService compilationService;
    private final CategoryService categoryService;
    private final EventService eventService;
    private final String dateTimePattern = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    public PublicController(CompilationService compilationService,
                            CategoryService categoryService,
                            EventService eventService) {
        this.compilationService = compilationService;
        this.categoryService = categoryService;
        this.eventService = eventService;
    }

    @GetMapping("/compilations")
    public Collection<CompilationResponseDto> getAllEventsCompilations(
            @RequestParam(value = "pinned", required = false) Boolean pinned,
            @RequestParam(value = "from", defaultValue = "0") int from,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        return compilationService.getAllEventsCompilations(pinned, from, size);
    }

    @GetMapping("/compilations/{compId}")
    public CompilationResponseDto getEventsCompilationById(@PathVariable(value = "compId") long compId) {

        return compilationService.getEventsCompilationById(compId);
    }

    @GetMapping("/categories")
    public Collection<CategoryResponseDto> getAllEventCategories(
            @RequestParam(value = "from", defaultValue = "0") int from,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        return categoryService.getAllEventCategories(from, size);
    }

    @GetMapping("/categories/{catId}")
    public CategoryResponseDto getEventCategoryById(@PathVariable(value = "catId") long catId) {

        return categoryService.getEventCategoryById(catId);
    }

    @GetMapping("/events")
    public Collection<EventResponseDto> getAllPublishedEvents(
            @RequestParam(value = "text", required = false) String searchText,
            @RequestParam(value = "categories", required = false) List<Long> categoryIds,
            @RequestParam(value = "paid", required = false) Boolean paid,
            @RequestParam(value = "rangeStart", required = false)
            @DateTimeFormat(pattern = dateTimePattern) LocalDateTime rangeStart,
            @RequestParam(value = "rangeEnd", required = false)
            @DateTimeFormat(pattern = dateTimePattern) LocalDateTime rangeEnd,
            @RequestParam(value = "onlyAvailable", defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(value = "sort", defaultValue = "EVENT_DATE") String sortType,
            @RequestParam(value = "from", defaultValue = "0") int from,
            @RequestParam(value = "size", defaultValue = "10") int size,
            HttpServletRequest request) {

        EventsSortType eventsSortType = EventsSortType.findByType(sortType);
        return eventService.getAllPublishedEvents(searchText, categoryIds, paid, rangeStart, rangeEnd,
                onlyAvailable, eventsSortType, from, size, request);
    }

    @GetMapping("/events/{id}")
    public EventFullInfoResponseDto getFullInfoAboutPublishedEventById(@PathVariable(value = "id") long eventId,
                                                                       HttpServletRequest request) {

        return eventService.getFullInfoAboutPublishedEventById(eventId, request);
    }
}
