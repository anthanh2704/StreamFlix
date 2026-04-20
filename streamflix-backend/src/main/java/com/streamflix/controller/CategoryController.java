package com.streamflix.controller;

import com.streamflix.dto.ApiResponse;
import com.streamflix.entity.Category;
import com.streamflix.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public ApiResponse<List<Category>> all() {
        return ApiResponse.ok(categoryRepository.findAll());
    }
}
