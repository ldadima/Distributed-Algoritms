/**
 * This file is part of the ID2203 course assignments kit.
 * <p>
 * Copyright (C) 2009-2013 KTH Royal Institute of Technology
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.kth.ict.id2203.components.crb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.ict.id2203.ports.crb.CausalOrderReliableBroadcast;
import se.kth.ict.id2203.ports.pp2p.Pp2pDeliver;
import se.kth.ict.id2203.ports.rb.ReliableBroadcast;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Event;
import se.sics.kompics.Handler;
import se.sics.kompics.Port;
import se.sics.kompics.Start;
import se.sics.kompics.address.Address;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class WaitingCrb extends ComponentDefinition {

    private static final Logger logger = LoggerFactory.getLogger(WaitingCrb.class);
    private final Port<CausalOrderReliableBroadcast> crb = requires(CausalOrderReliableBroadcast.class);
    private final Port<ReliableBroadcast> rb = requires(ReliableBroadcast.class);
    private int lsn = 0;

    public WaitingCrb(WaitingCrbInit init) {
        Map<Address, Integer> ranks = new HashMap<>();
        Set<Address> all = init.getAllAddresses();
        all.forEach(a -> {
            ranks.put(a, 0);
        });
        Address self = init.getSelfAddress();
        Set<Pending> pending = new HashSet<>();
        subscribe(new Handler<Start>() {
            @Override
            public void handle(Start event) {

            }
        }, crb);
        subscribe(new Handler<Event>() {
            @Override
            public void handle(Event event) {
                Map<Address, Integer> w = new HashMap<>(ranks);
                w.put(self, lsn);
                lsn++;
                // trigger();
            }
        }, crb);
        subscribe(new Handler<Pp2pDeliver>() {
            @Override
            public void handle(Pp2pDeliver event) {
                pending.add(new Pending());
                for(Iterator<Pending> it = pending.iterator(); it.hasNext();){
                    Pending pend = it.next();
                    if(isMapALessOrEqualsMapB(pend.getW(), ranks)){
                        it.remove();
                        ranks.computeIfPresent(pend.getP(),(k, v) -> v+1);
                        trigger();
                    }
                }
            }
        }, rb);
    }

    private boolean isMapALessOrEqualsMapB(Map<Address, Integer> a, Map<Address, Integer> b){
        for(Address p: a.keySet()){
            if(a.get(p) > b.get(p)){
                return false;
            }
        }
        return true;
    }

    private static class Pending{
        private final Address p;
        private final Map<Address, Integer> w;
        private final String m;

        public Pending(Address p, Map<Address, Integer> w, String m) {
            this.p = p;
            this.w = w;
            this.m = m;
        }

        public Address getP() {
            return p;
        }

        public Map<Address, Integer> getW() {
            return w;
        }

        public String getM() {
            return m;
        }
    }
}
