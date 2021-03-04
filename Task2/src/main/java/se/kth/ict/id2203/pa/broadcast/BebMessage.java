package se.kth.ict.id2203.pa.broadcast;

import se.kth.ict.id2203.ports.beb.BebDeliver;
import se.sics.kompics.address.Address;

import java.util.Map;

public class BebMessage extends BebDeliver {

	private final String message;

	private static final long serialVersionUID = 5491596109178800519L;

	private Integer sn;

	private Map<Address, Integer> w;

	public BebMessage(Address source, String message) {
		super(source);
		this.message = message;
	}

	public BebMessage(Address source, String message, int sn) {
		super(source);
		this.message = message;
		this.sn = sn;
	}

	public BebMessage(Address source, String message,  Map<Address, Integer> w) {
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
