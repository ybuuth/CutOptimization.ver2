package com.example.cut_optimization.service;

import com.example.cut_optimization.dto.ResultStacking;
import com.example.cut_optimization.dto.areas.FreeArea;
import com.example.cut_optimization.dto.areas.OccupiedArea;
import com.example.cut_optimization.dto.details.Detail;
import com.example.cut_optimization.dto.details.Workpiece;
import com.example.cut_optimization.optimizators.InitialDataOptimization;
import com.example.cut_optimization.service.stacking.StackingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class StackingManager {

    @Qualifier("greedyStackingStrategy")
    private final StackingStrategy greedyStackingStrategy;
    @Qualifier("simulatedAnnealingStackingStrategy")
    private final StackingStrategy simulatedAnnealingStackingStrategy;
    private final ResultEvaluator resultEvaluator;
    private final AreaManager areaManager;

    @Autowired
    public StackingManager(StackingStrategy greedyStackingStrategy, StackingStrategy simulatedAnnealingStackingStrategy, ResultEvaluator resultEvaluator, AreaManager areaManager) {
        this.greedyStackingStrategy = greedyStackingStrategy;
        this.simulatedAnnealingStackingStrategy = simulatedAnnealingStackingStrategy;
        this.resultEvaluator = resultEvaluator;
        this.areaManager = areaManager;
    }

    public Optional<ResultStacking> performInitialLaying(InitialDataOptimization initialData) {

        ResultStacking resultStackingInOneWorkpiece = new ResultStacking();

        if (initialData.isUsePartialSheets()) {

            initialData.getWorkpieces().sort((o1, o2) -> (o1.getSquare() > o2.getSquare()) ? 1 : -1);
            initialData.getDetails().sort((o1, o2) -> (o1.getSquare() > o2.getSquare()) ? -1 : 1);
            resultStackingInOneWorkpiece.setHasError(true);

        } else {

            boolean canStack = setupSingleWorkpieceForStacking(initialData);
            if (canStack) {

                boolean isStackingDetailsIntoOneWorkpiece = true;

                greedyStackingStrategy.stack(initialData, isStackingDetailsIntoOneWorkpiece);

                resultStackingInOneWorkpiece = initialData.getBestResultStacking();

            } else {
                resultStackingInOneWorkpiece.setHasError(true);
            }
        }

        //наполнить свободные области из заготовок
        initialData.getFreeAreas().clear();
        initialData.getOccupiedAreas().clear();

        for (Workpiece workpiece : initialData.getWorkpieces()) {
            FreeArea freeArea = new FreeArea(workpiece.getHeight(), workpiece.getWidth(), 0, 0,
                    workpiece.getTypeMaterial(), workpiece.getId(), initialData.getAreaIdGenerator().nextAreaId(),
                    initialData.getAreaIdGenerator().nextGroupAreaId(), false);
            initialData.getFreeAreas().add(freeArea);
        }

        greedyStackingStrategy.stack(initialData,false);

        ResultStacking resultStackingInMultipleWorkpieces = initialData.getBestResultStacking();

        if (resultStackingInOneWorkpiece.isHasError() && resultStackingInMultipleWorkpieces == null) {
            return Optional.empty();
        }

        ResultStacking bestResultStacking = resultEvaluator.selectBestStackingResultBetweenOneWorkpieceResultAndMultipleWorkpieceResult(resultStackingInOneWorkpiece,
                resultStackingInMultipleWorkpieces, initialData.isUsePartialSheets());

        initialData.setBestResultStacking(bestResultStacking);

        return Optional.of(bestResultStacking);
    }

    public Optional<ResultStacking> optimizeInitialLaying(InitialDataOptimization initialData) {
        simulatedAnnealingStackingStrategy.stack(initialData);
        return Optional.of(initialData.getBestResultStacking());
    }

    private boolean setupSingleWorkpieceForStacking(InitialDataOptimization initialData) {
        boolean hasError = false;
        //найдем минимально возможную по площади заготовку с площадью превышающей область всех деталей
        Optional<Workpiece> workpiece = findSuitableWorkpieceBySquare(initialData.getWorkpieces(), initialData.getDetails());

        if (workpiece.isEmpty()) {
            hasError = true;
            return hasError;
        }

        Workpiece SuitableWorkpieceBySquare = workpiece.get();

        FreeArea freeArea = new FreeArea(SuitableWorkpieceBySquare.getHeight(), SuitableWorkpieceBySquare.getWidth(), 0, 0,
                SuitableWorkpieceBySquare.getTypeMaterial(), SuitableWorkpieceBySquare.getId(), initialData.getAreaIdGenerator().nextAreaId(),
                initialData.getAreaIdGenerator().nextGroupAreaId(), false);

        initialData.getFreeAreas().add(freeArea);
        return hasError;
    }

    private Optional<Workpiece> findSuitableWorkpieceBySquare(List<Workpiece> workpieces, List<Detail> details) {

        details.sort((o1, o2) -> (o1.getSquare() > o2.getSquare()) ? -1 : 1);
        double allDetailsSquare = details.stream()
                .mapToDouble(Detail::getSquare)
                .sum();

        return workpieces.stream()
                .filter(w -> w.getSquare() > allDetailsSquare)
                .min(Comparator.comparingDouble(Workpiece::getSquare));
    }


    public void finalOptimization(InitialDataOptimization initialData) {
        if (initialData.getFreeAreas().isEmpty()) {
            return;
        }
        ResultStacking bestResultStacking = initialData.getBestResultStacking();
        bestResultStacking.restoreWayOfLayingAreas(initialData.getDetails().size(), initialData);

        List<OccupiedArea> occupiedAreas = initialData.getOccupiedAreas();
        List<FreeArea> freeAreas = initialData.getFreeAreas();

        occupiedAreas.sort((o1, o2) -> (o1.getSquare() > o2.getSquare()) ? -1 : 1);

        for (Workpiece workpiece : initialData.getWorkpieces()) {

            boolean hasReplacement;
            int size = occupiedAreas.size();

            do {
                hasReplacement = false;
                for (int i = 0; i < occupiedAreas.size() ; i++) {

                    OccupiedArea occupiedArea = occupiedAreas.get(size - 1 - i);

                    if (occupiedArea.getWorkpieceId() == workpiece.getId()) {

                        boolean replaceArea = areaManager.moveToSmallerFreeArea(occupiedArea, workpiece, initialData, bestResultStacking);
                        if (replaceArea) {
                            hasReplacement = true;
                        }
                    }
                }
            } while (hasReplacement);
        }
    }

}
