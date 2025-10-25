package com.example.cut_optimization.service.stacking;

import com.example.cut_optimization.dto.TypeOfMaterial;

public interface StackingStrategy {
    void stack(TypeOfMaterial.InitialDataOptimization initialDataOptimization, boolean isStackingDetailsIntoOneWorkpiece);
    void stack(TypeOfMaterial.InitialDataOptimization initialDataOptimization);
}
