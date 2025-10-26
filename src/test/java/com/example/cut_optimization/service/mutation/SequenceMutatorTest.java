package com.example.cut_optimization.service.mutation;

import com.example.cut_optimization.dto.StackingSequence;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SequenceMutatorTest {

    private static SequenceMutator sequenceMutator;

    @BeforeAll
    public static void setUp() {
        sequenceMutator = new SequenceMutator(new Random());
    }

    @Test
    void checkMutationFactor() {
        int factor = sequenceMutator.getSequenceMutationFactor(3);
        int expected = 1;
        assertEquals(expected, factor);

        factor = sequenceMutator.getSequenceMutationFactor(15);
        expected = 2;
        assertEquals(expected, factor);

        factor = sequenceMutator.getSequenceMutationFactor(13);
        expected = 2;
        assertEquals(expected, factor);

        factor = sequenceMutator.getSequenceMutationFactor(23);
        expected = 3;
        assertEquals(expected, factor);
    }

    @Test
    void checkChildSequenceNotEqualsParent() {
        List<StackingSequence> parentSequences = List.of(new StackingSequence(1,1, false),
                new StackingSequence(1,2, false),
                new StackingSequence(1,3, false));
        List<StackingSequence> childSequences = sequenceMutator.mutate(parentSequences, false);

        assertNotEquals(parentSequences, childSequences);

        parentSequences = List.of(new StackingSequence(1,1, false),
                new StackingSequence(1,2, false));
        childSequences = sequenceMutator.mutate(parentSequences, false);

        assertNotEquals(parentSequences, childSequences);
    }

    @Test
    void checkNotMutateIfSequencesSizeIsOne() {
        List<StackingSequence> parentSequences = List.of(new StackingSequence(1,1, false));

        List<StackingSequence> childSequences = sequenceMutator.mutate(parentSequences, true);

        assertEquals(parentSequences, childSequences);
    }
}