package com.example.cut_optimization.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StackingSequence implements Cloneable{
    private int workpieceId;
    private int detailId;
    private boolean rotated;

    @Override
    public StackingSequence clone() throws CloneNotSupportedException {
            return (StackingSequence) super.clone();
    }
}

