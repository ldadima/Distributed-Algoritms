/**
 * This file is part of the ID2203 course assignments kit.
 * 
 * Copyright (C) 2009-2013 KTH Royal Institute of Technology
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.kth.ict.id2203.components.beb;

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
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Event;
import se.sics.kompics.Handler;
import se.sics.kompics.Port;
import se.sics.kompics.address.Address;

import java.util.Set;

public class BasicBroadcast extends ComponentDefinition {

	private static final Logger logger = LoggerFactory.getLogger(BasicBroadcast.class);
	private final Port<BestEffortBroadcast> beb = requires(BestEffortBroadcast.class);
	private final Port<PerfectPointToPointLink> pp2p = requires(PerfectPointToPointLink.class);

	public BasicBroadcast(BasicBroadcastInit init) {
	    Set<Address> all = init.getAllAddresses();
	    Address self = init.getSelfAddress();
	    all.remove(self);
        subscribe(new Handler<BebBroadcast>() {
            @Override
            public void handle(BebBroadcast event) {
                BebDeliver deliver = event.getDeliverEvent();
                String message = deliver instanceof BebMessage ? ((BebMessage) deliver).getMessage() : "";
                for(Address p: all){
                    trigger(new Pp2pSend(p, new Pp2pMessage(deliver.getSource(), message)), pp2p);
                }
            }
        }, beb);
        subscribe(new Handler<Pp2pDeliver>() {
            @Override
            public void handle(Pp2pDeliver event) {
                String message = event instanceof Pp2pMessage ? ((Pp2pMessage) event).getMessage() : "";
                trigger(new Pp2pMessage(event.getSource(), message), beb);
            }
        }, pp2p);
	}

}
