package me.belaska.lltp.server.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SingleThreadedClaimStrategy;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;

import me.belaska.lltp.core.LltpEventBuffer;
import me.belaska.lltp.core.LltpEventBufferFactory;
import me.belaska.lltp.core.LltpEventTransformer;
import me.belaska.lltp.core.ReceiverExceptionHandler;
import me.belaska.lltp.core.ResponderExceptionHandler;
import me.belaska.lltp.core.event.transformer.BinaryEventTransformer;
import me.belaska.lltp.core.handler.JournalHandler;
import me.belaska.lltp.core.handler.MarshalHandler;
import me.belaska.lltp.core.handler.ProcessEventHandler;
import me.belaska.lltp.core.handler.ReplicateHandler;
import me.belaska.lltp.core.handler.RespondHandler;
import me.belaska.lltp.core.handler.UnmarshalHandler;
import me.belaska.lltp.server.example.event.ExampleLltpEventRegistryLltp;
import me.belaska.lltp.server.example.event.dispatcher.ExampleLltpEventDispatcher;
import me.belaska.lltp.server.example.event.dispatcher.ExampleLltpEventDispatcherImpl;

public class Main {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	private final static int RECEIVER_RING_BUFFER_SIZE = 2 * 1024 * 1024;
	private final static int RESPONDER_RING_BUFFER_SIZE = 1024 * 1024;

	private final static int PROCESSORS = Runtime.getRuntime().availableProcessors();

	private final static ExecutorService EXECUTOR = Executors.newFixedThreadPool(PROCESSORS);

	private static LltpEventTransformer<byte[], ExampleLltpEventDispatcher> eventTransformer;
	private static EventFactory<LltpEventBuffer<ExampleLltpEventDispatcher>> lltpEventBufferFactory;

	private static ReplicateHandler<ExampleLltpEventDispatcher> replicateHandler;
	private static UnmarshalHandler<ExampleLltpEventDispatcher> decodeHandler;
	private static JournalHandler<ExampleLltpEventDispatcher> journalHandler;
	private static ProcessEventHandler<ExampleLltpEventDispatcher> eventHandler;
	private static MarshalHandler<ExampleLltpEventDispatcher> encodeHandler;
	private static RespondHandler<ExampleLltpEventDispatcher> respondHandler;
	private static ResponderExceptionHandler responderExceptionHandler;
	private static ReceiverExceptionHandler<ExampleLltpEventDispatcher> receiverExceptionHandler;

	private static RingBuffer<LltpEventBuffer<ExampleLltpEventDispatcher>> responderRingBuffer;
	private static RingBuffer<LltpEventBuffer<ExampleLltpEventDispatcher>> receiverRingBuffer;

	private static ExampleLltpEventDispatcher eventDispatcher;

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {

		eventTransformer = new BinaryEventTransformer<>(new ExampleLltpEventRegistryLltp());

		journalHandler = new JournalHandler<>();

		responderExceptionHandler = new ResponderExceptionHandler();

		encodeHandler = new MarshalHandler<>(eventTransformer);

		respondHandler = new RespondHandler<>();

		lltpEventBufferFactory = new LltpEventBufferFactory<>();

		Disruptor<LltpEventBuffer<ExampleLltpEventDispatcher>> responderDisruptor = new Disruptor<>(
				lltpEventBufferFactory, EXECUTOR, new SingleThreadedClaimStrategy(RESPONDER_RING_BUFFER_SIZE),
				new SleepingWaitStrategy());
		responderDisruptor.handleExceptionsWith(responderExceptionHandler);
		responderDisruptor.handleEventsWith(encodeHandler).then(respondHandler);
		responderRingBuffer = responderDisruptor.start();

		//

		replicateHandler = new ReplicateHandler<>();

		receiverExceptionHandler = new ReceiverExceptionHandler<>(responderRingBuffer);

		decodeHandler = new UnmarshalHandler<>(eventTransformer);

		eventDispatcher = new ExampleLltpEventDispatcherImpl();
		eventHandler = new ProcessEventHandler<>(eventDispatcher, responderRingBuffer);

		Disruptor<LltpEventBuffer<ExampleLltpEventDispatcher>> receiverDisruptor = new Disruptor<>(
				lltpEventBufferFactory, EXECUTOR, new SingleThreadedClaimStrategy(RECEIVER_RING_BUFFER_SIZE),
				new SleepingWaitStrategy());
		receiverDisruptor.handleExceptionsWith(receiverExceptionHandler);
		receiverDisruptor.handleEventsWith(journalHandler, replicateHandler, decodeHandler).then(eventHandler);
		receiverRingBuffer = receiverDisruptor.start();

		// join clusteru a spusteni prijmu zprav v momente kdy se node stane MASTERem
		replicateHandler.start(receiverRingBuffer);

		LOG.info("Server running...");

		// TODO nacist snapshot a pak nacist ze journal souboru nejposlednejsi udalosti zpracovane od momentu vytvoreni
		// snapshotu

		// TODO pravidelne odeslat do receiverPublisher SnapshotEvent pro vytvoreni snapshot souboru in-memory databaze

		// TODO 2. Build a network protocol that supports replay. For example, at LMAX each event sent over the network
		// includes the ring buffer sequence number - if the receiver gets message 20 but hasn't yet received message
		// 19, it will NAK back to the sender requesting that 19 be sent. Using this system, when the hardware failure
		// is resolved the service starts back up and automatically requests a replay of any messages it missed.

		// READ http://mentaqueue.soliveirajr.com/Page.mtw
	}
}
