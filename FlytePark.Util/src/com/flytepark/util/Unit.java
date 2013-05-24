package com.flytepark.util;
 

public class Unit {

	public static final double CM_IN_INCHES = 0.393701;
	public static final double CM_IN_FEET = 0.0328084;
	public static final double M_IN_CM = 100.000000;
	public static final double FEET_IN_CM = 30.480000;

	private double value;
	protected double _displayValue;
	protected UnitType _displayUnits;
	protected UnitType _valueUnits;
	
	protected UnitResult displayValueCallback;
	
	private final static String unitTypeAbbreviations[] = new String[]  {
	    "m",       //0. METERS
	    "cm",      //1. CENTIMETERS
	    "ft",      //2. FEET
	    "\"",      //3. INCHES
	    "F",       //4. FARENHEIGHT
	    "C",       //5. CELCIUS
	    "K",       //6. KNOTS
	    "cm/sec", //7. CMS
	    "in/min", //8. INMIN
	    "ft/min", //9. FTMIN
	    "m/min" //10. MIN
	    
	};      //KNOTS
	

 
	
	
	public Unit()
	{
		
	}
	
	public Unit(double value, UnitType internalUnits)
	{
		this.value = value;
		this._valueUnits = internalUnits;
		this._displayUnits = internalUnits;
		
	    final Unit u = this;
		
		this.displayValueCallback  =  new UnitResult()
        {
        	@Override public double getValue(double value) { return u.noConversion(value) ;};
        };
	}
	
	public Unit(double value, UnitType internalUnits, UnitType displayUnits)
	{
		this.value = value;
		this.setDisplayUnits(displayUnits);
		this._valueUnits = internalUnits;
	}
	
 	
	protected double cmToInches(double value) throws Exception
	{
		throw new Exception("Not Implemented");		 
	}
	
	protected double noConversion(double value)
	{
		return value;
	}

	protected double cmToM(double value)
	{
		return value / 100.00;
	} 
	
	protected double cmsToMin(double value)
	{
		return  (value / 100.0) * 60.0;
	}
 
	protected double celciusToFarenheight(double value)
	{
		return 32.0 + (value*1.8);
	}

	protected double cmToFeet(double value)
	{
		return value * CM_IN_FEET;
	}
	
	protected double cmsToMMin(double value)
	{
	    return (value / 100.0) * 60.0;
	}

	protected double cmsToFeetMin(double value)
	{
		return  (value * CM_IN_FEET) * 60;
	}

	protected double mToCm(double value)
	{
    
		return  value * 100.0;
	}

	protected double FeetToCm(double value)
	{
		return  value * FEET_IN_CM;
	}


	public void setValue(double value)
	{
		this.value = value;
	}

	public void setDisplayValue(double value)
	{
		double result = 0;
    
		switch (_displayUnits) {
            
        	case METERS:
        		this.value= value * M_IN_CM;
            break;
            
        	case FEET:
        		this.value= value * FEET_IN_CM;
            break;
            
        	case INCHES:
        	case FARENHEIGHT:
        	default:
        	//TODO: set display value callback
            //displayValueCallback = @selector(noConversion);
            break;
		}
	}
    
 
	public void setDisplayUnits(UnitType units)
	{
    _displayUnits = units;
  
    final Unit u = this;
    switch (units) {
            
        case METERS:
            if (this._valueUnits==UnitType.CENTIMETERS) displayValueCallback = new UnitResult()
            {
            	@Override public double getValue(double value) { return u.cmToM(value) ;};
            };    
            break;
            
        case INCHES:
            if (this._valueUnits== UnitType.CENTIMETERS) displayValueCallback = new UnitResult()
            {
            	//@Override public double getValue(double value) { return u.cmToInches(value) ;};
            };
            break;
            
        case FEET:
            if (this._valueUnits== UnitType.CENTIMETERS) displayValueCallback =  new UnitResult()
            {
            	@Override public double getValue(double value) { return u.cmToFeet(value) ;};
            };
            break;
            
        case FARENHEIGHT:
            if (this._valueUnits == UnitType.CELCIUS) displayValueCallback =  new UnitResult()
            {
            	@Override public double getValue(double value) { return u.celciusToFarenheight(value) ;};
            };
            break;
            
        case MMIN:
            if (this._valueUnits== UnitType.CMS) displayValueCallback = new UnitResult()
            {
            	@Override public double getValue(double value) { return u.cmsToMMin(value) ;};
            };
            break;
            
        case FTMIN:
            if (this._valueUnits== UnitType.CMS) displayValueCallback = new UnitResult()
            {
            	@Override public double getValue(double value) { return u.cmsToFeetMin(value) ;};
            };
            break;
            
        case CMS:
        default:
            displayValueCallback =  new UnitResult()
            {
            	@Override public double getValue(double value) { return u.noConversion(value) ;};
            };
            break;

    }
    
	}
	
	public String getDisplayValueAbbreviation()
	{
		return  unitTypeAbbreviations[_displayUnits.ordinal()];
	}

	public UnitType getDisplayUnits()
	{
		return _displayUnits;
	}

	public double getDisplayValue()
	{
		return this.displayValueCallback.getValue(value);
	}

	/*
static Unit* unitWithUnit: (Unit*) unit
{
    return [Unit unitWithValue: [unit value]  unit: [unit unitType]  displayUnits: [unit displayUnits]];
}


+(Unit*) unitWithValue: (double) value unit: (UnitType) unitType
{
    return [[[Unit alloc ] initWithValue:value unitType:unitType] autorelease];
}

+(Unit*) unitWithValue: (double) value unit: (UnitType) unitType displayUnits: (UnitType) displayUnits
{
    Unit* unit = [[[Unit alloc ] initWithValue:value unitType:unitType] autorelease];
    [unit setDisplayUnits:displayUnits];
    return unit;
}*/

	static String displayValueAbbreviationForUnitType(UnitType type)
	{
		return unitTypeAbbreviations[type.ordinal()];
	}
 
}