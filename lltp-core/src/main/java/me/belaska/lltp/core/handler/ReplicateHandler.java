package me.belaska.lltp.core.handler;

import java.nio.ByteBuffer;

import me.belaska.lltp.core.LltpEventBuffer;
import me.belaska.lltp.core.LltpEventDispatcher;
import me.belaska.lltp.core.translator.ReceiverTranslator;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventPublisher;
import com.lmax.disruptor.RingBuffer;

import me.belaska.lltp.core.cluster.AbstractClusterPeer;
import me.belaska.lltp.core.io.JGroupsEventBridge;

/**
 * Single-thread use only.
 */
public class ReplicateHandler<D extends LltpEventDispatcher> extends AbstractClusterPeer implements
		EventHandler<LltpEventBuffer<D>> {

	private static final Logger LOG = LoggerFactory.getLogger(ReplicateHandler.class);

	private final static int MAX_MESSAGE_SIZE = 65536;

	private ByteBuffer buffer = ByteBuffer.allocate(MAX_MESSAGE_SIZE);

	private EventPublisher<LltpEventBuffer<D>> receiverPublisher;

	public void start(RingBuffer<LltpEventBuffer<D>> receiverRingBuffer) throws Exception {
		this.receiverPublisher = new EventPublisher<LltpEventBuffer<D>>(receiverRingBuffer);

		this.joinCluster();
	}

	@Override
	public void onBecameMaster(JChannel channel) {
		// spustit prijem udalosti od GATEWAY
		try {
			new JGroupsEventBridge<D>(receiverPublisher);
		} catch (Exception e) {
			LOG.error("Spusteni sluzby pro prijem udalosti selhalo", e);
		}
	}

	/**
	 * Ve volano vzdy pouze v jednom threadu.
	 */
	@Override
	public void onEvent(final LltpEventBuffer<D> event, final long sequence, final boolean endOfBatch) throws Exception {
		if (isMaster()) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("Replicate seq '{}' endOfBatch '{}' event '{}'", new Object[] { sequence, endOfBatch, event });
			}

			// odeslat replikovanou udalost na ostatni nody

			buffer.clear();
			buffer.putLong(event.getId());
			buffer.put(event.getBuffer());
			buffer.flip();

			channel.send(new Message(null, null, buffer.array(), 0, buffer.limit()));
		}
	}

	@Override
	public void receive(Message msg) {

		if (LOG.isTraceEnabled()) {
			LOG.trace("Received replica: {}", msg);
		}

		ByteBuffer b = ByteBuffer.wrap(msg.getBuffer());
		long messageId = b.getLong();

		byte[] messageBuf = new byte[b.remaining()];
		b.get(messageBuf);

		ReceiverTranslator<D> receiverTranslator = ReceiverTranslator.replicaBuffer(messageBuf, messageId);

		receiverPublisher.publishEvent(receiverTranslator);
	}
}
