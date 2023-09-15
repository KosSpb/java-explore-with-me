package ru.practicum.explorewithme.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explorewithme.dto.request.CommentRequestDto;
import ru.practicum.explorewithme.dto.response.CommentFullInfoResponseDto;
import ru.practicum.explorewithme.dto.response.CommentResponseDto;
import ru.practicum.explorewithme.model.Comment;
import ru.practicum.explorewithme.model.Event;
import ru.practicum.explorewithme.model.User;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", source = "eventEntity")
    @Mapping(target = "author", source = "authorEntity")
    Comment dtoToComment(CommentRequestDto commentRequestDto, Event eventEntity, User authorEntity);

    @Mapping(target = "authorName", source = "author.name")
    CommentResponseDto commentToShortDto(Comment comment);

    CommentFullInfoResponseDto commentToFullDto(Comment comment);

}
