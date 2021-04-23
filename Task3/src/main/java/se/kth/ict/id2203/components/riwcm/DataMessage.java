package se.kth.ict.id2203.components.riwcm;

import se.kth.ict.id2203.ports.pp2p.Pp2pDeliver;
import se.sics.kompics.address.Address;

public class DataMessage extends Pp2pDeliver {
	private static final long serialVersionUID = 2590220568568259852L;

	private final Integer r, ts, wr, val;

	protected DataMessage(Address source, Integer r, Integer ts, Integer wr,
			Integer val) {
		super(source);
		this.r = r;
		this.ts = ts;
		this.wr = wr;
		this.val = val;
	}

	public Integer getR() {
		return r;
	}

	public Integer getTs() {
		return ts;
	}

	public Integer getWr() {
		return wr;
	}

	public Integer getVal() {
		return val;
	}
}
