package ru.practicum.explorewithme.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.request.*;
import ru.practicum.explorewithme.dto.response.*;
import ru.practicum.explorewithme.service.*;
import ru.practicum.explorewithme.validation.OnCreate;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/admin")
@Validated
public class AdminController {
    private final CategoryService categoryService;
    private final EventService eventService;
    private final UserService userService;
    private final CompilationService compilationService;
    private final CommentService commentService;
    private final String dateTimePattern = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    public AdminController(CategoryService categoryService,
                           EventService eventService,
                           UserService userService,
                           CompilationService compilationService,
                           CommentService commentService) {
        this.categoryService = categoryService;
        this.eventService = eventService;
        this.userService = userService;
        this.compilationService = compilationService;
        this.commentService = commentService;
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDto createCategoryByAdmin(@RequestBody @Valid CategoryRequestDto categoryRequestDto) {

        CategoryResponseDto createdCategory = categoryService.createCategoryByAdmin(categoryRequestDto);
        log.info("createCategoryByAdmin - request for category \"{}\" creation by admin was processed.",
                categoryRequestDto.getName());
        return createdCategory;
    }

    @DeleteMapping("/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategoryByAdmin(@PathVariable(value = "catId") @Positive long catId) {

        categoryService.deleteCategoryByAdmin(catId);
        log.info("deleteCategoryByAdmin - request for deletion of category with id {} by admin was processed.", catId);
    }

    @PatchMapping("/categories/{catId}")
    public CategoryResponseDto updateCategoryByAdmin(@RequestBody @Valid CategoryRequestDto categoryRequestDto,
                                                     @PathVariable(value = "catId") @Positive long catId) {

        CategoryResponseDto updatedCategory = categoryService.updateCategoryByAdmin(categoryRequestDto, catId);
        log.info("updateCategoryByAdmin - request for update of category with id {} to \"{}\" by admin was processed.",
                catId, categoryRequestDto.getName());
        return updatedCategory;
    }

    @GetMapping("/events")
    public Collection<EventFullInfoResponseDto> getFullInfoAboutAllEventsByAdmin(
            @RequestParam(value = "users", required = false) List<Long> userIds,
            @RequestParam(value = "states", required = false) List<String> statesOfEvent,
            @RequestParam(value = "categories", required = false) List<Long> categoryIds,
            @RequestParam(value = "rangeStart", required = false)
            @DateTimeFormat(pattern = dateTimePattern) LocalDateTime rangeStart,
            @RequestParam(value = "rangeEnd", required = false)
            @DateTimeFormat(pattern = dateTimePattern) LocalDateTime rangeEnd,
            @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(value = "size", defaultValue = "10") @Positive int size) {

        return eventService.getFullInfoAboutAllEventsByAdmin(
                userIds, statesOfEvent, categoryIds, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/events/{eventId}")
    public EventFullInfoResponseDto updateEventAndPublicationStatusEditByAdmin(
            @RequestBody @Valid EventRequestDto eventRequestDto,
            @PathVariable(value = "eventId") @Positive long eventId) {

        EventRequestDto.checkEventDate(eventRequestDto.getEventDate());
        EventFullInfoResponseDto updatedEvent =
                eventService.updateEventAndPublicationStatusEditByAdmin(eventRequestDto, eventId);
        log.info("updateEventAndPublicationStatusEditByAdmin - request for update of event with id {} to \"{}\" " +
                "by admin was processed.", eventId, eventRequestDto);
        return updatedEvent;
    }

    @GetMapping("/users")
    public Collection<UserFullInfoResponseDto> getRequiredUsersByAdmin(
            @RequestParam(value = "ids", required = false) List<Long> userIds,
            @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(value = "size", defaultValue = "10") @Positive int size) {

        return userService.getRequiredUsersByAdmin(userIds, from, size);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserFullInfoResponseDto createUserByAdmin(@RequestBody @Valid UserRequestDto userRequestDto) {

        UserFullInfoResponseDto createdUser = userService.createUserByAdmin(userRequestDto);
        log.info("createUserByAdmin - request for user [\"{}\"] creation by admin was processed.", userRequestDto);
        return createdUser;
    }

    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserByAdmin(@PathVariable(value = "userId") @Positive long userId) {

        userService.deleteUserByAdmin(userId);
        log.info("deleteUserByAdmin - request for deletion of user with id {} by admin was processed.", userId);
    }

    @PostMapping("/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    @Validated(OnCreate.class)
    public CompilationResponseDto createCompilationByAdmin(
            @RequestBody @Valid CompilationRequestDto compilationRequestDto) {

        CompilationResponseDto createdCompilation = compilationService.createCompilationByAdmin(compilationRequestDto);
        log.info("createCompilationByAdmin - request for compilation [\"{}\"] creation by admin was processed.",
                compilationRequestDto);
        return createdCompilation;
    }

    @DeleteMapping("/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilationByAdmin(@PathVariable(value = "compId") @Positive long compId) {

        compilationService.deleteCompilationByAdmin(compId);
        log.info("deleteCompilationByAdmin - request for deletion of compilation with id {} by admin was processed.",
                compId);
    }

    @PatchMapping("/compilations/{compId}")
    public CompilationResponseDto updateCompilationByAdmin(
            @RequestBody @Valid CompilationRequestDto compilationRequestDto,
            @PathVariable(value = "compId") @Positive long compId) {

        CompilationResponseDto updatedCompilation =
                compilationService.updateCompilationByAdmin(compilationRequestDto, compId);
        log.info("updateCompilationByAdmin - request for update of compilation with id {} to [\"{}\"] " +
                "by admin was processed.", compId, compilationRequestDto);
        return updatedCompilation;
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByAdmin(@PathVariable(value = "commentId") @Positive long commentId) {

        commentService.deleteCommentByAdmin(commentId);
        log.info("deleteCommentByAdmin - request for deletion of comment with id {} by admin was processed.",
                commentId);
    }

    @PatchMapping("/comments/{commentId}")
    public CommentFullInfoResponseDto updateCommentByAdmin(
            @RequestBody @Valid CommentRequestDto commentRequestDto,
            @PathVariable(value = "commentId") @Positive long commentId) {

        CommentFullInfoResponseDto updatedComment = commentService.updateCommentByAdmin(commentRequestDto, commentId);
        log.info("updateCommentByAdmin - request for update of comment with id {} to [\"{}\"] " +
                "by admin was processed.", commentId, commentRequestDto);
        return updatedComment;
    }

    @GetMapping("/comments")
    public Collection<CommentFullInfoResponseDto> getFullInfoAboutAllCommentsByAdmin(
            @RequestParam(value = "text", required = false) String searchText,
            @RequestParam(value = "authors", required = false) List<Long> userIds,
            @RequestParam(value = "events", required = false) List<Long> eventIds,
            @RequestParam(value = "onlyEdited", defaultValue = "false") Boolean onlyEdited,
            @RequestParam(value = "rangeStart", required = false)
            @DateTimeFormat(pattern = dateTimePattern) LocalDateTime rangeStart,
            @RequestParam(value = "rangeEnd", required = false)
            @DateTimeFormat(pattern = dateTimePattern) LocalDateTime rangeEnd,
            @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(value = "size", defaultValue = "10") @Positive int size) {

        return commentService.getFullInfoAboutAllCommentsByAdmin(
                searchText, userIds, eventIds, onlyEdited, rangeStart, rangeEnd, from, size);
    }
}
