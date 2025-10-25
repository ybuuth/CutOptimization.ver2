package com.example.cut_optimization.dto.baseDto;

import com.example.cut_optimization.dto.TypeOfMaterial;

public interface BaseDetailInfo {
    double getHeight();
    void setHeight(double height);
    double getWidth();
    void setWidth(double width);
    int getQuantity();
    void setQuantity(int quantity);
    double getSquare();
    void setSquare(double square);
    TypeOfMaterial getTypeMaterial();
    void setTypeMaterial(TypeOfMaterial typeMaterial);
    boolean isRotated();
    void setRotated(boolean rotated);

    default double calculateSquare(){
        double square = getHeight() * getWidth();
        setSquare(square);
        return square;
    }
    default void rotate(){
        double temp = getHeight();
        setHeight(getWidth());
        setWidth(temp);
        setRotated(!isRotated());
    }
    default void orientDetailVertically() {
        if (getWidth() > getHeight()) {
            rotate();
        }
    }


}
