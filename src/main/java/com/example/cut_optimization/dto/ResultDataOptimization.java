package com.example.cut_optimization.dto;

import com.example.cut_optimization.dto.areas.FreeArea;
import com.example.cut_optimization.dto.areas.OccupiedArea;
import com.example.cut_optimization.dto.details.Detail;
import com.example.cut_optimization.dto.details.Workpiece;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResultDataOptimization {

    @Builder.Default
    private List<FreeArea> freeAreas = new ArrayList<>();

    @Builder.Default
    private List<OccupiedArea> occupiedAreas = new ArrayList<>();

    @Builder.Default
    private List<Detail> details = new ArrayList<>();

    @Builder.Default
    private List<TypeOfMaterial> typesOfMaterial = new ArrayList<>();

    @Builder.Default
    private List<Workpiece> workpieces = new ArrayList<>();
}
