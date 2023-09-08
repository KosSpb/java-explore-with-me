package ru.practicum.explorewithme.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import ru.practicum.explorewithme.dto.request.CategoryRequestDto;
import ru.practicum.explorewithme.dto.response.CategoryResponseDto;
import ru.practicum.explorewithme.model.Category;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface CategoryMapper {

    Category dtoToCategory(CategoryRequestDto categoryRequestDto);

    CategoryResponseDto categoryToDto(Category category);

}
