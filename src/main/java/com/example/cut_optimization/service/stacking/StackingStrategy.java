package com.example.cut_optimization.service.stacking;

import com.example.cut_optimization.dto.InitialDataOptimization;

public interface StackingStrategy {
    void stack(InitialDataOptimization initialDataOptimization);
}
