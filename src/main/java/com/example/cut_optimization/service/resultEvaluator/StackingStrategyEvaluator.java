package com.example.cut_optimization.service.resultEvaluator;

import com.example.cut_optimization.dto.ResultStacking;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StackingStrategyEvaluator implements Evaluatable{
    @Override
    public ResultStacking evaluate(ResultStacking topResult, ResultStacking currentResult, boolean isUsePartialSheets) {
        if (topResult == null) {
            topResult = currentResult.clone();
        } else {
            log.info("currentResultFreeAreaCount: {}, currentResultFreeAreaRatioByDetail: {}, currentResultOccupiedAreaRatioByDetail: {}," +
                            "currentResultFreeAreaRatioOfUsedWorkpieces: {}, currentResultOccupiedAreaRatio: {}",
                    currentResult.getStackingCoefficients().getFreeAreaCount(), currentResult.getStackingCoefficients().getFreeAreaRatioByDetail(),
                    currentResult.getStackingCoefficients().getOccupiedAreaRatioByDetail(),
                    currentResult.getStackingCoefficients().getFreeAreaRatioOfUsedWorkpieces(),
                    currentResult.getStackingCoefficients().getOccupiedAreaRatio());
            log.info("topResultFreeAreaCount: {}, topResultFreeAreaRatioByDetail: {}, topResultOccupiedAreaRatioByDetail: {}, "+
                            "topResultFreeAreaRatioOfUsedWorkpieces: {}, topResultOccupiedAreaRatio: {}",
                    topResult.getStackingCoefficients().getFreeAreaCount(), topResult.getStackingCoefficients().getFreeAreaRatioByDetail(),
                    topResult.getStackingCoefficients().getOccupiedAreaRatioByDetail(),
                    topResult.getStackingCoefficients().getFreeAreaRatioOfUsedWorkpieces(),
                    topResult.getStackingCoefficients().getOccupiedAreaRatio());
            if (isUsePartialSheets) {
                if (currentResultIsBetterThanTopResultByOccupiedAndFreeAreaRatio(currentResult, topResult)) {
                    topResult = currentResult.clone();
                }
            } else {
                if (currentResultIsBetterThanTopResultByOccupiedAndFreeAreaRatio(currentResult, topResult) ||
                    currentResultIsBetterThanTopResultByFreeAreaCount(currentResult, topResult)) {
                    topResult = currentResult.clone();
                }
            }
        }
        return topResult;
    }

    private boolean currentResultIsBetterThanTopResultByFreeAreaCount(ResultStacking currentResult, ResultStacking topResult) {
        return currentResult.getStackingCoefficients().getFreeAreaCount() < topResult.getStackingCoefficients().getFreeAreaCount();
    }

    private boolean currentResultIsBetterThanTopResultByOccupiedAndFreeAreaRatio(ResultStacking currentResult, ResultStacking topResult) {
        return currentResult.getStackingCoefficients().getOccupiedAreaRatio() > topResult.getStackingCoefficients().getOccupiedAreaRatio() ||
                currentResult.getStackingCoefficients().getOccupiedAreaRatio() == topResult.getStackingCoefficients().getOccupiedAreaRatio() &&
                        currentResult.getStackingCoefficients().getFreeAreaRatioOfUsedWorkpieces() >
                                topResult.getStackingCoefficients().getFreeAreaRatioOfUsedWorkpieces();
    }
}
