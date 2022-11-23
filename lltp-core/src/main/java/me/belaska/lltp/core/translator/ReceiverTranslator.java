package me.belaska.lltp.core.translator;

import com.lmax.disruptor.EventTranslator;

import me.belaska.lltp.core.LltpEventBuffer;
import me.belaska.lltp.core.LltpEventDispatcher;

public class ReceiverTranslator<D extends LltpEventDispatcher> implements EventTranslator<LltpEventBuffer<D>> {

	public static long INITIAL_SEQUENCE_VALUE = 0; 
	
	private volatile boolean replica = false;
	
	private volatile long replicatedEventId = 0;

	private volatile byte[] buffer;
	
	public static <D extends LltpEventDispatcher> ReceiverTranslator<D> replicaBuffer(byte[] buffer, long replicatedEventId) {
		ReceiverTranslator<D> t = new ReceiverTranslator<D>();
		t.setReplicaBuffer(buffer, replicatedEventId);
		return t;
	}

	public static <D extends LltpEventDispatcher> ReceiverTranslator<D> eventBuffer(byte[] buffer) {
		ReceiverTranslator<D> t = new ReceiverTranslator<D>();
		t.setEventBuffer(buffer);
		return t;
	}

	public void setReplicaBuffer(byte[] buffer, long replicatedEventId) {
		this.buffer = buffer;
		this.replicatedEventId = replicatedEventId;
		this.replica = true;
	}

	public void setEventBuffer(byte[] buffer) {
		this.buffer = buffer;
		this.replica = false;
	}

	@Override
	public LltpEventBuffer<D> translateTo(LltpEventBuffer<D> event, long sequence) {
		
		if (replica) {
			event.setId(replicatedEventId);
		} else {
			event.setId(INITIAL_SEQUENCE_VALUE + sequence);
		}
		
		event.setBuffer(buffer);
		event.setReplica(replica);
		event.clearEvent();
		return event;
	}
}
