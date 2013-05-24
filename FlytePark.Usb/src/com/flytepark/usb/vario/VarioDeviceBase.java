package com.flytepark.usb.vario;

import java.nio.ByteBuffer;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.util.Log;

import com.flytepark.events.EventListener;
import com.flytepark.usb.HIDDevice;

 

public class VarioDeviceBase extends HIDDevice implements Runnable {

	public final static int VENDOR_ID = 0x23F7;
	public final static int DEVICE_ID = 0x0100;
	public final static int PRODUCT_ID = 0x0001;
	public static final byte SET_MAGIC  = 0x24;
	public static final byte GET_MAGIC = 0x23;
	

	protected boolean isReading = false;
	protected boolean continuePollingDevice = true;
	protected boolean isStreamingRead = false;
	
	private EventListener onDeviceReady;
	
	private ByteBuffer sendData;
	
	Thread readThread;
 
	
	@Override protected void onDetectUsbDevice() 
	{
		this.setDevice();
	}
	
   @Override protected void onUsbDeviceDetached() 
   {
	super.onUsbDeviceDetached();
	this.stopRead();
   }
 
	protected void setDevice() {
		// TODO Auto-generated method stub
			Log.e("USB","setDevice");	
		
	        if (usbDevice.getInterfaceCount() != 1) {
	            return;
	        }
	        UsbInterface usbInterface = usbDevice.getInterface(0);

        

	        // endpoint should be of type interrupt
	        inputEndpoint = usbInterface.getEndpoint(0);
	        outputEndpoint = usbInterface.getEndpoint(1);
	           
	        String log = "";
//	        log+= "EP0 TYPE:" + inputEndpoint.getType() + "\n"; //3 Innterrupt
//	        log+= "EP0 DIRECTION:" + inputEndpoint.getDirection()+ "\n";//(128) INPUT from the device 
//	        log+= "EP0 MAXPACKETSIZE:" + inputEndpoint.getMaxPacketSize()+ "\n"; //64
//	        log+= "EP0 INTERVAL:" + inputEndpoint.getInterval()+ "\n"; //250
//	        log+= "EP0 ATTRIBUTES:" + inputEndpoint.getAttributes()+ "\n"; //3
//
//	        log+="EP1 TYPE:" + outputEndpoint.getType()+ "\n"; 
//	        log+="EP1 DIRECTION:" + outputEndpoint.getDirection()+ "\n"; //(0) OUTPUT host to device
//	        log+="EP1 MAXPACKETSIZE:" + outputEndpoint.getMaxPacketSize()+ "\n"; //64
//	        log+="EP1 INTERVAL:" + outputEndpoint.getInterval()+ "\n"; //250
//	        log+="EP1 ATTRIBUTES:" + outputEndpoint.getAttributes()+ "\n"; //3
//	        
	        
	        if (inputEndpoint.getType() != UsbConstants.USB_ENDPOINT_XFER_INT) {
	            //raiseLogEvent("endpoint is not interrupt type");
	            return;
	        }
	        
	        if (usbDevice != null && usbManager!=null) {
	            connection = usbManager.openDevice(usbDevice);
	            
	            if (connection == null || !connection.claimInterface(usbInterface, true)) {
	                connection = null;
	            }
	         }
	    }
	
	
	protected synchronized void sendDataAsync(ByteBuffer sendData,boolean continuePollingDevice)
	{
		this.sendData = sendData;
		
		Log.e("USB", "begin() called.");
		String log = "";
		

		
    	if (!isReading)
    	{
    		isReading=true;
    		this.continuePollingDevice=continuePollingDevice;
    		
    		readThread = new Thread(this)
    		{
    			@Override
    			public void destroy() {
    				Log.e("USB", "READ THREAD: destroy() called.");
    			};
    			
    		};
    		readThread.start();
    	}
	}
	
	
	protected synchronized void stopRead()
	{
		this.stopRead(null);
	}
	
	protected synchronized void stopRead(EventListener onDeviceReady)
	{
 
		
		if (isReading)
		{
		 isReading=false;	
		
		 if (readThread!=null)
		 {
			Log.e("USB", "stopRead, interrupting thread");
			this.onDeviceReady = onDeviceReady;
			readThread.interrupt();
		 }
		}
		else
		{
			onDeviceReady.onExecute(null);
		}
 
	}
 
    @Override
    public void run() {
    	
    	
    	try
    	{
    		String log="";
            log+= "EP0 TYPE:" + inputEndpoint.getType() + "\n"; //3 Innterrupt
            log+= "EP0 DIRECTION:" + inputEndpoint.getDirection()+ "\n";//(128) INPUT from the device 
            log+= "EP0 MAXPACKETSIZE:" + inputEndpoint.getMaxPacketSize()+ "\n"; //64
            log+= "EP0 INTERVAL:" + inputEndpoint.getInterval()+ "\n"; //250
            log+= "EP0 ATTRIBUTES:" + inputEndpoint.getAttributes()+ "\n"; //3

            log+="EP1 TYPE:" + outputEndpoint.getType()+ "\n"; 
            log+="EP1 DIRECTION:" + outputEndpoint.getDirection()+ "\n"; //(0) OUTPUT host to device
            log+="EP1 MAXPACKETSIZE:" + outputEndpoint.getMaxPacketSize()+ "\n"; //64
            log+="EP1 INTERVAL:" + outputEndpoint.getInterval()+ "\n"; //250
            log+="EP1 ATTRIBUTES:" + outputEndpoint.getAttributes()+ "\n"; //3
            
            Log.e("USB",log);
            
	    	if (connection.bulkTransfer(outputEndpoint, sendData.array(), 64, 250) > 0 )
	    	{
	    		//request sent now read.
	    		byte[] readBuffer = new byte[64];
	    		
	    		Thread.sleep(40);
	    		
	    		do {
	    			
	    			int length = connection.bulkTransfer(inputEndpoint, readBuffer, readBuffer.length,1);
	    			
	    			//Log.e("USB", "Length:" + length);
	   			 
	    			if (length>0 && GET_MAGIC == readBuffer[0])
	    			{
	    				byte[] read = new byte[length];
						System.arraycopy(readBuffer, 0, read, 0, length);										
						this.onVarioDataReceived(read);
						
						//readBuffer = new byte[64];
						
						if (!continuePollingDevice)
						{
							isReading=false;
							break;
						}
	    			}  
	   
	    			Thread.sleep(10);
	    			
	    		} while (isReading);
	    	}
	    	
	    	else
	    	{
	    		Log.e("USB","Write Failed");
	    	}
    	}
    	catch (Exception e)
    	{
    		Log.e("USB ERROR",e.getMessage() + "\n" + e.getCause() + "\n" + e.getStackTrace() );
    	}
    	
    	
    	 onReadStopped();
    }
      
 
    private void onReadStopped()
    {

		 isReading = false;   
		 
		 if (onDeviceReady!=null)
		 {
			 onDeviceReady.onExecute(null);
		 }
		 
    }
    
 
    protected synchronized void onVarioDataReceived(byte[] data)
    {
    	
    }


    
    public boolean isUsbConnected()
    {
    	return _isUsbConnected;
    }
	

}
