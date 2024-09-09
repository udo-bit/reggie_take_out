package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final DishService dishService;
    private final SetmealService setmealService;

    public CategoryServiceImpl(DishService dishService, SetmealService setmealService) {
        this.dishService = dishService;
        this.setmealService = setmealService;
    }

    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> dishQw = new LambdaQueryWrapper<>();
        dishQw.eq(Dish::getCategoryId, id);
        long dishCount = dishService.count(dishQw);
        if (dishCount > 0) {
            throw new CustomException("该分类下有菜品，无法删除");
        }

        LambdaQueryWrapper<Setmeal> setQw = new LambdaQueryWrapper<>();
        setQw.eq(Setmeal::getCategoryId, id);
        long setCount = setmealService.count(setQw);
        if (setCount > 0) {
            throw new CustomException("该分类下有套餐，无法删除");
        }

//        super.removeById(id);
        this.removeById(id);


    }
}
