package se.kth.ict.id2203.pa.broadcast;

import se.kth.ict.id2203.ports.rb.RbDeliver;
import se.sics.kompics.address.Address;

import java.util.Map;

public class RbMessage extends RbDeliver {

	private static final long serialVersionUID = -1855724247802103843L;

	private final String message;

    private Integer sn;

    private Map<Address, Integer> w;

    public RbMessage(Address source, String message) {
		super(source);
		this.message = message;
	}

    public RbMessage(Address source, String message, Integer sn, Map<Address, Integer> w) {
        super(source);
        this.message = message;
        this.sn = sn;
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
