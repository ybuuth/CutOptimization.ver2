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
    private List<FreeArea> freeAreasOnRight = new ArrayList<>();
    private List<FreeArea> freeAreasOnBottom = new ArrayList<>();
    private List<OccupiedArea> occupiedAreasOnRight = new ArrayList<>();
    private List<OccupiedArea> occupiedAreasOnBottom = new ArrayList<>();

}
