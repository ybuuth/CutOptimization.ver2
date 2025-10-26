package com.example.cut_optimization.dto.areas;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurroundingAreasInfo {

    private boolean freeAreaOnRight;
    private boolean freeAreaOnBottom;
    private boolean isAtRightEdgeOfWorkpiece;
    private boolean isAtBottomEdgeOfWorkpiece;

    @Builder.Default
    private List<FreeArea> freeAreasOnRight = new ArrayList<>();

    @Builder.Default
    private List<FreeArea> freeAreasOnBottom = new ArrayList<>();

    @Builder.Default
    private List<OccupiedArea> occupiedAreasOnRight = new ArrayList<>();

    @Builder.Default
    private List<OccupiedArea> occupiedAreasOnBottom = new ArrayList<>();

}
