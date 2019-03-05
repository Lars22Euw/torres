package torres;

public class Exit extends Exception {

	/**
	 * prints message in Chat.<br>
	 * No new line if null.
	 * @param message
	 */
	public Exit(String message) {
		if (message != null) System.out.println(message);
	}

	private static final long serialVersionUID = 1L;

}