package com.example.cut_optimization.dto.areas;

import com.example.cut_optimization.dto.TypeOfMaterial;
import com.example.cut_optimization.dto.baseDto.BaseArea;
import com.example.cut_optimization.dto.baseDto.BaseDetail;
import com.example.cut_optimization.dto.baseDto.BaseDetailInfo;
import com.example.cut_optimization.dto.details.Detail;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OccupiedArea extends BaseArea implements BaseDetailInfo, Cloneable {

    private int detailId;
    private boolean rotated;

    public OccupiedArea(Detail detail, BaseArea baseArea, int areaId, boolean isGroupArea) {
        super(detail.getHeight(), detail.getWidth(), baseArea.getUp(), baseArea.getLeft(), detail.getTypeMaterial(), baseArea.getWorkpieceId(),
                areaId, baseArea.getAreaGroupId(), isGroupArea);
    }

    public Detail toDetail() {
        Detail detail = new Detail();
        detail.setHeight(getHeight());
        detail.setWidth(getWidth());
        detail.setTypeMaterial(getTypeMaterial());
        detail.setRotated(isRotated());
        detail.setId(getDetailId());
        return detail;
    }

    @Override
    protected OccupiedArea clone() throws CloneNotSupportedException {
        OccupiedArea clone = (OccupiedArea) super.clone();

        if (getTypeMaterial() != null) {
            clone.setTypeMaterial(getTypeMaterial().clone());
        }

        return clone;
    }
}
