package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceimpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetMealDishMapper SetMealDishMapper;

    /**
     * 新增菜品和对应口味
     * @param dishDTO
     */
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //新增菜品一条数据
        dishMapper.insert(dish);

        //向口味表插入n条数据
        Long dishId = dish.getId();
       List<DishFlavor> flavors = dishDTO.getFlavors();
       if (flavors != null &&  flavors.size() > 0 ){
           flavors.forEach(dishFlavor ->
                   dishFlavor.setDishId(dishId));
           dishFlavorMapper.insertBatch(flavors);
       }

    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        //进行分页查询 使用pagehelper
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        //总记录数
        long total = page.getTotal();
        //当前页数据
        List<DishVO> record = page.getResult();
        return new PageResult(total, record);
    }


    /**
     * 菜品批量删除
     * @param ids
     */
    @Transactional
    @Override
    public void delete(List<Long> ids) {
        //判断菜品是否处于停售状态,否则不能删除
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //判断当前菜品是否关联了套餐，如果关联了就抛出业务异常
        List<Long> setmealId = SetMealDishMapper.getsetMealidBydishId(ids);
        if (setmealId != null && setmealId.size() >0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
//        //删除菜品表中的菜品信息
//        for (Long id : ids) {
//            dishMapper.deleteByid(id);
//            //删除口味表中的口味信息
//            dishFlavorMapper.deleteBydishid(id);
//        }
        //批量删除菜品表信息
        dishMapper.deleteByIds(ids);
        //批量删除口味表信息
        dishFlavorMapper.deleteBydishids(ids);



    }

    /**
     *  根据id查询菜品和对应的口味信息
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        //查询菜品数据
        Dish dish = dishMapper.getById(id);
        //查询口味数据
        List<DishFlavor> flavors = dishFlavorMapper.getBydishId(id);
        DishVO dishVO = DishVO.builder()
                .id(dish.getId())
                .categoryId(dish.getCategoryId())
                .name(dish.getName())
                .price(dish.getPrice())
                .image(dish.getImage())
                .description(dish.getDescription())
                .status(dish.getStatus())
                .flavors(flavors)
                .build();
        return  dishVO;
    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //修改菜品表信息
        dishMapper.update(dish);
        //删除口味信息
        dishFlavorMapper.deleteBydishid(dishDTO.getId());
        //重新添加口味信息
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0){
            flavors.forEach(dishFlavor ->
                    dishFlavor.setDishId(dishDTO.getId()));
            dishFlavorMapper.insertBatch(flavors);
        }
    }

}
