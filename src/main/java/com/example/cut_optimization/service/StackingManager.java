package com.example.cut_optimization.service;

import com.example.cut_optimization.dto.InitialDataOptimization;
import com.example.cut_optimization.dto.ResultStacking;
import com.example.cut_optimization.dto.areas.FreeArea;
import com.example.cut_optimization.dto.areas.OccupiedArea;
import com.example.cut_optimization.dto.details.Workpiece;
import com.example.cut_optimization.service.optimizators.AreaManager;
import com.example.cut_optimization.service.resultEvaluator.Evaluatable;
import com.example.cut_optimization.service.stacking.StackingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class StackingManager {

    private final StackingStrategy greedyStackingStrategy;
    private final StackingStrategy simulatedAnnealingStackingStrategy;
    private final AreaManager areaManager;
    private final Evaluatable evaluator;

    @Autowired
    public StackingManager(@Qualifier("greedyStackingStrategy") StackingStrategy greedyStackingStrategy,
                           @Qualifier("simulatedAnnealingStackingStrategy") StackingStrategy simulatedAnnealingStackingStrategy,
                           AreaManager areaManager, Evaluatable evaluator) {
        this.greedyStackingStrategy = greedyStackingStrategy;
        this.simulatedAnnealingStackingStrategy = simulatedAnnealingStackingStrategy;
        this.areaManager = areaManager;
        this.evaluator = evaluator;
    }

    public Optional<ResultStacking> performInitialLaying(InitialDataOptimization initialData) {

        ResultStacking resultStackingInOneWorkpiece = new ResultStacking();

        if (initialData.isUsePartialSheets()) {

            initialData.getWorkpieces().sort((o1, o2) -> (o1.getSquare() > o2.getSquare()) ? 1 : -1);
            initialData.getDetails().sort((o1, o2) -> (o1.getSquare() > o2.getSquare()) ? -1 : 1);
            //resultStackingInOneWorkpiece.setHasError(true);

        } else {
            //initialData.getWorkpieces().sort((o1, o2) -> (o1.getSquare() > o2.getSquare()) ? -1 : 1);
            //initialData.getDetails().sort((o1, o2) -> (o1.getSquare() > o2.getSquare()) ? -1 : 1);
//
            boolean canStack = setupSingleWorkpieceForStacking(initialData);
            if (canStack) {

                boolean isStackingDetailsIntoOneWorkpiece = true;

                greedyStackingStrategy.stack(initialData);

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

        greedyStackingStrategy.stack(initialData);

//        ResultStacking resultStackingInMultipleWorkpieces = initialData.getBestResultStacking();
//
//        if (resultStackingInOneWorkpiece.isHasError() && resultStackingInMultipleWorkpieces == null) {
//            return Optional.empty();
//        }
//
        ResultStacking bestResultStacking = initialData.getBestResultStacking();
        if (bestResultStacking.isHasError()) {
            return Optional.empty();
        }

        bestResultStacking.restoreWayOfLayingAreas(initialData.getDetails().size(), initialData);

        return Optional.of(bestResultStacking);
    }

    public Optional<ResultStacking> optimizeInitialLaying(InitialDataOptimization initialData) {
        simulatedAnnealingStackingStrategy.stack(initialData);
        return Optional.of(initialData.getBestResultStacking());
    }

    private boolean setupSingleWorkpieceForStacking(InitialDataOptimization initialData) {
        boolean canStack = true;
        //найдем минимально возможную по площади заготовку с площадью превышающей область всех деталей
        Optional<Workpiece> workpiece = areaManager.findSuitableWorkpieceBySquare(initialData.getDetails(), initialData.getWorkpieces());

        if (workpiece.isEmpty()) {
            canStack = false;
            return canStack;
        }

        Workpiece SuitableWorkpieceBySquare = workpiece.get();

        FreeArea freeArea = new FreeArea(SuitableWorkpieceBySquare.getHeight(), SuitableWorkpieceBySquare.getWidth(), 0, 0,
                SuitableWorkpieceBySquare.getTypeMaterial(), SuitableWorkpieceBySquare.getId(), initialData.getAreaIdGenerator().nextAreaId(),
                initialData.getAreaIdGenerator().nextGroupAreaId(), false);

        initialData.getFreeAreas().add(freeArea);
        return canStack;
    }

    public void finalOptimization(InitialDataOptimization initialData) {
        if (initialData.getFreeAreas().isEmpty()) {
            return;
        }

        ResultStacking bestResultStacking = initialData.getBestResultStacking();

        List<OccupiedArea> occupiedAreas = initialData.getOccupiedAreas();

        occupiedAreas.sort((o1, o2) -> (o1.getSquare() > o2.getSquare()) ? -1 : 1);

        for (Workpiece workpiece : initialData.getWorkpieces()) {

            boolean hasReplacement;
            int size = occupiedAreas.size();
            int count = 0; // временный костыль для отладки и сброса hasReplacement

            do {
                hasReplacement = false;
                count ++;

                for (int i = 0; i < occupiedAreas.size() ; i++) {

                    OccupiedArea occupiedArea = occupiedAreas.get(size - 1 - i);

                    if (occupiedArea.getWorkpieceId() == workpiece.getId()) {

                        boolean replaceArea = areaManager.moveToSmallerFreeArea(occupiedArea, workpiece, initialData, bestResultStacking);
                        if (replaceArea) {
                            hasReplacement = true;
                        }
                    }
                }
                if (count > 30) { // временный костыль для отладки и сброса hasReplacement
                    hasReplacement = false;
                    log.warn("finalOptimization: count > 30");
                    log.info("initilalData: {}", initialData);
                }
            } while (hasReplacement);
        }
    }
}
