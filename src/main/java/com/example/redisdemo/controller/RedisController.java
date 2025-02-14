package com.example.redisdemo.controller;

import com.example.redisdemo.common.ApiResponse;
import com.example.redisdemo.service.RedisService;
import com.example.redisdemo.service.SynchronizedQueryHiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RedisController {

    @Autowired
    private RedisService redisService;

    @Autowired
    private SynchronizedQueryHiveService synchronizedQueryHiveService;

    @PostMapping("/interest-log/query")
    public ApiResponse<String> queryInterestLog(@RequestParam String accountId, @RequestParam String startDate, @RequestParam String endDate){
        return synchronizedQueryHiveService.queryHiveService(accountId, startDate, endDate);
    }

    @PostMapping("/interest-log/get")
    public ApiResponse<String> getInterestLog(@RequestParam String uuid){
        /**
         * 代实现
         */
        return null;
    }


}
