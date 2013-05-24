package com.flytepark.usb.vario;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Queue;
import java.util.Stack;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.util.Log;

import com.flytepark.events.EventListener;
import com.flytepark.events.EventSource;
import com.flytepark.usb.vario.data.VarioRealtimeData;
 


public class VarioDevice extends VarioDeviceBase {

		//contastns

		static final int MIN_PACKET_SIZE = 4;  //Magic,Command,Checksum,Length
		static final int FIRST_TRACK = 0x002000;
		static final int LAST_TRACK = 0x1FFFFA;
		
		private UsbRequest sendRequest;
	
		private IVarioDeviceDelegate delegate;
		
		private short packetCounter = 0;
		private int receivedPacketCounter = -1;
 
		private boolean _trackError = false;
		
		private int _currentTrackAddress = 0x000000;
		private int _tracksReceived = 0;

		private int _trackCount = 0;
		private byte _trackPayloadSize =  57; //max size is 58, omitting one byte so all packets will fit in one request.
		private byte _trackEntrySize = 3;
		private byte _bytesRequested = 0;

	 
		private boolean isRealtimeStarted = false;
		private  boolean _isInitialized = false;
		
		private VarioData lastCommand;
		private ArrayDeque<VarioData> packetQueue;
		private boolean isBusy = false;
		
		//events
		public final EventSource varioInitialized;
		public final EventSource varioDisconnected;
		public final EventSource realtimeStarted;
		public final EventSource realtimeData;
		
		//internal handlers
		private final VarioDataHandler initializeHandler;
		private final VarioDataHandler realtimeHandler;
		private final VarioDataHandler transactionHandler;
		
		private VarioDataHandler varioDataHandler;
  
		
		public VarioDevice()
		{
			super();
			packetQueue = new ArrayDeque<VarioData>();
			varioInitialized = new EventSource();
			varioDisconnected  = new EventSource();
			realtimeStarted = new EventSource();
			realtimeData = new EventSource();
			
			
			//final VarioDevice d = this;
			initializeHandler = new VarioDataHandler()
			{
				@Override
				public void onData(byte[] data, boolean isNewPacket) {
					onInitializeRecieved(data, isNewPacket);
				}
			};
			
			realtimeHandler = new VarioDataHandler()
			{
				@Override
				public void onData(byte[] data, boolean isNewPacket) {
					
					if (!isRealtimeStarted)
					{
						isRealtimeStarted = true;
						realtimeStarted.raiseEvent(new EventObject(this));
					}
					
					if (isNewPacket)
					{
						onRealtimeDataReceived(data, isNewPacket);
					}
				}
			};
			
			transactionHandler = new VarioDataHandler()
			{
				@Override
				public void onData(byte[] data, boolean isNewPacket) {
					onTransactionalPacketReceived(data, isNewPacket);
				}
			};
			
			varioDataHandler = initializeHandler;
		}
		
		public void initializeDevice(Context context, UsbManager manager)
		{
			super.initializeDevice(context, manager, VENDOR_ID, PRODUCT_ID, DEVICE_ID);
		}
		
		@Override
		protected synchronized void onDetectUsbDevice()
		{
			super.onDetectUsbDevice();
			
			if (connection!=null)
			{
				initializeVarioDevice();			 
			}
		}
		
		@Override
		protected void onUsbDeviceDetached()
		{
			super.onUsbDeviceDetached();
			_isInitialized = false;
			isBusy=false;
			packetCounter=0;
			receivedPacketCounter=-1;
			varioDisconnected.raiseEvent(new EventObject(this));
		
		}
		
		/* Commands */
		
		public void initializeVarioDevice()
		{
			isBusy=false;
			this.sendCommand( new VarioData(VarioCommand.INITIALIZE) );
		}
		
		public void startRealtime()
		{
			this.sendCommand( new VarioData(VarioCommand.REALTIME_DATA_MODE)  );
			
		
		}
		
		protected VarioResponse getExpectedResponse(VarioCommand command)
		{
		    VarioResponse response = VarioResponse.OK;
		    
		    if (command == VarioCommand.ERASE_FLIGHTS) response= VarioResponse.OK;
		    else if (command==VarioCommand.ERASE_TRACKS) response = VarioResponse.OK;
		    else if (command==VarioCommand.INITIALIZE) response = VarioResponse.INITIALIZED;
		    else if (command==VarioCommand.READ_ID) response = VarioResponse.ID;
		    else if (command==VarioCommand.READ_LOG) response = VarioResponse.LOG;
		    else if (command==VarioCommand.READ_OPTION) response = VarioResponse.OPTION;
		    else if (command==VarioCommand.READ_OPTIONS) response = VarioResponse.OPTIONS;
		    else if (command==VarioCommand.READ_RTC) response = VarioResponse.RTC;
		    else if (command==VarioCommand.READ_TRACK) response = VarioResponse.TRACK;
		    else if (command==VarioCommand.READ_TRACK_LOG_COUNT) response = VarioResponse.TRLOG_COUNT;
		    else if (command==VarioCommand.REALTIME_DATA_MODE) response = VarioResponse.REALTIME_DATA;
		    else if (command==VarioCommand.SET_FACTORY_DEFAULTS) response = VarioResponse.OK;
		    else if (command==VarioCommand.WRITE_OPTION) response = VarioResponse.OK;
		    else if (command==VarioCommand.WRITE_OPTIONS) response = VarioResponse.OK;
		    
		    return response;
		}
		
		private void onRealtimeDataReceived(byte[] data,boolean isNewPacket)
		{
			//final int RT_PACKET_SIZE=14;
			
			int i = 3;
			ByteBuffer bitConverter= ByteBuffer.allocate(4);
			
			ArrayList<VarioRealtimeData> realtimeDataResult;
			
			//for (int i=start; i<data.length;i+=RT_PACKET_SIZE)
			//{
			
				VarioRealtimeData realtimeData = new VarioRealtimeData();
				
			 
				int altitude = 	 (data[i]   &0x000000FF  ) | 
								 (data[i+1] &0x000000FF ) << 8 | 
								 (data[i+2] &0x000000FF ) << 16;
				realtimeData.altitude = altitude;
		  

				short climb = (short) (0x0000 | (0xFF & data[i+3]) | (data[i+4] & 0xFF) <<8);
				realtimeData.climb = (double) climb;
				
				int battery =  0x0000 | (0xFF & data[i+5]) | (data[i+6] & 0xFF) <<8;
				realtimeData.battery = ((double) (battery) /100.0); //batt * 100
 				
				int temp =  0x0000 | (0xFF & data[i+7]) | (data[i+8] & 0xFF) <<8;
				realtimeData.temperature  = (double) temp / 100.0;  
	
				//Log.e("TEMP2", Double.toString(temp2)  );
				
			this.realtimeData.raiseEvent( new EventObject(realtimeData) );
			//}
			//TODO: call back
			
		}
		
		private void onInitializeRecieved(byte[] data,boolean isNewPacket)
		{
			  VarioResponse response =  VarioResponse.values()[data[1]]; 
        	
            if (response == VarioResponse.INITIALIZED)
            {
            	 if (response== VarioResponse.INITIALIZED)
            	 {
            		//vario has initialized
		            onVarioInitialzied();
		            this.stopRead(null);
            	 }
            }
		}
		
		private void onTransactionalPacketReceived(byte[] data, boolean isNewPacket)
		{
			if (isNewPacket)
			{
	            VarioResponse response =  VarioResponse.values()[data[1]]; 
	            VarioResponse expectedResponse = getExpectedResponse(lastCommand.command);
	            
	            boolean hasErrors = (response==expectedResponse);
	            
				 switch (expectedResponse) {
	                
             	case TIMING:
             		//no action needed for timing packets
             	  int i=0;
             	  i++;
             	break;
             
                 case OK:
                     onOk();
                     break;
                 case LOG: 
                     //TODO: handle log
                     //OnReadLog(data, response, hasErrors);
                     break;
         
                 case RTC: 
                 	 onTime(data);
                 break;

                 case OPTION: 
                     onOption(data);
                     break;
                 case OPTIONS:
                     onOptions(data);
                     break;
                 case TRACK: 
                     onReadTrack(data, response, hasErrors);
                     break;        
                 default:
                 	onUnexpectedResponse(response,data);
                 break;
             }
			}
		}
		
	    @Override
	    protected synchronized void onVarioDataReceived(byte[] data)
		{
	    	super.onVarioDataReceived(data);
	    	isBusy = false;
	    	
	    	if (this.varioDataHandler!=null)
	    	{
	    	
	    		long bytesAvailable = 0;
		    
	        	ByteBuffer packetCountBuffer = ByteBuffer.allocate(2);
	        	packetCountBuffer.put(0, data[data.length-2] );
	        	packetCountBuffer.put(1, data[data.length-1] );
		        int newPacketCounter= packetCountBuffer.getShort();
 
		        
		        //the device may send the same packet twice
		        boolean isNewPacket = (receivedPacketCounter != newPacketCounter)  ;
		        receivedPacketCounter = newPacketCounter;
		        	        
		        Log.e("VARIO","Data:" + data[0] + ", new:" + isNewPacket + "(" + newPacketCounter + ")" );
		        this.varioDataHandler.onData(data, isNewPacket);
		           
		   }
		    
		}
	    
	    
	    protected void onUnexpectedResponse(VarioResponse response, byte[] data)
	    {
	    	String responseString = response.toString();
	    	Log.e("VARIO", "ERROR:" + responseString);
	    }
		
		public void setDelegate(IVarioDeviceDelegate delegate)
		{
			this.delegate = delegate;
		}
		
		void sendCommand(VarioCommand command, byte data)
		{		 
			this.sendCommand(command, new byte[]{data});
		}
		
		synchronized void retrySendCommand()
		{
			
		}
		
		synchronized void sendCommand(VarioData packet)
		{
			if (this._isUsbConnected)
			{
				if (!isBusy)
				{
					if(packetQueue.isEmpty())
					{
						this.lastCommand = packet;
						sendCommand(packet.command, packet.data);
					}
					else
					{
						VarioData data= packetQueue.remove();
						this.lastCommand = data;
						packetQueue.add(packet);
					}
				}
				else
				{
					packetQueue.add(packet);
				}
 			}
		}
		
		synchronized void sendCommand(VarioCommand command, byte[] data)
		{
			 isBusy = true;
			 
			//set up receive handlers and read mode 
						
			final boolean continueReading;
			switch(command)
			{
				case REALTIME_DATA_MODE:
					this.varioDataHandler = this.realtimeHandler;
					continueReading=true;
				break;
				
				case INITIALIZE:
					this.varioDataHandler = this.initializeHandler;
					continueReading=true;
				break;
				
				default:
					this.varioDataHandler = this.transactionHandler;
					continueReading=false;
				break;
				
			}
           
        
            //create the packet
            //Minimum bytes in packet
            //1. MAGIC
            //2. COMMAND
            //3. NBytes
            //5. Checksum     
			
            long dataLength =  (data== null) ? 0 : data.length;
            byte packetLength = (byte) (MIN_PACKET_SIZE + dataLength);     //just the size of the data payload, used for calculating the checksum
            
            ByteBuffer buffer  = ByteBuffer.allocate(64);
            
            //write packet header            
            buffer.put(0, SET_MAGIC);
            buffer.put(1, (byte) command.ordinal());
            buffer.put(2, packetLength);
            buffer.position(3);
 
            if (data!=null)
            {
            	buffer.put(data);
            }

            //calculate checksum
            byte sum = 0; //checksum
           
            for (int i = 0; i < packetLength; i++)
            {
                sum += buffer.get(i);
            }
            
            sum = (byte) ~sum;
           
           buffer.put(packetLength-1,sum); //write checksum             
           buffer.position(packetLength);
           
           byte[] packetCounterData=  ByteBuffer.allocate(4).putInt(packetCounter).array();
           buffer.put(packetCounterData,0,2);
           
      
 
           final ByteBuffer sendBuffer = buffer;
           final VarioCommand sendCommand = command;
           
          
           
           this.stopRead( new  EventListener()
           {
        	   @Override
        	    public void onExecute(EventObject e) {
        	    super.onExecute(e);
        	    
        	    Log.e("VARIO", "sendDataAsync:" + sendCommand.toString());
        	    sendDataAsync(sendBuffer, continueReading);
        	    }  
        	  
           });
  	          
           packetCounter++;          
	
		}
		
		@Override
		protected synchronized void stopRead() {
			super.stopRead();
			
			//reset the packet counter
			this.receivedPacketCounter=-1;
		}

		protected void onVarioInitialzied()
		{
			_isInitialized=true;
			//TODO: Do stuff			
				
			this.varioInitialized.raiseEvent(new EventObject(this));
		}
		
		protected void onOk()
		{
			  switch (lastCommand.command)
			    {
			        case SET_VARIO_AUDIO:
			          //TODO: dispatch events          
			        break;
			        
			        case ERASE_FLIGHTS:
			        	 //TODO: dispatch events    
			         break;
			         
			        case ERASE_TRACKS:  
			            //TODO: dispatch notification
			        break;
			        
			        case SET_FACTORY_DEFAULTS:
			            //TODO: dispatch notification
		            break;            
			    }
		}
		
 
		
		protected void onOption(byte[] data)
		{
			//TODO: implement onOption
		}
		
		protected void onTime(byte[] data)
		{
			
		}
		
		protected void onOptions(byte[] data)
		{
			
		}
		
		protected void onLog(byte[] data,VarioResponse response, boolean hasErrors)
		{		
		
		}
		
		protected void onReadTrack(byte[] data,VarioResponse response, boolean hasErrors)
		{
			
		}
		
	    public boolean isConnected()
	    {
	    	return _isUsbConnected && _isInitialized;
	    }
		
}
