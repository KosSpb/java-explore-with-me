package ru.practicum.explorewithme.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import ru.practicum.explorewithme.dto.request.UserRequestDto;
import ru.practicum.explorewithme.dto.response.UserFullInfoResponseDto;
import ru.practicum.explorewithme.model.User;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface UserMapper {

    User dtoToUser(UserRequestDto userRequestDto);

    UserFullInfoResponseDto userToDto(User user);

}
