package com.example.cut_optimization.service.stacking;

import com.example.cut_optimization.dto.ResultStacking;
import com.example.cut_optimization.dto.TypeOfMaterial;
import com.example.cut_optimization.dto.areas.FreeArea;
import com.example.cut_optimization.dto.areas.OccupiedArea;
import com.example.cut_optimization.dto.areas.CuttingLayout;
import com.example.cut_optimization.dto.details.Detail;
import com.example.cut_optimization.optimizators.AreaManager;
import com.example.cut_optimization.service.ResultEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("greedyStackingStrategy")
public class GreedyStackingStrategy implements StackingStrategy {

    private final AreaManager areaManager;
    private final ResultEvaluator resultEvaluator;

    @Autowired
    public GreedyStackingStrategy(AreaManager areaManager, ResultEvaluator resultEvaluator) {
        this.areaManager = areaManager;
        this.resultEvaluator = resultEvaluator;
    }

    @Override
    public void stack(TypeOfMaterial.InitialDataOptimization initialData, boolean isStackingDetailsIntoOneWorkpiece) {

        ResultStacking bestResultStacking = null;
        ResultStacking currentResultStacking = new ResultStacking();
        Map<Integer, CuttingLayout> wayOfLayingAreas = currentResultStacking.getWayOfLayingAreas();
        if (wayOfLayingAreas == null) {
            wayOfLayingAreas = new HashMap<>();
        }

        //будем делать несколько проходов.
        //1 цикл - вращаем детали только при необходимости (если без вращения не помещаются в область)
        //2 цикл - вращаем если деталь может быть уложена поперек заготовки
        // Если запрещаем вращение, то проходим только 1 цикл

        int[] counter = initialData.isDisableRotation() ? new int[]{1} : new int[]{1, 2};

        for (int i = 0; i < counter.length; i++) {

            for (int j = 0; j < initialData.getDetails().size(); j++) {

                currentResultStacking.saveWayOfLayingAreas(j, initialData.getFreeAreas(), initialData.getOccupiedAreas());

                ResultStacking bestResultStackingForDetail = null;
                ResultStacking currentResultStackingForDetail = new ResultStacking();

                Detail detail = new Detail(initialData.getDetails().get(j));

                List<FreeArea> freeAreasSuitableForDetail = areaManager.getFreeAreasSuitableForDetail(detail,
                        initialData.getFreeAreas(), initialData.isDisableRotation());

                for (FreeArea freeArea : freeAreasSuitableForDetail) {

                    currentResultStacking.restoreWayOfLayingAreas(j, initialData);

                    if (counter[i] == 1) {

                        areaManager.stackDetailIntoFreeArea(detail, freeArea, initialData);
                        currentResultStackingForDetail.calculateStackingCoefficientsByDetail(detail, freeArea, initialData.getFreeAreas(), initialData.getWorkpieces());

                    } else if (counter[i] == 2) {

                        areaManager.stackDetailIntoFreeAreaCompactlyWithRotation(detail, freeArea, initialData);
                        currentResultStackingForDetail.calculateStackingCoefficientsByDetail(detail, freeArea, initialData.getFreeAreas(), initialData.getWorkpieces());

                    }

                    bestResultStackingForDetail = resultEvaluator.evaluateAndUpdateTopResultByDetail(bestResultStackingForDetail, currentResultStackingForDetail, isStackingDetailsIntoOneWorkpiece);

                    bestResultStackingForDetail.setAreaId(freeArea.getAreaId());
                }
                if (bestResultStackingForDetail == null) {

                    if (!isStackingDetailsIntoOneWorkpiece) {
                        currentResultStacking.setHasError(true);
                    }
                    break;
                }

                currentResultStacking.restoreWayOfLayingAreas(j, initialData);

                int areaId = bestResultStackingForDetail.getAreaId();

                FreeArea freeArea = initialData.getFreeAreas().stream()
                        .filter(fa -> fa.getAreaId() == areaId)
                        .findFirst()
                        .orElse(null);

                assert freeArea != null;

                if (counter[i] == 1) {

                    areaManager.stackDetailIntoFreeArea(detail, freeArea, initialData);

                } else if (counter[i] == 2) {

                    areaManager.stackDetailIntoFreeAreaCompactlyWithRotation(detail, freeArea, initialData);

                }
                currentResultStacking.saveWayOfLayingAreas(j + 1, initialData.getFreeAreas(), initialData.getOccupiedAreas());
            }

            // проверим если это укладка в одну заготовку, то не добавили ли заготовок с виртуального склада
            if (isStackingDetailsIntoOneWorkpiece) {
                Set<Integer> workpiecesId = initialData.getOccupiedAreas().stream()
                        .map(OccupiedArea::getWorkpieceId)
                        .collect(Collectors.toSet());
                if (workpiecesId.size() > 1) {
                    currentResultStacking.setHasError(true);
                }
            }
            if (initialData.getOccupiedAreas().size() < initialData.getDetails().size()) {
                currentResultStacking.setHasError(true);
            }
            if (currentResultStacking.isHasError()) {
                continue;
            }

            areaManager.enlargeAndCutOffLowerAreaVertically(initialData.getWorkpieces(),
                                                            initialData.getFreeAreas(), initialData.getOccupiedAreas(),
                                                            initialData.getSawCutWidth());

            currentResultStacking.setStackingSequenceFromOccupiedAreas(initialData.getOccupiedAreas());
            currentResultStacking.calculateStackingCoefficients(initialData.getOccupiedAreas(), initialData.getWorkpieces(), initialData.getFreeAreas());
            currentResultStacking.saveWayOfLayingAreas(initialData.getDetails().size(), initialData.getFreeAreas(),initialData.getOccupiedAreas());

            bestResultStacking = resultEvaluator.compareAndUpdateTopResultWithCurrentResult(bestResultStacking,
                    currentResultStacking, initialData.isUsePartialSheets());

            currentResultStacking.restoreWayOfLayingAreas(0, initialData);
        }
        if (bestResultStacking == null) {
            bestResultStacking = new ResultStacking();
            bestResultStacking.setHasError(true);
        }

        initialData.setBestResultStacking(bestResultStacking);
    }

    @Override
    public void stack(TypeOfMaterial.InitialDataOptimization initialDataOptimization) {}

}
