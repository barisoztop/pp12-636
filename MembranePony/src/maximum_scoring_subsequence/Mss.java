package maximum_scoring_subsequence;

import data.AminoAcid;
import data.Hydrophobicity;
import java.awt.Point;
import java.util.LinkedList;

/**
 *
 * @author tobias
 */
public class Mss {

    /**
     * Calculates the maximum scoring subsequence of a given amino acid sequence
     * @param seq the amino acid sequence in which mss's should be found
     * @param countMss the maximum number of mss's in the list which will be returned
     * @return 
     */
    public static LinkedList<Point> mss(AminoAcid[] seq, int countMss){
     
        LinkedList<Point> mssGetStartEndPoints = new LinkedList<Point>();

        double max = 0, rmax = 0;
        int rstart = 1, l = 0, r = 0;
        
        for (int i = 0; i < seq.length; i++) {
            
            if (rmax > 0) {

                rmax += Hydrophobicity.get(seq[i], Hydrophobicity.KYTE_DOOLITTLE);

            } else {

                rmax = Hydrophobicity.get(seq[i], Hydrophobicity.KYTE_DOOLITTLE);        
                rstart = i;      

            }

            if (rmax > max) {
                
                max = rmax;
                l = rstart;
                r = i;

                if(mssGetStartEndPoints.isEmpty() || mssGetStartEndPoints.size()<=countMss){
                    mssGetStartEndPoints.add(new Point(l,r));
                }
                
                if(mssGetStartEndPoints.size()>countMss){
                   
                    mssGetStartEndPoints.add(new Point(l, r));
                    findAndRemoveMinimum(mssGetStartEndPoints);                    
                }
            }
        }
        
        return mssGetStartEndPoints;
    }

    /**
     * 
     * @param points 
     */
    private static void findAndRemoveMinimum(LinkedList<Point> points){
        
        int minimum = Integer.MIN_VALUE;
        Point pMinimum = null;
        
        for(Point p : points){
            
            if(((int) p.getY() - (int) p.getX()) < minimum){
                minimum = (int) p.getY() - (int) p.getX();
                pMinimum = p;
            }         
        }
        
        points.remove(pMinimum);
    }
    
    /**
     * For testing purpose only
     *
     * @param args
     */
    public static void main(String[] args) {
        
    }
}
