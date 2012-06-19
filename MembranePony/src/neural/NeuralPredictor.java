package neural;

import interfaces.Predictor;
import interfaces.Sequence;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.core.learning.TrainingSet;
import org.neuroph.nnet.Perceptron;

/**
 * 
 * @author tobias
 */
public class NeuralPredictor implements Predictor{
    
    private String pathToSavedNeuralNetwork;
    private NeuralNetwork neuralNetwork;
    private int inputN;
    private int outputN;
    private NeuralTask task;
    
    /**
     * Use this constructor for training and saving the neural network
     * @param pathToSavedNeuralNetwork
     * @param inputNeurons
     * @param outPutNeurons
     */
     public NeuralPredictor(String pathToSavedNeuralNetwork, int inputNeurons, int outputNeurons){
        
        this.task = NeuralTask.TRAINING;
        this.pathToSavedNeuralNetwork = pathToSavedNeuralNetwork;
        this.inputN = inputNeurons;
        this.outputN = outputNeurons;
        
        //maybe here we could use a different subclass, 
        //eg maatrix multilayer perceptron, adaline, etc...
        this.neuralNetwork = new Perceptron(inputNeurons, outputNeurons);
    }
    
    /**
      * Use this constructor for loading an existing neural network and for prediction
      * @param pathToSavedNetwork 
      */
    public NeuralPredictor(String pathToSavedNetwork){
        
        this.task = NeuralTask.PREDICTION;
        this.pathToSavedNeuralNetwork = pathToSavedNetwork;
        
        try{
           this.neuralNetwork = NeuralNetwork.load(pathToSavedNetwork); 
        }catch(Exception e){
            System.out.println("NeuralNetwork could not be found at "+pathToSavedNetwork);
        }
    }
    
    @Override
    public void predict(Sequence sequence) {
        
        if(task != NeuralTask.PREDICTION){
            throw new IllegalStateException("Instance was created for Training."
                    + "Prediction is not possible at the moment.");
        }
    }

    @Override
    public void train(Sequence[] trainingCases) {
        
        if(task != NeuralTask.TRAINING){
            throw new IllegalStateException("Instance was created for Prediction."
                    + "Training is not possible at the moment.");
        }
        
        TrainingSet<SupervisedTrainingElement> trainingSet = new TrainingSet<SupervisedTrainingElement>(this.inputN, this.outputN);
        
        //first check if values of training cases corresond to configuration of network
        
        
    }
    
    public NeuralNetwork getNeuralNetWork(){
        return this.neuralNetwork;
    }
    
    public double[] getNNOutput(){
        return this.neuralNetwork.getOutput();
    }
    
    public String getPathToSavedNeuralNetwork(){
        return this.pathToSavedNeuralNetwork;
    }
}