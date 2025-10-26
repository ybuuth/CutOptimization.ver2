package com.example.cut_optimization.service;

import com.example.cut_optimization.dto.PossibleFreeAreas;
import com.example.cut_optimization.dto.Stackable;
import com.example.cut_optimization.dto.areas.FreeArea;
import com.example.cut_optimization.dto.baseDto.BaseDetailInfo;
import com.example.cut_optimization.dto.details.Detail;
import com.example.cut_optimization.dto.details.Workpiece;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FreeAreaSeeker {

    /**
     * Возвращает список свободных областей, подходящих для размещения заданной детали.
     *
     * <p>Метод фильтрует список переданных свободных областей и оставляет только те,
     * которые могут вместить деталь. Учет поворота детали зависит от флага {@code isDisableRotation}:
     * - Если {@code isDisableRotation == true}, то проверяется возможность размещения детали без поворота.
     * - Если {@code isDisableRotation == false}, то проверяется возможность размещения детали как в исходном,
     *   так и в повёрнутом состоянии (вызывается метод {@link Stackable#canStack(BaseDetailInfo)}).
     *
     * @param detail Деталь, которую нужно разместить.
     * @param freeAreas Список свободных областей, среди которых происходит поиск подходящих.
     * @param isDisableRotation Флаг, определяющий, запрещен ли поворот детали.
     *                          Если {@code true}, то деталь не может быть повернута.
     *
     * @return Список свободных областей, подходящих для размещения детали.
     */
    public List<FreeArea> getFreeAreasSuitableForDetail(Detail detail, List<FreeArea> freeAreas, boolean isDisableRotation) {
        return freeAreas.stream()
                .filter(freeArea ->
                        isDisableRotation ?
                                freeArea.canStackAlong(detail) :
                                freeArea.canStack(detail)

                )
                .collect(Collectors.toList());
    }

    /**
     * Среди возможных областей в первую очередь ищем область заготовки, на которую уже начата укладка,
     // во вторую область той же заготовки где еще нет укладок, в третью - области других заготовок
     * @param detail {@code Detail} - деталь, для которой ищем область
     * @param possibleFreeAreas {@code PossibleFreeAreas} - возможные области, в которых может быть размещена деталь
     * @return {@code Optional<FreeArea>} - найденная область, или пустой Optional, если не найдена
     */
    public Optional<FreeArea> getSuitableFreeArea(Detail detail, PossibleFreeAreas possibleFreeAreas) {
        //Среди возможных областей в первую очередь ищем область заготовки, на которую уже начата укладка,
        // во вторую область той же заготовки где еще нет укладок, в третью - области других заготовок
        // т.к. области складываются стеком, нужно идти обратным перебором
        ListIterator<FreeArea> freeAreaListIterator = possibleFreeAreas.getFreeAreasHighPriority()
                .listIterator(possibleFreeAreas.getFreeAreasHighPriority().size());
        while (freeAreaListIterator.hasPrevious()) {
            FreeArea freeArea = freeAreaListIterator.previous();
            if (freeArea.canStackAlong(detail)) {
                freeAreaListIterator.remove();
                return Optional.of(freeArea);
            }
        }

        ListIterator<FreeArea> freeAreaListIteratorMedium = possibleFreeAreas.getFreeAreasMediumPriority()
                .listIterator(possibleFreeAreas.getFreeAreasMediumPriority().size());
        while (freeAreaListIteratorMedium.hasPrevious()) {
            FreeArea freeArea = freeAreaListIteratorMedium.previous();
            if (freeArea.canStackAlong(detail)) {
                possibleFreeAreas.getFreeAreasHighPriority().clear();
                freeAreaListIteratorMedium.remove();
                return Optional.of(freeArea);
            }
        }

        ListIterator<FreeArea> freeAreaListIteratorLow = possibleFreeAreas.getFreeAreasLowPriority().listIterator();
        while (freeAreaListIteratorLow.hasNext()) {
            FreeArea freeArea = freeAreaListIteratorLow.next();
            if (freeArea.canStackAlong(detail)) {
                possibleFreeAreas.getFreeAreasHighPriority().clear();
                possibleFreeAreas.getFreeAreasMediumPriority().clear();
                freeAreaListIteratorLow.remove();
                return Optional.of(freeArea);
            }
        }
        return Optional.empty();
    }


    public Optional<Workpiece> findSuitableWorkpieceBySquare(List<Detail> details, List<Workpiece> workpieces) {

        details.sort((o1, o2) -> (o1.getSquare() > o2.getSquare()) ? -1 : 1);
        double allDetailsSquare = details.stream()
                .mapToDouble(Detail::getSquare)
                .sum();

        return workpieces.stream()
                .filter(w -> w.getSquare() > allDetailsSquare)
                .min(Comparator.comparingDouble(Workpiece::getSquare));
    }
}
