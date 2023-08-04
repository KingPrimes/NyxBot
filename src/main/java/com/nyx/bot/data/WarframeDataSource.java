package com.nyx.bot.data;


import com.nyx.bot.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WarframeDataSource  {


    public void init(){
        log.info("开始插入Warframe数据！");
        //获取代理类，异步执行方法
        WarframeDataSource bean = SpringUtils.getBean(WarframeDataSource.class);
        bean.getMarket();
    }


    @Async("myAsync")
    protected void getMarket(){
        log.info("开始获取数据！");
    }



}
