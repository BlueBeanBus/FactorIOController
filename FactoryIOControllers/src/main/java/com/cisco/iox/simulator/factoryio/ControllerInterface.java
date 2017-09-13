package com.cisco.iox.simulator.factoryio;

public interface ControllerInterface {
	public void readEvent(int addr, Object value);
	public void writeEvent(int addr, Object value);
	public void readEvents(int addr, Object value);
	public void writeEvents(int addr, Object value);
}
