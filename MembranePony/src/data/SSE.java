package data;

/**
 * secondary structure element type, (helix, sheet, coil)
 * @author Felix
 *
 */
public enum SSE {

    Helix("H"),
    Sheet("E"),
    Coil("C");
    
    
    private String singleLetter;


	private SSE(String singleLetter) {
		this.singleLetter = singleLetter;
	}
    
    
    public static SSE forProfRdb(char profRdb) {
    	switch(profRdb) {
    	case 'L': return Coil;
    	case 'E': return Sheet;
    	case 'H': return Helix;
    	
    	default: throw(new IllegalArgumentException("'"+profRdb+"' does not seem to be a proper ProfRdb SSE identifier (HEL)"));
    	}
    }
    
    
    public String getSingleLetterCode() {
    	return singleLetter;
    }

}
