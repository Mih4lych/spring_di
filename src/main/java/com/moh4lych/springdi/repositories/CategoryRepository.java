package com.moh4lych.springdi.repositories;

import com.moh4lych.springdi.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
}
