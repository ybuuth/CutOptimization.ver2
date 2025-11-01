package com.example.cut_optimization.service.optimizators;

import com.example.cut_optimization.dto.*;
import com.example.cut_optimization.dto.areas.FreeArea;
import com.example.cut_optimization.dto.areas.OccupiedArea;
import com.example.cut_optimization.dto.areas.SurroundingAreasInfo;
import com.example.cut_optimization.dto.details.Detail;
import com.example.cut_optimization.dto.details.Workpiece;
import com.example.cut_optimization.dto.results.AreaPlacementResult;
import com.example.cut_optimization.dto.AreaIdGenerator;
import com.example.cut_optimization.service.FreeAreaSeeker;
import com.example.cut_optimization.service.resultEvaluator.Evaluatable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AreaManager {

    private final AreaMerger areaMerger;
    private final FreeAreaSeeker freeAreaSeeker;
    private final Evaluatable evaluator;

    @Autowired
    public AreaManager(AreaMerger areaMerger, FreeAreaSeeker freeAreaSeeker, Evaluatable evaluator) {
        this.areaMerger = areaMerger;
        this.freeAreaSeeker = freeAreaSeeker;
        this.evaluator = evaluator;
    }

    public List<FreeArea> getFreeAreasSuitableForDetail(Detail detail, List<FreeArea> freeAreas, boolean isDisableRotation) {
        return freeAreaSeeker.getFreeAreasSuitableForDetail(detail, freeAreas, isDisableRotation);
    }

    public void stackDetailIntoFreeAreaCompactlyWithRotation(Detail detail, FreeArea freeArea, InitialDataOptimization initialData) {

        setDetailAlongArea(detail, freeArea);

        if (freeArea.canStackAcross(detail)) {
            detail.rotate();
        }

        stackDetailIntoFreeArea(detail, freeArea, initialData);

    }

    public void stackDetailIntoFreeArea(Detail detail, FreeArea freeArea,
                                                                 InitialDataOptimization initialData) {

        boolean canPlace = initialData.isDisableRotation() ? freeArea.canStackAlong(detail) : freeArea.canStack(detail);
        if (!canPlace) {
            return;
        }
        if (!initialData.isDisableRotation() && !freeArea.canStackAlong(detail) && freeArea.canStackAcross(detail)) {
            detail.rotate();
        }

        AreaPlacementResult areaPlacementResult = addDetailToWorkpiece(detail, freeArea, initialData.getSawCutWidth(), initialData.getAreaIdGenerator());

        updateInitialData(initialData, areaPlacementResult);
    }


    public void enlargeAndCutOffLowerAreaVertically(List<Workpiece> workpieces,
                                                    List<FreeArea> freeAreas,
                                                    List<OccupiedArea> occupiedAreas,
                                                    double sawCutWidth) {

        Set<Integer> workpieceIds = occupiedAreas.stream()
                .map(OccupiedArea::getWorkpieceId)
                .collect(Collectors.toSet());

        for (Integer workpieceId : workpieceIds) {

            Workpiece currentWorkpiece = workpieces.stream()
                    .filter(w -> w.getId() == workpieceId)
                    .findFirst()
                    .orElse(null);
            assert currentWorkpiece != null;

            enlargeAndCutOffLowerAreaVertically(currentWorkpiece, freeAreas, occupiedAreas, sawCutWidth);
        }
    }

    private void enlargeAndCutOffLowerAreaVertically(Workpiece workpiece,
                                                     List<FreeArea> freeAreas,
                                                     List<OccupiedArea> occupiedAreas,
                                                     double sawCutWidth) {

        List<FreeArea> freeAreasToRemove = new ArrayList<>();
        boolean hasEnlarge;
        do {

            freeAreasToRemove.clear();
            hasEnlarge = false;

            List<FreeArea> freeAreasByCurrentWorkpiece = freeAreas.stream()
                    .filter(fa -> fa.getWorkpieceId() == workpiece.getId())
                    .collect(Collectors.toList());

            List<OccupiedArea> occupiedAreasByCurrentWorkpiece = occupiedAreas.stream()
                    .filter(oa -> oa.getWorkpieceId() == workpiece.getId())
                    .collect(Collectors.toList());

            for (FreeArea freeArea : freeAreasByCurrentWorkpiece) {

                hasEnlarge = areaMerger.mergeLowerFreeAreaVerticallyWithOverlapRight(sawCutWidth, freeArea, freeAreasByCurrentWorkpiece, hasEnlarge);

                if (hasEnlarge) {
                    break;
                }

                //объединим области находящиеся на одной горизонтали равные по высоте
                hasEnlarge = areaMerger.mergeHorizontalAdjacentFreeAreas(sawCutWidth, freeArea,
                                                                                    freeAreasByCurrentWorkpiece,
                                                                                    freeAreasToRemove,
                                                                                    hasEnlarge);

                if (hasEnlarge) {
                    break;
                }
                // Если свободная область лежит на всю ширину детали, то ее можно опустить вниз если она не касается нижней части заготовки,
                // а занятые области поднять на высоту свободной области
                hasEnlarge = areaMerger.moveFullWidthFreeAreaDownWithLiftingLowerAreas(freeArea,
                                                                                                freeAreasByCurrentWorkpiece,
                                                                                                occupiedAreasByCurrentWorkpiece,
                                                                                                sawCutWidth,
                                                                                                workpiece);
                if (hasEnlarge) {
                    break;
                }

                //объединим области находящиеся на одной вертикали, равные по ширине
                hasEnlarge = areaMerger.mergeVerticalAdjacentFreeAreas(sawCutWidth,
                                                                                freeArea,
                                                                                freeAreasByCurrentWorkpiece,
                                                                                freeAreasToRemove,
                                                                                hasEnlarge);
                if (hasEnlarge) {
                    break;
                }
            }

            hasEnlarge = hasEnlarge || !freeAreasToRemove.isEmpty();

            freeAreas.removeAll(freeAreasToRemove);

        } while (hasEnlarge);
    }

    public List<Workpiece> getUsedWorkpieces(List<Workpiece> workpieces, List<StackingSequence> stackingSequences) {

        return stackingSequences.stream()
                .map(StackingSequence::getWorkpieceId)
                .distinct()
                .flatMap(id -> workpieces.stream()
                        .filter(w -> w.getId() == id))
                .collect(Collectors.toList());
    }

    private void updateInitialData(InitialDataOptimization initialData, AreaPlacementResult areaPlacementResult) {

        initialData.getFreeAreas().remove(areaPlacementResult.getAreaToRemove());

        if (areaPlacementResult.getRightFreeArea() != null) {
            initialData.getFreeAreas().add(areaPlacementResult.getRightFreeArea());
        }
        if (areaPlacementResult.getBottomFreeArea() != null) {
            initialData.getFreeAreas().add(areaPlacementResult.getBottomFreeArea());
        }
        if (areaPlacementResult.getOccupiedArea() != null) {
            initialData.getOccupiedAreas().add(areaPlacementResult.getOccupiedArea());
        }
    }

    private void setDetailAlongArea(Detail detail, FreeArea area) {

        if (area.getHeight() > area.getWidth()) {
            if (detail.getHeight() < detail.getWidth()) {
                detail.rotate();
            }
        } else {
            if (detail.getWidth() < detail.getHeight()) {
                detail.rotate();
            }
        }
    }

    private AreaPlacementResult addDetailToWorkpiece(Detail detail, FreeArea freeArea, double sawCutWidth, AreaIdGenerator areaIdGenerator) {

        OccupiedArea occupiedArea = new OccupiedArea(detail, freeArea, areaIdGenerator.nextAreaId(), false);

        occupiedArea.setDetailId(detail.getId());
        occupiedArea.setRotated(detail.isRotated());

        AreaPlacementResult result = AreaPlacementResult.builder()
                .occupiedArea(occupiedArea)
                .build();

        FreeArea freeAreaHorizontalCut = new FreeArea(detail.getHeight(), freeArea.getWidth() - detail.getWidth() - sawCutWidth, freeArea.getUp(),
                freeArea.getLeft() + detail.getWidth() + sawCutWidth, freeArea.getTypeMaterial(),
                freeArea.getWorkpieceId(), areaIdGenerator.nextAreaId(), freeArea.getAreaGroupId(), false);


        if (freeAreaHorizontalCut.getHeight() > 0 && freeAreaHorizontalCut.getWidth() > 0) {
            result.setRightFreeArea(freeAreaHorizontalCut);
        }

        FreeArea freeAreaVerticalCut = new FreeArea(freeArea.getHeight() - detail.getHeight() - sawCutWidth, freeArea.getWidth(),
                freeArea.getUp() + detail.getHeight() + sawCutWidth, freeArea.getLeft(), freeArea.getTypeMaterial(),
                freeArea.getWorkpieceId(), areaIdGenerator.nextAreaId(), 0, freeArea.isGroupArea());

        if (freeAreaVerticalCut.getHeight() > 0 && freeAreaVerticalCut.getWidth() > 0) {
            if (freeAreaVerticalCut.getLeft() == 0) {
                freeAreaVerticalCut.setAreaGroupId(areaIdGenerator.nextGroupAreaId());
            }
            result.setBottomFreeArea(freeAreaVerticalCut);
        }
        result.setAreaToRemove(freeArea);

        return result;
    }

    public void stackDetailsWithStackingSequences(List<StackingSequence> stackingSequences,
                                                  InitialDataOptimization initialData,
                                                  ResultStacking currentResultStacking) {

        PossibleFreeAreas possibleFreeAreas;

        List<Detail> details = initialData.getDetails();

        Detail detail1 = getDetailById(stackingSequences.get(0).getDetailId(), details);

        if (initialData.isUsePartialSheets()) {
            possibleFreeAreas = createPossibleFreeAreas(initialData.getWorkpieces(), initialData.getAreaIdGenerator(), detail1);
        } else {
            possibleFreeAreas = createPossibleFreeAreas(initialData.getWorkpieces(), initialData.getAreaIdGenerator());
        }

        addAllFreeAreasFromPossibleFreeAreas(initialData, possibleFreeAreas);

        for (StackingSequence stackingSequence : stackingSequences) {

            Detail detailOriginal = getDetailById(stackingSequence.getDetailId(), details);

            Detail detail = new Detail(detailOriginal);

            if (stackingSequence.isRotated()) {
                detail.rotate();
            }

            Optional<FreeArea> optionalFreeArea = freeAreaSeeker.getSuitableFreeArea(detail, possibleFreeAreas);
            if (optionalFreeArea.isEmpty()) {
                currentResultStacking.setHasError(true);
                return;
            }

            FreeArea freeArea = optionalFreeArea.get();

            AreaPlacementResult areaPlacementResult = addDetailToWorkpiece(detail, freeArea, initialData.getSawCutWidth(), initialData.getAreaIdGenerator());

            updateInitialData(initialData, areaPlacementResult);
            updatePossibleFreeAreas(possibleFreeAreas, areaPlacementResult);
        }

        enlargeAndCutOffLowerAreaVertically(initialData.getWorkpieces(), initialData.getFreeAreas(), initialData.getOccupiedAreas(), initialData.getSawCutWidth());
    }

    private void addAllFreeAreasFromPossibleFreeAreas(InitialDataOptimization initialData, PossibleFreeAreas possibleFreeAreas) {

        initialData.getFreeAreas().clear();

        for (FreeArea freeArea : possibleFreeAreas.getFreeAreasLowPriority()) {
            initialData.getFreeAreas().add(freeArea);
        }
        for (FreeArea freeArea : possibleFreeAreas.getFreeAreasMediumPriority()) {
            initialData.getFreeAreas().add(freeArea);
        }
        for (FreeArea freeArea : possibleFreeAreas.getFreeAreasHighPriority()) {
            initialData.getFreeAreas().add(freeArea);
        }
    }

    private void updatePossibleFreeAreas(PossibleFreeAreas possibleFreeAreas, AreaPlacementResult areaPlacementResult) {
        if (areaPlacementResult.getRightFreeArea() != null) {
            possibleFreeAreas.getFreeAreasHighPriority().add(areaPlacementResult.getRightFreeArea());
        }
        if (areaPlacementResult.getBottomFreeArea() != null) {
            possibleFreeAreas.getFreeAreasMediumPriority().add(areaPlacementResult.getBottomFreeArea());
        }
    }

    private PossibleFreeAreas createPossibleFreeAreas(List<Workpiece> workpieces, AreaIdGenerator areaIdGenerator, Detail detail) {

        PossibleFreeAreas possibleFreeAreas = createPossibleFreeAreas();

        for (Workpiece w : workpieces) {
            if (w.canStackAlong(detail)) {
                FreeArea freeArea = new FreeArea(w, areaIdGenerator.nextAreaId(), 0, false);
                possibleFreeAreas.getFreeAreasLowPriority().add(freeArea);
            }
        }
        return possibleFreeAreas;
    }

    private PossibleFreeAreas createPossibleFreeAreas(List<Workpiece> workpieces, AreaIdGenerator areaIdGenerator) {

        PossibleFreeAreas possibleFreeAreas = createPossibleFreeAreas();

        for (Workpiece w : workpieces) {
            FreeArea freeArea = new FreeArea(w, areaIdGenerator.nextAreaId(), 0, false);
            possibleFreeAreas.getFreeAreasLowPriority().add(freeArea);
        }
        return possibleFreeAreas;
    }

    private PossibleFreeAreas createPossibleFreeAreas() {

        PossibleFreeAreas possibleFreeAreas = new PossibleFreeAreas();

        possibleFreeAreas.setFreeAreasHighPriority(new ArrayList<>());
        possibleFreeAreas.setFreeAreasLowPriority(new ArrayList<>());
        possibleFreeAreas.setFreeAreasMediumPriority(new ArrayList<>());
        return possibleFreeAreas;
    }

    /**
     * Пытается переместить занятую область в свободную с меньшей площадью,
     * чтобы улучшить коэффициент заполнения.
     */
    public boolean moveToSmallerFreeArea(OccupiedArea occupiedArea, Workpiece currentWorkpiece, InitialDataOptimization initialData, ResultStacking bestResultStacking) {

        List<OccupiedArea> occupiedAreas = initialData.getOccupiedAreas();
        List<FreeArea> freeAreas = initialData.getFreeAreas();
        List<Workpiece> workpieces = initialData.getWorkpieces();
        List<Detail> details = initialData.getDetails();
        boolean disableRotation = initialData.isDisableRotation();

        SurroundingAreasInfo surroundingAreasInfo = getSurroundingAreasInfo(occupiedArea, currentWorkpiece, initialData);

        if (!surroundingAreasInfo.isFreeAreaOnRight() && !surroundingAreasInfo.isAtRightEdgeOfWorkpiece()) {
            return false;
        }

        Detail detail = occupiedArea.toDetail();
        List<FreeArea> freeAreasSuitableForDetail = freeAreaSeeker.getFreeAreasSuitableForDetail(detail, freeAreas, disableRotation);

        removeSurroundingAreasFromSuitableAreas(freeAreasSuitableForDetail, surroundingAreasInfo);

        if (freeAreasSuitableForDetail.isEmpty()) {
            return false;
        }

        FreeArea tempFreeArea = createFreeArea(occupiedArea, surroundingAreasInfo, initialData).orElse(null);
        if (tempFreeArea == null) {
            return false;
        }

        freeAreasSuitableForDetail = freeAreasSuitableForDetail.stream()
                .sorted(Comparator.comparingDouble(FreeArea::getSquare).thenComparing(FreeArea::getUp).thenComparing(FreeArea::getLeft))
                .collect(Collectors.toList());

        FreeArea smallerFreeArea = null;
        for (FreeArea freeArea : freeAreasSuitableForDetail) {
            if (freeArea.getAreaId() == tempFreeArea.getAreaId()) {
                continue;
            }
            if (freeArea.getSquare() < tempFreeArea.getSquare()) {
                smallerFreeArea = freeArea;
            } else {
                continue;
            }
            smallerFreeArea.setGroupArea(occupiedArea.isGroupArea());
            break;
        }

        if (smallerFreeArea == null) {
            return false;
        }

        if (!smallerFreeArea.isGroupArea()) {

            FreeArea freeArea = new FreeArea(occupiedArea, 0, 0, false);
            freeAreas.add(freeArea);

            occupiedAreas.remove(occupiedArea);

            stackDetailIntoFreeAreaCompactlyWithRotation(detail, smallerFreeArea, initialData);
        }

        enlargeAndCutOffLowerAreaVertically(currentWorkpiece, freeAreas, occupiedAreas, initialData.getSawCutWidth());

        ResultStacking resultStacking = new ResultStacking();
        resultStacking.calculateStackingCoefficients(occupiedAreas, workpieces, freeAreas);

        resultStacking.saveWayOfLayingAreas(details.size(), initialData.getFreeAreas(), initialData.getOccupiedAreas());

        bestResultStacking = evaluator.evaluate(bestResultStacking,
                resultStacking, initialData.isUsePartialSheets());

        initialData.setBestResultStacking(bestResultStacking);

        return true;
    }

    private SurroundingAreasInfo getSurroundingAreasInfo(OccupiedArea occupiedArea,
                                                         Workpiece currentWorkpiece,
                                                         InitialDataOptimization initialData) {

        List<OccupiedArea> occupiedAreas = initialData.getOccupiedAreas();
        List<FreeArea> freeAreas = initialData.getFreeAreas();
        double sawCutWidth = initialData.getSawCutWidth();

        SurroundingAreasInfo surroundingAreasInfo = new SurroundingAreasInfo();

        if (occupiedArea.getLeft() + occupiedArea.getWidth() == currentWorkpiece.getWidth()) {
            surroundingAreasInfo.setAtRightEdgeOfWorkpiece(true);
        } else {
            List<FreeArea> freeAreasOnTheRight = freeAreas.stream()
                    .filter(fa -> fa.getLeft() == occupiedArea.getLeft() + occupiedArea.getWidth() + sawCutWidth &&
                            fa.getWorkpieceId() == currentWorkpiece.getId() &&
                            fa.getUp() == occupiedArea.getUp())
                    .collect(Collectors.toList());
            surroundingAreasInfo.setFreeAreasOnRight(freeAreasOnTheRight);
            surroundingAreasInfo.setFreeAreaOnRight(!freeAreasOnTheRight.isEmpty());

            List<OccupiedArea> occupiedAreasOnTheRight = occupiedAreas.stream()
                    .filter(oa -> oa.getLeft() == occupiedArea.getLeft() + occupiedArea.getWidth() + sawCutWidth &&
                            oa.getWorkpieceId() == currentWorkpiece.getId() &&
                            oa.getUp() == occupiedArea.getUp())
                    .collect(Collectors.toList());
            surroundingAreasInfo.setOccupiedAreasOnRight(occupiedAreasOnTheRight);
        }

        if (occupiedArea.getUp() + occupiedArea.getHeight() == currentWorkpiece.getHeight()) {
            surroundingAreasInfo.setAtBottomEdgeOfWorkpiece(true);
        } else {

            List<FreeArea> freeAreasOnTheBottom = freeAreas.stream()
                    .filter(fa -> fa.getUp() == occupiedArea.getUp() + occupiedArea.getHeight() + sawCutWidth &&
                            fa.getWorkpieceId() == currentWorkpiece.getId() &&
                            (!surroundingAreasInfo.isAtRightEdgeOfWorkpiece() ||
                                    fa.getLeft() == occupiedArea.getLeft()) &&
                            fa.getWidth() == occupiedArea.getWidth())
                    .collect(Collectors.toList());

            surroundingAreasInfo.setFreeAreasOnBottom(freeAreasOnTheBottom);

            for (FreeArea freeArea : freeAreasOnTheBottom) {
                if (freeArea.getLeft() <= occupiedArea.getLeft() && freeArea.getLeft() + freeArea.getWidth() >= occupiedArea.getLeft() + occupiedArea.getWidth()) {
                    surroundingAreasInfo.setFreeAreaOnBottom(true);
                }
            }

            List<OccupiedArea> occupiedAreasOnTheBottom = occupiedAreas.stream()
                    .filter(oa -> oa.getUp() == occupiedArea.getUp() + occupiedArea.getHeight() + sawCutWidth &&
                            oa.getWorkpieceId() == currentWorkpiece.getId() &&
                            oa.getLeft() <= occupiedArea.getLeft())
                    .collect(Collectors.toList());
            surroundingAreasInfo.setOccupiedAreasOnBottom(occupiedAreasOnTheBottom);
        }
        return surroundingAreasInfo;
    }

    private void removeSurroundingAreasFromSuitableAreas(List<FreeArea> freeAreasSuitableForDetail, SurroundingAreasInfo surroundingAreasInfo) {
        Iterator<FreeArea> iterator = freeAreasSuitableForDetail.iterator();
        while (iterator.hasNext()) {
            FreeArea next = iterator.next();
            for (FreeArea freeArea : surroundingAreasInfo.getFreeAreasOnRight()) {
                if (next.getAreaId() == freeArea.getAreaId()) {
                    iterator.remove();
                    break;
                }
            }
            for (FreeArea freeArea : surroundingAreasInfo.getFreeAreasOnBottom()) {
                if (next.getAreaId() == freeArea.getAreaId()) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    private Optional<FreeArea> createFreeArea(OccupiedArea occupiedArea, SurroundingAreasInfo surroundingAreasInfo, InitialDataOptimization initialData) {

        double sawCutWidth = initialData.getSawCutWidth();
        AreaIdGenerator areaIdGenerator = initialData.getAreaIdGenerator();

        FreeArea tempFreeArea = null;
        if (surroundingAreasInfo.isFreeAreaOnRight() && surroundingAreasInfo.isFreeAreaOnBottom() &&
                surroundingAreasInfo.getFreeAreasOnBottom().get(0).getLeft() == occupiedArea.getLeft() && (
                surroundingAreasInfo.getFreeAreasOnBottom().get(0).getLeft() + surroundingAreasInfo.getFreeAreasOnBottom().get(0).getWidth() ==
                        surroundingAreasInfo.getFreeAreasOnRight().get(0).getLeft() + surroundingAreasInfo.getFreeAreasOnRight().get(0).getWidth() ||
                        surroundingAreasInfo.getFreeAreasOnBottom().get(0).getUp() + surroundingAreasInfo.getFreeAreasOnBottom().get(0).getHeight() ==
                                surroundingAreasInfo.getFreeAreasOnRight().get(0).getUp() + surroundingAreasInfo.getFreeAreasOnRight().get(0).getHeight())) {

            tempFreeArea = new FreeArea(occupiedArea.getHeight() + sawCutWidth + surroundingAreasInfo.getFreeAreasOnBottom().get(0).getHeight(),
                    occupiedArea.getWidth() + sawCutWidth + surroundingAreasInfo.getFreeAreasOnRight().get(0).getWidth(),
                    occupiedArea.getUp(), occupiedArea.getLeft(), occupiedArea.getTypeMaterial(), occupiedArea.getWorkpieceId(), areaIdGenerator.nextAreaId(),
                    0, false);

        } else if (surroundingAreasInfo.isFreeAreaOnRight()) {

            FreeArea attachedFreeArea = surroundingAreasInfo.getFreeAreasOnRight().get(0);

            if (attachedFreeArea.getUp() + attachedFreeArea.getHeight() == occupiedArea.getUp() + occupiedArea.getHeight()) {

                tempFreeArea = new FreeArea(occupiedArea.getHeight() + sawCutWidth,
                        occupiedArea.getWidth() + sawCutWidth + attachedFreeArea.getWidth(),
                        occupiedArea.getUp(), occupiedArea.getLeft(), occupiedArea.getTypeMaterial(), occupiedArea.getWorkpieceId(), areaIdGenerator.nextAreaId(),
                        0, false);
            }

        } else if (surroundingAreasInfo.isAtRightEdgeOfWorkpiece() && surroundingAreasInfo.isFreeAreaOnBottom()) {

            FreeArea attachedFreeArea = surroundingAreasInfo.getFreeAreasOnBottom().get(0);

            if (attachedFreeArea.getWidth() == occupiedArea.getWidth() && attachedFreeArea.getLeft() == occupiedArea.getLeft()) {

                tempFreeArea = new FreeArea(occupiedArea.getHeight() + sawCutWidth + surroundingAreasInfo.getFreeAreasOnBottom().get(0).getHeight(),
                        occupiedArea.getWidth() + sawCutWidth, occupiedArea.getUp(), occupiedArea.getLeft(), occupiedArea.getTypeMaterial(),
                        occupiedArea.getWorkpieceId(), areaIdGenerator.nextAreaId(), 0, false);
            }
        }
        return Optional.ofNullable(tempFreeArea);
    }
    private Detail getDetailById(int stackingSequence, List<Detail> details) {
        return details.stream()
                .filter(d -> d.getId() == stackingSequence)
                .findFirst()
                .orElse(null);
    }

    public Optional<Workpiece> findSuitableWorkpieceBySquare(List<Detail> details, List<Workpiece> workpieces) {
        return freeAreaSeeker.findSuitableWorkpieceBySquare(details, workpieces);
    }
}
