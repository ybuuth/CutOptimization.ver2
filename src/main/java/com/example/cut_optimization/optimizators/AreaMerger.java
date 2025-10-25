package com.example.cut_optimization.optimizators;

import com.example.cut_optimization.dto.areas.FreeArea;
import com.example.cut_optimization.dto.areas.OccupiedArea;
import com.example.cut_optimization.dto.details.Workpiece;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AreaMerger {

    public boolean mergeVerticalAdjacentFreeAreas(double sawCutWidth, FreeArea freeArea, List<FreeArea> freeAreasByCurrentWorkpiece, List<FreeArea> freeAreasToRemove, boolean hasEnlarge) {
        List<FreeArea> candidates;
        candidates = freeAreasByCurrentWorkpiece.stream()
                .filter(fa1 -> freeArea.getLeft() == fa1.getLeft() &&
                        freeArea.getWidth() == fa1.getWidth() &&
                        freeArea.getUp() + freeArea.getHeight() + sawCutWidth == fa1.getUp())
                .toList();

        for (FreeArea candidate : candidates) {
            freeArea.setHeight(freeArea.getHeight() + sawCutWidth + candidate.getHeight());
            freeArea.calculateSquare();
            freeAreasToRemove.add(candidate);

            hasEnlarge = true;
            break;
        }
        return hasEnlarge;
    }

    public boolean moveFullWidthFreeAreaDownWithLiftingLowerAreas(FreeArea freeArea,
                                                                   List<FreeArea> freeAreasByCurrentWorkpiece,
                                                                   List<OccupiedArea> occupiedAreasByCurrentWorkpiece,
                                                                   double sawCutWidth, Workpiece workpiece) {

        double diffInYMovingAreas = 0.0;

        if (freeArea.getWidth() == workpiece.getWidth() && !isAtBottomEdgeOfWorkpiece(freeArea, workpiece)) {
            // отобрать все области, лежащие ниже текущей свободной области и поднять их на высоту этой области, а эту область сдвинуть вниз
            double moveUp = freeArea.getHeight() + sawCutWidth;

            freeAreasByCurrentWorkpiece.stream()
                    .filter(fa1 -> fa1.getUp() > freeArea.getUp())
                    .filter(fa1 -> fa1.getAreaId() != freeArea.getAreaId())
                    .filter(fa1 -> !(fa1.getWidth() == workpiece.getWidth() && isAtBottomEdgeOfWorkpiece(fa1, workpiece)))
                    .peek(fa1 -> fa1.setUp(fa1.getUp() - moveUp))
                    .collect(Collectors.toList());

            for (OccupiedArea occupiedArea : occupiedAreasByCurrentWorkpiece) {
                if (occupiedArea.getUp() < freeArea.getUp()) {
                    continue;
                }
                if (occupiedArea.getLeft() == 0) {
                    diffInYMovingAreas += occupiedArea.getHeight() + sawCutWidth;
                }
                occupiedArea.setUp(occupiedArea.getUp() - moveUp);
            }
            freeArea.setUp(freeArea.getUp() + diffInYMovingAreas);
        }
        return diffInYMovingAreas > 0;
    }

    public boolean mergeHorizontalAdjacentFreeAreas(double sawCutWidth, FreeArea freeArea, List<FreeArea> freeAreasByCurrentWorkpiece, List<FreeArea> freeAreasToRemove, boolean hasEnlarge) {
        List<FreeArea> candidates;
        candidates = freeAreasByCurrentWorkpiece.stream()
                .filter(fa1 -> freeArea.getUp() == fa1.getUp() &&
                        freeArea.getHeight() == fa1.getHeight() &&
                        freeArea.getLeft() + freeArea.getWidth() + sawCutWidth == fa1.getLeft())
                .toList();

        for (FreeArea candidate : candidates) {
            freeArea.setWidth(freeArea.getWidth() + sawCutWidth + candidate.getWidth());
            freeArea.calculateSquare();
            freeAreasToRemove.add(candidate);
            hasEnlarge = true;
            break;
        }
        return hasEnlarge;
    }


    /* Если область №1 касается нижней частью область №2 и их правые части совпадают и площадь области №1 больше чем №2,
            и левая сторона области №2 находится левее левой стороны области №1
            и они принадлежат одной заготовке то перераспределим отрез в пользу области №1
     * @param sawCutWidth
     * @param freeArea
     * @param freeAreasByCurrentWorkpiece
     * @param hasEnlarge
     * @return
     */
    public boolean mergeLowerFreeAreaVerticallyWithOverlapRight(double sawCutWidth,
                                                                 FreeArea freeArea,
                                                                 List<FreeArea> freeAreasByCurrentWorkpiece,
                                                                 boolean hasEnlarge) {
        List<FreeArea> candidates = freeAreasByCurrentWorkpiece.stream()
                .filter(fa1 -> freeArea.getUp() + freeArea.getHeight() + sawCutWidth == fa1.getUp()
                        && freeArea.getLeft() + freeArea.getWidth() == fa1.getLeft() + fa1.getWidth()
                        && freeArea.getSquare() > fa1.getSquare()
                        && freeArea.getLeft() > fa1.getLeft())
                .toList();

        for (FreeArea freeArea1 : candidates) {
            freeArea1.setWidth(freeArea.getLeft() - sawCutWidth - freeArea1.getLeft());
            freeArea1.calculateSquare();
            freeArea.setHeight(freeArea.getHeight() + sawCutWidth + freeArea1.getHeight());
            freeArea.calculateSquare();

            hasEnlarge = true;
            break;
        }
        return hasEnlarge;
    }

    private boolean isAtBottomEdgeOfWorkpiece(FreeArea freeArea, Workpiece workpiece) {
        return freeArea.getUp() + freeArea.getHeight() == workpiece.getHeight();
    }
}
