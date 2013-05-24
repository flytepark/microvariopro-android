package com.flytepark.usb.vario;

public class VarioData {
	
	public  VarioData()
	{
		
	}
	
	public VarioData(VarioCommand command, byte[] data)
	{
		this.command=command;
		this.data=data;
	}
	
	public VarioData(VarioCommand command, byte data)
	{
		this.command=command;
		this.data= new byte[] { data };
	}
	
	public VarioData(VarioCommand command)
	{
		this.command=command;
		this.data= (byte[]) null;
	}
	
	public VarioCommand command;
	public byte[] data;
}
