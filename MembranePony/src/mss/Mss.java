package mss;

import data.AminoAcid;
import data.Hydrophobicity;
import java.util.LinkedList;

/**
 *
 * @author tobias
 */
public class Mss {

    /**
     * Calculates the maximum scoring subsequence of a given amino acid sequence
     * @param seq the amino acid sequence in which mss's should be found
     * @param n the maximum number of mss's in the list which will be returned
     * @return a list of n mss's
     */
    public static LinkedList<MssResult> mss(AminoAcid[] seq, int n){
     
        LinkedList<MssResult> mssGetStartEndPoints = new LinkedList<MssResult>();

        double max = 0, rmax = 0;
        int rstart = 1, l = 0, r = 0;
        
        for(int i = 0; i < seq.length; i++) {
            
            if(rmax > 0) {

                rmax += Hydrophobicity.get(seq[i], Hydrophobicity.KYTE_DOOLITTLE);
                
            }else {

                rmax = Hydrophobicity.get(seq[i], Hydrophobicity.KYTE_DOOLITTLE);        
                rstart = i;      
            }

            if(rmax > max) {
                
                max = rmax;
                l = rstart;
                r = i;

                if(mssGetStartEndPoints.isEmpty() || mssGetStartEndPoints.size()<=n){
                    mssGetStartEndPoints.add(new MssResult(l,r,max));
                }
                
                if(mssGetStartEndPoints.size()>n){
                   
                    mssGetStartEndPoints.add(new MssResult(l,r,max));
                    findAndRemoveMinimum(mssGetStartEndPoints);                    
                }
            }
        }
        
        return mssGetStartEndPoints;
    }

    /**
     * 
     * @param mssResults 
     */
    private static void findAndRemoveMinimum(LinkedList<MssResult> mssResults){
        
        MssResult minRes = mssResults.get(0);
        double min = minRes.getMaxMss();
        
        for(int i=1; i<mssResults.size(); i++){
            if(mssResults.get(i).getMaxMss()<min){
                minRes = mssResults.get(i);
                min = minRes.getMaxMss();
            }
        }
        
        mssResults.remove(minRes);
    }
    
    /**
     * For testing purpose only
     *
     * @param args
     */
    public static void main(String[] args) {
        
    }
}
