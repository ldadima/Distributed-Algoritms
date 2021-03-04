package se.kth.ict.id2203.components.rb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.ict.id2203.pa.broadcast.BebMessage;
import se.kth.ict.id2203.pa.broadcast.Pp2pMessage;
import se.kth.ict.id2203.ports.beb.BebBroadcast;
import se.kth.ict.id2203.ports.beb.BebDeliver;
import se.kth.ict.id2203.ports.beb.BestEffortBroadcast;
import se.kth.ict.id2203.ports.pp2p.Pp2pDeliver;
import se.kth.ict.id2203.ports.rb.ReliableBroadcast;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Port;
import se.sics.kompics.Start;
import se.sics.kompics.address.Address;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class EagerRb extends ComponentDefinition {

	private static final Logger logger = LoggerFactory.getLogger(EagerRb.class);
    private final Port<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
    private final Port<ReliableBroadcast> rb = requires(ReliableBroadcast.class);
    private int seqnum = 0;

	public EagerRb(EagerRbInit init) {
        Set<Deliver> delivered = new HashSet<>();
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

                trigger(new BebBroadcast(new BebMessage(self, message, seqnum)), beb);
            }
        }, rb);
        subscribe(new Handler<Pp2pDeliver>() {
            @Override
            public void handle(Pp2pDeliver event) {
                if(!(event instanceof Pp2pMessage) ){
                    throw new RuntimeException("Non correct work, wrong Pp2pEvent");
                }
                Pp2pMessage e = (Pp2pMessage)event;
                Deliver d = new Deliver(e.getSn(), e.getSource());
                if(delivered.contains(d)){
                    delivered.add(d);
                    trigger(new Pp2pMessage(e.getSource(), e.getMessage()), rb);
                    trigger(new BebBroadcast(new BebMessage(e.getSource(), e.getMessage(), e.getSn())), beb);
                }
            }
        }, beb);
	}

	private static class Deliver{
	    private final int sn;
	    private final Address address;

        public Deliver(int sn, Address address) {
            this.sn = sn;
            this.address = address;
        }

        public int getSn() {
            return sn;
        }

        public Address getAddress() {
            return address;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Deliver)) return false;
            Deliver deliver = (Deliver) o;
            return sn == deliver.sn && Objects.equals(address, deliver.address);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sn, address);
        }
    }
}
