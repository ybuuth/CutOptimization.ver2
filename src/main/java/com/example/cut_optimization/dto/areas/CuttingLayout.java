package com.example.cut_optimization.dto.areas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class CuttingLayout implements Cloneable {
    private List<FreeArea> freeAreas;
    private List<OccupiedArea> occupiedAreas;

    public CuttingLayout(List<FreeArea> freeAreas, List<OccupiedArea> occupiedAreas) {
        this.freeAreas = freeAreas.stream()
                                .map(area -> {
                                    try {
                                        return area.clone();
                                    } catch (CloneNotSupportedException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                                .collect(Collectors.toList());

        this.occupiedAreas = occupiedAreas.stream()
                                        .map(area -> {
                                            try {
                                                return area.clone();
                                            } catch (CloneNotSupportedException e) {
                                                throw new RuntimeException(e);
                                            }
                                        })
                                        .collect(Collectors.toList());
    }

    @Override
    public CuttingLayout clone() throws CloneNotSupportedException {
        try {
            CuttingLayout clone = (CuttingLayout) super.clone();
            if (this.freeAreas != null) {
                clone.freeAreas = new ArrayList<>(this.freeAreas);
            }
            if (this.occupiedAreas != null) {
                clone.occupiedAreas = new ArrayList<>(this.occupiedAreas);
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new CloneNotSupportedException();
        }
    }
}
