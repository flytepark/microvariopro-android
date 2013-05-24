package com.flytepark.events; 

import java.util.EventObject;

public interface IEventListener {
	public void onExecute(EventObject e);
}
