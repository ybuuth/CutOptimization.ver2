package com.example.cut_optimization.service;

import com.example.cut_optimization.dto.*;
import com.example.cut_optimization.dto.areas.CuttingLayout;
import com.example.cut_optimization.dto.baseDto.BaseArea;
import com.example.cut_optimization.dto.details.Detail;
import com.example.cut_optimization.dto.details.Workpiece;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OptimizeDispatcherService {

    private final StackingManager stackingManager;

    @Autowired
    public OptimizeDispatcherService(StackingManager stackingManager) {
        this.stackingManager = stackingManager;
    }

    public ResultDataOptimization optimize(InitialDataOptimization initialData) {

        PreliminaryChecker.initializePreliminarySettings(initialData);

        // Разделим расчеты по каждому типу материала
        List<TypeOfMaterial> typesOfMaterial = initialData.getTypesOfMaterial();
        List<InitialDataOptimization> dataOptimizationsByTypes = splitByMaterialType(initialData, typesOfMaterial);

        for (InitialDataOptimization initialDataByType : dataOptimizationsByTypes) {

            Optional<ResultStacking> optionalResultStacking = stackingManager.performInitialLaying(initialDataByType);

            if (!initialDataByType.isExpressCalculation() && optionalResultStacking.isPresent()) {
                initialDataByType.setBestResultStacking(optionalResultStacking.get());
                optionalResultStacking =stackingManager.optimizeInitialLaying(initialDataByType);
            }

            if (optionalResultStacking.isPresent()) {
                initialDataByType.setBestResultStacking(optionalResultStacking.get());
                stackingManager.finalOptimization(initialDataByType);
            }
        }
        return getResultDataOptimization(initialData.getDetails(), initialData.getTypesOfMaterial(), dataOptimizationsByTypes);
    }

    private List<InitialDataOptimization> splitByMaterialType(InitialDataOptimization initialData, List<TypeOfMaterial> typesOfMaterial) {
        List<InitialDataOptimization> dataOptimizationsByTypes = new ArrayList<>();

        for (TypeOfMaterial typeOfMaterial : typesOfMaterial) {
            InitialDataOptimization initialDataByType = new InitialDataOptimization();

            initialDataByType.setDetails(initialData.filterByType(initialData.getDetails(), typeOfMaterial));
            initialDataByType.setWorkpieces(initialData.filterByType(initialData.getWorkpieces(), typeOfMaterial));
            initialDataByType.setEndlessWorkpieces(initialData.filterByType(initialData.getEndlessWorkpieces(), typeOfMaterial));

            initialDataByType.setFreeAreas(new ArrayList<>());
            initialDataByType.setOccupiedAreas(new ArrayList<>());
            initialDataByType.setTypesOfMaterial(List.of(typeOfMaterial));

            copyCommonSettings(initialData, initialDataByType);

            dataOptimizationsByTypes.add(initialDataByType);
        }
        return dataOptimizationsByTypes;
    }

    public ResultDataOptimization postProcessOnly(InitialDataOptimization initialData) {
        initialData.finalOptimization();
        return getResultDataOptimization(initialData.getDetails(), initialData.getTypesOfMaterial(), List.of(initialData));
    }

    public ResultDataOptimization enlarge(InitialDataOptimization initialData) {
        initialData.enlargeAndCutOffLowerAreaVertically();
        ResultStacking resultStacking = new ResultStacking();
        CuttingLayout cuttingLayout = new CuttingLayout();
        cuttingLayout.setOccupiedAreas(initialData.getOccupiedAreas());
        cuttingLayout.setFreeAreas(initialData.getFreeAreas());
        resultStacking.getWayOfLayingAreas().put(initialData.getDetails().size(), cuttingLayout);
        initialData.setBestResultStacking(resultStacking);

        return getResultDataOptimization(initialData.getDetails(), initialData.getTypesOfMaterial(), List.of(initialData));
    }

    private ResultDataOptimization getResultDataOptimization(List<Detail> details, List<TypeOfMaterial> typeOfMaterials,
                                                                    List<InitialDataOptimization> dataOptimizationsByTypes) {
        ResultDataOptimization result = new ResultDataOptimization();

        for (InitialDataOptimization initialDataByType : dataOptimizationsByTypes) {
            CuttingLayout cuttingLayout = initialDataByType.getBestResultStacking().getWayOfLayingAreas().get(initialDataByType.getDetails().size());
            if (cuttingLayout != null) {

                Set<Integer> usedWorkpiecesIds = initialDataByType.getOccupiedAreas().stream()
                            .map(BaseArea::getWorkpieceId)
                            .collect(Collectors.toSet());
                List<Workpiece> orderedWorkpieces = initialDataByType.getWorkpieces().stream()
                            .filter(workpiece -> usedWorkpiecesIds.contains(workpiece.getId()))
                            .sorted(Comparator.comparing(Workpiece::getId))
                            .toList();

                result.getWorkpieces().addAll(orderedWorkpieces);
                result.getFreeAreas().addAll((cuttingLayout.getFreeAreas()));
                result.getOccupiedAreas().addAll((cuttingLayout.getOccupiedAreas()));
            }
        }
        result.setDetails(details);
        result.setTypesOfMaterial(typeOfMaterials);
        return result;
    }

    private void copyCommonSettings(InitialDataOptimization initialData, InitialDataOptimization initialDataByType) {
        initialDataByType.setInitialTemperature(initialData.getInitialTemperature());
        initialDataByType.setDisableRotation(initialData.isDisableRotation());
        initialDataByType.setSawCutWidth(initialData.getSawCutWidth());
        initialDataByType.setExpressCalculation(initialData.isExpressCalculation());
        initialDataByType.setUsePartialSheets(initialData.isUsePartialSheets());
        initialDataByType.setIterationsCount(initialData.getIterationsCount());
        initialDataByType.setAreaIdGenerator(initialData.getAreaIdGenerator());
    }
}
