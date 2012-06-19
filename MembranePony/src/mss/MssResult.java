/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mss;

/**
 *
 * @author tobias
 */
public class MssResult {
    private int start;
    private int end;
    private double value;
    
    /**
     * Creates a new MssResult object
     * @param p
     * @param max 
     */
    public MssResult(int start, int end, double value){
        this.start = start;
        this.end = end;
        this.value = value;
    }
    
    /**
     * Returns start point of current MssResult
     * @return 
     */
    public int getStartPointMss(){
        return this.start;
    }
    
    /**
     * Returns end point of current MssResult
     * @return 
     */
    public int getEndPoint(){
        return this.end;
    }
    
    /**
     * Returns value of current MssResult
     * @return 
     */
    public double getMaxMss(){
        return this.value;
    }
}
