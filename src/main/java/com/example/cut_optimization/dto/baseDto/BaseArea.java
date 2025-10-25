package com.example.cut_optimization.dto.baseDto;

import com.example.cut_optimization.dto.TypeOfMaterial;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public abstract class BaseArea extends BaseDetail  {

    private int workpieceId;
    private int areaId;
    private int areaGroupId;
    private boolean isGroupArea;
    private double up;
    private double left;

    public BaseArea(double height, double width, double up, double left, TypeOfMaterial typeMaterial,
                    int workpieceId, int areaId, int areaGroupId, boolean isGroupArea) {

        super(height, width, typeMaterial);
        this.workpieceId = workpieceId;
        this.areaGroupId = areaGroupId;
        this.areaId = areaId;
        this.isGroupArea = isGroupArea;
        this.up = up;
        this.left = left;
    }

    public BaseArea(BaseDetail baseDetail, int workpieceId, int areaGroupId, int areaId, boolean isGroupArea, double up, double left) {
        super(baseDetail.getHeight(), baseDetail.getWidth(), baseDetail.getTypeMaterial());
        this.workpieceId = workpieceId;
        this.areaGroupId = areaGroupId;
        this.areaId = areaId;
        this.isGroupArea = isGroupArea;
        this.up = up;
        this.left = left;
    }
}
