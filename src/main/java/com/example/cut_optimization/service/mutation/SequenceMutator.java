package com.example.cut_optimization.service.mutation;

import com.example.cut_optimization.dto.StackingSequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class SequenceMutator implements Mutatable {

    private final Random random;

    @Autowired
    public SequenceMutator(Random random) {
        this.random = random;
    }

    public int getSequenceMutationFactor(int sequenceSize) {
        return (int) Math.round((double)sequenceSize / 10 + 0.5); //расчет интенсивности мутации деталей, на каждые 10 деталей +1 перестановка
    }


    @Override
    public List<StackingSequence> mutate(List<StackingSequence> stackingSequences, boolean isDisableRotation) {

        int sequenceMutationFactor =  getSequenceMutationFactor(stackingSequences.size());

        List<StackingSequence> stackingSequencesClone = stackingSequences.stream()
                .map(seq -> {
                    try {
                        return seq.clone();
                        } catch (CloneNotSupportedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());

        for (int i = 0; i < sequenceMutationFactor; i++) {

            int k = random.nextInt(stackingSequencesClone.size());
            int j = random.nextInt(stackingSequencesClone.size());

            if (k == j && stackingSequencesClone.size() > 1) {
                i --;
                continue;
            }
            Collections.swap(stackingSequencesClone, k, j);

            if (isDisableRotation) {
                continue;
            }

            boolean rotate1 = random.nextBoolean();
            boolean rotate2 = random.nextBoolean();
            stackingSequencesClone.get(j).setRotated(rotate1);
            stackingSequencesClone.get(k).setRotated(rotate2);

        }
        return stackingSequencesClone;
    }
}
