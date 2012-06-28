/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neural;

import interfaces.Prediction;
import interfaces.Region;
import interfaces.Result;
import interfaces.Sequence;
import java.util.LinkedList;

/**
 *
 * @author tobiassander
 */
public class NeuralPrediction implements Prediction{
    
    private Sequence sequence;
    private Result[] results;
            
    public NeuralPrediction(Sequence seq, Result[] results){
        this.sequence = seq;
        this.results = results;
    }
    
    /**
     * Returns the actual sequence contained by this object
     * @return 
     */
    @Override
    public Sequence getInputSequence() {
        return this.sequence;
    }
    
    /**
     * Returns the prediction result of a given residue
     * @param residueNr
     * @return 
     */
    @Override
    public Result getPredictionForResidue(int residueNr) {
        if(residueNr>=0 && residueNr<this.results.length){
            return this.results[residueNr];
        }else{
            throw new IllegalArgumentException("Residue at position "+residueNr+" is not"
                    + "available. Please run prediction first.");
        }
    }
    
    /**
     * 
     * @return 
     */
    @Override
    public Region[] getPredictedRegions() {
        
        if(results==null){
            throw new UnsupportedOperationException("No results available.");
        }
        
        LinkedList<Region> regions = new LinkedList<Region>();
        Result tempResult=null;
        int start=0;
               
        for(int i=0; i<results.length; i++){
            
            if(tempResult==null){
                start = i;
                tempResult = results[i];
                continue;
            }
            
            if(i==results.length-1 && tempResult!=results[i]){
                regions.add(new Region(i, i, results[i]));
            }
            
            if(i==results.length-1 && tempResult==results[i]){
                regions.add(new Region(start, i, tempResult));
            }
            
            if(tempResult!=results[i]){     
                regions.add(new Region(start, i-1, tempResult));
                tempResult = results[i];
                start = i;
            }         
        }
        
        return regionListToArray(regions);    
    }
    
    /**
     * 
     * @param regions
     * @return 
     */
    private Region[] regionListToArray(LinkedList<Region> regions){
        
        Region[] regs = new Region[regions.size()];
        
        for(int i=0; i<regions.size(); i++){
            regs[i] = regions.get(i);
        }
        
        return regs;
    }
}
