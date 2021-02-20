package se.kth.ict.id2203.pa.epfd;

import se.kth.ict.id2203.sim.epfd.EpfdTester;

public class AutomaticCorrection {
	public static void main(String[] args) {
		String email = "enter email here";
		String password = "enter password here";
		EpfdTester.correctAndSubmit(email, password);
	}
}
