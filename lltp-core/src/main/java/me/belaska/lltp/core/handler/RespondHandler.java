package me.belaska.lltp.core.handler;

import me.belaska.lltp.core.LltpEventBuffer;
import me.belaska.lltp.core.LltpEventDispatcher;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.EventHandler;

public class RespondHandler<D extends LltpEventDispatcher> implements EventHandler<LltpEventBuffer<D>> {

	private static final Logger LOG = LoggerFactory.getLogger(RespondHandler.class);

	private String configFile = "target/classes/jgroups-response.xml";

	private String clusterName = "lltp-example-response";

	private JChannel channel;

	public RespondHandler() throws Exception {
		channel = new JChannel(configFile);
		channel.setDiscardOwnMessages(true);
		channel.connect(clusterName);
	}

	@Override
	public void onEvent(final LltpEventBuffer<D> event, final long sequence, final boolean endOfBatch) throws Exception {

		if (LOG.isTraceEnabled()) {
			LOG.trace("Respond seq '{}' endOfBatch '{}' event '{}'", new Object[] { sequence, endOfBatch, event });
		}

		try {
			// odeslani odpovedi
			channel.send(new Message(null, null, event.getBuffer()));
		} finally {
			// uvolneni alokovanych dat z event
			event.clearBuffer();
			event.clearEvent();
		}
	}
}
