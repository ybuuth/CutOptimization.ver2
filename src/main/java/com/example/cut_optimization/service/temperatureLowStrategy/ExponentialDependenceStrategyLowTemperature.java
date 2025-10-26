package com.example.cut_optimization.service.temperatureLowStrategy;

import org.springframework.stereotype.Service;

@Service("exponentialDependenceStrategyLowTemperature")
public class ExponentialDependenceStrategyLowTemperature implements TemperatureLowStrategy {

    private final double DECREASE_FACTOR = 0.5;

    @Override
    public double lowTemperature(int initialTemperature, int counter) {
        return initialTemperature * Math.pow(DECREASE_FACTOR, counter);
    }

    @Override
    public int getMaxCounter(int initialTemperature, double minimumTemperature) {
        return Integer.MAX_VALUE;
    }

    @Override
    public double getMinimumTemperature() {
        return 0;
    }
}
