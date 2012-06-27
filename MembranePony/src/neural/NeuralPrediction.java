/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neural;

import interfaces.Prediction;
import interfaces.Region;
import interfaces.Result;
import interfaces.Sequence;

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

    @Override
    public Region[] getPredictedRegions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }    
}
