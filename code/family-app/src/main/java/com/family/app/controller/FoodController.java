package com.family.app.controller;

import com.family.app.common.ApiResponse;
import com.family.app.dto.FoodRequest;
import com.family.app.entity.Food;
import com.family.app.service.FoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/food")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    @GetMapping("/random")
    public ApiResponse<Food> random() {
        return foodService.getRandomFood()
                .map(ApiResponse::success)
                .orElse(ApiResponse.failure(404, "暂无可用菜品"));
    }

    @PostMapping
    public ApiResponse<Food> addFood(@RequestBody FoodRequest request) {
        if (!StringUtils.hasText(request.getName()) || !StringUtils.hasText(request.getType())) {
            return ApiResponse.failure(400, "菜品名称和类型不能为空");
        }
        Food created = foodService.addFood(request.getName(), request.getType(), request.getDescription());
        return ApiResponse.success("新增菜品成功", created);
    }

    @GetMapping("/listAll")
    public ApiResponse<List<Food>> listAll() {
        List<Food> foods = foodService.listAllValidFoods();
        return ApiResponse.success(foods);
    }

    @GetMapping("/city-recommend")
    public ApiResponse<Map<String, Object>> cityRecommend(@RequestParam double latitude,
                                                          @RequestParam double longitude) {
        return ApiResponse.success(foodService.recommendCityFoodsByLocation(latitude, longitude));
    }
}

