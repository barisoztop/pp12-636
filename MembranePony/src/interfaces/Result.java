package interfaces;

public enum Result {

	/**
	 * inside of cellular membrane
	 */
	INSIDE,

	/**
	 * outside of cellular membrane
	 */
	OUTSIDE,

	/**
	 * inside transmembrane helix
	 */
	TMH("T"),


	NON_TMH(".");



	private String singleLetterCode = "?";
	
	
	private Result() {}
	
	private Result(String singleLetterCode) {
		this.singleLetterCode = singleLetterCode;
	}
	
	public String getSingleLetterCode() {
		return singleLetterCode;
	}
	
}
