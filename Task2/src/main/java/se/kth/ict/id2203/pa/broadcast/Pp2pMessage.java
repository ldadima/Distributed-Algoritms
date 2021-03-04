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
package se.kth.ict.id2203.pa.broadcast;

import se.kth.ict.id2203.ports.pp2p.Pp2pDeliver;
import se.sics.kompics.address.Address;

import java.util.Map;

public class Pp2pMessage extends Pp2pDeliver {

	private static final long serialVersionUID = 2193713942080123560L;
	
	private final String message;
	
	private Integer sn;

    private Map<Address, Integer> w;

    public Pp2pMessage(Address source, String message) {
        super(source);
        this.message = message;
    }

    public Pp2pMessage(Address source, String message, int sn) {
        super(source);
        this.message = message;
        this.sn = sn;
    }

    public Pp2pMessage(Address source, String message,  Map<Address, Integer> w) {
        super(source);
        this.message = message;
        this.w = w;
    }

    public String getMessage() {
        return message;
    }

    public Integer getSn() {
        return sn;
    }

    public Map<Address, Integer> getW() {
        return w;
    }
}
