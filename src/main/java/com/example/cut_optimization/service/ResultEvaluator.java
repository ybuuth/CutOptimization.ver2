package com.example.cut_optimization.service;

import com.example.cut_optimization.dto.ResultStacking;
import org.springframework.stereotype.Service;

@Service
public class ResultEvaluator {

    public ResultStacking selectBestStackingResultBetweenOneWorkpieceResultAndMultipleWorkpieceResult(ResultStacking oneWorkpieceResult,
                                                                                                      ResultStacking multipleWorkpiecesResult,
                                                                                                      boolean isUsePartialSheets) {

        if (isUsePartialSheets) {
            return multipleWorkpiecesResult;
        }

        if (!oneWorkpieceResult.isHasError()) {
            if (oneWorkpieceResult.getStackingCoefficients().getAreaFillingFactor() >
                    multipleWorkpiecesResult.getStackingCoefficients().getAreaFillingFactor() ||
                    (oneWorkpieceResult.getStackingCoefficients().getAreaFillingFactor() ==
                            multipleWorkpiecesResult.getStackingCoefficients().getAreaFillingFactor() &&
                            oneWorkpieceResult.getStackingCoefficients().getFreeAreaRatioOfUsedWorkpieces() >=
                                    multipleWorkpiecesResult.getStackingCoefficients().getFreeAreaRatioOfUsedWorkpieces())) {
                return oneWorkpieceResult.clone();
            }
        }
        return multipleWorkpiecesResult.clone();
    }

    public ResultStacking evaluateAndUpdateTopResultByDetail(ResultStacking topResult,
                                                     ResultStacking currentResult,
                                                     boolean isStackingDetailsIntoOneWorkpiece) {

        if (topResult == null) {
            topResult = currentResult.clone();
        } else {

            if (isStackingDetailsIntoOneWorkpiece) {
                //чем меньше свободного пространства на заготовке, тем лучше для листа в который набиваем детали
                if (currentResult.getStackingCoefficients().getFreeAreaCount() <= topResult.getStackingCoefficients().getFreeAreaCount() &&
                        currentResult.getStackingCoefficients().getFreeAreaRatioByDetail() >
                                topResult.getStackingCoefficients().getFreeAreaRatioByDetail()) {
                    topResult = currentResult.clone();
                }
            } else {
                if (currentResult.getStackingCoefficients().getOccupiedAreaRatioByDetail() >
                        topResult.getStackingCoefficients().getOccupiedAreaRatioByDetail()) {

                    topResult = currentResult.clone();
                }
            }
        }
        return topResult;
    }

    public ResultStacking compareAndUpdateTopResultWithCurrentResult(ResultStacking topResult,
                                                             ResultStacking currentResult,
                                                             boolean isUsePartialSheets) {

        if (topResult == null) {
            topResult = currentResult.clone();
        } else {
            if (isUsePartialSheets) {
                if (currentResult.getStackingCoefficients().getFreeAreaRatio() > topResult.getStackingCoefficients().getFreeAreaRatio()) {
                    topResult = currentResult.clone();
                }
            } else {
                if (currentResult.getStackingCoefficients().getAreaFillingFactor() >
                        topResult.getStackingCoefficients().getAreaFillingFactor() ||
                        (currentResult.getStackingCoefficients().getAreaFillingFactor() ==
                                topResult.getStackingCoefficients().getAreaFillingFactor() &&
                                currentResult.getStackingCoefficients().getFreeAreaRatioOfUsedWorkpieces() >=
                                        topResult.getStackingCoefficients().getFreeAreaRatioOfUsedWorkpieces())) {
                    topResult = currentResult.clone();
                }
            }
        }
        return topResult;
    }
}
