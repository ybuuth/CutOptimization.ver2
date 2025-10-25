package com.example.cut_optimization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StackingCoefficients implements Cloneable {
    private double freeAreaRatio;
    private double freeAreaRatioByDetail;
    private double occupiedAreaRatio;
    private double occupiedAreaRatioByDetail;
    private double freeAreaRatioOfUsedWorkpieces;
    private double areaFillingFactor;
    private int freeAreaCount;

    @Override
    public StackingCoefficients clone() throws CloneNotSupportedException {
        try {
            return (StackingCoefficients) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
