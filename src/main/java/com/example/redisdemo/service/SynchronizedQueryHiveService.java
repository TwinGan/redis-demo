package com.example.redisdemo.service;

import com.example.redisdemo.Entities.Customer;
import com.example.redisdemo.common.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import static com.example.redisdemo.config.RedisConstant.*;


@Service
public class SynchronizedQueryHiveService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private ThreadPoolTaskExecutor hiveQueryThreadPool;

    @Autowired
    private CustomerService customerService;


    private static final Logger logger = LoggerFactory.getLogger(SynchronizedQueryHiveService.class);

    public ApiResponse queryHiveService(String accountId, String startDate, String endDate) {

        String queryParam = QUERY_PARAMS_PREFIX.getValue().concat(accountId).concat(startDate).concat(endDate);

        String existUUID = redisService.get(queryParam);

        if (existUUID != null) {
            logger.info("已存在对应参数查询");
            return ApiResponse.success(200, existUUID);
        }

        final String uuid = UUID.randomUUID().toString();

        boolean isFirstRequest = redisService.setNX(queryParam, uuid, 600);
        if (!isFirstRequest) {
            logger.info("已存在对应参数查询");
            return ApiResponse.success(200, redisService.get(queryParam));
        }

        String queryId = QUERY_ID_PREFIX.getValue().concat(uuid);
        redisService.set(queryId, QUERY_STATUS_PROCESSING, 600, TimeUnit.SECONDS);

        try {
            CompletableFuture.supplyAsync(() -> {
                try {
                    /**
                     * 模拟查询hive的耗时
                     */
                    logger.info("准备查询hive");
                    Thread.sleep(1000);
                    List<Customer> list = customerService.getAllCustomers();
                    return list;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, hiveQueryThreadPool).thenAccept(list -> {
                logger.info("hive 查询完毕，结果写入redis");
                String queryResult = QUERY_RESULT_PREFIX.getValue().concat(uuid);
                redisService.setList(queryResult, list, 600, TimeUnit.SECONDS);
                redisService.set(queryId, QUERY_STATUS_FINISHED, 600, TimeUnit.SECONDS);
            });
        } catch (RejectedExecutionException e) {
            logger.info("查询线程池已满，请稍后再试");
            redisService.delete(queryId);
            redisService.delete(queryParam);
            return ApiResponse.error(500, "访问过于频繁，请稍后再试", null);
        }

        return ApiResponse.success(200, uuid);
    }
}
