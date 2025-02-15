package com.example.redisdemo.service;

import com.example.redisdemo.common.ApiResponse;
import com.example.redisdemo.config.RedisConstant;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.redisdemo.config.RedisConstant.*;


@Service
public class RedisService {

    private final RedissonClient redissonClient;

    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);

    @Autowired
    public RedisService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public <T> ApiResponse<List<T>> getQueryResult(String uuid) {
        String queryId = QUERY_ID_PREFIX.getValue() + uuid;
        if (!redissonClient.getBucket(queryId).isExists()) {
            return ApiResponse.error(500, "UUID不存在", Collections.emptyList());
        }

        String status = (String) redissonClient.getBucket(queryId).get();

        if (status.equals(QUERY_STATUS_PROCESSING.getValue())) {
            return ApiResponse.error(500, "UUID对应查询仍在进行中", Collections.emptyList());
        }

        String queryResult = QUERY_RESULT_PREFIX.getValue() + uuid;
        return ApiResponse.success(200, redissonClient.getList(queryResult));
    }

    /**
     * 存储字符串到 Redis
     */
    public <T> void set(String key, T value, long timeout, TimeUnit unit) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        bucket.set(value, timeout, unit);
    }

    /**
     * key - value 读取
     */
    public <T>T get(String key) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    /**
     * 存储 List<T> 到 Redis，确保 JSON 序列化
     */
    public <T> void setList(String key, List<T> value, long timeout, TimeUnit unit) {
        RList<T> rlist = redissonClient.getList(key, new JsonJacksonCodec());
        rlist.clear(); // 清空列表
        try {
            rlist.addAll(value); // 添加新数据
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        rlist.expire(timeout, unit); // 设置过期时间，使用传入参数
    }

    /**
     * 读取 List<T>，确保 JSON 反序列化
     */
    public <T> List<T> getList(String key, Class<T> clazz) {
        RList<T> rlist = redissonClient.getList(key, new JsonJacksonCodec());
        return rlist.readAll(); // 读取整个列表
    }

    /**
     * Atomic set（setNX）
     */
    public <T> boolean setNX(String key, T value, long timeout) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.setIfAbsent(value, Duration.ofSeconds(timeout));
    }

    /**
     *  delete
     */
    public <T> boolean delete(String key) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.delete();
    }
}
