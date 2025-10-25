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
    private List<FreeArea> freeAreas = new ArrayList<>();
    private List<OccupiedArea> occupiedAreas = new ArrayList<>();
    private List<Detail> details = new ArrayList<>();
    private List<TypeOfMaterial> typesOfMaterial = new ArrayList<>();
    private List<Workpiece> workpieces = new ArrayList<>();
}
