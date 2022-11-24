package me.belaska.lltp.server.example;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jgroups.BytesMessage;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.belaska.lltp.core.LltpEvent;
import me.belaska.lltp.core.LltpEventDispatcher;
import me.belaska.lltp.core.event.SnapshotLltpEvent;
import me.belaska.lltp.core.event.transformer.BinaryEventTransformer;
import me.belaska.lltp.server.example.event.ExampleLltpEventRegistryLltp;
import me.belaska.lltp.server.example.event.PaymentAuthorizationLltpEvent;
import me.belaska.lltp.server.example.event.RechargeLltpEvent;
import me.belaska.lltp.server.example.event.dispatcher.ExampleLltpEventDispatcher;

public class MainClient {

	private static final Logger LOG = LoggerFactory.getLogger(MainClient.class);

	private final static BinaryEventTransformer<ExampleLltpEventDispatcher> eventTransformer = new BinaryEventTransformer<ExampleLltpEventDispatcher>(
			new ExampleLltpEventRegistryLltp());

	private final static String eventConfigFile = "target/classes/jgroups-event.xml";

	private final static String eventClusterName = "lltp-example-event";

	private final static String responseConfigFile = "target/classes/jgroups-response.xml";

	private final static String responseClusterName = "lltp-example-response";

	private static JChannel rspChannel;

	private static JChannel evtChannel;

	@SuppressWarnings("unchecked")
	public static void send(LltpEvent<? extends LltpEventDispatcher> event) throws Exception {
		long t0 = System.currentTimeMillis();
		evtChannel
				.send(new BytesMessage(null, eventTransformer.marshal((LltpEvent<ExampleLltpEventDispatcher>) event)));
		long t1 = System.currentTimeMillis();
		LOG.info("Sent in {} msec(s), waiting...", (t1 - t0));
	}

	public static void main(String[] args) throws Exception {

		final CountDownLatch latch = new CountDownLatch(3);

		MainClient.evtChannel = new JChannel(eventConfigFile);
		evtChannel.setDiscardOwnMessages(true);
		evtChannel.connect(eventClusterName);

		MainClient.rspChannel = new JChannel(responseConfigFile);
		rspChannel.setReceiver(new Receiver() {
			@Override
			public void receive(Message msg) {

				LltpEvent<?> ev = eventTransformer.unmarshal(msg.getArray());

				if (LOG.isTraceEnabled()) {
					LOG.trace("Received length:{} event:'{}'", msg.getLength(), ev);
				}

				latch.countDown();
			}
		});
		rspChannel.setDiscardOwnMessages(true);
		rspChannel.connect(responseClusterName);

		Thread.sleep(1000);

		long cln = 123456;
		long correlationId = 0;

		send(new RechargeLltpEvent(correlationId++, cln, 100));
		send(new PaymentAuthorizationLltpEvent(correlationId++, cln, 10));
		send(new PaymentAuthorizationLltpEvent(correlationId++, cln, 100));
		send(new SnapshotLltpEvent());

		LOG.info("Waiting...");
		latch.await(30, TimeUnit.SECONDS);

		LOG.info("Done");

		System.exit(0);
	}
}
