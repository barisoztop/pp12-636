package data;

import java.util.HashMap;

/**
 *
 * Lookup table for 7 different hydrophobicity scales
 *
 */
public class Hydrophobicity {

    public static final int KYTE_DOOLITTLE = 0, HOPP_WOODS = 1, CORNETTE = 2, EISENBERG = 3, /*
             * ROSE = 4,
             */ JANIN = 4, ENGELMAN = 5;
    private static HashMap<String, Double> kyteDoolittle = new HashMap<String, Double>();
    private static HashMap<String, Double> hoppWoods = new HashMap<String, Double>();
    private static HashMap<String, Double> cornette = new HashMap<String, Double>();
    private static HashMap<String, Double> eisenberg = new HashMap<String, Double>();
//    private static HashMap<String, Double> rose = new HashMap<String, Double>();
    private static HashMap<String, Double> janin = new HashMap<String, Double>();
    private static HashMap<String, Double> engelman = new HashMap<String, Double>();

    static {

        kyteDoolittle.put("A", 1.80);
        hoppWoods.put("A", -0.50);
        cornette.put("A", 0.20);
        eisenberg.put("A", 0.62);
        janin.put("A", 0.74);
        engelman.put("A", 0.30);
        kyteDoolittle.put("C", 2.50);
        hoppWoods.put("C", -1.00);
        cornette.put("C", 4.10);
        eisenberg.put("C", 0.29);
        janin.put("C", 0.91);
        engelman.put("C", 0.90);
        kyteDoolittle.put("D", -3.50);
        hoppWoods.put("D", 3.00);
        cornette.put("D", -3.10);
        eisenberg.put("D", -0.90);
        janin.put("D", 0.62);
        engelman.put("D", -0.60);
        kyteDoolittle.put("E", -3.50);
        hoppWoods.put("E", 3.00);
        cornette.put("E", -1.80);
        eisenberg.put("E", -0.74);
        janin.put("E", 0.62);
        engelman.put("E", -0.70);
        kyteDoolittle.put("F", 2.80);
        hoppWoods.put("F", -2.50);
        cornette.put("F", 4.40);
        eisenberg.put("F", 1.19);
        janin.put("F", 0.88);
        engelman.put("F", 0.50);
        kyteDoolittle.put("G", -0.40);
        hoppWoods.put("G", 0.00);
        cornette.put("G", 0.00);
        eisenberg.put("G", 0.48);
        janin.put("G", 0.72);
        engelman.put("G", 0.30);
        kyteDoolittle.put("H", -3.20);
        hoppWoods.put("H", -0.50);
        cornette.put("H", 0.50);
        eisenberg.put("H", -0.40);
        janin.put("H", 0.78);
        engelman.put("H", -0.10);
        kyteDoolittle.put("I", 4.50);
        hoppWoods.put("I", -1.80);
        cornette.put("I", 4.80);
        eisenberg.put("I", 1.38);
        janin.put("I", 0.88);
        engelman.put("I", 0.70);
        kyteDoolittle.put("K", -3.90);
        hoppWoods.put("K", 3.00);
        cornette.put("K", -3.10);
        eisenberg.put("K", -1.50);
        janin.put("K", 0.52);
        engelman.put("K", -1.80);
        kyteDoolittle.put("L", 3.80);
        hoppWoods.put("L", -1.80);
        cornette.put("L", 5.70);
        eisenberg.put("L", 1.06);
        janin.put("L", 0.85);
        engelman.put("L", 0.50);
        kyteDoolittle.put("M", 1.90);
        hoppWoods.put("M", -1.30);
        cornette.put("M", 4.20);
        eisenberg.put("M", 0.64);
        janin.put("M", 0.85);
        engelman.put("M", 0.40);
        kyteDoolittle.put("N", -3.50);
        hoppWoods.put("N", 0.20);
        cornette.put("N", -0.50);
        eisenberg.put("N", -0.78);
        janin.put("N", 0.63);
        engelman.put("N", -0.50);
        kyteDoolittle.put("P", -1.60);
        hoppWoods.put("P", 0.00);
        cornette.put("P", -2.20);
        eisenberg.put("P", 0.12);
        janin.put("P", 0.64);
        engelman.put("P", -0.30);
        kyteDoolittle.put("Q", -3.50);
        hoppWoods.put("Q", 0.20);
        cornette.put("Q", -2.80);
        eisenberg.put("Q", -0.85);
        janin.put("Q", 0.62);
        engelman.put("Q", -0.70);
        kyteDoolittle.put("R", -4.50);
        hoppWoods.put("R", 3.00);
        cornette.put("R", 1.40);
        eisenberg.put("R", -2.53);
        janin.put("R", 0.64);
        engelman.put("R", -1.40);
        kyteDoolittle.put("S", -0.80);
        hoppWoods.put("S", 0.30);
        cornette.put("S", -0.50);
        eisenberg.put("S", -0.18);
        janin.put("S", 0.66);
        engelman.put("S", -0.10);
        kyteDoolittle.put("T", -0.70);
        hoppWoods.put("T", -0.40);
        cornette.put("T", -1.90);
        eisenberg.put("T", -0.05);
        janin.put("T", 0.70);
        engelman.put("T", -0.20);
        kyteDoolittle.put("V", 4.20);
        hoppWoods.put("V", -1.50);
        cornette.put("V", 4.70);
        eisenberg.put("V", 1.08);
        janin.put("V", 0.86);
        engelman.put("V", 0.60);
        kyteDoolittle.put("W", -0.90);
        hoppWoods.put("W", -3.40);
        cornette.put("W", 1.00);
        eisenberg.put("W", 0.81);
        janin.put("W", 0.85);
        engelman.put("W", 0.30);
        kyteDoolittle.put("Y", -1.30);
        hoppWoods.put("Y", -2.30);
        cornette.put("Y", 3.20);
        eisenberg.put("Y", 0.26);
        janin.put("Y", 0.76);
        engelman.put("Y", -0.40);



    }

    /**
     * looks up given amino acid in the specified matrix and returns the value.
     *
     * @param aminoacid the amino acid (case insensitive)
     * @param scale KYTE_DOOLITTLE, HOPP_WOODS etc. constants
     * @return matrix value
     * @throws NullPointerException for invalid amino acid
     * @throws RuntimeException for invalid scale
     */
    public static double get(AminoAcid aminoAcid, int scale) {
        String aa = aminoAcid.toString();

        Double result;
        
        if (scale == KYTE_DOOLITTLE) {
            result = kyteDoolittle.get(aa);
        } else if (scale == HOPP_WOODS) {
        	result = hoppWoods.get(aa);
        } else if (scale == CORNETTE) {
        	result = cornette.get(aa);
        } else if (scale == EISENBERG) {
        	result = eisenberg.get(aa);
//        } else if (scale == ROSE) {
//            return rose.get(aa);
        } else if (scale == JANIN) {
        	result = janin.get(aa);
        } else if (scale == ENGELMAN) {
        	result = engelman.get(aa);
        } else {
            throw (new IllegalArgumentException("Invalid Scale"));
        }
        
        if(result==null)
        	throw(new IllegalArgumentException(aa+" has no entry in the specified table ("+scale+")"));
        
        return result;
    }

    /**
     * returns an double array with min max value of the given scale
     *
     * @param scale KYTE_DOOLITTLE, HOPP_WOODS etc. constants
     * @return double[2] of min|max
     * @throws RuntimeException for invalid scale
     */
    public static double[] getMinMax(int scale) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        HashMap<String, Double> tmp;

        if (scale == KYTE_DOOLITTLE) {
            tmp = kyteDoolittle;
        } else if (scale == HOPP_WOODS) {
            tmp = hoppWoods;
        } else if (scale == CORNETTE) {
            tmp = cornette;
        } else if (scale == EISENBERG) {
            tmp = eisenberg;
//        } else if (scale == ROSE) {
//            return rose.get(aa);
        } else if (scale == JANIN) {
            tmp = janin;
        } else if (scale == ENGELMAN) {
            tmp = engelman;
        } else {
            throw (new RuntimeException("Invalid Scale"));
        }

        for (Double d : tmp.values()) {
            if (d > max) {
                max = d;
            }
            if (d < min) {
                min = d;
            }
        }
        return new double[] {min, max};
    }
}
