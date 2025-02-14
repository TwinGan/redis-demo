package com.example.redisdemo.service;

import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    private final RedissonClient redissonClient;

    @Autowired
    public RedisService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
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
        RList<T> rlist = redissonClient.getList(key, new JsonJacksonCodec()); // 使用 JSON 序列化
        rlist.clear();
        rlist.addAll(value);
        rlist.expire(timeout, unit);
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
