package me.belaska.lltp.core.io;

import me.belaska.lltp.core.LltpEventBuffer;
import me.belaska.lltp.core.LltpEventDispatcher;
import me.belaska.lltp.core.translator.ReceiverTranslator;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.EventPublisher;

public class JGroupsEventBridge<D extends LltpEventDispatcher> implements Receiver {

	private static final Logger LOG = LoggerFactory.getLogger(JGroupsEventBridge.class);

	private EventPublisher<LltpEventBuffer<D>> receiverPublisher;

	private String configFile = "target/classes/jgroups-event.xml";

	private String clusterName = "lltp-example-event";

	private JChannel channel;

	public JGroupsEventBridge(EventPublisher<LltpEventBuffer<D>> receiverPublisher) throws Exception {
		this.receiverPublisher = receiverPublisher;

		channel = new JChannel(configFile);
		channel.setReceiver(this);
		channel.setDiscardOwnMessages(true);
		channel.connect(clusterName);

		LOG.info("Event bridge started");
	}

	@Override
	public void receive(Message msg) {

		if (LOG.isTraceEnabled()) {
			LOG.trace("Received event: {}", msg);
		}

		ReceiverTranslator<D> receiverTranslator = ReceiverTranslator.eventBuffer(msg.getArray());

		receiverPublisher.publishEvent(receiverTranslator);
	}
}
