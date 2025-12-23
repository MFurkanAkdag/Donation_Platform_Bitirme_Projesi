package com.seffafbagis.api.repository;

import com.seffafbagis.api.entity.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Category entity.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findByName(String name);

    Optional<Category> findBySlug(String slug);

    List<Category> findAllByIsActiveTrue();

    List<Category> findByIsActiveTrueOrderByDisplayOrderAsc();

    List<Category> findAllByParentIdIsNull();

    List<Category> findByParentIdIsNullAndIsActiveTrueOrderByDisplayOrderAsc();

    List<Category> findAllByParentId(UUID parentId);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);
}
