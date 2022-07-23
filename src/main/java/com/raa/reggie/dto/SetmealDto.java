package com.raa.reggie.dto;

import com.raa.reggie.entity.Setmeal;
import com.raa.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
