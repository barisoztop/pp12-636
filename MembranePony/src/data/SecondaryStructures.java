package data;


/**
 *
 * @author rgreil
 */
public class SecondaryStructures {

    private static final String[] secondaryStructures = new String[]{
        "H", //helix
        "S", //sheet
        "C" //coil
    };

    public static String[] get() {
        return secondaryStructures;
    }


}
