package com.family.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.family.app.entity.Food;
import com.family.app.repository.FoodRepository;
import com.family.app.service.FoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FoodServiceImpl implements FoodService {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int MAX_CITY_FOOD_COUNT = 12;
    private static final Map<String, List<String>> CITY_FOOD_FALLBACK = buildCityFoodFallback();

    private final FoodRepository foodRepository;

    @Override
    public Optional<Food> getRandomFood() {
        LambdaQueryWrapper<Food> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Food::getIsValid, true)
                .last("ORDER BY RAND() LIMIT 1");
        return Optional.ofNullable(foodRepository.selectOne(wrapper));
    }

    @Override
    public Food addFood(String name, String type, String description) {
        LocalDateTime now = LocalDateTime.now();
        Food food = new Food();
        food.setName(name);
        food.setType(type);
        food.setDescription(description);
        food.setIsValid(true);
        food.setCreateTime(now);
        food.setUpdateTime(now);
        foodRepository.insert(food);
        return food;
    }

    @Override
    public List<Food> listAllValidFoods() {
        LambdaQueryWrapper<Food> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Food::getIsValid, true)
                .orderByAsc(Food::getName);
        return foodRepository.selectList(wrapper);
    }

    @Override
    public Map<String, Object> recommendCityFoodsByLocation(double latitude, double longitude) {
        validateCoordinates(latitude, longitude);
        String city = resolveCityByGeo(latitude, longitude);

        LinkedHashSet<String> foods = new LinkedHashSet<>();
        int sourceCount = 0;

        List<String> baiduSuggest = searchFoodsFromBaiduSuggest(city);
        if (!baiduSuggest.isEmpty()) {
            foods.addAll(baiduSuggest);
            sourceCount++;
        }

        List<String> baiduSugrec = searchFoodsFromBaiduSugrec(city);
        if (!baiduSugrec.isEmpty()) {
            foods.addAll(baiduSugrec);
            sourceCount++;
        }

        List<String> localMatched = matchFoodPoolByCity(city);
        if (!localMatched.isEmpty()) {
            foods.addAll(localMatched);
            sourceCount++;
        }

        List<String> fallback = fallbackFoods(city);
        if (!fallback.isEmpty()) {
            foods.addAll(fallback);
        }

        List<String> finalFoods = foods.stream()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .limit(MAX_CITY_FOOD_COUNT)
                .collect(Collectors.toList());

        if (finalFoods.isEmpty()) {
            finalFoods = List.of("家常小炒", "鸡蛋面", "清炒时蔬");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("city", city);
        result.put("foods", finalFoods);
        result.put("source", sourceCount > 0 ? "online" : "fallback");
        return result;
    }

    private void validateCoordinates(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("经纬度参数不合法");
        }
    }

    private String resolveCityByGeo(double latitude, double longitude) {
        String url = "https://nominatim.openstreetmap.org/reverse?format=jsonv2&accept-language=zh-CN"
                + "&lat=" + latitude + "&lon=" + longitude;
        String body = httpGet(url, Map.of(
                "User-Agent", "family-app/1.0",
                "Accept", "application/json"
        ));

        try {
            JsonNode root = OBJECT_MAPPER.readTree(body);
            JsonNode address = root.path("address");
            String city = firstText(address, "city", "town", "county", "state");
            if (!StringUtils.hasText(city)) {
                throw new IllegalArgumentException("定位失败，未识别到城市");
            }
            return normalizeCity(city);
        } catch (Exception e) {
            throw new IllegalArgumentException("定位失败，无法解析城市");
        }
    }

    private String firstText(JsonNode node, String... fields) {
        for (String field : fields) {
            String value = node.path(field).asText();
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return "";
    }

    private String normalizeCity(String city) {
        String normalized = city.trim()
                .replace("特别行政区", "")
                .replace("自治州", "州")
                .replace("自治县", "县");
        return normalized;
    }

    private List<String> searchFoodsFromBaiduSuggest(String city) {
        try {
            String query = URLEncoder.encode(city + " 特色美食", StandardCharsets.UTF_8);
            String url = "https://suggestion.baidu.com/su?wd=" + query + "&json=1&p=3&ie=utf-8";
            String body = unwrapJson(httpGet(url, Map.of("User-Agent", "family-app/1.0")));
            JsonNode root = OBJECT_MAPPER.readTree(body);
            List<String> foods = new ArrayList<>();
            for (JsonNode item : root.path("s")) {
                String parsed = parseFoodFromSearchKeyword(item.asText(), city);
                if (StringUtils.hasText(parsed)) {
                    foods.add(parsed);
                }
            }
            return foods;
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private List<String> searchFoodsFromBaiduSugrec(String city) {
        try {
            String query = URLEncoder.encode(city + " 美食", StandardCharsets.UTF_8);
            String url = "https://www.baidu.com/sugrec?prod=pc&wd=" + query + "&pre=1";
            String body = unwrapJson(httpGet(url, Map.of("User-Agent", "family-app/1.0")));
            JsonNode root = OBJECT_MAPPER.readTree(body);
            List<String> foods = new ArrayList<>();
            for (JsonNode item : root.path("g")) {
                String parsed = parseFoodFromSearchKeyword(item.path("q").asText(), city);
                if (StringUtils.hasText(parsed)) {
                    foods.add(parsed);
                }
            }
            return foods;
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private String parseFoodFromSearchKeyword(String raw, String city) {
        if (!StringUtils.hasText(raw)) {
            return "";
        }
        String text = raw.trim()
                .replace(city, "")
                .replace(normalizeCity(city), "")
                .replace("特色", "")
                .replace("必吃", "")
                .replace("推荐", "")
                .replace("排名", "")
                .replace("附近", "")
                .replace("攻略", "")
                .replace("美食", "")
                .replace("有哪些", "")
                .replace("是什么", "")
                .replace("哪里", "")
                .replace(" ", "");
        if (!StringUtils.hasText(text)) {
            return "";
        }
        if (text.length() > 12) {
            return "";
        }
        return text;
    }

    private List<String> matchFoodPoolByCity(String city) {
        String keyword = normalizeCity(city).replace("市", "");
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        return listAllValidFoods().stream()
                .filter(food -> containsIgnoreCase(food.getType(), keyword)
                        || containsIgnoreCase(food.getDescription(), keyword)
                        || containsIgnoreCase(food.getName(), keyword))
                .map(Food::getName)
                .limit(6)
                .collect(Collectors.toList());
    }

    private boolean containsIgnoreCase(String text, String keyword) {
        if (!StringUtils.hasText(text) || !StringUtils.hasText(keyword)) {
            return false;
        }
        return text.toLowerCase().contains(keyword.toLowerCase());
    }

    private List<String> fallbackFoods(String city) {
        String normalized = normalizeCity(city);
        String stripped = normalized.replace("市", "");
        for (Map.Entry<String, List<String>> entry : CITY_FOOD_FALLBACK.entrySet()) {
            String key = entry.getKey();
            if (normalized.contains(key) || key.contains(normalized)
                    || stripped.contains(key) || key.contains(stripped)) {
                return entry.getValue();
            }
        }
        return List.of("炒饭", "馄饨", "盖浇饭");
    }

    private String httpGet(String url, Map<String, String> headers) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(8))
                    .GET();
            headers.forEach(builder::header);
            HttpResponse<String> response = HTTP_CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("HTTP " + response.statusCode());
            }
            return response.body();
        } catch (Exception e) {
            throw new IllegalStateException("请求城市美食失败");
        }
    }

    private String unwrapJson(String raw) {
        String body = raw.trim();
        int firstBrace = body.indexOf('{');
        int lastBrace = body.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            return body.substring(firstBrace, lastBrace + 1);
        }
        return body;
    }

    private static Map<String, List<String>> buildCityFoodFallback() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        map.put("北京", List.of("北京烤鸭", "炸酱面", "卤煮火烧"));
        map.put("上海", List.of("生煎包", "本帮红烧肉", "排骨年糕"));
        map.put("广州", List.of("肠粉", "烧鹅", "云吞面"));
        map.put("深圳", List.of("沙井蚝", "肠粉", "椰子鸡"));
        map.put("成都", List.of("火锅", "麻婆豆腐", "钵钵鸡"));
        map.put("重庆", List.of("重庆火锅", "小面", "酸辣粉"));
        map.put("西安", List.of("肉夹馍", "凉皮", "羊肉泡馍"));
        map.put("武汉", List.of("热干面", "豆皮", "排骨藕汤"));
        map.put("长沙", List.of("臭豆腐", "口味虾", "剁椒鱼头"));
        map.put("杭州", List.of("西湖醋鱼", "东坡肉", "龙井虾仁"));
        map.put("南京", List.of("盐水鸭", "鸭血粉丝汤", "皮肚面"));
        map.put("天津", List.of("煎饼果子", "锅巴菜", "耳朵眼炸糕"));
        return map;
    }
}

