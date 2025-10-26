package com.example.cut_optimization.service;

import com.example.cut_optimization.dto.PossibleFreeAreas;
import com.example.cut_optimization.dto.areas.FreeArea;
import com.example.cut_optimization.dto.details.Detail;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FreeAreaSeeker {

    public List<FreeArea> getFreeAreasSuitableForDetail(Detail detail, List<FreeArea> freeAreas, boolean isDisableRotation) {
        return freeAreas.stream()
                .filter(freeArea ->
                        isDisableRotation ?
                                freeArea.canStackAlong(detail) :
                                freeArea.canStack(detail)

                )
                .collect(Collectors.toList());
    }

    /**Среди возможных областей в первую очередь ищем область заготовки, на которую уже начата укладка,
     // во вторую область той же заготовки где еще нет укладок, в третью - области других заготовок
     * @param detail {@code Detail}
     * @param possibleFreeAreas {@code PossibleFreeAreas}
     * @return {@code Optional<FreeArea>}
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
}
