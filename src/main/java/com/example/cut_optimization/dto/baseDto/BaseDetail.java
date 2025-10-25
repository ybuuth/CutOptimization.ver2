package com.example.cut_optimization.dto.baseDto;

import com.example.cut_optimization.dto.TypeOfMaterial;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
public abstract class BaseDetail {
    @JsonProperty("height")
    private double height;
    @JsonProperty("width")
    private double width;
    @ToString.Exclude
    private int quantity;
    private double square;
    @ToString.Exclude
    private TypeOfMaterial typeMaterial;
    private boolean rotated;


    public BaseDetail(BaseDetailInfo baseDetailInfo) {
        if (baseDetailInfo != null) {
            this.height = baseDetailInfo.getHeight();
            this.width = baseDetailInfo.getWidth();
            this.quantity = baseDetailInfo.getQuantity();
            this.square = baseDetailInfo.calculateSquare() * baseDetailInfo.getQuantity();
            this.typeMaterial = baseDetailInfo.getTypeMaterial();
            this.rotated = baseDetailInfo.isRotated();
        }
    }

    public BaseDetail(double height, double width, TypeOfMaterial typeMaterial) {
        this.height = height;
        this.width = width;
        this.quantity = 1;
        this.square = height * width;
        this.typeMaterial = typeMaterial;
    }
}
