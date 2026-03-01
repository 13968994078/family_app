package com.family.app.service;

import com.family.app.entity.Food;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FoodService {

    Optional<Food> getRandomFood();

    Food addFood(String name, String type, String description);

    List<Food> listAllValidFoods();

    Map<String, Object> recommendCityFoodsByLocation(double latitude, double longitude);
}

