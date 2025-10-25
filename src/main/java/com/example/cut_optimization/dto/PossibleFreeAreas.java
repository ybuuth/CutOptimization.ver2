package com.example.cut_optimization.dto;

import com.example.cut_optimization.dto.areas.FreeArea;
import lombok.Data;

import java.util.List;

@Data
public class PossibleFreeAreas {
    List<FreeArea> freeAreasHighPriority;
    List<FreeArea> freeAreasMediumPriority;
    List<FreeArea> freeAreasLowPriority;
}
