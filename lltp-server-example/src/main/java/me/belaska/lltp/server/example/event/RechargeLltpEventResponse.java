package me.belaska.lltp.server.example.event;

import java.nio.ByteBuffer;

import me.belaska.lltp.core.LltpEventResponse;
import me.belaska.lltp.core.event.AbstractLltpEventResponse;
import me.belaska.lltp.server.example.event.dispatcher.ExampleLltpEventDispatcher;

public class RechargeLltpEventResponse extends AbstractLltpEventResponse<ExampleLltpEventDispatcher> {

	private volatile long cln;

	private volatile long balance;

	public long getCln() {
		return cln;
	}

	public void setCln(long cln) {
		this.cln = cln;
	}

	public long getBalance() {
		return balance;
	}

	public void setBalance(long balance) {
		this.balance = balance;
	}

	public RechargeLltpEventResponse() {
		super();
	}

	public RechargeLltpEventResponse(RechargeLltpEvent event, long balance) {
		this(event.getId(), event.getCorrelationId(), event.getCln(), balance);
	}

	public RechargeLltpEventResponse(long id, long correlationId, long cln, long balance) {
		super(id, correlationId);
		this.cln = cln;
		this.balance = balance;
	}

	@Override
	public LltpEventResponse<ExampleLltpEventDispatcher> process(ExampleLltpEventDispatcher dispatcher) {
		dispatcher.rechargeResponse(this);
		return null;
	}

	@Override
	public void read(ByteBuffer byteBuffer) {
		super.read(byteBuffer);
		this.cln = byteBuffer.getLong();
		this.balance = byteBuffer.getLong();
	}

	@Override
	public void write(ByteBuffer byteBuffer) {
		super.write(byteBuffer);
		byteBuffer.putLong(cln);
		byteBuffer.putLong(balance);
	}
}
