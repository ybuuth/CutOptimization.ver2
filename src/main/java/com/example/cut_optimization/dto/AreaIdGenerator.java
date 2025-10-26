package com.example.cut_optimization.dto;

import java.util.concurrent.atomic.AtomicInteger;


public class AreaIdGenerator {

    private final AtomicInteger areaIdCounter;
    private final AtomicInteger groupAreaIdCounter;

    public AreaIdGenerator() {
        this.areaIdCounter = new AtomicInteger(0);
        this.groupAreaIdCounter = new AtomicInteger(0);
    }

    public int nextAreaId() {
        return areaIdCounter.incrementAndGet();
    }
    public int nextGroupAreaId() {
        return groupAreaIdCounter.incrementAndGet();
    }
}
