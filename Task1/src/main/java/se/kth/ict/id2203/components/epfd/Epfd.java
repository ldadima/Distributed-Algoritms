package se.kth.ict.id2203.components.epfd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.ict.id2203.ports.epfd.EventuallyPerfectFailureDetector;
import se.kth.ict.id2203.ports.epfd.Restore;
import se.kth.ict.id2203.ports.epfd.Suspect;
import se.kth.ict.id2203.ports.pp2p.PerfectPointToPointLink;
import se.kth.ict.id2203.ports.pp2p.Pp2pSend;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.address.Address;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;

import java.util.HashSet;
import java.util.Set;


public class Epfd extends ComponentDefinition {
    private static final Logger logger = LoggerFactory.getLogger(Epfd.class);
    private final Positive<Timer> timer = requires(Timer.class);
    private final Negative<EventuallyPerfectFailureDetector> epfd = provides(EventuallyPerfectFailureDetector.class);
    private final Positive<PerfectPointToPointLink> pp2p = requires(PerfectPointToPointLink.class);
    private int seqnum = 0;
    private final Set<Address> alive = new HashSet<>();
    private final Set<Address> suspected = new HashSet<>();
    private final Address self;
    private long delay;


    public Epfd(EpfdInit init) {
        alive.addAll(init.getAllAddresses());
        delay = init.getInitialDelay();
        self = init.getSelfAddress();
        Set<Address> all = init.getAllAddresses();
        all.remove(self);

        subscribe(new Handler<Start>() {
            @Override
            public void handle(Start event) {
                logger.info("Start event handling on control port");
                ScheduleTimeout st = new ScheduleTimeout(delay);
                st.setTimeoutEvent(new CheckTimeout(st));
                trigger(st, timer);
                logger.info("Trigger timer with delay - {} to timer port", delay);
            }
        }, control);
        subscribe(new Handler<CheckTimeout>() {
            @Override
            public void handle(CheckTimeout event) {
                logger.info("CheckTimeout event handling on timer port");
                Set<Address> intersect = new HashSet<>(alive);
                intersect.retainAll(suspected);
                if (!intersect.isEmpty()) {
                    delay += init.getDeltaDelay();
                    logger.info("Delay incremented on {}", init.getDeltaDelay());
                }
                seqnum++;
                for (Address p : all) {
                    if (!alive.contains(p) && !suspected.contains(p)) {
                        suspected.add(p);
                        trigger(new Suspect(p), epfd);
                    } else if (alive.contains(p) && suspected.contains(p)) {
                        suspected.remove(p);
                        trigger(new Restore(p), epfd);
                    }
                    trigger(new Pp2pSend(p, new HeartbeatRequestMessage(self, seqnum)), pp2p);
                }
                alive.clear();
                ScheduleTimeout st = new ScheduleTimeout(delay);
                st.setTimeoutEvent(new CheckTimeout(st));
                trigger(st, timer);
                logger.info("Trigger timer with delay - {} to timer port", delay);
            }
        }, timer);
        subscribe(new Handler<HeartbeatRequestMessage>() {
            @Override
            public void handle(HeartbeatRequestMessage event) {
                logger.info("Handling heartbeat request from {}", event.getSource());
                trigger(new Pp2pSend(event.getSource(), new HeartbeatReplyMessage(self, event.getSn())), pp2p);
            }
        }, pp2p);
        subscribe(new Handler<HeartbeatReplyMessage>() {
            @Override
            public void handle(HeartbeatReplyMessage event) {
                logger.info("Handling heartbeat reply from {}", event.getSource());
                if (event.getSn() == seqnum || suspected.contains(event.getSource())) {
                    alive.add(event.getSource());
                }
            }
        }, pp2p);
    }
}