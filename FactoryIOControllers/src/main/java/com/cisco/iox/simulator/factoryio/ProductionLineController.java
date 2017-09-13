package com.cisco.iox.simulator.factoryio;

import java.io.IOException;

import de.gandev.modjn.entity.exception.ConnectionException;

public class ProductionLineController implements ControllerInterface {

	private FactoryIOSlavehandler handler;
	private ProductionLineLogic plc;

	public static void closeModbusServer(FactoryIOModbusServer fioMS) {
		try {
			if (fioMS!=null) 
				fioMS.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		ProductionLineController plc = new ProductionLineController();
		FactoryIOModbusServer fioMS = null;
		try {
			fioMS = new FactoryIOModbusServer("0.0.0.0", 502, plc);
			plc.handler = fioMS.getHandler();
			plc.plc = new ProductionLineLogic(plc.handler);
			plc.plc.reset();
		} catch (ConnectionException e) {
			e.printStackTrace();
			closeModbusServer(fioMS);
			System.exit(-1);
		} catch (Exception e) {
			e.printStackTrace();
			closeModbusServer(fioMS);
			System.exit(-1);
		}

		while (true) {
			try {
				if (plc.plc.isFactoryIORunning()) {
					plc.plc.runTillStopped(8l);
				} else {
					Thread.sleep(100);
				}
			} catch (InterruptedException e) {
				System.out.println("Intterpted, stopping ... ");
				closeModbusServer(fioMS);
				System.exit(0);
			}
		}

	}

	public void readEvent(int addr, Object value) {
		// System.out.println("Read event : " + addr + " Value : " + value);

	}

	public void writeEvent(int addr, Object value) {
		System.out.println("Write event : " + addr + " Value : " + value);
	}

	public void readEvents(int addr, Object value) {
		System.out.println("Read events addr : " + addr + " Value : " +
		value);
	}

	public void writeEvents(int addr, Object value) {
		System.out.println("Write events addr : " + addr + " Value : " + value);
	}

}

