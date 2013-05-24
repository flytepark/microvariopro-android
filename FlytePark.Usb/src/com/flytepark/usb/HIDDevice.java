/**
 * 
 */
package com.flytepark.usb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.hardware.usb.*;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.app.Activity;
import android.app.PendingIntent;

import android.util.Log;


/**
 * @author Brian Vogel, Techrhythm
 * 
 */
public class HIDDevice extends java.lang.Object {

	static final String USB_PERMISSION = "com.android.example.USB_PERMISSION";
	static final String USB_DEVICE_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
	static final String USB_DEVICE_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
	
	protected Context context;	
	protected UsbManager usbManager;
	protected UsbDevice usbDevice;	
	protected UsbInterface usbInterface;
	protected UsbEndpoint outputEndpoint;
	protected UsbEndpoint inputEndpoint;
	protected UsbDeviceConnection connection;
	
	public  BroadcastReceiver usbDeviceAttachedReceiver;
	public  BroadcastReceiver usbDeviceDetachedReceiver;
 
	
	protected PendingIntent permissionRequest;
	
	protected int vendorId;
	protected int productId;
	protected int deviceId;
	
	protected boolean _isUsbConnected = false;
	
	protected int hits = 0;
	
	//logging
	private List _listeners = new ArrayList();

	  
	protected HIDDevice(Context context,UsbManager manager, int vendorId, int productId, int deviceId)
	{
		this.context = context;		
		this.initializeDevice(context, manager, vendorId, productId, deviceId);
	}
	
	protected HIDDevice()
	{
 
		
		usbDeviceAttachedReceiver =new BroadcastReceiver() {	
			@Override
			public void onReceive(Context context, Intent intent) {
				
				if (usbDevice==null)
				{						
					detectDevice();						
				}
			}
        };
        
        usbDeviceDetachedReceiver =new BroadcastReceiver() {	        
        	
			@Override
			public void onReceive(Context context, Intent intent) 
			{
					usbDevice = null;
					detachDevice();						
			}
        };
	}
	

	
	public void initializeDevice(Context context, UsbManager manager,int vendorId, int productId, int deviceId)
	{
		this.context = context;
		this.usbManager = manager;
		this.vendorId = vendorId;
		this.productId = productId;
		this.deviceId = deviceId;
		
		this.permissionRequest = PendingIntent.getBroadcast(context,0,new Intent(USB_PERMISSION),0);
		 
		this.context.registerReceiver(usbDeviceAttachedReceiver, new IntentFilter(USB_PERMISSION));
		this.context.registerReceiver(usbDeviceAttachedReceiver, new IntentFilter(USB_DEVICE_ATTACHED)); //This doesn't do anything right now.  Android never calls this, only calls onResume.
		this.context.registerReceiver(usbDeviceDetachedReceiver, new IntentFilter(USB_DEVICE_DETACHED));
		
		detectDevice();
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
		
		this.context.unregisterReceiver(usbDeviceAttachedReceiver);
		this.context.unregisterReceiver(usbDeviceDetachedReceiver);
 
	}
	
	protected synchronized void detachDevice()
	{
		_isUsbConnected=false;
		onUsbDeviceDetached();
	}
	
	public synchronized void detectDevice()
	{
        HashMap<String, UsbDevice> devices= usbManager.getDeviceList();
  
        String usbDudes = "";
        for(UsbDevice value : devices.values())
        {
        	//TODO: incoroporate product id/deviceId if needed
        	if (value.getVendorId()==vendorId)
        	{
        		usbDevice = value;
        		_isUsbConnected=true;
        		
        		if (usbManager.hasPermission(usbDevice))
        		{
        			onDetectUsbDevice();
        		}
        		else
        		{
        			Log.e("USB","No permission for device:" + vendorId + ", Requesting...");      			
        			usbManager.requestPermission(usbDevice, permissionRequest);        			
        			
        			onUsbDeviceDetached();
        		}
        		
        		
        		break;
        	}
        }	
	}
	
	public  void setPermissionRequest(PendingIntent permissionRequest)
	{
		this.permissionRequest = permissionRequest;
	}
	
 
	protected void onUsbDeviceDetached()
	{
		Log.e("System.out","Device Detached");
	}
	
	protected void onDetectUsbDevice()
	{
		Log.e("System.out","Device Detected");

		//usbInterface = usbDevice.getInterface(0);
		//connection = usbManager.openDevice(usbDevice);
	}

}
