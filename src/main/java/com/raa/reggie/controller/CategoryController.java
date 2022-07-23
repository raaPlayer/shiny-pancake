package com.raa.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.raa.reggie.common.R;
import com.raa.reggie.entity.Category;
import com.raa.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public R<String> save(@RequestBody Category category){
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    @GetMapping("/page")
    public R<Page<Category>> page(int page, int pageSize){
        Page<Category> pageInfo = new Page<>(page, pageSize);
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件，根据sort进行排序
        queryWrapper.orderByAsc(Category::getSort);
        //进行分野查下
        categoryService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    @DeleteMapping
    public R<String> delete(Long id){
        log.info("id: {}", id);
        //删除前需判断，自写一个remove
        categoryService.remove(id);
        return R.success("分类删除成功");
    }

    @PutMapping
    public R<String> updata(@RequestBody Category category){
        categoryService.updateById(category);
        return R.success("分类修改成功");
    }

    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // Select (Category::getName)name
//        queryWrapper.select(Category::getName);
        // Where (Category::getType)type == (category.getType())1
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        //  ORDER BY sort ASC,update_time DESC (Asc 升序 Desc 降序)
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        // From (table)category
        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }
}
