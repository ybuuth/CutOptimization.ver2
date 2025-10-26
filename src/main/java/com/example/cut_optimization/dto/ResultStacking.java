package com.example.cut_optimization.dto;

import com.example.cut_optimization.dto.areas.FreeArea;
import com.example.cut_optimization.dto.areas.OccupiedArea;
import com.example.cut_optimization.dto.areas.CuttingLayout;
import com.example.cut_optimization.dto.baseDto.BaseArea;
import com.example.cut_optimization.dto.details.Detail;
import com.example.cut_optimization.dto.details.Workpiece;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultStacking implements Cloneable {

    private int areaId;
    private boolean hasError;

    @Builder.Default
    private List<StackingSequence> stackingSequences = new ArrayList<>();

    @Builder.Default
    private StackingCoefficients stackingCoefficients = new StackingCoefficients();

    @Builder.Default
    private Map<Integer, CuttingLayout> wayOfLayingAreas = new HashMap<>();

    public void saveWayOfLayingAreas(int key, List<FreeArea> freeAreas, List<OccupiedArea> occupiedAreas) {
        if (wayOfLayingAreas == null) {
            wayOfLayingAreas = new HashMap<>();
        } else {
            wayOfLayingAreas.put(key, new CuttingLayout(freeAreas, occupiedAreas));
            setWayOfLayingAreas(wayOfLayingAreas);
        }
    }

    public void restoreWayOfLayingAreas(int key, InitialDataOptimization initialDataOptimization) {

        CuttingLayout cuttingLayout = wayOfLayingAreas.get(key);
        initialDataOptimization.restoreFrom(cuttingLayout);
    }

    @Override
    public ResultStacking clone() {
        try {
            ResultStacking clone = (ResultStacking) super.clone();
            if (this.stackingSequences != null) {
                clone.stackingSequences = new ArrayList<>(this.stackingSequences);
            }
            if (this.stackingCoefficients != null) {
                clone.stackingCoefficients = this.stackingCoefficients.clone();
            }
            if (this.wayOfLayingAreas != null) {
                clone.wayOfLayingAreas = new HashMap<>();
                for (Map.Entry<Integer, CuttingLayout> entry : this.wayOfLayingAreas.entrySet()) {
                    clone.wayOfLayingAreas.put(entry.getKey(), entry.getValue().clone());
                }
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public void calculateStackingCoefficients(List<OccupiedArea> occupiedAreas,
                                                              List<Workpiece> workpieces,
                                                              List<FreeArea> freeAreas) {

        if (stackingCoefficients == null) {
            stackingCoefficients = new StackingCoefficients();
        }

        AtomicReference<Double> sumOfWorkpiecesArea = new AtomicReference<>((double) 0);
        double squareOfSumOfWorkpieces = 0;
        AtomicReference<Double> sumOfSquaresOfUsedWorkpiecesAreas = new AtomicReference<>((double) 0);
        AtomicReference<Double> sumOfSquaresOfFreeAreas = new AtomicReference<>((double) 0);
        double sumOfSquaresOfOccupiedAreas = 0;
        double sumOfAreasOfOccupiedAreas = 0;
        double squareOfSumOfOccupiedAreas = 0;
        AtomicReference<Double> sumOfSquaresOfFreeAreasOfUsedWorkpieces = new AtomicReference<>((double) 0);
        AtomicReference<Integer> amountOfFreeAreasOfUsedWorkpieces = new AtomicReference<>(0);


        Set<Integer> usedWorkpiecesIds = occupiedAreas.stream()
                .map(BaseArea::getWorkpieceId)
                .collect(Collectors.toSet());

        workpieces.stream()
                .filter(workpiece -> usedWorkpiecesIds.contains(workpiece.getId()))
                .forEach(workpiece -> {
                    sumOfWorkpiecesArea.updateAndGet(v -> (v + workpiece.getSquare()));
                    sumOfSquaresOfUsedWorkpiecesAreas.updateAndGet(v -> (v + Math.pow(workpiece.getSquare(), 2)));
                });

        squareOfSumOfWorkpieces = Math.pow(sumOfWorkpiecesArea.get(), 2);

        freeAreas.stream()
                .filter(area -> usedWorkpiecesIds.contains(area.getWorkpieceId()))
                .forEach(area -> {
                    sumOfSquaresOfFreeAreasOfUsedWorkpieces.updateAndGet(v -> (v + Math.pow(area.getSquare(), 2)));
                    sumOfSquaresOfFreeAreas.updateAndGet(v -> (v + Math.pow(area.getSquare(), 2)));
                    amountOfFreeAreasOfUsedWorkpieces.updateAndGet(v -> (v + 1));
                });

        for (BaseArea area : occupiedAreas) {
            if (usedWorkpiecesIds.contains(area.getWorkpieceId())) {
                sumOfSquaresOfOccupiedAreas += Math.pow(area.getSquare(), 2);
                sumOfAreasOfOccupiedAreas += area.getSquare();
            }
        }

        squareOfSumOfOccupiedAreas = Math.pow(sumOfAreasOfOccupiedAreas, 2);

        stackingCoefficients.setFreeAreaRatioOfUsedWorkpieces(sumOfSquaresOfFreeAreasOfUsedWorkpieces.get() / sumOfSquaresOfUsedWorkpiecesAreas.get());
        stackingCoefficients.setFreeAreaRatio(sumOfSquaresOfFreeAreas.get() / sumOfSquaresOfUsedWorkpiecesAreas.get()); //КоэфСвободныхПлощадей, КоэфИспользованияПлощади
        stackingCoefficients.setOccupiedAreaRatio(sumOfSquaresOfOccupiedAreas / sumOfSquaresOfUsedWorkpiecesAreas.get());
        stackingCoefficients.setAreaFillingFactor(squareOfSumOfOccupiedAreas / squareOfSumOfWorkpieces);

    }

    public void calculateStackingCoefficientsByDetail(Detail detail, FreeArea freeArea, List<FreeArea> freeAreas, List<Workpiece> workpieces) {

        AtomicReference<Double> sumOfSquaresOfFreeAreasOfUsedWorkpieces = new AtomicReference<>(0.0);
        double sumOfSquaresOfUsedWorkpieces = 0;
        AtomicReference<Integer> countOfFreeAreas = new AtomicReference<>(0);

        if (stackingCoefficients == null) {
            stackingCoefficients = new StackingCoefficients();
        }

        int workpieceId = freeArea.getWorkpieceId();
        sumOfSquaresOfUsedWorkpieces = workpieces.stream()
                .filter(workpiece -> workpiece.getId() == workpieceId)
                .mapToDouble(workpiece -> Math.pow(workpiece.getSquare(), 2))
                .sum();

        freeAreas.stream()
                .filter(area -> area.getWorkpieceId() == workpieceId)
                .forEach(freeArea1 -> {
                    sumOfSquaresOfFreeAreasOfUsedWorkpieces.updateAndGet(v -> (v + Math.pow(freeArea1.getSquare(), 2)));
                    countOfFreeAreas.updateAndGet(v -> (v + 1));
                });

        double squareAreaDetail = detail.getSquare() * detail.getSquare();
        double squareAreaFreeArea = freeArea.getSquare() * freeArea.getSquare();

        stackingCoefficients.setFreeAreaCount(countOfFreeAreas.get());
        stackingCoefficients.setOccupiedAreaRatioByDetail(squareAreaDetail / squareAreaFreeArea);
        stackingCoefficients.setFreeAreaRatioByDetail(sumOfSquaresOfFreeAreasOfUsedWorkpieces.get() / sumOfSquaresOfUsedWorkpieces);
    }

    public void setStackingSequenceFromOccupiedAreas(List<OccupiedArea> occupiedAreas) {

        List<Integer> usedWorkpiecesIds = occupiedAreas.stream()
                .map(BaseArea::getWorkpieceId)
                .distinct()
                .collect(Collectors.toList());

        List<OccupiedArea> sortedCopy = new ArrayList<>();

        for (Integer workpieceId : usedWorkpiecesIds) {
            sortedCopy.addAll(occupiedAreas.stream()
                    .filter(area -> area.getWorkpieceId() == workpieceId)
                    .sorted(Comparator.comparing(OccupiedArea::getUp)
                            .thenComparing(OccupiedArea::getLeft))
                    .collect(Collectors.toList()));
        }

        occupiedAreas.clear();
        occupiedAreas.addAll(sortedCopy);

        List<StackingSequence> sequences = occupiedAreas.stream()
                .map(area -> new StackingSequence(
                        area.getWorkpieceId(),
                        area.getDetailId(),
                        area.isRotated()))
                .collect(Collectors.toList());
        setStackingSequences(sequences);
    }

}
