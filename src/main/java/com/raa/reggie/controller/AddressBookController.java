package com.raa.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.raa.reggie.common.BaseContext;
import com.raa.reggie.common.R;
import com.raa.reggie.entity.AddressBook;
import com.raa.reggie.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressService;

    @GetMapping("/list")
    public R<List<AddressBook>> list(){
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<AddressBook> wrapper = new LambdaQueryWrapper<AddressBook>()
                .eq(AddressBook::getUserId, userId);

        List<AddressBook> addressBooks = addressService.list(wrapper);
        return R.success(addressBooks);
    }

    @PostMapping
    public R<String> save(@RequestBody AddressBook addressBook){
        addressBook.setUserId(BaseContext.getCurrentId());
        addressService.save(addressBook);
        return R.success("添加地址成功");
    }

    @GetMapping("/{id}")
    public R<AddressBook> get(@PathVariable Long id){
        return R.success(addressService.getById(id));
    }

    @PutMapping
    public R<String> update(@RequestBody AddressBook addressBook){
        addressService.updateById(addressBook);
        return R.success("更新地址信息成功");
    }

    @DeleteMapping
    public R<String> reomve(@RequestParam List<Long> ids){
        addressService.removeByIds(ids);
        return R.success("设置默认地址成功");
    }

    @PutMapping("/default")
    public R<String> setDefault(@RequestBody AddressBook addressBook){
        addressBook = addressService.getById(addressBook.getId());
        //对于原来是默认地址的点击不会发请求取消默认地址，不判1
//        if(addressBook.getIsDefault() == 0) {
        addressBook.setIsDefault(1);
        LambdaQueryWrapper<AddressBook> wrapper = new LambdaQueryWrapper<AddressBook>()
                .eq(AddressBook::getUserId, addressBook.getUserId())
                .eq(AddressBook::getIsDefault, 1);
        AddressBook addressOld = addressService.getOne(wrapper);
        if(addressOld != null){
            addressOld.setIsDefault(0);
            addressService.updateById(addressOld);
        }
//        }
//        else
//            addressBook.setIsDefault(0);
        addressService.updateById(addressBook);
        return R.success("设置默认地址成功");
    }

    @GetMapping("/default")
    public R<AddressBook> getDefault(){
        LambdaQueryWrapper<AddressBook> wrapper = new LambdaQueryWrapper<AddressBook>()
                .eq(AddressBook::getUserId, BaseContext.getCurrentId())
                .eq(AddressBook::getIsDefault, 1);
        AddressBook one = addressService.getOne(wrapper);
        return R.success(one);
    }

}
