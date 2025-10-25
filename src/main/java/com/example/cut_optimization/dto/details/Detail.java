package com.example.cut_optimization.dto.details;

import com.example.cut_optimization.dto.baseDto.BaseDetail;
import com.example.cut_optimization.dto.baseDto.BaseDetailInfo;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Detail extends BaseDetail implements BaseDetailInfo {

    private int id;

    public Detail(Detail detail) {
        super(detail);
        if (detail != null) {
            this.id = detail.id;
        }
    }

}
