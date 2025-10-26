package com.example.cut_optimization.service;

import com.example.cut_optimization.dto.InitialDataOptimization;
import com.example.cut_optimization.dto.TypeOfMaterial;
import com.example.cut_optimization.dto.details.Detail;
import com.example.cut_optimization.dto.details.EndlessWorkpiece;
import com.example.cut_optimization.dto.details.Workpiece;
import com.example.cut_optimization.exception.CommonException;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@UtilityClass
public class PreliminaryChecker {

    public void initializePreliminarySettings(InitialDataOptimization initialDataOptimization) {
        checkNotEmptyDetails(initialDataOptimization);
        checkNotEmptyWorkpieces(initialDataOptimization);
        checkNotEmptyTemperature(initialDataOptimization);
        checkNotEmptyEndlessWorkpieces(initialDataOptimization);
        fillTypesOfMaterial(initialDataOptimization);
        checkSizeOfDetailsDotNotExceedSizeOfWorkpieces(initialDataOptimization);
        calculateArea(initialDataOptimization);
        checkWorkpicesSquareMoreThanDetailsSquare(initialDataOptimization);
        setDetailsAndWorkpiecesAmountAsOneAndSetId(initialDataOptimization);
        initFreeAreaAndOccupiedArea(initialDataOptimization);
        initAreaIdGenerator(initialDataOptimization);
    }

    private static void initAreaIdGenerator(InitialDataOptimization initialDataOptimization) {
        if (initialDataOptimization.getAreaIdGenerator() == null) {
            initialDataOptimization.setAreaIdGenerator(new AreaIdGenerator());
        }
    }

    private static void initFreeAreaAndOccupiedArea(InitialDataOptimization initialDataOptimization) {
        initialDataOptimization.setFreeAreas(new ArrayList<>());
        initialDataOptimization.setOccupiedAreas(new ArrayList<>());
    }


    private static void checkSizeOfDetailsDotNotExceedSizeOfWorkpieces(InitialDataOptimization initialDataOptimization) {

        for (TypeOfMaterial typeOfMaterial : initialDataOptimization.getTypesOfMaterial()) {

            Set<Detail> detailsByType = new HashSet<>(initialDataOptimization.filterByType(initialDataOptimization.getDetails(), typeOfMaterial));
            Set<Workpiece> workpiecesByType = new HashSet<>(initialDataOptimization.filterByType(initialDataOptimization.getWorkpieces(), typeOfMaterial));
            Set<EndlessWorkpiece> endlessWorkpiecesByType = new HashSet<>(initialDataOptimization.filterByType(initialDataOptimization.getEndlessWorkpieces(), typeOfMaterial));

            List<Detail> detailsCanNotStack = new ArrayList<>();

            for (Detail detail : detailsByType) {
                boolean canStack = false;
                for (Workpiece workpiece : workpiecesByType) {
                    if (workpiece.canStack(detail)) {
                        canStack = true;
                        break;
                    }
                }
                if (!canStack) {
                    detailsCanNotStack.add(detail);
                }
            }

            List<Detail> detailsToRemove = new ArrayList<>();

            detailsCanNotStack.stream()
                    .forEach(detail -> {
                        endlessWorkpiecesByType.stream()
                                .filter(endlessWorkpiece -> (endlessWorkpiece.canStack(detail)))
                                .findFirst()
                                .ifPresent(endlessWorkpiece -> {
                                    Workpiece workpiece = new Workpiece(endlessWorkpiece);
                                    workpiece.orientDetailVertically();
                                    initialDataOptimization.getWorkpieces().add(workpiece);
                                    detailsToRemove.add(detail);
                                });
                    });

            detailsCanNotStack.removeAll(detailsToRemove);

            if (!detailsCanNotStack.isEmpty()) {
                StringBuilder message = new StringBuilder();
                for (Detail detailCanNotStack : detailsCanNotStack) {
                    message.append(String.format("The size of the detail %.3f * %.3f is too large for the workpieces",
                            detailCanNotStack.getWidth(), detailCanNotStack.getHeight()));
                    message.append("\n");
                }
                if (!message.isEmpty()) {
                    throw new CommonException(message.toString(), HttpStatus.BAD_REQUEST);
                }
            }
        }
    }

    private static void fillTypesOfMaterial(InitialDataOptimization initialDataOptimization) {

        if (initialDataOptimization.getTypesOfMaterial() == null || initialDataOptimization.getTypesOfMaterial().isEmpty()) {

            TypeOfMaterial defaultTypeOfMaterial = new TypeOfMaterial("");

            List<TypeOfMaterial> typesOfMaterial = Arrays.asList(defaultTypeOfMaterial);

            initialDataOptimization.getDetails()
                    .forEach(detail -> {
                        detail.setTypeMaterial(defaultTypeOfMaterial);
                    });

            initialDataOptimization.getWorkpieces()
                    .forEach(workpiece -> {
                        workpiece.setTypeMaterial(defaultTypeOfMaterial);
                    });

            initialDataOptimization.getEndlessWorkpieces()
                    .forEach(endlessWorkpiece -> {
                        endlessWorkpiece.setTypeMaterial(defaultTypeOfMaterial);
                    });

            initialDataOptimization.setTypesOfMaterial(typesOfMaterial);

        } else {

            String message = "Type of material must be specified for each %s";

            initialDataOptimization.getDetails()
                    .forEach(detail -> {if (detail.getTypeMaterial() == null) {
                    throw new CommonException(String.format(message, "detail"), HttpStatus.BAD_REQUEST);}
                    });
            initialDataOptimization.getWorkpieces()
                    .forEach(workpiece -> {if (workpiece.getTypeMaterial() == null) {
                        throw new CommonException(String.format(message, "workpiece"), HttpStatus.BAD_REQUEST);}
                    });
            initialDataOptimization.getEndlessWorkpieces()
                    .forEach(endlessWorkpiece -> {if (endlessWorkpiece.getTypeMaterial() == null) {
                        throw new CommonException(String.format(message, "endless workpiece"), HttpStatus.BAD_REQUEST);}
                    });

        }
    }

    private static void checkNotEmptyEndlessWorkpieces(InitialDataOptimization initialDataOptimization) {
        if (initialDataOptimization.getEndlessWorkpieces() == null) {
            initialDataOptimization.setEndlessWorkpieces(new ArrayList<>());
        }
    }

    private static void checkWorkpicesSquareMoreThanDetailsSquare(InitialDataOptimization initialDataOptimization) {

        for (TypeOfMaterial typeOfMaterial : initialDataOptimization.getTypesOfMaterial()) {

            double detailsSquareByType = initialDataOptimization.calculateDetailsSquareByType(typeOfMaterial);
            double workpiecesSquareByType = initialDataOptimization.calculateWorkpiecesSquareByType(typeOfMaterial);

            while (detailsSquareByType * 3 > workpiecesSquareByType) {
                if (initialDataOptimization.filterByType(initialDataOptimization.getEndlessWorkpieces(), typeOfMaterial).isEmpty()) {
                    throw new CommonException("The total area of the workpieces may not be sufficient for efficient calculation. Add blanks to the workpieces table or to the virtual warehouse",
                            HttpStatus.BAD_REQUEST);
                }
                List<EndlessWorkpiece> endlessWorkpiecesByType = initialDataOptimization.filterByType(initialDataOptimization.getEndlessWorkpieces(), typeOfMaterial);
                endlessWorkpiecesByType
                        .forEach(endlessWorkpiece -> {
                            initialDataOptimization.getWorkpieces().add(new Workpiece(endlessWorkpiece));
                        });
                workpiecesSquareByType = initialDataOptimization.calculateWorkpiecesSquareByType(typeOfMaterial);
            }
        }

    }

    private static void checkNotEmptyTemperature(InitialDataOptimization initialDataOptimization) {
        if (initialDataOptimization.getInitialTemperature() == 0) {
            throw new CommonException("Initial temperature can't be zero", HttpStatus.BAD_REQUEST);
        }
    }

    private static void checkNotEmptyWorkpieces(InitialDataOptimization initialDataOptimization) {
        if (initialDataOptimization.getWorkpieces().isEmpty() && initialDataOptimization.getEndlessWorkpieces().isEmpty()) {
            throw new CommonException("Must be at least one workpiece or endless workpiece", HttpStatus.BAD_REQUEST);
        }
    }

    private static void checkNotEmptyDetails(InitialDataOptimization initialDataOptimization) {
        if (initialDataOptimization.getDetails().isEmpty()) {
            throw new CommonException("Must be at least one detail", HttpStatus.BAD_REQUEST);
        }
    }

    private void setDetailsAndWorkpiecesAmountAsOneAndSetId(InitialDataOptimization initialDataOptimization) {
        List<Detail> newDetails = new ArrayList<>();

        AtomicInteger idCounter = new AtomicInteger(1);
        initialDataOptimization.getDetails().stream()
                .flatMap(detail -> IntStream.range(0, detail.getQuantity())
                        .mapToObj(i -> {
                            Detail newDetail = new Detail(detail);
                            newDetail.setId(idCounter.getAndIncrement());
                            newDetail.setQuantity(1);
                            newDetail.setSquare(newDetail.calculateSquare());
                            return newDetail;
                        }))
                .forEach(newDetails::add);

        initialDataOptimization.setDetails(newDetails);

        idCounter.set(1);
        List<Workpiece> newWorkpieces = new ArrayList<>();

        initialDataOptimization.getWorkpieces().stream()
                .flatMap(workpiece -> IntStream.range(0, workpiece.getQuantity())
                        .mapToObj(i -> {
                            Workpiece newWorkpiece = new Workpiece(workpiece);
                            newWorkpiece.setId(idCounter.getAndIncrement());
                            newWorkpiece.setQuantity(1);
                            newWorkpiece.setSquare(newWorkpiece.calculateSquare());
                            return newWorkpiece;
                        }))
                .forEach(newWorkpieces::add);
        initialDataOptimization.setWorkpieces(newWorkpieces);
    }

    private void setIdAndQuantityAs1AndCalculateSquare(List<Workpiece> workpiece) {
        for (int i = 0; i < workpiece.size(); i++) {
            workpiece.get(i).setId(i+1);
            workpiece.get(i).setQuantity(1);
            workpiece.get(i).setSquare(workpiece.get(i).calculateSquare());
        }
    }

    private void calculateArea(InitialDataOptimization initialDataOptimization) {
        initialDataOptimization.calculateAllSquares();
    }
}
