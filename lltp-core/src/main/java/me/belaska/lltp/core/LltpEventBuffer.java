package me.belaska.lltp.core;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class LltpEventBuffer<D extends LltpEventDispatcher> {

	private volatile boolean replica = false;

	private volatile long id;

	private volatile byte[] buffer;

	private volatile LltpEvent<D> event;

	public void setReplica(boolean replica) {
		this.replica = replica;
	}

	/**
	 * @return true pokud se jedna o replikovanou udalost, false pokud se jedna o udalost prijatou na MASTER nodu
	 */
	public boolean isReplica() {
		return replica;
	}

	/**
	 * @return false pokud se jedna o replikovanou udalost, true pokud se jedna o udalost prijatou na MASTER nodu
	 */
	public boolean isNotReplica() {
		return !isReplica();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public byte[] getBuffer() {
		return buffer;
	}

	public void setBuffer(byte[] buffer) {
		this.buffer = buffer;
	}

	public void clearBuffer() {
		this.buffer = null;
	}

	public LltpEvent<D> getEvent() {
		return event;
	}

	public void setEvent(LltpEvent<D> event) {
		this.event = event;
	}

	public void clearEvent() {
		this.event = null;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}
