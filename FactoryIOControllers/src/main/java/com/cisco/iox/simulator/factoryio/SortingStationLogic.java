package com.cisco.iox.simulator.factoryio;

import java.util.Random;

public class SortingStationLogic {
	static private int FACTORY_IO_RUNNING = 1;
	static public int FACTORY_IO_RESET= 3;
	static public int NORMAL_RESET = 6;
	static public int RESET = 7;
	static public int AUTO = 8;
	static public int MANUAL = 9;
	static private int AT_EXIT = 10;
	
	static private int ENTRY_CONVEYER = 10002;
	static private int EXIT_CONVEYER = 10003;
	static protected int VISION_SENSOR = 40001;
	static private int SORTER_BELT1 = 10008;
	static private int SORTER_TURN1 = 10009;
	static private int SORTER_BELT2 = 10010;
	static private int SORTER_TURN2 = 10011;
	static private int SORTER_BELT3 = 10012;
	static private int SORTER_TURN3 = 10013;
	static private int REMOVER1 = 30001;
	static private int REMOVER2 = 30002;
	static private int REMOVER3 = 30003;
	static private int GREEN_BASES_CNT = 30004;
	static private int GREEN_LIDS_CNT = 30005;
	static private int BLUE_BASES_CNT = 30006;
	static private int BLUE_LIDS_CNT = 30007;
	static private int GREEN_SCRAP_CNT = 30008;
	static private int BLUE_SCRAP_CNT = 30009;
	static private int DELIVER_TIME = 30010;
	static private int LIGHT_ON = 10014;
	static private int LIGHT_OFF = 10016;
	static protected int RESET_LIGHT = 10007;
	static protected int PRODUCE_LIDS = 10018;
	static protected int PRODUCE_BASES = 10019;
	
	static protected int PANEL_RESET = 10020;
	
	private int sensorId = 0;
	private int lastSensorId = 0;
	
	private Colors currentColor = Colors.NONE;
	private Colors lastColor = Colors.NONE;
	private int ticks = 0;
	
	private enum Colors {BLUE, GREEN, SCRAP, NONE};
	
	volatile protected boolean fioReset = false;
	volatile protected boolean fiostop = false;

	private FactoryIOSlavehandler handler;
	private FunctionTrigger ftAtExit = new FunctionTrigger();
	private State feederState;
	private State sorterState;
	private int count = 0;
	private long arrivalTime = 0;

	public void reset() {
		this.feederState = State.State0;
		this.sorterState = State.State0;
		resetSensorId();
		arrivalTime = 0;
		resetDeliverTime();
		currentColor = Colors.NONE;
		lastColor = Colors.NONE;
		ticks = 0;
	}
	
	public void emergencyStop() {
		stopEntryConveyer();
		stopExitConveyer();
		reset();
	}
	
	public void normalStop() {
		stopEntryConveyer();
		stopExitConveyer();
		reset();
	}

	public SortingStationLogic(FactoryIOSlavehandler handler) {
		super();
		this.handler = handler;
		reset();
		this.handler.addCoil(PRODUCE_LIDS, true);
	}
	
	private boolean checkStuckCondition() { 
			this.ticks++;
			if (this.ticks >= 2000) {
				this.ticks  = 0;
				return true;
			}
			return false;		
	}
	
	private boolean checkMalfunction(int n) { 
		if (!isFactoryIOReset())
			return false;
		if (sensorId == n) {
			this.count++;
			if (this.count == 1000) {
				sorterState = State.State0;
				this.count  = 0;
			}
			return true;
		}
		return false;
		
	}
	
	public void setSensorId() {
		sensorId = lastSensorId % 3 + 1;
		System.out.println("Setting sensor Id : " + sensorId + "for fault injection");
	}
	
	public void resetSensorId() {
		lastSensorId = sensorId;
		sensorId = 0;
		count = 0;
	}

	public boolean isFactoryIOReset() {
		return handler.getCoil(RESET);
	}
	
	public boolean isFactoryIORunning() {
		return handler.getCoil(FACTORY_IO_RUNNING);
	}
	
	public boolean isFactoryIORestarted() {
		return handler.getCoil(FACTORY_IO_RUNNING);
	}

	private void startEntryConveyer() {
		handler.addCoil(ENTRY_CONVEYER, true);
	}
	
	private void startLightOn() {
		handler.addCoil(LIGHT_ON, true);
		handler.addCoil(LIGHT_OFF, false);
		handler.addCoil(RESET_LIGHT, false);
	}
	
	private void startLightOff() {
		handler.addCoil(LIGHT_ON, false);
		handler.addCoil(LIGHT_OFF, true);
		if (fioReset) {
			handler.addCoil(RESET_LIGHT, true);
		}
	}

	private void startExitConveyer() {
		handler.addCoil(EXIT_CONVEYER, true);
	}

	private void stopEntryConveyer() {
		handler.addCoil(ENTRY_CONVEYER, false);
	}

	private void stopExitConveyer() {
		handler.addCoil(EXIT_CONVEYER, false);
	}

	public int getRemover1Count() {
		return handler.getReg(REMOVER1);
	}

	public int getRemover2Count() {
		return handler.getReg(REMOVER2);
	}

	public int getRemover3Count() {
		return handler.getReg(REMOVER3);
	}

	public void incrRemover1Count() {
		handler.incrReg(REMOVER1);
	}

	public void incrRemover2Count() {
		handler.incrReg(REMOVER2);
	}

	public void incrRemover3Count() {
		handler.incrReg(REMOVER3);
	}

	public void incrGreenBasesCount() {
		handler.incrReg(GREEN_BASES_CNT);
	}

	public void incrGreenLidsCount() {
		handler.incrReg(GREEN_LIDS_CNT);
	}

	public void incrBlueBasesCount() {
		handler.incrReg(BLUE_BASES_CNT);
	}

	public void incrBlueLidsCount() {
		handler.incrReg(BLUE_LIDS_CNT);
	}

	public void incrGreenScrapCount() {
		handler.incrReg(GREEN_SCRAP_CNT);
	}

	public void incrBlueScrapCount() {
		handler.incrReg(BLUE_SCRAP_CNT);
	}
	
	public void resetCounters() {
		handler.addReg(GREEN_BASES_CNT, 0);
		handler.addReg(GREEN_LIDS_CNT, 0);
		handler.addReg(GREEN_SCRAP_CNT, 0);
		handler.addReg(BLUE_BASES_CNT, 0);
		handler.addReg(BLUE_LIDS_CNT, 0);
		handler.addReg(BLUE_SCRAP_CNT, 0);
	}

	public void CheckAndHandleConveyer() {
		if (isFactoryIORunning()) {
			handler.resetHandler();
			startEntryConveyer();
			startExitConveyer();
		} else {
			stopEntryConveyer();
			stopExitConveyer();
			handler.resetHandler();
		}
	}

	private void startBlade() {
		handler.addCoil(SORTER_TURN1, true);
	}

	private boolean atExitValue() {
		return handler.getCoil(AT_EXIT);
	}

	private int getVisionSensor() {
		return handler.getReg(VISION_SENSOR);
	}

	private void startCB1() {
		handler.addCoil(SORTER_BELT1, true);
	}

	private void startTurn1() {
		if (!checkMalfunction(1))
			handler.addCoil(SORTER_TURN1, true);
	}

	private void stopTurn1() {
		handler.addCoil(SORTER_TURN1, false);
	}

	private void stopCB1() {
		handler.addCoil(SORTER_BELT1, false);
	}

	private void startCB2() {
		handler.addCoil(SORTER_BELT2, true);
	}

	private void startTurn2() {
		if (!checkMalfunction(2))
			handler.addCoil(SORTER_TURN2, true);
	}

	private void stopTurn2() {
		handler.addCoil(SORTER_TURN2, false);
	}

	private void stopCB2() {
		handler.addCoil(SORTER_BELT2, false);
	}

	private void startCB3() {
		handler.addCoil(SORTER_BELT3, true);
	}

	private void startTurn3() {
		if (!checkMalfunction(3))
			handler.addCoil(SORTER_TURN3, true);
	}

	private void stopTurn3() {
		handler.addCoil(SORTER_TURN3, false);
	}

	private void stopCB3() {
		handler.addCoil(SORTER_BELT3, false);
	}

	public void runTillStopped(long cycleTime) throws InterruptedException {
		if (isFactoryIORunning()&& (!fioReset) && (!fiostop)) {
			startLightOn();
			this.handler.addCoil(PRODUCE_LIDS, true);
		}
		
		while (isFactoryIORunning() && (!fioReset)&& (!fiostop)) {
			this.Execute();
			Thread.sleep(cycleTime);
		}
		
		startLightOff();
		sensorId = 0;
		count = 0;
		handler.resetHandler();
		//System.out.println("feeder is : " + feederState + " sorter is : " + sorterState);
		// this.reset();
	}
	
	public void computerDeliverTime() {
		int deliverTime = (int) ((System.currentTimeMillis() - arrivalTime)/1000);
		System.out.println("Deliver time is: " + deliverTime);
		handler.addReg(DELIVER_TIME, deliverTime);
		arrivalTime = 0;
	}
	
	public void resetDeliverTime() {
		handler.addReg(DELIVER_TIME, 0);
		arrivalTime = 0;
	}
	
	private void checkPartSorted() {
		if (ftAtExit.Q()) {
			sorterState = State.State0;
			computerDeliverTime();
			this.ticks = 0;
			
		} else if (checkStuckCondition()) {
			System.out.println("Part got stuck, moving");
			sorterState = State.State0;
			computerDeliverTime();
		}
	}

	public void Execute() {
		ftAtExit.CLK(!atExitValue());

		// Feeder
		if (feederState == State.State0) {
			startEntryConveyer();

			if (getVisionSensor() != 0) {
				if (sorterState == State.State0) {
					int block = getVisionSensor();
					// Set sorter state
					if (block == 2 || block == 3) {
						// Blue raw material
						sorterState = State.State1;
						lastColor = currentColor;
						currentColor = Colors.BLUE;
					} else if (block == 5 || block == 6) {
						// Green raw material
						sorterState = State.State2;
						lastColor = currentColor;
						currentColor = Colors.GREEN;
					} else {
						// Other
						sorterState = State.State3;
						lastColor = currentColor;
						currentColor = Colors.SCRAP;
						//flipRobots();
					}

					feederState = State.State1;
					if (arrivalTime == 0)
						arrivalTime = System.currentTimeMillis();
				} else {
					stopEntryConveyer();
				}
			}
		} else if (feederState == State.State1) {
			startEntryConveyer();

			if (getVisionSensor() == 0) {
				feederState = State.State0;
			}
		}

		// Sorters
		if (sorterState == State.State0) {
			stopCB1();
			stopTurn1();
			stopCB2();
			stopTurn2();
			stopCB3();
			stopTurn3();
			stopExitConveyer();
		} else if (sorterState == State.State1) // First sorter
		{
			startExitConveyer();

			startCB1();
			startTurn1();
			stopCB2();
			stopTurn2();
			stopCB3();
			stopTurn3();

			checkPartSorted();
		} else if (sorterState == State.State2) // Second sorter
		{
			startExitConveyer();

			stopCB1();
			stopTurn1();
			startCB2();
			startTurn2();
			stopCB3();
			stopTurn3();

			checkPartSorted();
		} else if (sorterState == State.State3) // Third sorter
		{
			startExitConveyer();

			stopCB1();
			stopTurn1();
			stopCB2();
			stopTurn2();
			startCB3();
			startTurn3();

			checkPartSorted();
		}

		// System.out.println("feeder is : " + feederState + " sorter is : " +
		// sorterState);
	}

	private void flipRobots() {
		boolean robot1 = handler.getCoil(PRODUCE_BASES);
		boolean robot2 = handler.getCoil(PRODUCE_LIDS);
		
		handler.addCoil(PRODUCE_BASES, robot2);
		handler.addCoil(PRODUCE_LIDS, robot1);
		
	}

	public void countBlocks(int addr, int value) {
		if (addr == VISION_SENSOR) {
			if (value == 2) {
				incrRemover1Count();
				incrBlueLidsCount();
			} else if (value == 3) {
				incrRemover1Count();
				incrBlueBasesCount();
			} else if (value == 5) {
				incrRemover2Count();
				incrGreenLidsCount();
			} else if (value == 6) {
				incrRemover2Count();
				incrGreenBasesCount();
			} else if (value == 1 || value == 4) {
				incrRemover3Count();
				if (value == 1) {
					incrBlueScrapCount();
				} else if (value == 4) {
					incrGreenScrapCount();
				}
			}
		}

	}

}
