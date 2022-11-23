package me.belaska.lltp.server.example.event;

import java.nio.ByteBuffer;

import me.belaska.lltp.core.LltpEventResponse;
import me.belaska.lltp.core.event.AbstractLltpEvent;
import me.belaska.lltp.server.example.event.dispatcher.ExampleLltpEventDispatcher;

public class PaymentAuthorizationLltpEvent extends AbstractLltpEvent<ExampleLltpEventDispatcher> {

	private volatile long cln;
	
	private volatile long amount;

	public long getCln() {
		return cln;
	}
	
	public void setCln(long cln) {
		this.cln = cln;
	}

	public long getAmount() {
		return amount;
	}

	public void setAmount(long amount) {
		this.amount = amount;
	}

	public PaymentAuthorizationLltpEvent() {
		super();
	}

	public PaymentAuthorizationLltpEvent(long correlationId, long cln, long amount) {
		super(0, correlationId);
		this.cln = cln;
		this.amount = amount;
	}

	@Override
	public void read(ByteBuffer byteBuffer) {
		super.read(byteBuffer);
		this.cln = byteBuffer.getLong();
		this.amount = byteBuffer.getLong();
	}

	@Override
	public void write(ByteBuffer byteBuffer) {
		super.write(byteBuffer);
		byteBuffer.putLong(cln);
		byteBuffer.putLong(amount);
	}

	@Override
	public LltpEventResponse<ExampleLltpEventDispatcher> process(ExampleLltpEventDispatcher dispatcher) {
		return dispatcher.paymentAuthorization(this);
	}
}
