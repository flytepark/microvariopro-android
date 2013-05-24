package com.flytepark.events;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
 

public class EventSource {
	  private List _listeners = new ArrayList();
 	  public synchronized void addEventListener(Object listener)  {
   	    _listeners.add(listener);
   	  }
   	  public synchronized void removeEventListener(Object listener)   {
  	    _listeners.remove(listener);
  	  }
  	 
  	  // call this method whenever you want to notify
  	  //the event listeners of the particular event
  	  public synchronized void raiseEvent(EventObject e) {
  	    Iterator i = _listeners.iterator();
  	    while(i.hasNext())  {
  	      
  	      ((IEventListener)  i.next()).onExecute(e);
  	    }
  	  }
}
