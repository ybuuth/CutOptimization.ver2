package com.example.cut_optimization.service.temperatureLowStrategy;

public interface TemperatureLowStrategy {
    double lowTemperature(int initialTemperature, int counter);
    int getMaxCounter(int initialTemperature, double minimumTemperature);
    double getMinimumTemperature();
}
