package predictor.neural;

import data.AminoAcid;
import data.SSE;
import interfaces.Prediction;
import interfaces.Predictor;
import interfaces.Result;
import interfaces.Sequence;
import interfaces.SequencePosition;
import interfaces.SlidingWindow;
import java.io.File;
import java.util.LinkedList;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.core.learning.TrainingSet;
import org.neuroph.nnet.Perceptron;

/**
 *
 * @author tobias
 */
@Deprecated
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
        //eg matrix multilayer perceptron, adaline, etc...
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

        LinkedList<Double> inputVector = new LinkedList<Double>();
        LinkedList<Result[]> output = new LinkedList<Result[]>();

        for(SlidingWindow window : sequence.getWindows()){

            inputVector.clear();

            for(int i=0; i<window.getSequence().length; i++){

                SequencePosition pos = window.getSequence()[i];

                inputVector.add(mapAAsOntoDoubleValues(pos.getAminoAcid()));
                inputVector.add(pos.getHydrophobicity());
                inputVector.add(mapSSEontoDoubleValues(pos.getSecondaryStructure()));
            }

            //predict
            this.neuralNetwork.setInput(doubleLinkedListToDoubleArray(inputVector));
            this.neuralNetwork.calculate();

            //get output
            output.add(mapDoubleValuesOntoResult(this.neuralNetwork.getOutput()));
        }

        //Why do we give back just one result array for the whole sequence?
        //Should'nt we give back an array of result arrays according to each window which
        //has been preidicted? Maybe we have to adapt the interfaces...
        return new NeuralPrediction(sequence, output.get(0)); //???????????????????
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

        //first check if values of training cases correspond to configuration of network

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
     * Generates a training set
     * @param trainingCases
     * @param trainingSet
     */
    private void generateTrainingSet(Sequence[] trainingCases, TrainingSet<SupervisedTrainingElement> trainingSet){

        LinkedList<Double> trainingInputVector = new LinkedList<Double>();
        LinkedList<Double> trainingOutputVector = new LinkedList<Double>();

        for(Sequence s : trainingCases){

            SlidingWindow[] seqWindows = s.getWindows();

            for(SlidingWindow window : seqWindows){

                trainingInputVector.clear();
                trainingOutputVector.clear();

                for(int i=0; i<window.getSequence().length; i++){

                    SequencePosition position = window.getSequence()[i];

                    //fill input vector
                    trainingInputVector.add(mapAAsOntoDoubleValues(position.getAminoAcid()));
                    trainingInputVector.add(position.getHydrophobicity());
                    trainingInputVector.add(mapSSEontoDoubleValues(position.getSecondaryStructure()));

                    //fill output vector
                    trainingOutputVector.add(mapResultOntoDoubleValues(position.getRealClass()));
                }

                //add a training element to the training set
                trainingSet.addElement(new SupervisedTrainingElement(doubleLinkedListToDoubleArray(trainingInputVector),doubleLinkedListToDoubleArray(trainingOutputVector)));
            }
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

    /**
     *
     * @param list
     * @return
     */
    private double[] doubleLinkedListToDoubleArray(LinkedList<Double> list){

        double[] output = new double[list.size()];

        for(int i=0; i<list.size(); i++){
            output[i] = list.get(i);
        }

        return output;
    }

    /**
     *
     * @param res
     * @return
     */
    private double mapResultOntoDoubleValues(Result res){

        switch(res){
            case INSIDE     : return 1.0;
            case OUTSIDE    : return 2.0;
            case TMH        : return 3.0;
            case NON_TMH    : return 4.0;

            default         : return 0.0;
        }
    }

    /**
     *
     * @param d
     * @return
     */
    private Result[] mapDoubleValuesOntoResult(double[] d){

        Result[] results = new Result[d.length];

        for(int i=0; i<d.length; i++){

            switch((int) d[i]){

                case 1  : results[i]=Result.INSIDE;
                            break;
                case 2  : results[i]=Result.OUTSIDE;
                            break;
                case 3  : results[i]=Result.TMH;
                            break;
                case 4  : results[i]=Result.NON_TMH;
                            break;

                default : results[i]=null;
                            break;
             }
        }

        return results;
    }

    /**
     *
     * @param aa
     * @return
     */
    private double mapAAsOntoDoubleValues(AminoAcid aa){

        //maybe here it is neccessary to play with the mapped values, eg
        //-10 to 10 etc..., depends how the neural network will work

        switch(aa){

            case A  : return 1.0;
            case C  : return 2.0;
            case D  : return 3.0;
            case E  : return 4.0;
            case F  : return 5.0;
            case G  : return 6.0;
            case H  : return 7.0;
            case I  : return 8.0;
            case K  : return 9.0;
            case L  : return 10.0;
            case M  : return 11.0;
            case N  : return 12.0;
            case P  : return 13.0;
            case Q  : return 14.0;
            case R  : return 15.0;
            case S  : return 16.0;
            case T  : return 17.0;
            case V  : return 18.0;
            case W  : return 19.0;
            case Y  : return 20.0;

            default : return 0.0;
        }
    }

    /**
     *
     * @param sse
     * @return
     */
    private double mapSSEontoDoubleValues(SSE sse){

        switch(sse){
            case Helix  : return 1.0;
            case Coil   : return 2.0;
            case Sheet  : return 3.0;

            default     : return 0.0;
        }
    }

    /**
     * For testing purpose only
     * @param args
     */
    public static void main(String[] args){

    }
}