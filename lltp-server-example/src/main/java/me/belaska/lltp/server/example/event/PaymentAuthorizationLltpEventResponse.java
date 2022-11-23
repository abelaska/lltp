package me.belaska.lltp.server.example.event;

import java.nio.ByteBuffer;

import me.belaska.lltp.core.LltpEventResponse;
import me.belaska.lltp.core.event.AbstractLltpEventResponse;
import me.belaska.lltp.server.example.event.dispatcher.ExampleLltpEventDispatcher;

public class PaymentAuthorizationLltpEventResponse extends AbstractLltpEventResponse<ExampleLltpEventDispatcher> {

	private volatile long cln;

	private volatile long amount;

	private volatile long balance;

	private volatile boolean authorized;

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

	public long getBalance() {
		return balance;
	}

	public void setBalance(long balance) {
		this.balance = balance;
	}

	public boolean isAuthorized() {
		return authorized;
	}

	public void setAuthorized(boolean authorized) {
		this.authorized = authorized;
	}

	public PaymentAuthorizationLltpEventResponse() {
		super();
	}

	public PaymentAuthorizationLltpEventResponse(PaymentAuthorizationLltpEvent event, boolean authorized, long balance) {
		this(event.getId(), event.getCorrelationId(), event.getCln(), event.getAmount(), authorized, balance);
	}

	public PaymentAuthorizationLltpEventResponse(long id, long correlationId, long cln, long amount, boolean authorized,
												 long balance) {
		super(id, correlationId);
		this.cln = cln;
		this.amount = amount;
		this.authorized = authorized;
		this.balance = balance;
	}

	@Override
	public void read(ByteBuffer byteBuffer) {
		super.read(byteBuffer);
		this.cln = byteBuffer.getLong();
		this.amount = byteBuffer.getLong();
		this.balance = byteBuffer.getLong();
		this.authorized = byteBuffer.get() == 1;
	}

	@Override
	public void write(ByteBuffer byteBuffer) {
		super.write(byteBuffer);
		byteBuffer.putLong(cln);
		byteBuffer.putLong(amount);
		byteBuffer.putLong(balance);
		byteBuffer.put((byte) (authorized ? 1 : 0));
	}

	@Override
	public LltpEventResponse<ExampleLltpEventDispatcher> process(ExampleLltpEventDispatcher dispatcher) {
		dispatcher.paymentAuthorizationResponse(this);
		return null;
	}
}
