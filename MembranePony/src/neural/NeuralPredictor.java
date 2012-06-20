package neural;

import interfaces.Prediction;
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
public class NeuralPredictor implements  Predictor{

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
        //here all input data n should be mapped onto a single perceptron
        //returning 1 for TMH and 0 for non TMH
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

        System.out.println("Saving network...");

        try{
            this.neuralNetwork.save(this.pathToSavedNeuralNetwork);
        }catch(Exception ex){
            System.out.println("Neural Network could not be saved to path "+this.pathToSavedNeuralNetwork);
        }

        System.out.println("Network saved to "+this.pathToSavedNeuralNetwork);
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
     * Returns the file path to the neural network on hard disk
     * @return
     */
    public String getPathToSavedNeuralNetwork(){
        return this.pathToSavedNeuralNetwork;
    }

    /**
     * Generates a training set
     * @param trainingCases
     * @param trainingSet
     */
    private void generateTrainingSet(Sequence[] trainingCases, TrainingSet<SupervisedTrainingElement> trainingSet){

    }

    /**
     * Checks if the configuration of the neural network matches the training cases
     * @param trainingCases
     */
    private void checkIfConfigurationMatchesData(Sequence[] trainingCases){

    }

}