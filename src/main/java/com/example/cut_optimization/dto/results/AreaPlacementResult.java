package com.example.cut_optimization.dto.results;

import com.example.cut_optimization.dto.areas.FreeArea;
import com.example.cut_optimization.dto.areas.OccupiedArea;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AreaPlacementResult {
    private FreeArea rightFreeArea;
    private FreeArea bottomFreeArea;
    private OccupiedArea occupiedArea;
    private FreeArea areaToRemove;
}
