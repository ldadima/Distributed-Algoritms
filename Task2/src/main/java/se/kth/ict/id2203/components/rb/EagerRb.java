package se.kth.ict.id2203.components.rb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.ict.id2203.pa.broadcast.BebMessage;
import se.kth.ict.id2203.pa.broadcast.Pp2pMessage;
import se.kth.ict.id2203.ports.beb.BebBroadcast;
import se.kth.ict.id2203.ports.beb.BebDeliver;
import se.kth.ict.id2203.ports.beb.BestEffortBroadcast;
import se.kth.ict.id2203.ports.pp2p.PerfectPointToPointLink;
import se.kth.ict.id2203.ports.pp2p.Pp2pDeliver;
import se.kth.ict.id2203.ports.pp2p.Pp2pSend;
import se.kth.ict.id2203.ports.rb.ReliableBroadcast;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Event;
import se.sics.kompics.Handler;
import se.sics.kompics.Init;
import se.sics.kompics.Port;
import se.sics.kompics.Start;
import se.sics.kompics.address.Address;

import java.util.HashSet;
import java.util.Set;

public class EagerRb extends ComponentDefinition {

	private static final Logger logger = LoggerFactory.getLogger(EagerRb.class);
    private final Port<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
    private final Port<ReliableBroadcast> rb = requires(ReliableBroadcast.class);
    private int seqnum = 0;

	public EagerRb(EagerRbInit init) {
        Set<Address> deliver = new HashSet<>();
        Address self = init.getSelfAddress();
        subscribe(new Handler<Start>() {
            @Override
            public void handle(Start event) {

            }
        }, rb);
        subscribe(new Handler<BebBroadcast>() {
            @Override
            public void handle(BebBroadcast event) {
                seqnum++;
                BebDeliver deliver = event.getDeliverEvent();
                String message = deliver instanceof BebMessage ? ((BebMessage) deliver).getMessage() : "";

                trigger(new BebBroadcast(new BebMessage(self, message)), beb);
            }
        }, rb);
        subscribe(new Handler<Pp2pDeliver>() {
            @Override
            public void handle(Pp2pDeliver event) {

            }
        }, beb);
	}

}
