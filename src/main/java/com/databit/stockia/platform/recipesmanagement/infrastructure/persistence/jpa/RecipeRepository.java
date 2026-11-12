package com.databit.stockia.platform.recipesmanagement.infrastructure.persistence.jpa;

import com.databit.stockia.platform.recipesmanagement.infrastructure.persistence.jpa.entities.RecipeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("recipeJpaRepository")
public interface RecipeRepository extends JpaRepository<RecipeJpaEntity, Long> {
    List<RecipeJpaEntity> findAllByRestaurantIdOrderByIdDesc(Long restaurantId);
}
