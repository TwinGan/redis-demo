package com.example.redisdemo.config;

public enum RedisConstant {
    QUERY_PARAMS_PREFIX("query-param: "),
    QUERY_RESULT_PREFIX("query-result: "),
    QUERY_ID_PREFIX("query-id: "),
    QUERY_STATUS_PROCESSING("PROCESSING"),
    QUERY_STATUS_FINISHED("FINISHED");

    private final String value;

    RedisConstant(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
