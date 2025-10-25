package com.example.cut_optimization.service.stacking;

import com.example.cut_optimization.dto.ResultStacking;
import com.example.cut_optimization.optimizators.InitialDataOptimization;

public interface StackingStrategy {
    void stack(InitialDataOptimization initialDataOptimization, boolean isStackingDetailsIntoOneWorkpiece);
    void stack(InitialDataOptimization initialDataOptimization);
}
