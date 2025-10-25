package com.example.cut_optimization.service.mutation;

import com.example.cut_optimization.dto.StackingSequence;

import java.util.List;

public interface Mutatable {

    List<StackingSequence> mutate(
            List<StackingSequence> stackingSequences,
            boolean isDisableRotation
    );
}
