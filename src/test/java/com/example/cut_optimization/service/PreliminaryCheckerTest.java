package com.example.cut_optimization.service;

import com.example.cut_optimization.dto.*;
import com.example.cut_optimization.dto.details.Detail;
import com.example.cut_optimization.dto.details.EndlessWorkpiece;
import com.example.cut_optimization.dto.details.Workpiece;
import com.example.cut_optimization.exception.CommonException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PreliminaryCheckerTest {

    private static InitialDataOptimization initialDataOptimization;

    @BeforeEach
    void beforeEach() {
        initialDataOptimization = new InitialDataOptimization();
        initialDataOptimization.setInitialTemperature(10);
        List<Detail> details = new ArrayList<>();
        Detail detail = new Detail();
        detail.setHeight(24.3);
        detail.setWidth(21.3);
        detail.setQuantity(4);
        detail.setTypeMaterial(new TypeOfMaterial("wood"));
        details.add(detail);
        detail = new Detail();
        detail.setHeight(11.3);
        detail.setWidth(13.3);
        detail.setQuantity(3);
        detail.setTypeMaterial(new TypeOfMaterial("wood"));
        details.add(detail);

        List<Workpiece> workpieces = new ArrayList<>();
        Workpiece workpiece = new Workpiece();
        workpiece.setHeight(49.3);
        workpiece.setWidth(45.3);
        workpiece.setQuantity(15);
        workpiece.setTypeMaterial(new TypeOfMaterial("iron"));
        workpieces.add(workpiece);
        initialDataOptimization.setDetails(details);
        initialDataOptimization.setWorkpieces(workpieces);

        List<EndlessWorkpiece> endlessWorkpieces = new ArrayList<>();
        EndlessWorkpiece endlessWorkpiece = new EndlessWorkpiece();
        endlessWorkpiece.setHeight(100);
        endlessWorkpiece.setWidth(100);
        endlessWorkpiece.setTypeMaterial(new TypeOfMaterial("iron"));
        endlessWorkpieces.add(endlessWorkpiece);
        initialDataOptimization.setEndlessWorkpieces(endlessWorkpieces);
    }

    @Test
    public void checkDetailsAndWorkpiecesAmountAsOne() {
        Workpiece newWorkpiece = new Workpiece();
        newWorkpiece.setHeight(49.3);
        newWorkpiece.setWidth(45.3);
        newWorkpiece.setQuantity(15);
        newWorkpiece.setTypeMaterial(new TypeOfMaterial("wood"));
        initialDataOptimization.getWorkpieces().add(newWorkpiece);

        PreliminaryChecker.initializePreliminarySettings(initialDataOptimization);

        assertEquals(7, initialDataOptimization.getDetails().size());
        initialDataOptimization.getDetails()
                .forEach(detail -> assertEquals(1, detail.getQuantity()));
        initialDataOptimization.getDetails()
                .forEach(detail -> assertThat(detail.getSquare()).isGreaterThan(0));
        initialDataOptimization.getWorkpieces()
                .forEach(workpiece -> assertThat(workpiece.getSquare()).isGreaterThan(0));

        assertEquals(30, initialDataOptimization.getWorkpieces().size());
        initialDataOptimization.getWorkpieces()
                .forEach(workpiece -> {
                    assertEquals(1, workpiece.getQuantity());
                    assertThat(workpiece.getId()).isGreaterThan(0);
                });
    }

    @Test
    public void checkWorkpicesSquareMoreThanDetailsSquareExpectException() {

        TypeOfMaterial typeOfMaterial = new TypeOfMaterial("wood");
        initialDataOptimization.setTypesOfMaterial(Arrays.asList(typeOfMaterial));

        Workpiece workpiece = new Workpiece();
        workpiece.setHeight(30.3);
        workpiece.setWidth(30.3);
        workpiece.setQuantity(1);
        workpiece.setTypeMaterial(new TypeOfMaterial("wood"));
        initialDataOptimization.getWorkpieces().add(workpiece);

        CommonException exception = assertThrows(CommonException.class, () -> {PreliminaryChecker.initializePreliminarySettings(initialDataOptimization);});
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals("The total area of the workpieces may not be sufficient for efficient calculation. Add blanks to the workpieces table or to the virtual warehouse",
                exception.getMessage());

    }

    @Test
    public void checkWorkpicesSquareMoreThanDetailsSquare() {
        TypeOfMaterial typeOfMaterial = new TypeOfMaterial("wood");
        initialDataOptimization.setTypesOfMaterial(Arrays.asList(typeOfMaterial));

        initialDataOptimization.getEndlessWorkpieces().forEach(workpiece -> {
            workpiece.setTypeMaterial(typeOfMaterial);
        });
        assertDoesNotThrow(() -> {PreliminaryChecker.initializePreliminarySettings(initialDataOptimization);});
        int expected = 17;
        assertEquals(expected, initialDataOptimization.getWorkpieces().size());
        expected = 2;
        assertEquals(expected, initialDataOptimization.filterByType(initialDataOptimization.getWorkpieces(), typeOfMaterial).size());
    }

    @Test
    public void checkSizeOfDetailsDotNotExceedSizeOfWorkpiecesExpectException() {

        TypeOfMaterial typeOfMaterial = new TypeOfMaterial("wood");
        initialDataOptimization.setTypesOfMaterial(Arrays.asList(typeOfMaterial));

        Workpiece workpiece = new Workpiece();
        workpiece.setHeight(13.3);
        workpiece.setWidth(20.3);
        workpiece.setQuantity(1);
        workpiece.setTypeMaterial(new TypeOfMaterial("wood"));
        initialDataOptimization.getWorkpieces().add(workpiece);

        CommonException exception = assertThrows(CommonException.class, () -> {PreliminaryChecker.initializePreliminarySettings(initialDataOptimization);});
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals("The size of the detail 21,300 * 24,300 is too large for the workpieces\n",
                exception.getMessage());
    }

    @Test
    public void checkSizeOfDetailsDotExceedSizeOfWorkpiecesExpectAddEndlessWorkpiect() {
        TypeOfMaterial typeOfMaterial = new TypeOfMaterial("plastics");
        initialDataOptimization.setTypesOfMaterial(Arrays.asList(typeOfMaterial));

        Detail detail = new Detail();
        detail.setHeight(1);
        detail.setWidth(2);
        detail.setQuantity(1);
        detail.setTypeMaterial(typeOfMaterial);
        initialDataOptimization.getDetails().add(detail);

        Workpiece workpiece = new Workpiece();
        workpiece.setHeight(1);
        workpiece.setWidth(1);
        workpiece.setQuantity(1);
        workpiece.setTypeMaterial(typeOfMaterial);
        initialDataOptimization.getWorkpieces().add(workpiece);

        EndlessWorkpiece endlessWorkpiece = new EndlessWorkpiece();
        endlessWorkpiece.setHeight(1);
        endlessWorkpiece.setWidth(2);
        endlessWorkpiece.setTypeMaterial(typeOfMaterial);
        initialDataOptimization.getEndlessWorkpieces().add(endlessWorkpiece);

        int expected = 4; // ожидаю что будет (1+1*3) заготовки из дерева, три из них добавится из бесконечной заготовки т.к. площадь должна быть
        // в 3 раза больше площади детали
        PreliminaryChecker.initializePreliminarySettings(initialDataOptimization);
        Set<Workpiece> workpiecesByType = initialDataOptimization.getWorkpieces().stream()
                .filter(workpiece1 -> workpiece1.getTypeMaterial().equals(typeOfMaterial))
                .collect(Collectors.toSet());
        assertEquals(expected, workpiecesByType.size());
    }

    @Test
    public void checkFreeAreaNotNull() {
        PreliminaryChecker.initializePreliminarySettings(initialDataOptimization);
        assertThat(initialDataOptimization.getFreeAreas()).isNotNull();
    }

    @Test
    public void checkOccupiedAreaNotNull() {
        PreliminaryChecker.initializePreliminarySettings(initialDataOptimization);
        assertThat(initialDataOptimization.getOccupiedAreas()).isNotNull();
    }

    @Test
    public void checkSortAreas() {
        initialDataOptimization.getDetails().forEach(detail -> {
            detail.setSquare(detail.getHeight() * detail.getWidth());
                });
        initialDataOptimization.getDetails().sort((o1, o2) -> (o1.getSquare() > o2.getSquare()) ? 1 : -1);
        System.out.println((initialDataOptimization.getDetails()));
    }
}