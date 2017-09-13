package com.cisco.iox.simulator.factoryio;

import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import de.gandev.modjn.entity.func.WriteSingleCoil;
import de.gandev.modjn.entity.func.WriteSingleRegister;
import de.gandev.modjn.entity.func.request.ReadCoilsRequest;
import de.gandev.modjn.entity.func.request.ReadDiscreteInputsRequest;
import de.gandev.modjn.entity.func.request.ReadHoldingRegistersRequest;
import de.gandev.modjn.entity.func.request.ReadInputRegistersRequest;
import de.gandev.modjn.entity.func.request.WriteMultipleCoilsRequest;
import de.gandev.modjn.entity.func.request.WriteMultipleRegistersRequest;
import de.gandev.modjn.entity.func.response.ReadCoilsResponse;
import de.gandev.modjn.entity.func.response.ReadDiscreteInputsResponse;
import de.gandev.modjn.entity.func.response.ReadHoldingRegistersResponse;
import de.gandev.modjn.entity.func.response.ReadInputRegistersResponse;
import de.gandev.modjn.entity.func.response.WriteMultipleCoilsResponse;
import de.gandev.modjn.entity.func.response.WriteMultipleRegistersResponse;
import de.gandev.modjn.handler.ModbusRequestHandler;
import io.netty.channel.ChannelHandler.Sharable;

@Sharable
public class FactoryIOSlavehandler extends ModbusRequestHandler {

	private Map<Integer, AtomicInteger> slaveRegisters;
	private Map<Integer, AtomicBoolean> slaveCoils;
	private ControllerInterface ci;


	public void resetHandler() {
		for (int i=10001; i <= 10001 + 16 ; i++)
			this.slaveCoils.put(i, new AtomicBoolean(false));
		//for (int i=30001; i <= 30001 + 16; i++)
			//this.slaveRegisters.put(i, new AtomicInteger(0));
	}
	
	private void reset() {
		for (int i=1; i <= 64 ; i++)
			this.slaveCoils.put(i, new AtomicBoolean(false));
		for (int i=10001; i <= 10001 + 64 ; i++)
			this.slaveCoils.put(i, new AtomicBoolean(false));
		for (int i=30001; i <= 30001 + 64; i++)
			this.slaveRegisters.put(i, new AtomicInteger(0));
		for (int i=40001; i <= 40001 + 64; i++)
			this.slaveRegisters.put(i, new AtomicInteger(0));
	}
	
	public FactoryIOSlavehandler(ControllerInterface ci) {
		this.slaveRegisters = new ConcurrentHashMap<Integer, AtomicInteger>();
		this.slaveCoils = new ConcurrentHashMap<Integer, AtomicBoolean>();
		this.reset();
		this.ci = ci;
	}

	public void addReg(int addr, Integer val) {
		this.slaveRegisters.get(addr).set(val);
	}

	public Integer getReg(int addr) {
		return this.slaveRegisters.get(addr).get();
	}
	
	public Integer incrReg(int addr) {
		return this.slaveRegisters.get(addr).incrementAndGet();
	}

	public void addCoil(int addr, boolean val) {
		this.slaveCoils.get(addr).set(val);
	}

	public boolean getCoil(int addr) {
		if (!this.slaveCoils.containsKey(addr)) return false;
		return this.slaveCoils.get(addr).get();
	}

	@Override
	protected ReadCoilsResponse readCoilsRequest(ReadCoilsRequest arg0) {
		BitSet coils = new BitSet(arg0.getQuantityOfCoils()+1);
		for (int i = 0; i < arg0.getQuantityOfCoils()+1; i++) {
			coils.set(i);
		}
		for (int i = 0; i < arg0.getQuantityOfCoils(); i++) {
			boolean val = this.getCoil(1 + arg0.getStartingAddress()+i);
			//System.out.println("coil: " + val);
			if (val) {
				coils.set(i);				
			} else {
				coils.clear(i);
			}
			this.ci.readEvents(1 + arg0.getStartingAddress()+i, val);
		}
		ReadCoilsResponse rcr = new ReadCoilsResponse(coils);
		return rcr;
	}

	@Override
	protected ReadDiscreteInputsResponse readDiscreteInputsRequest(ReadDiscreteInputsRequest arg0) {
		BitSet coils = new BitSet(arg0.getQuantityOfCoils());
		for (int i = 0; i < arg0.getQuantityOfCoils()+1; i++) {
			coils.set(i);
		}
		for (int i = 0; i < arg0.getQuantityOfCoils(); i++) {
			boolean val = this.getCoil(10001 + arg0.getStartingAddress() + i);
			if (val) {
				coils.set(i);
			} else {
				coils.clear(i);
			}
			//this.ci.readEvents(10001 + arg0.getStartingAddress()+i, val);
		}
		ReadDiscreteInputsResponse rdir =  new ReadDiscreteInputsResponse(coils);
		return rdir;
	}

	@Override
	protected ReadHoldingRegistersResponse readHoldingRegistersRequest(ReadHoldingRegistersRequest arg0) {
		int[] registers = new int[arg0.getQuantityOfInputRegisters()];
		for (int i = 0; i < arg0.getQuantityOfInputRegisters(); i++) {
			registers[i] = this.getReg(40001 + arg0.getStartingAddress() + i);
			this.ci.readEvents(40001 + arg0.getStartingAddress()+i, registers[i]);
		}
		return new ReadHoldingRegistersResponse(registers);
	}

	@Override
	protected ReadInputRegistersResponse readInputRegistersRequest(ReadInputRegistersRequest arg0) {
		int[] registers = new int[arg0.getQuantityOfInputRegisters()];
		for (int i = 0; i < arg0.getQuantityOfInputRegisters(); i++) {
			registers[i] = this.getReg(30001 + arg0.getStartingAddress() + i);
			this.ci.readEvents(30001 + arg0.getStartingAddress()+i, registers[i]);
		}
		return new ReadInputRegistersResponse(registers);
	}

	@Override
	protected WriteMultipleCoilsResponse writeMultipleCoilsRequest(WriteMultipleCoilsRequest request) {
		BitSet coils = request.getOutputsValue();
		for (int i = 0; i < request.getQuantityOfOutputs(); i++) {
			boolean oldVal = this.getCoil(1 + request.getStartingAddress()+i);
			boolean newVal = coils.get(i);
			this.addCoil(1 + request.getStartingAddress()+i, newVal);
			if (oldVal != newVal)
				this.ci.writeEvents(1 + request.getStartingAddress()+i, newVal);
		}
		return new WriteMultipleCoilsResponse(request.getStartingAddress(), request.getQuantityOfOutputs());
	}

	@Override
	protected WriteMultipleRegistersResponse writeMultipleRegistersRequest(WriteMultipleRegistersRequest request) {
		int[] values = request.getRegisters();
		for (int i = 0; i < request.getQuantityOfRegisters(); i++) { 
			int oldVal = this.getReg(40001 + request.getStartingAddress() + i);
			int newVal = values[i];
			this.addReg(40001 + request.getStartingAddress() + i, newVal);
			if (oldVal != newVal)
				this.ci.writeEvents(40001 + request.getStartingAddress()+i, values[i]);
		}		
		return new WriteMultipleRegistersResponse(request.getStartingAddress(), request.getQuantityOfRegisters());
	}

	@Override
	protected WriteSingleCoil writeSingleCoil(WriteSingleCoil req) {
		boolean oldVal = this.getCoil(1 + req.getOutputAddress());
		boolean newVal = req.isState();
		this.addCoil(1 + req.getOutputAddress(), newVal);
		if (oldVal != newVal)
			this.ci.writeEvent(1 + req.getOutputAddress(), req.isState());
		return req;
	}

	@Override
	protected WriteSingleRegister writeSingleRegister(WriteSingleRegister req) {
		int oldVal = this.getReg(40001 + req.getRegisterAddress());
		int newVal = req.getRegisterValue();
		this.addReg(40001 + req.getRegisterAddress(), newVal);
		if (oldVal != newVal)
			this.ci.writeEvent(40001 + req.getRegisterAddress(), newVal);
		return req;
	}


}