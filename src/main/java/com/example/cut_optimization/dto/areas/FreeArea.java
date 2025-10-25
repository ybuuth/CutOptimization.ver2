package com.example.cut_optimization.dto.areas;

import com.example.cut_optimization.dto.Stackable;
import com.example.cut_optimization.dto.TypeOfMaterial;
import com.example.cut_optimization.dto.baseDto.BaseArea;
import com.example.cut_optimization.dto.baseDto.BaseDetailInfo;
import com.example.cut_optimization.dto.details.Workpiece;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
public class FreeArea extends BaseArea implements Stackable, Cloneable {

    public FreeArea(double height, double width, double up, double left, TypeOfMaterial typeMaterial, int workpieceId, int areaId,
                    int groupAreaId, boolean isGroupArea) {
        super(height, width, up, left, typeMaterial, workpieceId, areaId, groupAreaId, isGroupArea);
    }

    public FreeArea(Workpiece workpiece, int areaId, int groupAreaId, boolean isGroupArea) {
        super(workpiece, workpiece.getId(), areaId, groupAreaId, isGroupArea, 0, 0);
    }

    public FreeArea(OccupiedArea occupiedArea, int areaId, int groupAreaId, boolean isGroupArea) {
        super(occupiedArea, occupiedArea.getWorkpieceId(), areaId, groupAreaId, isGroupArea, occupiedArea.getUp(), occupiedArea.getLeft());
    }

    @Override
    protected FreeArea clone() throws CloneNotSupportedException {

        FreeArea clone = (FreeArea) super.clone();

        if (getTypeMaterial() != null) {
            clone.setTypeMaterial(getTypeMaterial().clone());
        }
        return clone;
    }
}
