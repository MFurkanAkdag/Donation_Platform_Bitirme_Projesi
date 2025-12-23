package com.seffafbagis.api.dto.mapper;

import com.seffafbagis.api.dto.request.category.CreateCategoryRequest;
import com.seffafbagis.api.dto.response.category.CategoryResponse;
import com.seffafbagis.api.dto.response.category.CategoryTreeResponse;
import com.seffafbagis.api.dto.response.category.DonationTypeResponse;
import com.seffafbagis.api.entity.category.Category;
import com.seffafbagis.api.entity.category.DonationType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {

    public Category toEntity(CreateCategoryRequest request) {
        if (request == null) {
            return null;
        }
        Category category = new Category();
        category.setName(request.getName());
        category.setNameEn(request.getNameEn());
        category.setDescription(request.getDescription());
        category.setIconName(request.getIconName());
        category.setColorCode(request.getColorCode());
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }
        return category;
    }

    public CategoryResponse toResponse(Category entity) {
        if (entity == null) {
            return null;
        }
        CategoryResponse response = new CategoryResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setNameEn(entity.getNameEn());
        response.setSlug(entity.getSlug());
        response.setDescription(entity.getDescription());
        response.setIconName(entity.getIconName());
        response.setColorCode(entity.getColorCode());
        response.setDisplayOrder(entity.getDisplayOrder());
        response.setActive(entity.getActive());
        if (entity.getParent() != null) {
            response.setParentId(entity.getParent().getId());
        }
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }

    public CategoryTreeResponse toTreeResponse(Category entity) {
        if (entity == null) {
            return null;
        }
        CategoryTreeResponse response = new CategoryTreeResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setNameEn(entity.getNameEn());
        response.setSlug(entity.getSlug());
        response.setDescription(entity.getDescription());
        response.setIconName(entity.getIconName());
        response.setColorCode(entity.getColorCode());
        response.setDisplayOrder(entity.getDisplayOrder());
        response.setActive(entity.getActive());
        if (entity.getParent() != null) {
            response.setParentId(entity.getParent().getId());
        }
        response.setCreatedAt(entity.getCreatedAt());

        if (entity.getChildren() != null && !entity.getChildren().isEmpty()) {
            response.setChildren(entity.getChildren().stream()
                    .map(this::toTreeResponse)
                    .collect(Collectors.toList()));
        }

        return response;
    }

    public DonationTypeResponse toResponse(DonationType entity) {
        if (entity == null) {
            return null;
        }
        DonationTypeResponse response = new DonationTypeResponse();
        response.setId(entity.getId());
        response.setTypeCode(entity.getTypeCode());
        response.setName(entity.getName());
        response.setNameEn(entity.getNameEn());
        response.setDescription(entity.getDescription());
        response.setRules(entity.getRules());
        response.setMinimumAmount(entity.getMinimumAmount());
        response.setActive(entity.getActive());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }

    public List<CategoryResponse> toResponseList(List<Category> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
