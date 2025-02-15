package com.example.redis_mysql_web.util;

import com.example.redis_mysql_web.mapper.GradeMapper;
import com.example.redis_mysql_web.pojo.Grade;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Grade 布隆过滤器初始化工具类
 * 初始化：将 mysql中的 Grade 加入布隆过滤器
 * 布隆过滤器名为:GradeBloomFilter
 *
 **/
@Component
public class GradeBloomFilterUtil {
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    // @Autowired
    private GradeMapper gradeMapper;

    // 初始化UserBloomFilter,
    // 将mysql中User表中的合法数据加入布隆过滤器中
    // @PostConstruct注解用于标注在方法上，这个方法会在依赖注入完成后自动执行。
    // 它通常用于执行一些初始化操作，比如设置一些初始值、启动定时任务、初始化数据库连接等。
    @PostConstruct
    public void init(){
        //1.获取mysql中 user表的数据
        List<Grade> gradeList= gradeMapper.getAllGrade();
        //2.将每一个user加入 布隆过滤器
        for (Grade grade: gradeList){
            //2.1 构造key
            String key="grade:"+grade.getGrade_number();
            //2.2 计算key 的hash值
            int hashValue = Math.abs(key.hashCode());
            //2.3 对2的32次方，计算hash值的余数，获得索引值
            long index = (long)(hashValue % Math.pow(2, 32));
            System.out.println(key+":...对应...:"+index);
            //2.4 将index加入布隆过滤器中，将对应的位置设置为 1
            redisTemplate.opsForValue().setBit("GradeBloomFilter",index,true);

        }
    }

    //检查布隆过滤器中是否存在某个用户
    public boolean checkBloomFilter(String number){
        String key="grade:"+number;
        int hashValue = Math.abs(key.hashCode());
        //2.3 对2的32次方，计算hash值的余数，获得索引值
        long index = (long)(hashValue % Math.pow(2, 32));
        boolean existOK= redisTemplate.opsForValue().getBit("GradeBloomFilter",index);
        System.out.println(key+"是否存在: " + index +" : "+ existOK);
        return existOK;
    }
}
