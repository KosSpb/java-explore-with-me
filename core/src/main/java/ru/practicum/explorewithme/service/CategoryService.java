package ru.practicum.explorewithme.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.dao.CategoryRepository;
import ru.practicum.explorewithme.dao.EventRepository;
import ru.practicum.explorewithme.dto.request.CategoryRequestDto;
import ru.practicum.explorewithme.dto.response.CategoryResponseDto;
import ru.practicum.explorewithme.exception.ConditionsNotMetException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.mapper.CategoryMapper;
import ru.practicum.explorewithme.model.Category;
import ru.practicum.explorewithme.model.Event;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository,
                           EventRepository eventRepository,
                           CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.eventRepository = eventRepository;
        this.categoryMapper = categoryMapper;
    }

    public CategoryResponseDto createCategoryByAdmin(CategoryRequestDto categoryRequestDto) {

        return categoryMapper.categoryToDto(categoryRepository.save(categoryMapper.dtoToCategory(categoryRequestDto)));
    }

    public void deleteCategoryByAdmin(long catId) {

        Category category = categoryRepository.findById(catId).orElseThrow(() -> {
            throw new NotFoundException("deletion of category: Category with id=" + catId + " was not found");
        });

        List<Event> eventsWithRequestedCategory = eventRepository.findByCategory(category);
        if (!eventsWithRequestedCategory.isEmpty()) {
            throw new ConditionsNotMetException("deletion of category: The category is not empty");
        }

        categoryRepository.deleteById(catId);
    }

    public CategoryResponseDto updateCategoryByAdmin(CategoryRequestDto categoryRequestDto, long catId) {

        Category categoryToUpdate = categoryRepository.findById(catId).orElseThrow(() -> {
            throw new NotFoundException("update of category: Category with id=" + catId + " was not found");
        });

        categoryToUpdate.setName(categoryRequestDto.getName());
        return categoryMapper.categoryToDto(categoryRepository.save(categoryToUpdate));
    }

    public Collection<CategoryResponseDto> getAllEventCategories(int from, int size) {

        Pageable pageRequest = PageRequest.of(from > 0 ? from / size : 0, size, Sort.by("id").ascending());
        List<Category> requestedCategories = categoryRepository.findAll(pageRequest).getContent();

        return requestedCategories.stream()
                .map(categoryMapper::categoryToDto)
                .collect(Collectors.toUnmodifiableList());
    }

    public CategoryResponseDto getEventCategoryById(long catId) {

        Category category = categoryRepository.findById(catId).orElseThrow(() -> {
            throw new NotFoundException("get category by id: Category with id=" + catId + " was not found");
        });

        return categoryMapper.categoryToDto(category);
    }
}
