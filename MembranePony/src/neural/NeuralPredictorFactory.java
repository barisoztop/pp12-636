package neural;

import interfaces.Predictor;
import interfaces.PredictorFactory;

@Deprecated
public class NeuralPredictorFactory implements PredictorFactory{

    private NeuralTask task;
    private boolean configuration;
    private int inputNeurons;
    private int outputNeurons;

    public NeuralPredictorFactory(NeuralTask task){
        this.task = task;
        this.configuration = false;
    }

    @Override
    public Predictor getInstance() {

        if(task == NeuralTask.TRAINING){

            if(this.configuration){

                //used for training
                return new NeuralPredictor(this.inputNeurons, this.outputNeurons);

            }else{
                throw new IllegalStateException("Please use configuration method first"
                        + "before using getInstance() method.");
            }

        }else if(task == NeuralTask.PREDICTION){

            //used for prediction
            return new NeuralPredictor();

        }else{
            throw new IllegalStateException("NeuralPredictor Instance can not be created.");
        }
    }

    public void setTask(NeuralTask task){
        this.task = task;
    }

    public NeuralTask getActualTask(){
        return this.task;
    }

    public void configureNeuralNetwork(String pathToSaveNN, int inputNeurons, int outputNeurons){
        this.configuration = true;
        this.inputNeurons = inputNeurons;
        this.outputNeurons = outputNeurons;
    }
}