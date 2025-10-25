package com.example.cut_optimization.dto.details;

import com.example.cut_optimization.dto.Stackable;
import com.example.cut_optimization.dto.areas.FreeArea;
import com.example.cut_optimization.dto.areas.OccupiedArea;
import com.example.cut_optimization.dto.baseDto.BaseDetail;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
public class Workpiece extends BaseDetail implements Stackable, Cloneable {

    private int id;

    public Workpiece(Workpiece workpiece) {
        super(workpiece);
        this.id = workpiece.getId();
    }

    public Workpiece(EndlessWorkpiece endlessWorkpiece) {
        super(endlessWorkpiece);
        this.setQuantity(1);
        this.calculateSquare();
    }
    @Override
    public Workpiece clone() throws CloneNotSupportedException {
        return (Workpiece) super.clone();
    }
}
