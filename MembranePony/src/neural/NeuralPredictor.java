package neural;

import interfaces.Prediction;
import interfaces.Predictor;
import interfaces.Sequence;
import java.io.File;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.core.learning.TrainingSet;
import org.neuroph.nnet.Perceptron;

/**
 *
 * @author tobias
 */
public class NeuralPredictor implements Predictor{

    private NeuralNetwork neuralNetwork;
    private int inputN;
    private int outputN;
    private boolean networkLoaded;
    private NeuralTask task;

    /**
     * Use this constructor for training and saving the neural network
     * @param pathToSavedNeuralNetwork
     * @param inputNeurons
     * @param outPutNeurons
     */
     public NeuralPredictor(int inputNeurons, int outputNeurons){

        this.task = NeuralTask.TRAINING;
        this.inputN = inputNeurons;
        this.outputN = outputNeurons;

        //maybe here we could use a different subclass,
        //eg maatrix multilayer perceptron, adaline, etc...
        //here all input data n should be mapped onto a single perceptron
        //returning 1 for TMH and 0 for non TMH
        this.neuralNetwork = new Perceptron(inputNeurons, outputNeurons);
    }   
     
     /**
      * Use this constructor to load a saved neural network and for prediction
      */
     public NeuralPredictor(){
         this.networkLoaded = true;
         this.task = NeuralTask.PREDICTION;
     }
    
    /**
      * Predicts a 
      * @param sequence
      * @return 
      */
    @Override
    public Prediction predict(Sequence sequence) {

        if(task != NeuralTask.PREDICTION){
            throw new IllegalStateException("Instance was created for Training."
                    + "Prediction is not possible at the moment.");
        }
        
        if(!this.networkLoaded){
            throw new IllegalStateException("Neural Network was not loaded"
                    + "from harddisk. Please use load method to load it.");
        }
        
        Prediction p;
        
        
        
        return null;
    }
    
    /**
     * 
     * @param trainingCases 
     */
    @Override
    public void train(Sequence[] trainingCases) {

        if(task != NeuralTask.TRAINING){
            throw new IllegalStateException("Instance was created for Prediction."
                    + "Training is not possible at the moment.");
        }

        //first check if values of training cases corresond to configuration of network

        //create a training set
        System.out.println("Generating training set...");

        TrainingSet<SupervisedTrainingElement> trainingSet = new TrainingSet<SupervisedTrainingElement>(this.inputN, this.outputN);

        long a = System.currentTimeMillis();

        generateTrainingSet(trainingCases, trainingSet);

        long b = System.currentTimeMillis();

        System.out.println("Training set generated. This took "+(b-a)+" milliseconds.");

        //train the network
        System.out.println("Training the network...");

        a = System.currentTimeMillis();

        this.neuralNetwork.learn(trainingSet);

        b = System.currentTimeMillis();

        System.out.println("Network trained. This took "+(b-a)+" milliseconds.");
    }

    /**
     * Returns the actual neural network
     * @return
     */
    public NeuralNetwork getNeuralNetWork(){
        return this.neuralNetwork;
    }

    /**
     * Returns the output of the neurtal network after prediction
     * @return
     */
    public double[] getNNOutput(){
        if(task == NeuralTask.PREDICTION){
            return this.neuralNetwork.getOutput();
        }else{
            throw new IllegalStateException("Output of NN cannot be delivered in training mode.");
        }
    }

    /**
     * Generates a training set
     * @param trainingCases
     * @param trainingSet
     */
    private void generateTrainingSet(Sequence[] trainingCases, TrainingSet<SupervisedTrainingElement> trainingSet){
        for(Sequence s : trainingCases){
            
        }
    }

    /**
     * Saves trained model to hard disk
     * @param model
     * @throws Exception 
     */
    @Override
    public void save(File model) throws Exception {
        try{
            this.neuralNetwork.save(model.toString());
        }catch(Exception e){
            System.out.println("Model could not saved to path "+model.toString());
        }
    }
    
    /**
     * Loads model from harddisk
     * @param model
     * @throws Exception 
     */
    @Override
    public void load(File model) throws Exception {
        try{
            this.neuralNetwork = NeuralNetwork.load(model.toString());
            this.networkLoaded = true;
        }catch(Exception e){
            System.out.println("Model could be load from path "+model.toString());
        }
    }
    
    /**
     * Returns the number of input neurons used in this neural network
     * @return 
     */
    public int getNumberOfInputNeurons(){
        return this.inputN;
    }
    
    /**
     * Returns the number of output neurons used in this neural network
     * @return 
     */
    public int getNumberOfOutputNeurons(){
        return this.outputN;
    }
}