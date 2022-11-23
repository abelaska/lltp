package me.belaska.lltp.server.example;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import me.belaska.lltp.server.example.event.ExampleLltpEventRegistryLltp;
import me.belaska.lltp.server.example.event.PaymentAuthorizationLltpEvent;
import me.belaska.lltp.server.example.event.dispatcher.ExampleLltpEventDispatcher;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.belaska.lltp.core.LltpEvent;
import me.belaska.lltp.core.event.transformer.BinaryEventTransformer;

public class PerfClient implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(PerfClient.class);

	private static final int SEND_EVENTS = 1000000;

	private static final int RUN_CLIENTS = 1;

	private final BinaryEventTransformer<ExampleLltpEventDispatcher> eventTransformer = new BinaryEventTransformer<ExampleLltpEventDispatcher>(
			new ExampleLltpEventRegistryLltp());

	private final static String eventConfigFile = "target/classes/jgroups-event.xml";

	private final static String eventClusterName = "lltp-example-event";

	private final static String responseConfigFile = "target/classes/jgroups-response.xml";

	private final static String responseClusterName = "lltp-example-response";

	private JChannel rspChannel;

	private JChannel evtChannel;

	private long lastRcv;

	private long tm;

	private int id;

	private CountDownLatch latch;

	public PerfClient(final int id) throws Exception {
		this.id = id;

		latch = new CountDownLatch(RUN_CLIENTS * SEND_EVENTS);

		evtChannel = new JChannel(eventConfigFile);
		evtChannel.setDiscardOwnMessages(true);
		evtChannel.connect(eventClusterName);

		rspChannel = new JChannel(responseConfigFile);
		rspChannel.setReceiver(new ReceiverAdapter() {
			@Override
			public void receive(Message msg) {

				LltpEvent<?> ev = eventTransformer.unmarshal(msg.getBuffer());

				lastRcv = System.currentTimeMillis();

				if (LOG.isTraceEnabled()) {
					LOG.trace("[{}] Received length:{} event:'{}'", new Object[] { id, msg.getBuffer().length, ev });
				}

				if ((latch.getCount() % 10000) == 0) {
					LOG.info("[{}] Received {}...", id, (RUN_CLIENTS * SEND_EVENTS - latch.getCount()));
				}

				latch.countDown();
			}
		});
		rspChannel.setDiscardOwnMessages(true);
		rspChannel.connect(responseClusterName);
	}

	public void await() throws InterruptedException {
		latch.await();
	}

	@Override
	public void run() {
		try {
			Thread.sleep(1000);

			tm = System.currentTimeMillis();
			lastRcv = tm;

			long t0 = System.currentTimeMillis();
			for (int i = 0; i < SEND_EVENTS; i++) {
				if (i > 0 && (i % 10000) == 0) {
					long t1 = System.currentTimeMillis();
					LOG.info("[{}] Sending {}...{} ms", new Object[] { id, i, (t1 - t0) });
					t0 = t1;
				}
				evtChannel.send(new Message(null, null, eventTransformer.marshal(new PaymentAuthorizationLltpEvent(i,
						123456L, i))));
			}

			LOG.info("[{}] All sent, waiting...", id);

			latch.await(30, TimeUnit.SECONDS);

			rspChannel.disconnect();

			LOG.info("[{}] Done", id);
		} catch (Exception e) {
			LOG.error("[" + id + "]", e);
		}
	}

	public void print() {
		LOG.info("[{}] Canceling, {} req/sec, {} in {} msec(s), missed {}",
				new Object[] { id, (lastRcv == tm ? 0 : (1000 * (SEND_EVENTS - latch.getCount())) / (lastRcv - tm)),
						(SEND_EVENTS - latch.getCount()), (lastRcv - tm), latch.getCount() });
	}

	public static void main(String[] args) throws Exception {

		ExecutorService executor = Executors.newFixedThreadPool(RUN_CLIENTS);

		PerfClient[] clients = new PerfClient[RUN_CLIENTS];

		for (int i = 0; i < RUN_CLIENTS; i++) {
			clients[i] = new PerfClient(i);
		}

		Thread.sleep(1000);

		for (int i = 0; i < RUN_CLIENTS; i++) {
			executor.submit(clients[i]);
		}

		for (int i = 0; i < RUN_CLIENTS; i++) {
			clients[i].await();
		}

		for (int i = 0; i < RUN_CLIENTS; i++) {
			clients[i].print();
		}

		System.exit(0);
	}
}
