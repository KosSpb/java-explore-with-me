package ru.practicum.explorewithme.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import ru.practicum.explorewithme.dto.request.UserRequestDto;
import ru.practicum.explorewithme.dto.response.UserResponseDto;
import ru.practicum.explorewithme.model.User;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface UserMapper {

    User dtoToUser(UserRequestDto userRequestDto);

    UserResponseDto userToDto(User user);

}
