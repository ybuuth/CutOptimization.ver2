package com.example.cut_optimization.dto.details;

import com.example.cut_optimization.dto.Stackable;
import com.example.cut_optimization.dto.baseDto.BaseDetail;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class EndlessWorkpiece extends BaseDetail implements Stackable {
}
