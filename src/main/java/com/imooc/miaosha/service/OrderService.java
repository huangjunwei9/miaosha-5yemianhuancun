package com.imooc.miaosha.service;

import com.imooc.miaosha.dao.OrderDao;
import com.imooc.miaosha.domain.MiaoshaOrder;
import com.imooc.miaosha.domain.MiaoshaUser;
import com.imooc.miaosha.domain.OrderInfo;
import com.imooc.miaosha.redis.OrderKey;
import com.imooc.miaosha.redis.RedisService;
import com.imooc.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


@Service
public class OrderService {

    @Autowired
    RedisService redisService;

    @Autowired
    OrderDao orderDao;

    //根据用户ID和商品Id，从缓存中获取秒杀订单
    public MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(long userId, long goodsId) {
//        return orderDao.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
        return redisService.get(OrderKey.getMiaoshaOrderByUidGid,"" + userId + "_" + goodsId, MiaoshaOrder.class);
    }

    //根据订单id 获取订单详情
    public OrderInfo getOrderById(long orderId) {
        return orderDao.getOrderById(orderId);
    }


    //生成订单和放入redis缓存
    @Transactional
    public OrderInfo createOrder(MiaoshaUser miaoshaUser, GoodsVo goodsVo) {
        //生成订单，并设置订单信息
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date()); //设置订单生成时间为系统当前时间
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);//假设下单的商品数量为1个
        orderInfo.setGoodsId(goodsVo.getId());


        orderInfo.setGoodsName(goodsVo.getGoodsName());
        orderInfo.setGoodsPrice(goodsVo.getMiaoshaPrice());
        orderInfo.setOrderChannel(1);//假设客户是用电脑下单的
        orderInfo.setStatus(0); //设置订单状态为未支付状态
        orderInfo.setUserId(miaoshaUser.getId());

        //把订单插入到订单表中，并返回插入的订单id，用于设置秒杀订单的订单id
        long orderId = orderDao.insert(orderInfo);

        //把订单插入到秒杀订单表中
        MiaoshaOrder miaoshaOrder = new MiaoshaOrder();
        miaoshaOrder.setGoodsId(goodsVo.getId());
        miaoshaOrder.setOrderId(orderId);
        miaoshaOrder.setUserId(miaoshaUser.getId());
        orderDao.insertMiaoshaOrder(miaoshaOrder);

        //把订单写入缓存
        redisService.set(OrderKey.getMiaoshaOrderByUidGid,"" + miaoshaUser.getId() + "_" + goodsVo.getId(), miaoshaOrder);

        return orderInfo;

    }


}












