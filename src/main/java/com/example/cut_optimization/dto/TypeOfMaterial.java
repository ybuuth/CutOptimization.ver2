package com.example.cut_optimization.dto;

import com.example.cut_optimization.dto.areas.CuttingLayout;
import com.example.cut_optimization.dto.areas.FreeArea;
import com.example.cut_optimization.dto.areas.OccupiedArea;
import com.example.cut_optimization.dto.baseDto.BaseDetailInfo;
import com.example.cut_optimization.dto.details.Detail;
import com.example.cut_optimization.dto.details.EndlessWorkpiece;
import com.example.cut_optimization.dto.details.Workpiece;
import com.example.cut_optimization.service.AreaIdGenerator;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class TypeOfMaterial implements Cloneable {
    @JsonProperty("name")
    private String name;

    @JsonCreator
    public TypeOfMaterial(@JsonProperty("name") String name){
       this.name = name;
    }

    @Override
    public TypeOfMaterial clone() throws CloneNotSupportedException {
        return (TypeOfMaterial) super.clone();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InitialDataOptimization {

        private double sawCutWidth;
        private int initialTemperature;
        private boolean usePartialSheets;
        private boolean disableRotation;
        private boolean expressCalculation;
        private int iterationsCount;

        private AreaIdGenerator areaIdGenerator;
        private int workpiecesNumerator;

        private double allDetailsSquare;
        private double allWorkpiecesSquare;

        private ResultStacking bestResultStacking;
        private ResultStacking currentResultStacking;

        private List<Detail> details;
        private List<Workpiece> workpieces;
        private List<EndlessWorkpiece> endlessWorkpieces;
        private List<TypeOfMaterial> typesOfMaterial;

        private List<FreeArea> freeAreas;
        private List<OccupiedArea> occupiedAreas;

        @JsonCreator
        public InitialDataOptimization(@JsonProperty("isExpressCalculation") boolean expressCalculation) {
            this.expressCalculation = expressCalculation;
        }

        public double calculateDetailsSquareByType(TypeOfMaterial typeOfMaterial) {
            double square;
            square = details.stream()
                    .filter(detail -> detail.getTypeMaterial().equals(typeOfMaterial))
                    .mapToDouble(detail -> detail.calculateSquare() * detail.getQuantity())
                    .sum();
            return square;
        }

        public double calculateWorkpiecesSquareByType(TypeOfMaterial typeOfMaterial) {
            double square;
            square = workpieces.stream()
                    .filter(workpiece -> workpiece.getTypeMaterial().equals(typeOfMaterial))
                    .mapToDouble(detail -> detail.calculateSquare() * detail.getQuantity())
                    .sum();
            return square;
        }

        public <T extends BaseDetailInfo> List<T> filterByType (List<T> items, TypeOfMaterial typeOfMaterial) {
            return items.stream()
                    .filter(item -> item.getTypeMaterial().equals(typeOfMaterial))
                    .collect(Collectors.toList());
        }

        public void calculateAllSquares() {
            details.forEach(detail -> detail.setSquare(detail.calculateSquare() * detail.getQuantity()));
            workpieces.forEach(workpiece -> workpiece.setSquare(workpiece.calculateSquare() * workpiece.getQuantity()));
            endlessWorkpieces.forEach(endlessWorkpiece -> endlessWorkpiece.setSquare(endlessWorkpiece.calculateSquare() *
                                                    endlessWorkpiece.getQuantity()));
        }

        public void finalOptimization()  {
            if (freeAreas.isEmpty()) {
                return;
            }
        }

        public void enlargeAndCutOffLowerAreaVertically() {

            Set<Integer> workpieceIds = occupiedAreas.stream()
                    .map(OccupiedArea::getWorkpieceId)
                    .collect(Collectors.toSet());

            for (Integer workpieceId : workpieceIds) {

                Workpiece currentWorkpiece = workpieces.stream()
                        .filter(w -> w.getId() == workpieceId)
                        .findFirst()
                        .orElse(null);
                assert currentWorkpiece != null;


            }
        }


        public void restoreFrom(CuttingLayout cuttingLayout) {
            setFreeAreas(new ArrayList<>(cuttingLayout.getFreeAreas()));
            setOccupiedAreas(new ArrayList<>(cuttingLayout.getOccupiedAreas()));
        }
    }
}
