package com.seffafbagis.api.service.category;

import com.seffafbagis.api.dto.mapper.CategoryMapper;
import com.seffafbagis.api.dto.request.category.CreateCategoryRequest;
import com.seffafbagis.api.dto.response.category.CategoryResponse;
import com.seffafbagis.api.dto.response.category.CategoryTreeResponse;
import com.seffafbagis.api.entity.category.Category;
import com.seffafbagis.api.exception.BadRequestException;
import com.seffafbagis.api.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Spy
    private CategoryMapper categoryMapper = new CategoryMapper();

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private CreateCategoryRequest createRequest;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Education");
        category.setSlug("education");
        category.setActive(true);

        createRequest = new CreateCategoryRequest();
        createRequest.setName("Education");
    }

    @Test
    void getAllActiveCategories_ShouldReturnActiveCategories() {
        when(categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc())
                .thenReturn(Collections.singletonList(category));

        List<CategoryResponse> result = categoryService.getAllActiveCategories();

        assertFalse(result.isEmpty());
        assertEquals(category.getName(), result.get(0).getName());
        verify(categoryRepository).findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    @Test
    void getCategoryTree_ShouldReturnTreeStructure() {
        when(categoryRepository.findByParentIdIsNullAndIsActiveTrueOrderByDisplayOrderAsc())
                .thenReturn(Collections.singletonList(category));

        List<CategoryTreeResponse> result = categoryService.getCategoryTree();

        assertFalse(result.isEmpty());
        assertEquals(category.getName(), result.get(0).getName());
        verify(categoryRepository).findByParentIdIsNullAndIsActiveTrueOrderByDisplayOrderAsc();
    }

    @Test
    void createCategory_ShouldGenerateSlugAndSave() {
        when(categoryRepository.existsBySlug(any())).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category c = invocation.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        CategoryResponse result = categoryService.createCategory(createRequest);

        assertNotNull(result);
        assertEquals("education", result.getSlug());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void deactivateCategory_WithActiveChildren_ShouldThrowException() {
        UUID categoryId = UUID.randomUUID();
        Category parent = new Category();
        parent.setId(categoryId);
        parent.setActive(true);

        Category child = new Category();
        child.setActive(true);
        parent.setChildren(Collections.singletonList(child));

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(parent));

        assertThrows(BadRequestException.class, () -> categoryService.deactivateCategory(categoryId));
        verify(categoryRepository, never()).save(any());
    }
}
