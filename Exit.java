package turrets;

public class Exit extends Exception {

	public Exit(String message) {
		System.out.println(message);
	}

	public Exit() {
	}

	private static final long serialVersionUID = 1L;

}