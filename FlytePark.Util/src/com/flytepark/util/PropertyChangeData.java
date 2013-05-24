package com.flytepark.util;

public class PropertyChangeData {
	
	public final Object sender;
	public final Object oldValue;
	public final Object newValue;
	public final String propertyName; 

	public PropertyChangeData(Object sender,String propertyName, Object oldValue, Object newValue)
	{
		this.sender=sender;
		this.oldValue=oldValue;
		this.newValue = newValue;
		this.propertyName = propertyName;
	}
}
