package me.belaska.lltp.core.event;

import java.nio.ByteBuffer;

import me.belaska.lltp.core.LltpEvent;
import me.belaska.lltp.core.LltpEventDispatcher;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public abstract class AbstractLltpEvent<D extends LltpEventDispatcher> implements LltpEvent<D> {

	private volatile long id;

	private volatile long correlationId;

	public AbstractLltpEvent() {
	}

	public AbstractLltpEvent(long id) {
		this();
		this.id = id;
	}

	public AbstractLltpEvent(long id, long correlationId) {
		this(id);
		this.correlationId = correlationId;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(long correlationId) {
		this.correlationId = correlationId;
	}

	@Override
	public void read(ByteBuffer byteBuffer) {
		setId(byteBuffer.getLong());
		setCorrelationId(byteBuffer.getLong());
	}

	@Override
	public void write(ByteBuffer byteBuffer) {
		byteBuffer.putLong(getId());
		byteBuffer.putLong(getCorrelationId());
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}
