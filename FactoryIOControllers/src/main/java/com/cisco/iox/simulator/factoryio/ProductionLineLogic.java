package com.cisco.iox.simulator.factoryio;

public class ProductionLineLogic {

	static private int FACTORY_IO_RUNNING = 9;
	static private int BASES_AT_EXIT = 8;
	static private int BASES_CENTER_ERROR = 7;
	static private int BASES_CENTER_BUSY = 6;
	static private int BASES_ENTRY = 5;
	static private int LIDS_EXIT = 4;
	static private int LIDS_CENTER_ERROR = 3;
	static private int LIDS_CENTER_BUSY = 2;
	static private int LIDS_ENTRY = 1;
	static private int LIDS_RAW_CONVEYER = 10001;
	static private int LIDS_CENTER_PRODUCE_LIDS = 10002;
	static private int LIDS_CENTER_START = 10003;
	static private int LIDS_CENTER_RESET = 10004;
	static private int LIDS_CENTER_STOP = 10005;
	static private int LIDS_EXIT_CONV1 = 10006;
	static private int LIDS_EXIT_CONV2 = 10007;
	static private int BASE_RAW_CONV = 10008;
	static private int BASES_CENTER_PRODUCE_LIDS = 10009;
	static private int BASES_CENTER_START = 10010;
	static private int BASES_CENTER_RESET = 10011;
	static private int BASES_CENTER_STOP = 10012;
	static private int BASES_EXIT_CONV1 = 10013;
	static private int BASES_EXIT_CONV2 = 10014;
	static private int EXIT_CONV = 10015;

	static private int LIDS_COUNTER = 30001;
	static private int BASES_COUNTER = 30002;

	private FunctionTrigger ftLidsAtEntry = new FunctionTrigger();
	private FunctionTrigger ftLidsCenterBusy = new FunctionTrigger();

	private FunctionTrigger ftBasesAtEntry = new FunctionTrigger();
	private FunctionTrigger ftBasesCenterBusy = new FunctionTrigger();

	private boolean feedLidMaterial = true;
	private boolean feedBaseMaterial = true;

	private FactoryIOSlavehandler handler;

	public void reset() {
		stopLidsRawConveyor();
		startLidsCenterStart();
		stopBasesRawConveyer();
		startBasesCenterStart();

		feedBaseMaterial = true;
		feedLidMaterial = true;
		resetBasesCounter();
		resetLidsCounter();

		startLidsExitConveyer1();
		startLidsExitConveyer2();
		startBasesExitConveyer1();
		startBasesExitConveyer2();

		startExitConveyer();
	}

	public ProductionLineLogic(FactoryIOSlavehandler handler) {
		super();
		this.handler = handler;
	}

	public boolean isFactoryIORunning() {
		return handler.getCoil(FACTORY_IO_RUNNING);
	}

	private void startLidsRawConveyor() {
		handler.addCoil(LIDS_RAW_CONVEYER, true);
	}

	private void stopLidsRawConveyor() {
		handler.addCoil(LIDS_RAW_CONVEYER, false);
	}

	private void startLidsCenterStart() {
		handler.addCoil(LIDS_CENTER_START, true);
	}

	private void stopLidsCenterStart() {
		handler.addCoil(LIDS_CENTER_START, false);
	}

	private void startBasesRawConveyer() {
		handler.addCoil(BASE_RAW_CONV, true);
	}

	private void stopBasesRawConveyer() {
		handler.addCoil(BASE_RAW_CONV, false);
	}

	private void startBasesCenterStart() {
		handler.addCoil(BASES_CENTER_START, true);
	}

	private void stopBasesCenterStart() {
		handler.addCoil(BASES_CENTER_START, false);
	}

	private void resetLidsCounter() {
		handler.addReg(LIDS_COUNTER, 0);
	}

	private void incrLidsCounter() {
		handler.incrReg(LIDS_COUNTER);
	}

	private void incrBasesCounter() {
		handler.incrReg(BASES_COUNTER);
	}

	private void resetBasesCounter() {
		handler.addReg(BASES_COUNTER, 0);
	}

	private void startLidsExitConveyer1() {
		handler.addCoil(LIDS_EXIT_CONV1, true);
	}

	private void startLidsExitConveyer2() {
		handler.addCoil(LIDS_EXIT_CONV2, true);
	}

	private void stopLidsExitConveyer1() {
		handler.addCoil(LIDS_EXIT_CONV1, false);
	}

	private void stopLidsExitConveyer2() {
		handler.addCoil(LIDS_EXIT_CONV2, false);
	}

	private void startBasesExitConveyer1() {
		handler.addCoil(BASES_EXIT_CONV1, true);
	}

	private void startBasesExitConveyer2() {
		handler.addCoil(BASES_EXIT_CONV2, true);
	}

	private void stopBasesExitConveyer1() {
		handler.addCoil(BASES_EXIT_CONV1, false);
	}

	private void stopBasesExitConveyer2() {
		handler.addCoil(BASES_EXIT_CONV2, false);
	}

	private void startExitConveyer() {
		handler.addCoil(EXIT_CONV, true);
	}

	private void stopExitConveyer() {
		handler.addCoil(EXIT_CONV, false);
	}

	public boolean getLidsAtEntry() {
		return handler.getCoil(LIDS_ENTRY);
	}

	public boolean getLidsCenterBusy() {
		return handler.getCoil(LIDS_CENTER_BUSY);
	}

	public boolean getBasesAtEntry() {
		return handler.getCoil(BASES_ENTRY);
	}

	public boolean getBasesCenterBusy() {
		return handler.getCoil(BASES_CENTER_BUSY);
	}

	public void runTillStopped(long cycleTime) throws InterruptedException {
		while (isFactoryIORunning()) {
			this.Execute();
			Thread.sleep(cycleTime);
		}
		handler.resetHandler();
		reset();
		// this.reset();
	}

	public void Execute() {
		ftLidsAtEntry.CLK(!getLidsAtEntry());
		ftLidsCenterBusy.CLK(getLidsCenterBusy());

		if (ftLidsAtEntry.Q()) {
			feedLidMaterial = false;
		}

		if (ftLidsCenterBusy.Q()) {
			feedLidMaterial = true;
			incrLidsCounter();
		}

		if (feedLidMaterial && !getLidsCenterBusy()) {
			startLidsRawConveyor();
		} else {
			stopLidsRawConveyor();
		}

		// Bases
		ftBasesAtEntry.CLK(!getBasesAtEntry());
		ftBasesCenterBusy.CLK(getBasesCenterBusy());

		if (ftBasesAtEntry.Q()) {
			feedBaseMaterial = false;
		}

		if (ftBasesCenterBusy.Q()) {
			feedBaseMaterial = true;
			incrBasesCounter();
		}

		if (feedBaseMaterial && !getBasesCenterBusy()) {
			startBasesRawConveyer();
		} else {
			stopBasesRawConveyer();
		}

	}

}
