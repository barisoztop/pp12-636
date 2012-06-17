package maximum_scoring_subsequence;

import java.awt.Point;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;

/**
 *
 * @author tobias
 */
public class Mss {

    private String pathToData;
    private HashMap<String, Float> mapAAsOntoHydrophobicity;

    /**
     * Creates a Mss object, needs path to the file containing AA hydrophobicity
     * information
     *
     * @param pathToDataFile
     */
    public Mss(String pathToDataFile) {
        this.pathToData = pathToDataFile;
        mapAAsOntoHydrophobicity = new HashMap<String, Float>();
    }

    /**
     * Parses datafile containing information about amino acid hydrophobicity
     */
    public void parseDataFile() {

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(new File(this.pathToData)));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Mss.class.getName()).log(Level.SEVERE, null, ex);
        }

        String line;
        Pattern h = Pattern.compile("^#+");
        Matcher m = null;

        try {
            while ((line = reader.readLine()) != null) {

                m = h.matcher(line);

                if (m.find()) {
                    continue;
                }

                String[] lineParts = line.split("\\t");

                mapAAsOntoHydrophobicity.put(lineParts[2].toUpperCase(), Float.parseFloat(lineParts[5]));
                mapAAsOntoHydrophobicity.put(lineParts[2].toLowerCase(), Float.parseFloat(lineParts[5]));
            }
        } catch (IOException ex) {
            Logger.getLogger(Mss.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            reader.close();
        } catch (IOException ex) {
            Logger.getLogger(Mss.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Calculates the most scoring subsequence(es)
     *
     * @param seq a string containing a amino acid primary sequence
     * @param countMss max number of mss's inside list which will be returned, of course
     * if they exists
     * @return a linked list of points saving start and end of most scoring
     * subsequences if there are mss with the same max score they will saved
     * into the list, if there is only one mss, so there will be just one entry
     * in the list
     */
    
    public LinkedList<Point> mss(String seq, int countMss){
     
        LinkedList<Point> mssGetStartEndPoints = new LinkedList<Point>();

        float max = 0, rmax = 0;
        int rstart = 1, l = 0, r = 0;
        char[] pseq = seq.toCharArray();

        for (int i = 0; i < pseq.length; i++) {

            if (!mapAAsOntoHydrophobicity.containsKey(String.valueOf(pseq[i]))) {

                try {
                    throw new DataFormatException("AA primary sequence contains " + String.valueOf(pseq[i]) + "."
                            + "This is not a valid character used in primary sequences.");
                } catch (DataFormatException ex) {
                    return null;
                }
            }

            if (rmax > 0) {

                rmax += mapAAsOntoHydrophobicity.get(String.valueOf(pseq[i]));

            } else {


                rmax = mapAAsOntoHydrophobicity.get(String.valueOf(pseq[i]));        
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
