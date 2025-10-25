package com.example.cut_optimization.service.temperatureLowStrategy;

import org.springframework.stereotype.Component;

@Component
public class ModerateTemperatureLowStrategy implements TemperatureLowStrategy{

    private final double DECREASE_FACTOR = 0.15;

    @Override
    public double lowTemperature(int initialTemperature, int counter) {
        return initialTemperature * DECREASE_FACTOR / counter;
    }

    @Override
    public int getMaxCounter(int initialTemperature, double minimumTemperature) {
        return (int) (initialTemperature * DECREASE_FACTOR / minimumTemperature);
    }

}
