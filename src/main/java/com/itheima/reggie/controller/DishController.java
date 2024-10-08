package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    private final DishService dishService;
    private final CategoryService categoryService;
    private final DishFlavorService dishFlavorService;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;


    public DishController(DishService dishService, CategoryService categoryService, DishFlavorService dishFlavorService) {
        this.dishFlavorService = dishFlavorService;
        this.dishService = dishService;
        this.categoryService = categoryService;
    }

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info("新增菜品:{}", dishDto);
        dishService.saveWithFlavor(dishDto);

        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);


        return R.success("新增菜品成功");
    }

    @GetMapping("/page")
    public R<Page<DishDto>> page(int page, int pageSize, String name) {
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(name != null, Dish::getName, name);
        wrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo, wrapper);

        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).toList();
        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info("更新菜品:{}", dishDto);
        Set<Object> keys = redisTemplate.keys("dish_*");
        assert keys != null;
        redisTemplate.delete(keys);
        dishService.updateWithFlavor(dishDto);
        return R.success("更新菜品成功");
    }

    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        List<DishDto> dishDtoList = null;
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        if (dishDtoList != null) {
            return R.success(dishDtoList);
        }
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        wrapper.eq(Dish::getStatus, 1);
        wrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(wrapper);
        dishDtoList = list.stream().map((item) -> {
            
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            Long id = item.getId();
            LambdaQueryWrapper<DishFlavor> flavorWrapper = new LambdaQueryWrapper<>();
            flavorWrapper.eq(DishFlavor::getDishId, id);
            List<DishFlavor> flavors = dishFlavorService.list(flavorWrapper);
            dishDto.setFlavors(flavors);

            return dishDto;
        }).toList();
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);


        return R.success(dishDtoList);
    }


}
