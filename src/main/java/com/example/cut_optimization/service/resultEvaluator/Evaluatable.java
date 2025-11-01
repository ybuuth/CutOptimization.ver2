package com.example.cut_optimization.service.resultEvaluator;

import com.example.cut_optimization.dto.ResultStacking;

public interface Evaluatable {
    ResultStacking evaluate(ResultStacking resultStacking1,ResultStacking resultStacking2, boolean isUsePartialSheets);
}
