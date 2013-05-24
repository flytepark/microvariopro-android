package com.flytepark.usb.vario.data;
 
public class VarioRealtimeData {
	
	public double climb;
	public double altitude;
	public double battery;
	public double temperature;
		
	public String toString()
	{
		return String.format("climb: %1$.2f altitude: %2$.2f battery: %3$.2f temperature: %4$.2f", climb,altitude,battery,temperature);
		
	}
}
