package com.seffafbagis.api.service.category;

import com.seffafbagis.api.dto.mapper.CategoryMapper;
import com.seffafbagis.api.dto.request.category.CreateCategoryRequest;
import com.seffafbagis.api.dto.request.category.UpdateCategoryRequest;
import com.seffafbagis.api.dto.response.category.CategoryResponse;
import com.seffafbagis.api.dto.response.category.CategoryTreeResponse;
import com.seffafbagis.api.entity.category.Category;
import com.seffafbagis.api.exception.BadRequestException;
import com.seffafbagis.api.exception.ResourceNotFoundException;
import com.seffafbagis.api.repository.CategoryRepository;
import com.seffafbagis.api.util.SlugGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllActiveCategories() {
        return categoryMapper.toResponseList(categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc());
    }

    @Transactional(readOnly = true)
    public List<CategoryTreeResponse> getCategoryTree() {
        // Only fetch root categories that are active
        List<Category> rootCategories = categoryRepository.findByParentIdIsNullAndIsActiveTrueOrderByDisplayOrderAsc();
        return rootCategories.stream()
                .map(categoryMapper::toTreeResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID id) {
        Category category = findCategoryById(id);
        return categoryMapper.toResponse(category);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category", slug));
        return categoryMapper.toResponse(category);
    }

    public CategoryResponse createCategory(CreateCategoryRequest request) {
        Category category = categoryMapper.toEntity(request);

        // Handle Parent
        if (request.getParentId() != null) {
            Category parent = findCategoryById(request.getParentId());
            category.setParent(parent);
        }

        // Generate Unique Slug
        String slug = SlugGenerator.generateUniqueSlug(
                request.getName(),
                categoryRepository::existsBySlug);
        category.setSlug(slug);

        category.setActive(true);
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toResponse(savedCategory);
    }

    public CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request) {
        Category category = findCategoryById(id);

        if (request.getName() != null) {
            category.setName(request.getName());
            // Update slug if name changes
            String newSlug = SlugGenerator.generateUniqueSlug(
                    request.getName(),
                    s -> categoryRepository.existsBySlugAndIdNot(s, id) // Helper method needed or logic check
            );
            // Note: The helper method existsBySlugAndIdNot is in the repo
            // Re-checking logic: generateUniqueSlug takes a simple Predicate<String>.
            // We need to wrap repository.existsBySlugAndIdNot(slug, id) into a
            // Function<String, Boolean>
            String currentSlug = category.getSlug();
            String baseSlug = SlugGenerator.generateSlug(request.getName());

            if (!baseSlug.equals(currentSlug) && !baseSlug.isEmpty()) {
                // Regenerate
                String uniqueSlug = SlugGenerator.generateUniqueSlug(
                        request.getName(),
                        s -> categoryRepository.existsBySlugAndIdNot(s, id));
                category.setSlug(uniqueSlug);
            }
        }

        if (request.getNameEn() != null)
            category.setNameEn(request.getNameEn());
        if (request.getDescription() != null)
            category.setDescription(request.getDescription());
        if (request.getIconName() != null)
            category.setIconName(request.getIconName());
        if (request.getColorCode() != null)
            category.setColorCode(request.getColorCode());
        if (request.getDisplayOrder() != null)
            category.setDisplayOrder(request.getDisplayOrder());

        if (request.getParentId() != null) {
            // Check for circular dependency if needed, but for simple parent update:
            if (request.getParentId().equals(id)) {
                throw new BadRequestException("Category cannot be its own parent");
            }
            Category parent = findCategoryById(request.getParentId());
            category.setParent(parent);
        }
        // If parentId is explicitly null in request (not handled by current DTO
        // structure which treats null as no update),
        // we might leave it. logic: DTO fields null means no update for patch-style.
        // If we wanted to remove parent, we'd need a way to signal that. Assuming null
        // means "don't change".

        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toResponse(savedCategory);
    }

    public void deactivateCategory(UUID id) {
        Category category = findCategoryById(id);

        // Check if has active children
        boolean hasActiveChildren = category.getChildren().stream().anyMatch(Category::getActive);
        if (hasActiveChildren) {
            throw new BadRequestException("Cannot deactivate category with active sub-categories");
        }

        category.setActive(false);
        categoryRepository.save(category);
    }

    private Category findCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id.toString()));
    }
}
