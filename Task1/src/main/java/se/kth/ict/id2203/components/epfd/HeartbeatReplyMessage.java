package se.kth.ict.id2203.components.epfd;

import se.kth.ict.id2203.ports.pp2p.Pp2pDeliver;
import se.sics.kompics.address.Address;

public class HeartbeatReplyMessage extends Pp2pDeliver {
    private static final long serialVersionUID = 2193713942080123562L;
    private final int sn;

    public HeartbeatReplyMessage(Address source, int seqnum) {
        super(source);
        sn = seqnum;
    }

    public int getSn() {
        return sn;
    }
}
