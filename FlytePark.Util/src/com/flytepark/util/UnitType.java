package com.flytepark.util;

public enum UnitType {
    METERS(0),
    CENTIMETERS(1),
    FEET(2),
    INCHES(3),
    FARENHEIGHT(4),
    CELCIUS(5),
    KNOTS(6),
    CMS(7),
    INMIN(8),
    FTMIN(9),
    MMIN(10);
    
    private final int value;    

    private UnitType (int value) {
      this.value=value;
    }

    public int getValue() {
      return value;
    }
}
