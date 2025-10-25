package com.example.cut_optimization.dto;

import com.example.cut_optimization.dto.baseDto.BaseDetailInfo;

public interface Stackable extends BaseDetailInfo {

    default boolean canStack(BaseDetailInfo detail) {
        return (canStackAlong(detail) || canStackAcross(detail));
    }

    default boolean canStackAlong(BaseDetailInfo detail) {
        return detail.getWidth() <= getWidth() && detail.getHeight() <= getHeight();
    }

    default boolean canStackAcross(BaseDetailInfo detail) {
        return detail.getWidth() <= getHeight() && detail.getHeight() <= getWidth();
    }
}
