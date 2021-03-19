package se.kth.ict.id2203.components.rb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.ict.id2203.pa.broadcast.BebMessage;
import se.kth.ict.id2203.pa.broadcast.Pp2pMessage;
import se.kth.ict.id2203.pa.broadcast.RbMessage;
import se.kth.ict.id2203.ports.beb.BebBroadcast;
import se.kth.ict.id2203.ports.beb.BebDeliver;
import se.kth.ict.id2203.ports.beb.BestEffortBroadcast;
import se.kth.ict.id2203.ports.pp2p.Pp2pDeliver;
import se.kth.ict.id2203.ports.rb.RbBroadcast;
import se.kth.ict.id2203.ports.rb.RbDeliver;
import se.kth.ict.id2203.ports.rb.ReliableBroadcast;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Port;
import se.sics.kompics.address.Address;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class EagerRb extends ComponentDefinition {

	private static final Logger logger = LoggerFactory.getLogger(EagerRb.class);
    private final Port<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
    private final Port<ReliableBroadcast> rb = provides(ReliableBroadcast.class);
    private int seqnum = 0;

	public EagerRb(EagerRbInit init) {
        Set<Deliver> delivered = new HashSet<>();
        Address self = init.getSelfAddress();
        logger.info("SELF - {}", self);
        subscribe(new Handler<RbBroadcast>() {
            @Override
            public void handle(RbBroadcast event) {
                seqnum++;
                RbDeliver deliver = event.getDeliverEvent();
                String message = deliver instanceof RbMessage ? ((RbMessage) deliver).getMessage() : "";

                logger.info("SN RB send - {}", seqnum);

                Map<Address, Integer> map = deliver instanceof RbMessage ? ((RbMessage) deliver).getW() : new HashMap<>();
                map = map == null ? new HashMap<>(): map;
                logger.info("SEND RB W - {}", map);

                trigger(new BebBroadcast(new BebMessage(self, message, seqnum, map)), beb);
            }
        }, rb);
        subscribe(new Handler<BebDeliver>() {
            @Override
            public void handle(BebDeliver event) {
                if(!(event instanceof BebMessage) ){
                    throw new RuntimeException("Non correct work, wrong BebEvent");
                }
                BebMessage e = (BebMessage)event;
                logger.info("SRC - {}", e.getSource());
                Deliver d = new Deliver(e.getSn(), e.getSource());
                if(!delivered.contains(d)){
                    delivered.add(d);
                    trigger(new RbMessage(e.getSource(), e.getMessage(), e.getSn(), e.getW()), rb);
                    trigger(new BebBroadcast(new BebMessage(e.getSource(), e.getMessage(), e.getSn(), e.getW())), beb);
                }
            }
        }, beb);
	}

	private static class Deliver{
	    private final Integer sn;
	    private final Address address;

        public Deliver(Integer sn, Address address) {
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
            return Objects.equals(sn, deliver.sn) && Objects.equals(address, deliver.address);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sn, address);
        }
    }
}
