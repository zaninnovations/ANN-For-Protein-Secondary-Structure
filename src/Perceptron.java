import java.util.*;
public class Perceptron {

    public double [] weights = null;
    public double [] deltas = null;
    public double [] prevDeltas = null;
    public double [] bestState = null;
    ArrayList<Perceptron> incoming = new ArrayList<Perceptron>();
    ArrayList<Perceptron> outcoming = new ArrayList<Perceptron>();
    double[] incomingInput = null; 
    Perceptron currBias;
    double output;
    static final double bias = .5;
    static int count = 0;
    final int myCount;
    final int function;
    boolean dropped = false;

    //within the network
    public Perceptron(int numWeights,Perceptron layerBias, Rand rand, int activationFun){

        this.weights = new double[numWeights + 1]; //why+1? bias
        this.deltas = new double[numWeights]; 
        this.prevDeltas = new double[numWeights];
        
        for(int i=0; i<weights.length;i++)
            this.weights[i] = rand.getNextRand();
        this.currBias = layerBias;
        this.myCount = count;
        count++;

        this.function = activationFun;
    }

    //bias perceptron
    public Perceptron(int numWeights, Rand rand, int activationFun){
        this.weights = new double[numWeights];
        
        for(int i=0; i<weights.length;i++)
            this.weights[i] = rand.getNextRand();
        
        this.myCount = -1;
        
        this.function = activationFun;
    }


    public double activateInput(int weightIndex) {
        
        double result = this.weights[weightIndex]*1;
        
        result = result + this.currBias.getWeight(this.myCount)*bias;
        
        this.output = fun(result);
        
        return this.output;
    }
    
    public void drop() {
        this.dropped = true;
    }
    
    public void unDrop() {
        this.dropped = false;
    }
    
    public boolean isDropped() {
        return this.dropped;
    }

    public void addInputs(double[] inputs){
        this.incomingInput = inputs;
    }

    public double activate(){
        double result = 0;
        for(int i=0; i < this.incoming.size(); i++){

          
            double incomingOutput = this.incoming.get(i).getOutput();

            result = result + incomingOutput*this.weights[i]; 
        }

        result = result + (this.currBias.getWeight(this.myCount)*bias);

        this.output = fun(result);

        return this.output;
    }

    double fun(double x){
        if(function==0) //RL
            return Math.max(0,x);
        else if(function==1) //Sigmoid
            return (1.0/(1.0+Math.exp(-x)));
        else if(function==2)  //Analytic function
            return Math.log(1+Math.exp(x));
        else
            return -1;
    }
    
    public void setBestState() {
        this.bestState = this.weights;
    }
    
    public void useBestState() {
        this.weights = this.bestState;
    }

    public void addIncoming (ArrayList<Perceptron> feed){
        this.incoming.addAll(feed);
    }

    public void addOutcoming (ArrayList<Perceptron> outs) {
        this.outcoming.addAll(outs);
    }

    public void addIncomingPerceptron(Perceptron add){
        this.incoming.add(add);
    }

    //set adder for bias, maybe???
    public Perceptron getIncoming(int i){
        return this.incoming.get(i);
    }

    public ArrayList<Perceptron> getallIncoming(){
        return this.incoming;
    }

    double getWeight(int i) {
        return this.weights[i];
    }
    
    void setWeight(int i, double newWeight) {
        this.weights[i] = newWeight;
    }
    
    void setDelta(int i, double newDelta) {
        this.prevDeltas[i] = this.deltas[i];
        this.deltas[i] = newDelta;
    }
    
    double getDelta(int i) {
        return this.prevDeltas[i];
    }

    double getOutput(){
        return (dropped ? 0 : this.output);
    }

    int getCount() {
        return this.myCount;
    }
    
}
