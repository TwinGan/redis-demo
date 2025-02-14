package com.example.redisdemo.service;

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

@Service
public class SynchronizedQueryHiveService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private ThreadPoolTaskExecutor hiveQueryThreadPool;

    private final static String QUERY_PARAMS_PREFIX = "query-param: ";

    private final static String QUERY_RESULT_PREFIX = "query-result: ";

    private final static String QUERY_ID_PREFIX = "query-id: ";

    private final static String QUERY_STATUS_PROCESSING = "PROCESSING";

    private final static String QUERY_STATUS_FINISHED = "FINISHED";

    private static final Logger logger = LoggerFactory.getLogger(SynchronizedQueryHiveService.class);

    public ApiResponse queryHiveService(String accountId, String startDate, String endDate) {

        String queryParam = QUERY_PARAMS_PREFIX.concat(accountId).concat(startDate).concat(endDate);

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

        String queryId = QUERY_ID_PREFIX.concat(uuid);
        redisService.set(queryId, QUERY_STATUS_PROCESSING, 600, TimeUnit.SECONDS);

        try {
            CompletableFuture.supplyAsync(() -> {
                try {
                    /**
                     * 模拟查询hive的耗时
                     */
                    logger.info("准备查询hive");
                    Thread.sleep(10000);
                    List<String> list = new ArrayList<>();
                    Random random = new Random();
                    for (int i = 0; i < 5; i++) {
                        char[] tmp = new char[10];
                        Arrays.fill(tmp, (char) ('A' + random.nextInt((i + 1) * 3)));
                        list.add(new String(tmp));
                    }
                    return list;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, hiveQueryThreadPool).thenAccept(list -> {
                logger.info("hive 查询完毕，结果写入redis");
                String queryResult = QUERY_RESULT_PREFIX.concat(uuid);
                redisService.setList(queryResult, list, 600, TimeUnit.SECONDS);
                redisService.set(queryId, QUERY_STATUS_FINISHED, 600, TimeUnit.SECONDS);
            });
        } catch (RejectedExecutionException e) {
            logger.info("查询线程池已满，请稍后再试");
            redisService.delete(queryId);
            redisService.delete(queryParam);
            return null;
        }

        return ApiResponse.success(200, uuid);
    }
}
