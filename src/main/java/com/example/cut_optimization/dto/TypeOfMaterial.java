package com.example.cut_optimization.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TypeOfMaterial implements Cloneable {
    @JsonProperty("name")
    private String name;

    @JsonCreator
    public TypeOfMaterial(@JsonProperty("name") String name){
       this.name = name;
    }

    @Override
    public TypeOfMaterial clone() throws CloneNotSupportedException {
        return (TypeOfMaterial) super.clone();
    }

}
