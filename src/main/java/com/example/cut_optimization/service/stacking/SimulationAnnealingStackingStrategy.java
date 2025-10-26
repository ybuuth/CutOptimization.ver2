package com.example.cut_optimization.service.stacking;

import com.example.cut_optimization.dto.InitialDataOptimization;
import com.example.cut_optimization.dto.ResultStacking;
import com.example.cut_optimization.dto.StackingSequence;
import com.example.cut_optimization.dto.details.Workpiece;
import com.example.cut_optimization.exception.CommonException;
import com.example.cut_optimization.service.ResultEvaluator;
import com.example.cut_optimization.service.TransitionManager;
import com.example.cut_optimization.service.mutation.SequenceMutator;
import com.example.cut_optimization.service.optimizators.AreaManager;
import com.example.cut_optimization.service.temperatureLowStrategy.TemperatureLowStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service("simulatedAnnealingStackingStrategy")
public class SimulationAnnealingStackingStrategy implements StackingStrategy {

    private final AreaManager areaManager;
    private final SequenceMutator sequenceMutator;
    private final TemperatureLowStrategy temperatureManager;
    private final TransitionManager transitionManager;
    private final ResultEvaluator resultEvaluator;

    @Autowired
    public SimulationAnnealingStackingStrategy(AreaManager areaManager, SequenceMutator sequenceMutator, TemperatureLowStrategy temperatureManager, TransitionManager transitionManager, ResultEvaluator resultEvaluator) {
        this.areaManager = areaManager;
        this.sequenceMutator = sequenceMutator;
        this.temperatureManager = temperatureManager;
        this.transitionManager = transitionManager;
        this.resultEvaluator = resultEvaluator;
    }

    @Override
    public void stack(InitialDataOptimization initialDataOptimization, boolean isStackingDetailsIntoOneWorkpiece) {
    }

    @Override
    public void stack(InitialDataOptimization initialData) {

        ResultStacking bestResultStacking = initialData.getBestResultStacking();

        List<StackingSequence> baseStackingSequences = bestResultStacking.getStackingSequences();
        List<Workpiece> usedWorkpieces = areaManager.getUsedWorkpieces(initialData.getWorkpieces(), baseStackingSequences);
        initialData.setWorkpieces(usedWorkpieces);

        initialData.getFreeAreas().clear();
        initialData.getOccupiedAreas().clear();

        double tmin = 0.001;
        int maxCounter = temperatureManager.getMaxCounter(initialData.getInitialTemperature(), tmin);
        int coolingPhaseStart = (int) (maxCounter * 0.67);
        int counter = 1;

        ResultStacking currentResultStacking = new ResultStacking();

        currentResultStacking.saveWayOfLayingAreas(0, initialData.getFreeAreas(), initialData.getOccupiedAreas());

        areaManager.stackDetailsWithStackingSequences(baseStackingSequences, initialData, currentResultStacking);

        if (currentResultStacking.isHasError()) {
            throw new CommonException("Error during first stacking after initial laying", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        while (counter < maxCounter) {
            if (counter > coolingPhaseStart) {
                baseStackingSequences = bestResultStacking.getStackingSequences();
            }

            currentResultStacking.restoreWayOfLayingAreas(0, initialData);

            List<StackingSequence> currentStackingSequences = sequenceMutator.mutate(baseStackingSequences, initialData.isDisableRotation());
            currentResultStacking.setStackingSequences(currentStackingSequences);
            currentResultStacking.setHasError(false);

            areaManager.stackDetailsWithStackingSequences(currentStackingSequences, initialData, currentResultStacking);

            if (initialData.getOccupiedAreas().size() != initialData.getDetails().size()) {
                currentResultStacking.setHasError(true);
            }
            if (currentResultStacking.isHasError()) {
                continue;
            }

            double currentTemperature = temperatureManager.lowTemperature(initialData.getInitialTemperature(), counter);
            if (currentTemperature < tmin) {
                break;
            }

            currentResultStacking.calculateStackingCoefficients(initialData.getOccupiedAreas(), initialData.getWorkpieces(), initialData.getFreeAreas());

            boolean allowToNewTransition = transitionManager.allowTransition(counter,
                                                                                                        coolingPhaseStart,
                                                                                                        currentTemperature,
                                                                                                        initialData.isUsePartialSheets(),
                                                                                                        currentResultStacking,
                                                                                                        bestResultStacking);

            if (allowToNewTransition) {
                currentResultStacking.saveWayOfLayingAreas(initialData.getDetails().size(), initialData.getFreeAreas(), initialData.getOccupiedAreas());

                bestResultStacking = resultEvaluator.compareAndUpdateTopResultWithCurrentResult(bestResultStacking,
                        currentResultStacking, initialData.isUsePartialSheets());

                baseStackingSequences = currentStackingSequences;
            }
            counter++;
        }
        initialData.setBestResultStacking(bestResultStacking);
        bestResultStacking.restoreWayOfLayingAreas(initialData.getDetails().size(), initialData);
    }
}
