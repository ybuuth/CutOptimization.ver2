package com.example.cut_optimization.service;

import com.example.cut_optimization.dto.ResultStacking;
import org.springframework.stereotype.Component;

@Component
public class TransitionManager {

    public boolean allowTransition(int counter, int coolingPhaseStart,
                                   double currentTemperature, boolean isUsePartialSheets,
                                   ResultStacking currentResultStacking, ResultStacking bestResultStacking) {
        boolean allowToNewTransition = false;
        if (isUsePartialSheets) {
            if (currentResultStacking.getStackingCoefficients().getFreeAreaRatio() > bestResultStacking.getStackingCoefficients().getFreeAreaRatio()) {
                allowToNewTransition = true;
            } else if (currentResultStacking.getStackingCoefficients().getFreeAreaRatio() < bestResultStacking.getStackingCoefficients().getFreeAreaRatio()) {
                if (counter < coolingPhaseStart) {
                    allowToNewTransition = shouldAcceptTransition(bestResultStacking.getStackingCoefficients().getFreeAreaRatio() -
                            currentResultStacking.getStackingCoefficients().getFreeAreaRatio(), currentTemperature);
                }
            }
        }
        if (bestResultStacking.getStackingCoefficients().getOccupiedAreaRatio() < currentResultStacking.getStackingCoefficients().getOccupiedAreaRatio() ||
                bestResultStacking.getStackingCoefficients().getOccupiedAreaRatio() == currentResultStacking.getStackingCoefficients().getOccupiedAreaRatio() &&
                        bestResultStacking.getStackingCoefficients().getFreeAreaRatio() < currentResultStacking.getStackingCoefficients().getFreeAreaRatio()) {
            allowToNewTransition = true;
        } else if (bestResultStacking.getStackingCoefficients().getOccupiedAreaRatio() > currentResultStacking.getStackingCoefficients().getOccupiedAreaRatio() ||
                bestResultStacking.getStackingCoefficients().getOccupiedAreaRatio() == currentResultStacking.getStackingCoefficients().getOccupiedAreaRatio() &&
                        bestResultStacking.getStackingCoefficients().getFreeAreaRatio() > currentResultStacking.getStackingCoefficients().getFreeAreaRatio()) {
            if (counter < coolingPhaseStart) {
                allowToNewTransition = shouldAcceptTransition(bestResultStacking.getStackingCoefficients().getFreeAreaRatio() -
                        currentResultStacking.getStackingCoefficients().getFreeAreaRatio(), currentTemperature);
            }
        }
        return allowToNewTransition;
    }

    private boolean shouldAcceptTransition(double energyDelta, double currentTemperature) {

        double pow = -(energyDelta / currentTemperature);

        double transitionProbability = Math.exp(pow);

        return transitionProbability > Math.random();
    }
}
