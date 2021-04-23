package se.kth.ict.id2203.components.riwcm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.ict.id2203.ports.ar.ArReadRequest;
import se.kth.ict.id2203.ports.ar.ArReadResponse;
import se.kth.ict.id2203.ports.ar.ArWriteRequest;
import se.kth.ict.id2203.ports.ar.ArWriteResponse;
import se.kth.ict.id2203.ports.ar.AtomicRegister;
import se.kth.ict.id2203.ports.beb.BebBroadcast;
import se.kth.ict.id2203.ports.beb.BestEffortBroadcast;
import se.kth.ict.id2203.ports.pp2p.PerfectPointToPointLink;
import se.kth.ict.id2203.ports.pp2p.Pp2pSend;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.address.Address;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ReadImposeWriteConsultMajority extends ComponentDefinition {

	private static final Logger logger = LoggerFactory
			.getLogger(ReadImposeWriteConsultMajority.class);

	private final Positive<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
	private final Positive<PerfectPointToPointLink> pp2p = requires(PerfectPointToPointLink.class);
	private final Negative<AtomicRegister> ar = provides(AtomicRegister.class);

	private final Address self;
    private final Integer numberOfNodes;
	private final List<ReadObject> readList;
	private Integer rid;
	private Integer acks;
	private Integer wr, val, writeVal, readVal;
	private Integer ts;
	private Boolean reading;
    private Integer rr;
	private Integer maxts;

	public ReadImposeWriteConsultMajority(
			ReadImposeWriteConsultMajorityInit event) {
		this.self = event.getSelfAddress();
        Set<Address> nodes = new HashSet<>(event.getAllAddresses());
		this.readList = new LinkedList<>();
		this.numberOfNodes = nodes.size();
		this.rid = 0;
		this.acks = 0;
		this.reading = false;
		this.ts = 0;
		this.wr = 0;
		this.val = 0;
		this.writeVal = null;
		this.readVal = null;
		this.rr = 0;
		this.maxts = -1;
		
		subscribe(new Handler<Start>() {

            @Override
            public void handle(Start event) {
                logger.info("Atomic Register component started");
            }
        }, control);
		
		subscribe(new Handler<ArReadRequest>() {

            @Override
            public void handle(ArReadRequest event) {
                rid++;
                acks = 0;
                readList.clear();
                reading = true;
                ReadBebDataMessage bebMessage = new ReadBebDataMessage(self, rid);
                trigger(new BebBroadcast(bebMessage), beb);
            }
        }, ar);

		subscribe(new Handler<ArWriteRequest>() {

            @Override
            public void handle(ArWriteRequest event) {
                rid++;
                writeVal = event.getValue();
                acks = 0;
                readList.clear();
                ReadBebDataMessage bebMessage = new ReadBebDataMessage(self, rid);
                trigger(new BebBroadcast(bebMessage), beb);
            }
        }, ar);
		
		subscribe(new Handler<ReadBebDataMessage>() {

            @Override
            public void handle(ReadBebDataMessage event) {
                DataMessage arMessage = new DataMessage(self, event.getR(),
                        ts, wr, val);
                trigger(new Pp2pSend(event.getSource(), arMessage), pp2p);
            }
        }, beb);

		subscribe(new Handler<WriteBebDataMessage>() {

            @Override
            public void handle(WriteBebDataMessage event) {
                if (event.getTs().equals(ts)) {
                    if (event.getWr() > wr) {
                        ts = event.getTs();
                        wr = event.getWr();
                        val = event.getVal();
                    }
                } else if (event.getTs() > ts) {
                    ts = event.getTs();
                    wr = event.getWr();
                    val = event.getVal();
                }
                trigger(new Pp2pSend(event.getSource(), new AckMsg(self,
                        event.getR())), pp2p);
            }
        }, beb);
		
		subscribe(new Handler<DataMessage>() {

            @Override
            public void handle(DataMessage event) {
                if (event.getR().equals(rid)) {
                    readList.add(new ReadObject(event.getTs(), event.getWr(), event.getVal(), event.getSource().getId()));

                    if (readList.size() > (numberOfNodes / 2)) {
                        readList.sort((obj0, obj1) -> {
                            if (obj0.getTs().equals(obj1.getTs())) {
                                return obj0.getNodeId().compareTo(obj1.getNodeId());
                            } else if (obj0.getTs() < obj1.getTs()) {
                                return -1;
                            } else {
                                return 1;
                            }
                        });

                        ReadObject highest = readList.get(readList.size() - 1);

                        rr = highest.getWr();
                        readVal = highest.getVal();
                        maxts = highest.getTs();

                        readList.clear();

                        WriteBebDataMessage wBebMsg;
                        if (reading) {
                            wBebMsg = new WriteBebDataMessage(
                                    self, rid, maxts, rr, readVal);
                        } else {
                            wBebMsg = new WriteBebDataMessage(
                                    self, rid, maxts + 1, self.getId(),
                                    writeVal);
                        }
                        trigger(new BebBroadcast(wBebMsg), beb);
                    }
                }
            }
        }, pp2p);
		subscribe(new Handler<AckMsg>() {

            @Override
            public void handle(AckMsg event) {
                if (event.getR().equals(rid)) {
                    acks++;
                    if (acks > (numberOfNodes / 2)) {
                        acks = 0;

                        if (reading) {
                            reading = false;
                            ArReadResponse resp = new ArReadResponse(readVal);
                            trigger(resp, ar);
                        } else {
                            trigger(new ArWriteResponse(), ar);
                        }
                    }
                }
            }
        }, pp2p);
	}
}
