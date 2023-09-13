package ru.practicum.explorewithme.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.dao.CommentRepository;
import ru.practicum.explorewithme.dao.EventRepository;
import ru.practicum.explorewithme.dao.UserRepository;
import ru.practicum.explorewithme.dto.request.CommentRequestDto;
import ru.practicum.explorewithme.dto.response.CommentFullInfoResponseDto;
import ru.practicum.explorewithme.enums.EventModerationState;
import ru.practicum.explorewithme.exception.ConditionsNotMetException;
import ru.practicum.explorewithme.exception.IncorrectRequestException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.mapper.CommentMapper;
import ru.practicum.explorewithme.model.Comment;
import ru.practicum.explorewithme.model.Event;
import ru.practicum.explorewithme.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;

    @Autowired
    public CommentService(CommentRepository commentRepository,
                          UserRepository userRepository,
                          EventRepository eventRepository,
                          CommentMapper commentMapper) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.commentMapper = commentMapper;
    }

    public Collection<CommentFullInfoResponseDto> getFullInfoAboutAllCommentsByAdmin(String searchText,
                                                                                     List<Long> userIds,
                                                                                     List<Long> eventIds,
                                                                                     Boolean onlyEdited,
                                                                                     LocalDateTime rangeStart,
                                                                                     LocalDateTime rangeEnd,
                                                                                     int from, int size) {

        Pageable pageRequest = PageRequest.of(from > 0 ? from / size : 0, size);

        List<Comment> requestedComments =
                commentRepository.findAllCommentsByParametersForAdmin(
                        searchText, userIds, eventIds, onlyEdited, rangeStart, rangeEnd, pageRequest).getContent();

        return requestedComments.stream()
                .map(commentMapper::commentToFullDto)
                .peek(commentFullInfoResponseDto -> commentFullInfoResponseDto.setIsAuthorInitiatorOfEvent(
                        commentFullInfoResponseDto.getAuthor().getId()
                                .equals(commentFullInfoResponseDto.getEvent().getInitiator().getId())))
                .collect(Collectors.toUnmodifiableList());
    }

    public CommentFullInfoResponseDto createCommentByUser(CommentRequestDto commentRequestDto,
                                                          long userId, long eventId) {

        User author = userRepository.findById(userId).orElseThrow(() -> {
            throw new NotFoundException("create comment by user: User with id=" + userId + " was not found");
        });
        Event eventToComment = eventRepository.findById(eventId).orElseThrow(() -> {
            throw new NotFoundException("create comment by user: Event with id=" + eventId + " was not found");
        });

        if (eventToComment.getState() != EventModerationState.PUBLISHED) {
            throw new ConditionsNotMetException("create comment by user: Cannot comment on an unpublished event");
        }

        CommentFullInfoResponseDto createdCommentDto =
                commentMapper.commentToFullDto(
                        commentRepository.save(commentMapper.dtoToComment(commentRequestDto, eventToComment, author)));

        createdCommentDto.setIsAuthorInitiatorOfEvent(author.getId().equals(eventToComment.getInitiator().getId()));

        return createdCommentDto;
    }

    public CommentFullInfoResponseDto updateCommentByUser(CommentRequestDto commentRequestDto,
                                                          long userId, long commentId) {

        User author = userRepository.findById(userId).orElseThrow(() -> {
            throw new NotFoundException("update comment by user: User with id=" + userId + " was not found");
        });
        Comment commentToUpdate = commentRepository.findById(commentId).orElseThrow(() -> {
            throw new NotFoundException("update comment by user: Comment with id=" + commentId + " was not found");
        });

        if (!author.getId().equals(commentToUpdate.getAuthor().getId())) {
            throw new IncorrectRequestException("update comment by user: " +
                    "It is forbidden to edit other user's comments");
        }

        if (LocalDateTime.now().isAfter(commentToUpdate.getCreated().plusHours(1))) {
            throw new ConditionsNotMetException("update comment by user: Cannot edit a comment " +
                    "if more than an hour has passed since it was posted. Comment was posted at: " +
                    commentToUpdate.getCreated());
        }

        commentToUpdate.setText(commentRequestDto.getText());
        if (!commentToUpdate.getIsEdited()) {
            commentToUpdate.setIsEdited(true);
        }

        CommentFullInfoResponseDto updatedCommentDto =
                commentMapper.commentToFullDto(commentRepository.save(commentToUpdate));

        updatedCommentDto.setIsAuthorInitiatorOfEvent(
                author.getId().equals(commentToUpdate.getEvent().getInitiator().getId()));

        return updatedCommentDto;
    }

    public void deleteCommentByUser(long userId, long commentId) {

        User author = userRepository.findById(userId).orElseThrow(() -> {
            throw new NotFoundException("deletion of comment by user: User with id=" + userId + " was not found");
        });
        Comment commentToDelete = commentRepository.findById(commentId).orElseThrow(() -> {
            throw new NotFoundException("deletion of comment by user: Comment with id=" + commentId + " was not found");
        });

        if (!author.getId().equals(commentToDelete.getAuthor().getId())) {
            throw new IncorrectRequestException("deletion of comment by user: " +
                    "It is forbidden to delete other user's comments");
        }

        commentRepository.deleteById(commentId);
    }

    public void deleteCommentByAdmin(long commentId) {

        commentRepository.findById(commentId).orElseThrow(() -> {
            throw new NotFoundException("deletion of comment by admin: Comment with id=" + commentId + " was not found");
        });
        commentRepository.deleteById(commentId);
    }
}
