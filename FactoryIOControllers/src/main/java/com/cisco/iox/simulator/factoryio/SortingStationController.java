package com.cisco.iox.simulator.factoryio;

import java.io.IOException;

import de.gandev.modjn.entity.exception.ConnectionException;
import io.netty.handler.codec.http.LastHttpContent;

public class SortingStationController implements ControllerInterface {

	private FactoryIOSlavehandler handler;
	private SortingStationLogic ssl;

	public static void closeModbusServer(FactoryIOModbusServer fioMS) {
		try {
			if (fioMS != null)
				fioMS.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static void main(String[] args) {
		SortingStationController ssc = new SortingStationController();
		FactoryIOModbusServer fioMS = null;
		try {
			fioMS = new FactoryIOModbusServer("0.0.0.0", 502, ssc);
			ssc.handler = fioMS.getHandler();
			ssc.ssl = new SortingStationLogic(ssc.handler);
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
				if (ssc.ssl.isFactoryIORunning()) {
					ssc.ssl.runTillStopped(8l);
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
		ssl.countBlocks(addr, (Integer) value);
		// System.out.println(
		// "Counters " + ssl.getRemover1Count() + ":" + ssl.getRemover2Count() +
		// ":" + ssl.getRemover3Count());
	}

	public void readEvents(int addr, Object value) {
		// System.out.println("Read events addr : " + addr + " Value : " +
		// value);
	}

	public void writeEvents(int addr, Object value) {
		System.out.println("Write events addr : " + addr + " Value : " + value);
		if (addr == SortingStationLogic.AUTO) {
			boolean val = (Boolean) value;
			if (val == true) {
				ssl.setSensorId();
			} else {
				ssl.resetSensorId();
			}
		} else if (addr == SortingStationLogic.RESET) {
			boolean val = (Boolean) value;
			if (val == false) {
				ssl.fioReset = true;
				ssl.emergencyStop();
				handler.addCoil(SortingStationLogic.PANEL_RESET, true);
			} else {
				ssl.fioReset = false;
				handler.addCoil(SortingStationLogic.PANEL_RESET, false);
			}
		} else if (addr == SortingStationLogic.NORMAL_RESET) {
			boolean val = (Boolean) value;
			System.out.println("RESET : " + val);
			if (val == true) {
				ssl.resetCounters();
			}
		} 
	}

}
