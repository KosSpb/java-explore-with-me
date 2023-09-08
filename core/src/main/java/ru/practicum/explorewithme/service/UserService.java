package ru.practicum.explorewithme.service;

import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.dao.UserRepository;
import ru.practicum.explorewithme.dto.request.UserRequestDto;
import ru.practicum.explorewithme.dto.response.UserResponseDto;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.mapper.UserMapper;
import ru.practicum.explorewithme.model.User;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.userMapper = Mappers.getMapper(UserMapper.class);
    }

    public Collection<UserResponseDto> getRequiredUsersByAdmin(List<Long> userIds, int from, int size) {
        Pageable pageRequest = PageRequest.of(from > 0 ? from / size : 0, size);
        List<User> requiredUsers = userRepository.findRequiredUsers(userIds, pageRequest).getContent();

        return requiredUsers.stream()
                .map(userMapper::userToDto)
                .collect(Collectors.toUnmodifiableList());
    }

    public UserResponseDto createUserByAdmin(UserRequestDto userRequestDto) {
        return userMapper.userToDto(userRepository.save(userMapper.dtoToUser(userRequestDto)));
    }

    public void deleteUserByAdmin(long userId) {
        userRepository.findById(userId).orElseThrow(() -> {
            throw new NotFoundException("deletion of user: User with id=" + userId + " was not found");
        });
        userRepository.deleteById(userId);
    }
}
