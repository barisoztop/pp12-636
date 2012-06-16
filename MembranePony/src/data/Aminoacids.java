package data;

/**
 *
 * @author rgreil
 */
public class Aminoacids{

    private static final String[] aminoacids = new String[]{
        "A", //ala -> alanin
        "C", //cys -> cystein
        "D", //asp -> asparaginsäure
        "E", //glu -> glutaminsäure
        "F", //phe -> phenylalanin
        "G", //gly -> glycin
        "H", //his -> histidin
        "I", //ile -> isoleucin
        "K", //lys -> lysin
        "L", //leu -> leucin
        "M", //met -> methionin
        "N", //asn -> asparagin
        "P", //pro -> prolin
        "Q", //gln -> glutamin
        "R", //arg -> arginin
        "S", //ser -> serin
        "T", //thr -> threonin
        "V", //val -> valin
        "W", //trp -> tryptophan
        "Y" //tyr -> tyrosin
    };

    public static String[] get() {
        return aminoacids;
    }
}
