package com.cisco.iox.simulator.factoryio;

import java.io.Closeable;
import java.io.IOException;

import de.gandev.modjn.ModbusServer;
import de.gandev.modjn.entity.exception.ConnectionException;

public class FactoryIOModbusServer implements Closeable {
	
	final private ModbusServer modbusServer;
	final private FactoryIOSlavehandler fioSH;
	
	public  FactoryIOModbusServer(String host, int port, ControllerInterface ci) throws ConnectionException {
		System.out.println("host - " + host);
		System.out.println("port - " + port);
		short unitId = 1;
		this.modbusServer = new ModbusServer(host, port);
		this.fioSH = new FactoryIOSlavehandler(ci);
		modbusServer.setup(this.fioSH);
	}

	public void close() throws IOException {
		if(modbusServer != null) {
			modbusServer.close();
		}
	}
	
	public FactoryIOSlavehandler getHandler() {
		return this.fioSH;
	}

}
