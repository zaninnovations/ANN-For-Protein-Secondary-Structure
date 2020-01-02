
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
//import java.util.Scanner;
import java.util.*;


public class Lab2 {
    //global rand object with seed 638*638
    static final Rand rand = new Rand();

    //debugging flags
    static boolean windowDbg = false, setsDbg = false, protCountDbg = false,
            trainDbg = false, tuneDbg = false, testDbg = false;

    //sets variable declarations 
    static int trainGLCount = 0, tuneGLCount = 0, testGLCount = 0;
    static String [] trainSet, tuneSet, testSet;

    //input layer
    private static int inputUnitsNumber = Input.WINDOW_SZ;
    private static Perceptron inputBias;
    private static ArrayList<Perceptron> inputLayer = new ArrayList<Perceptron>();
    //hidden layer
    private static int HUNumber = 100;
    private static Perceptron hiddenBias;
    private static ArrayList<Perceptron> hiddenLayer = new ArrayList<Perceptron>();
    //output layer
    private static int outputNumber = 3;
    private static Perceptron outputBias;
    private static ArrayList<Perceptron> outputLayer = new ArrayList<Perceptron>();

    //learning params
    static int function = 1; //0=RL, 1=Sigmoid, 2=Analytic function
    static int EPOCH_NUM = 100;
    static boolean aggressiveDropout = true;
    static double LEARNING_RATE = 0.01;
    static double MOMENTUM_TERM = 0.9;
    static final double TARGET_ACCURACY = .65;
    static double DROPOUT_RATE = 0.3;
    static boolean bestState = true;
    static int trainProtCount = 0, testProtCount = 0, tuneProtCount = 0;
    static int [][] confMatrix = new int[3][3];
    static double [] traintAccurcies = new double[EPOCH_NUM];
    static double [] tuneAccurcies = new double[EPOCH_NUM];
    static PrintWriter trainData = null;
    static PrintWriter tuneData = null;
    static PrintWriter testData = null;

    private static void handleInputFile(String file) {
        Scanner data = null;
        try {
            data = new Scanner(new File(file));
        } catch (FileNotFoundException e) {
            System.err.println("Could not find file '" + file + "'.");
            System.exit(1);
        }
        int proteinNum = 0;
        int aminoNum = 0;
        int lineC =0;
        PrintWriter writer = null;
        PrintWriter train = null;
        PrintWriter test = null;
        PrintWriter tune = null;
        try {
            writer = new PrintWriter("input", "UTF-8");
            train = new PrintWriter("TrainSet", "UTF-8");
            test = new PrintWriter("TestSet", "UTF-8");
            tune = new PrintWriter("TuneSet", "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        while(data.hasNext()){
            String line = data.nextLine();
            if(line.startsWith("#") || line.equals("") || line.equals(" "))
                ;
            else if((line.startsWith("<>") || line.contains("end")) && aminoNum > 0){
                if(data.hasNext()){
                    writer.println();
                }
                aminoNum = 0;
                proteinNum++;
            }
            else {
                if(!line.contains("<>") && !line.contains("end")){
                    writer.print(line + ",");
                    aminoNum++;
                }
            }
        }
        writer.close();

        Scanner input = null;
        try {
            input = new Scanner(new File("input"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int tuneLine = 1, testLine = 1;
        int gl =0;
        //divide protiens into the different sets
        while(input.hasNext()){
            
            if(tuneLine==5){
                tune.println(input.nextLine());
                testLine++;
                tuneLine=0;
                tuneGLCount++;
            }  else if(testLine==6){
                test.println(input.nextLine());
                tuneLine++;
                testLine = 1;
                testGLCount++;

            } else{
                train.println(input.nextLine());
                tuneLine++;
                testLine++;
                trainGLCount++;
            }
            gl++;
        }
        tune.close();
        test.close();
        train.close();
        input.close();


        //count how many protiens in each set
        Scanner trainScnnr = null, tuneScnnr = null, testScnnr = null;
        try {
            trainScnnr = new Scanner(new File("TrainSet"));
            tuneScnnr = new Scanner(new File("TuneSet"));
            testScnnr = new Scanner(new File("TestSet"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while(trainScnnr.hasNext()) {
            String [] splitted = trainScnnr.nextLine().split(",");
            trainProtCount += splitted.length;
        }

        while(testScnnr.hasNext()) {
            String [] splitted = testScnnr.nextLine().split(",");
            testProtCount +=splitted.length;
        }

        while(tuneScnnr.hasNext()) {
            String [] splitted = tuneScnnr.nextLine().split(",");
            tuneProtCount += splitted.length;
        }

        if(protCountDbg) {
            System.out.println("train set protein count: " + trainProtCount);
            System.out.println("tune set protien count: " + tuneProtCount);
            System.out.println("test set protien count: " + testProtCount);
            System.out.println("Total count is:  " +  (trainProtCount + tuneProtCount + testProtCount));
        }

        trainScnnr.close();
        tuneScnnr.close();
        testScnnr.close();

    }

    private static void createANN() {

        if(windowDbg) {
            Scanner windowT = null;
            try {
                windowT = new Scanner(new File("windowT"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Input trainLine = new Input(windowT.nextLine());
            double[][] window = trainLine.getWindow();
            int count = 0;
            while(window!=null){
                for(int i=0; i<window.length;i++){
                    System.out.print(i + " ");
                    for(int j=0; j<window[i].length;j++)
                        System.out.print(window[i][j] + " ");
                    System.out.println();
                }
                count++;
                System.out.println("*****************  " + count + "  ***********");
                window = trainLine.getWindow();
            }
            System.out.println(count);
        }

        inputLayer = new ArrayList<Perceptron>();
        hiddenLayer = new ArrayList<Perceptron>();
        outputLayer = new ArrayList<Perceptron>();

        //create input layer Perceptrons
        inputBias = new Perceptron(inputUnitsNumber,rand,0);
        for(int i=0; i<inputUnitsNumber;i++)
            inputLayer.add(new Perceptron(21,inputBias,rand,function));
        Perceptron.count = 0;

        //create hidden layer
        hiddenBias = new Perceptron(HUNumber,rand,0);
        for(int i=0; i<HUNumber;i++)
            hiddenLayer.add(new Perceptron(inputUnitsNumber,hiddenBias,rand,function));
        Perceptron.count = 0;

        //output layer
        outputBias = new Perceptron(outputNumber,rand,0);
        for(int i=0; i<outputNumber;i++)
            outputLayer.add(new Perceptron(HUNumber,outputBias,rand,1));
        Perceptron.count = 0;

        //incoming for hidden and output layers 
        for(Perceptron curr : hiddenLayer)
            curr.addIncoming(inputLayer);
        for(Perceptron curr: outputLayer)
            curr.addIncoming(hiddenLayer);


        //outComingPerceptrons for input and hidden layer
        for(Perceptron curr: inputLayer)
            curr.addOutcoming(hiddenLayer);
        for(Perceptron curr: hiddenLayer)
            curr.addOutcoming(outputLayer);

    }
    //put the three sets into string arrays
    private static void setDataSets() {

        Scanner trainScnnr = null, tuneScnnr = null, testScnnr = null;
        try {
            trainScnnr = new Scanner(new File("TrainSet"));
            tuneScnnr = new Scanner(new File("TuneSet"));
            testScnnr = new Scanner(new File("TestSet"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        trainSet = new String[trainGLCount];
        tuneSet = new String[tuneGLCount];
        testSet = new String[testGLCount];

        for(int i=0; trainScnnr.hasNextLine(); i++)
            trainSet[i] = trainScnnr.nextLine();
        for(int i=0; tuneScnnr.hasNextLine(); i++)
            tuneSet[i] = tuneScnnr.nextLine();
        for(int i=0; testScnnr.hasNextLine();i++)
            testSet[i] = testScnnr.nextLine();
        
       // shuffleSets(trainSet);
        shuffleSets(tuneSet);
        shuffleSets(testSet);
        
        if(setsDbg) {
            boolean printSet = true;
            System.out.println("************** TrainSet " + trainSet.length + " *****************");
            if(printSet)
                for(String curr : trainSet)
                    System.out.println(curr);
            System.out.println("************** TuneSet " + tuneSet.length + " *****************");
            if(printSet)
                for(String curr : tuneSet)
                    System.out.println(curr);
            System.out.println("************** TestSet " + testSet.length + " *****************");
            if(printSet)
                for(String curr : testSet)
                    System.out.println(curr);
        }

        trainScnnr.close();
        tuneScnnr.close();
        testScnnr.close();


    }

    public static void shuffleSets(String[] array) {

        for(int i=array.length -1;i>0; i--) {
            int index = rand.getNextInt(i+1);

            String a = array[index];
            array[index] = array[i];
            array[i] = a;
        }
    }

    private static void analyzeRun() {

        String aggressive = null;
        String best = null;
        if(aggressiveDropout == true) 
            aggressive = "Yes";
        else 
            aggressive = "No";
        if(bestState == true)
            best = "Yes";
        else
            best = "No";

        String header = "********************************************************************************\n";
        header += "* HUNumber: " + HUNumber + ", Epoch_Num: " + EPOCH_NUM + 
                ", Aggressive_Drop : " + aggressive + ", Learning_Rate: " + LEARNING_RATE + "\n";
        header += "* Momentum_Term: " + MOMENTUM_TERM + ", Dropout_Rate: " 
                + DROPOUT_RATE + ", BestSate: " + best + "\n";
        header += "********************************************************************************";

        trainData.println(header);
        tuneData.println(header);
        testData.println(header);

        int newLine = 0;
        for(double curr : traintAccurcies) {
            if(newLine == 3) {
                trainData.println(curr);
                newLine = 0;
            } else {
                trainData.print(curr + " ");
                newLine++;
            }
        }
        trainData.println();
        newLine = 0;
        for(double curr : tuneAccurcies) {
            if(newLine == 3) {
                tuneData.println(curr);
                newLine = 0;
            } else {
                tuneData.print(curr + " ");
                newLine++;
            }
        }
        tuneData.println();

        printMatrix(testData);
        double matrixAccuracy = (((double)confMatrix[0][0] + confMatrix[1][1] + confMatrix[2][2])/testProtCount);
        double recall_helix = (((double)confMatrix[0][0]) / (confMatrix[0][0] + confMatrix[0][1] + confMatrix[0][2]));
        double recall_coil = (((double)confMatrix[1][1]) / (confMatrix[1][0] + confMatrix[1][1] + confMatrix[1][2]));
        double recall_beta = (((double)confMatrix[2][2]) / (confMatrix[2][0] + confMatrix[2][1] + confMatrix[2][2]));
        double precision_helix = (((double)confMatrix[0][0]) / (confMatrix[0][0] + confMatrix[1][0] + confMatrix[2][0]));
        double precision_coil = (((double)confMatrix[1][1]) / (confMatrix[0][1] + confMatrix[1][1] + confMatrix[2][1]));
        double precision_beta = (((double)confMatrix[2][2]) / (confMatrix[0][2] + confMatrix[1][2] + confMatrix[2][2]));

        testData.println("Accuracy: " + matrixAccuracy + ", wtih error: " + (1 - matrixAccuracy));
        testData.println("Recall for Helix: " + recall_helix + ", with precision: " + precision_helix);
        testData.println("Recall for Coil: " + recall_coil + ", with precision: " + precision_coil);
        testData.println("Recall for Beta: " + recall_beta + ", with precision: " + precision_beta);

        trainData.flush();
        tuneData.flush();
        testData.flush();

    }

    private static void __letsTrain__() {
        try {
            trainData = new PrintWriter("trainData", "UTF-8");
            tuneData = new PrintWriter("tuneData", "UTF-8");
            testData = new PrintWriter("testData", "UTF-8");
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        function = 1;
        for(int i=10; i<1001; i*=10) { //number of HUNumbers
            HUNumber = i;
            for(int j=10; j<1001; j*=10) { //epoch number, 10, 100, 1000
                EPOCH_NUM = j;
                for(int k=0; k<2; k++) { //aggressive dropout, 0 yes, 1 no
                    if(k==0)
                        aggressiveDropout = true;
                    else
                        aggressiveDropout = false;
                    for(int l=0; l<4; l++) { //learning rate, 0=.001, 1=.05, 2=.1, 3=.01
                        switch(l) {
                        case 0:
                            LEARNING_RATE = 0.001;
                            break;
                        case 1:
                            LEARNING_RATE = 0.05;
                            break;
                        case 2:
                            LEARNING_RATE = 0.1;
                            break;
                        case 3:
                            LEARNING_RATE = 0.01;
                            break;
                        }
                        for(double p=0.6; p<1; p+=0.1) { //momentum term, .6, .7, .8, .9
                            MOMENTUM_TERM = p;
                            for(double o=0.1; o<.6; o+=0.1) {//dropout rate, .1, .2, .3, .4, .5
                                DROPOUT_RATE = o;
                                for(int u=0; u<2; u++) { //user best state, 0 yes, 1 no
                                    if(u==0)
                                        bestState = true;
                                    else
                                        bestState = false;
                                    createANN();
                                    //System.out.println("Strating to train...");
                                    letsTrain();
                                    System.out.println("Analyzing run..");
                                    analyzeRun();
                                }
                            }
                        }
                    }
                }
            }
        }
        trainData.close();
        tuneData.close();
        testData.close();
    }
    
    private static void _letsTrain_() {
        System.out.println("Trial 1: ");
        HUNumber = 5;
        LEARNING_RATE = 0.01;
        EPOCH_NUM = 100;
        createANN();
        letsTrain();
        System.out.println("Trial 2:");
        HUNumber = 10;
        LEARNING_RATE = 0.01;
        EPOCH_NUM = 100;
        createANN();
        letsTrain();
        System.out.println("Trail 3:");
        HUNumber = 30;
        LEARNING_RATE = 0.01;
        EPOCH_NUM = 100;
        createANN();
        letsTrain();
        System.out.println("Trail 4:");
        HUNumber = 100;
        LEARNING_RATE = 0.01;
        EPOCH_NUM = 100;
        createANN();
        letsTrain();
    }

    private static void letsTrain() {


        traintAccurcies = new double[EPOCH_NUM];
        tuneAccurcies = new double[EPOCH_NUM];
        double bestAccurcy = 0;


        for(int i=0; i<EPOCH_NUM;i++) {
            
            setDataSets();
            //train set
            for(String currLine : trainSet) {

                Input currProtien = new Input(currLine);
                double[][] window;

                if(!aggressiveDropout)
                    setDropOut(true);

                
                while((window = currProtien.getWindow())!=null) {

                    if(aggressiveDropout)
                        setDropOut(true);

                    int predictedOutput = feedForward(window);
                    int actualOutput = (int) window[8][1];
                    
                    System.out.println(Input.letterIndex(window[8][0]) + " " + Input.outputVal(predictedOutput));
                    
                    backprop(predictedOutput, (int) (actualOutput));
                    //System.out.println("Predicted " + preditctedOutput + " VS. real " + actualOutput);
                    confMatrix[actualOutput][predictedOutput]++;

                    if(aggressiveDropout)
                        setDropOut(false);

                }

                if(!aggressiveDropout)
                    setDropOut(false);
            }

            double currTrainAccurcy = ((double)confMatrix[0][0] + confMatrix[1][1] + confMatrix[2][2])/trainProtCount;
            traintAccurcies[i] = currTrainAccurcy;


            if(trainDbg) {
                System.out.println("accucry for epoch " + i + " is=: " + currTrainAccurcy/100);
                printMatrix();

            }
            confMatrix = new int[3][3];

            //early stopping
            for(String currLine: tuneSet) {

                Input currProtien = new Input(currLine);
                double[][] window;

                while((window = currProtien.getWindow())!=null) {
                    int predictedOutput = feedForward(window);
                    int actualOutput = (int) window[8][1];
                    confMatrix[actualOutput][predictedOutput]++;
                }
            }

            double currTuneAccurcy = (((double)confMatrix[0][0] + confMatrix[1][1] + confMatrix[2][2])/tuneProtCount);
            tuneAccurcies[i] = currTuneAccurcy;
            //System.out.println("tune prot count: " + tuneProtCount + " for this confmatrix values: " + confMatrix[0][0] + " "+ confMatrix[1][1] + " " + confMatrix[2][2] );


            if(currTuneAccurcy > bestAccurcy) {
                //System.out.println("##############FOUNDBETTER############");
                bestAccurcy = currTuneAccurcy;
                saveBestState();

            }
            if(tuneDbg) {
                System.out.println("accucry for epoch " + i + " is=: " + currTuneAccurcy);
                printMatrix();
            }


            confMatrix = new int[3][3];


        }

        System.out.println("==================train accurcies:==================");
        for(double curr : traintAccurcies)
            System.out.println(curr);
        System.out.println("=====================================================");

        System.out.println("*********************tune accurcies:*******************");
        for(double curr: tuneAccurcies)
            System.out.println(curr);
        System.out.println("********************************************************");
        System.out.println("Best accurcy is: " + bestAccurcy);

        
        if(bestState)
            useBestState();

        confMatrix = new int[3][3];

        int testSetC = 0, helixC = 0, coilC = 0, betaC = 0;
        //test set
        for(String currLine: testSet) {

            Input currProtien = new Input(currLine);
            double[][] window;

            while((window = currProtien.getWindow())!=null) {
                testSetC++;
                int predictedOutput = feedForward(window);
                int actualOutput = (int) window[8][1];
                if(actualOutput==0)
                    helixC++;
                else if(actualOutput==1)
                    coilC++;
                else
                    betaC++;
                // System.out.println("feedForward outputs: " + predictedOutput + " VS. actual: " + actualOutput);
                confMatrix[actualOutput][predictedOutput]++;
            }
        }

        double currTestAccurcy = (((double)confMatrix[0][0] + confMatrix[1][1] + confMatrix[2][2])/testSetC);
        printMatrix();

        //System.out.println("final accurcy is " + currTestAccurcy + " for this many C: " + testSetC + " I think this is the total: " + testProtCount);
        //System.out.println("Helix count: " + helixC + " coil count: " + coilC + " beta count: " + betaC);
        
        double recall_helix = (((double)confMatrix[0][0]) / (confMatrix[0][0] + confMatrix[0][1] + confMatrix[0][2]));
        double recall_coil = (((double)confMatrix[1][1]) / (confMatrix[1][0] + confMatrix[1][1] + confMatrix[1][2]));
        double recall_beta = (((double)confMatrix[2][2]) / (confMatrix[2][0] + confMatrix[2][1] + confMatrix[2][2]));
        double precision_helix = (((double)confMatrix[0][0]) / (confMatrix[0][0] + confMatrix[1][0] + confMatrix[2][0]));
        double precision_coil = (((double)confMatrix[1][1]) / (confMatrix[0][1] + confMatrix[1][1] + confMatrix[2][1]));
        double precision_beta = (((double)confMatrix[2][2]) / (confMatrix[0][2] + confMatrix[1][2] + confMatrix[2][2]));

        System.out.println("Accuracy: " + currTestAccurcy + ", wtih error: " + (1 - currTestAccurcy));
        System.out.println("Recall for Helix: " + recall_helix + ", with precision: " + precision_helix);
        System.out.println("Recall for Coil: " + recall_coil + ", with precision: " + precision_coil);
        System.out.println("Recall for Beta: " + recall_beta + ", with precision: " + precision_beta);

    }

    private static void setDropOut(boolean drop) {
        if(drop) {
            for(Perceptron curr: hiddenLayer)
                if(rand.getNextRand()<=DROPOUT_RATE)
                    curr.drop();
        } else {
            for(Perceptron curr: hiddenLayer)
                curr.unDrop();
        }


    }

    private static void useBestState() {
        for(Perceptron curr: inputLayer)
            curr.useBestState();
        for(Perceptron curr: hiddenLayer)
            curr.useBestState();
        for(Perceptron curr: outputLayer)
            curr.useBestState();
    }

    private static void saveBestState() {
        for(Perceptron curr: inputLayer)
            curr.setBestState();
        for(Perceptron curr: hiddenLayer)
            curr.setBestState();
        for(Perceptron curr: outputLayer)
            curr.setBestState();
    }

    private static void printMatrix() {
        System.out.println("Act\\Pre\tHelix\tCoil\tbeta");
        System.out.println("Helix\t"+confMatrix[0][0]+"\t"+confMatrix[0][1]+"\t"+confMatrix[0][2]);
        System.out.println("Coil\t"+confMatrix[1][0]+"\t"+confMatrix[1][1]+"\t"+confMatrix[1][2]);
        System.out.println("Beta\t"+confMatrix[2][0]+"\t"+confMatrix[2][1]+"\t"+confMatrix[2][2]);
    }

    private static void printMatrix(PrintWriter file) {
        file.println("Act\\Pre\tHelix\tCoil\tbeta");
        file.println("Helix\t"+confMatrix[0][0]+"\t"+confMatrix[0][1]+"\t"+confMatrix[0][2]);
        file.println("Coil\t"+confMatrix[1][0]+"\t"+confMatrix[1][1]+"\t"+confMatrix[1][2]);
        file.println("Beta\t"+confMatrix[2][0]+"\t"+confMatrix[2][1]+"\t"+confMatrix[2][2]);
    }

    private static void backprop(int networkOutput, int actualOutput) {
        //maybe normalize errs

        //output Layer
        for(Perceptron currOutputUnit : outputLayer) {

            ArrayList<Perceptron> currIncoming = currOutputUnit.getallIncoming();

            for(int i=0; i<currIncoming.size(); i++) {
                double currOutUnitOutput = currOutputUnit.getOutput();
                Perceptron currIncomingUnit = currIncoming.get(i);
                double currIncomingOutput = currIncomingUnit.getOutput();

                double partialDeriv = -currOutUnitOutput * (1 - currOutUnitOutput) *
                        currIncomingOutput * (actualOutput - currOutUnitOutput);
                //double partialDeriv = -networkOutput * (1 - networkOutput) *
                //        currIncomingOutput * (actualOutput - networkOutput);

                double delta = -LEARNING_RATE * partialDeriv;
                double oldWeight = currOutputUnit.getWeight(i);
                double newWeight = oldWeight + delta;
                currOutputUnit.setDelta(i, delta);
                currOutputUnit.setWeight(i, newWeight + (MOMENTUM_TERM * currOutputUnit.getDelta(i)));
            }
        }
        //hidden Layer
        for(int i=0; i<hiddenLayer.size(); i++) {

            Perceptron currHiddenUnit = hiddenLayer.get(i);
            if(!currHiddenUnit.isDropped()) {
                ArrayList<Perceptron> currIncoming = currHiddenUnit.getallIncoming();
                double currHiddenOutput = currHiddenUnit.getOutput();
                //incoming into hidden 
                for(int j=0; j<currIncoming.size(); j++) {

                    Perceptron currIncomingUnit = currIncoming.get(j);
                    double currIncomingOutput = currIncomingUnit.getOutput();
                    double outputLayerSumOutputs = 0;
                    //output layer
                    for(int k=0; k<outputLayer.size(); k++) {
                        Perceptron currOutputUnit = outputLayer.get(k);
                        double outLayerWeight = currOutputUnit.getWeight(i);
                        double currOutUnitOutput = currOutputUnit.getOutput();
                        outputLayerSumOutputs = outputLayerSumOutputs + 
                                (-(actualOutput - currOutUnitOutput)) * 
                                currOutUnitOutput * (1 - currOutUnitOutput) * 
                                outLayerWeight;
                    }

                    double partialDeriv = currHiddenOutput * (1 - currHiddenOutput) 
                            * currIncomingOutput * outputLayerSumOutputs;
                    //double partialDeriv = networkOutput * (1 - networkOutput) 
                    //        * currIncomingOutput * outputLayerSumOutputs;
                    double delta = -LEARNING_RATE * partialDeriv;
                    double oldWeight = currHiddenUnit.getWeight(j);
                    double newWeight = oldWeight + delta;
                    currHiddenUnit.setDelta(j, delta);
                    currHiddenUnit.setWeight(j, newWeight + (MOMENTUM_TERM * currHiddenUnit.getDelta(j)));
                }
            }
        }
    }

    private static int maxOutputIndex() {
        int maxIndex = 0;
        double maxVal = outputLayer.get(0).getOutput();
        for(int i=0; i<outputLayer.size(); i++)
            if(outputLayer.get(i).getOutput() > maxVal) {
                maxVal = outputLayer.get(i).getOutput();
                maxIndex = i;
            }
        return maxIndex;
    }

    private static int feedForward(double[][] currInput) {

        //feed through input layer
        for(int i=0; i < inputLayer.size(); i++) {
            int currInputPos = 0;
            try {
                currInputPos = (int) currInput[i][0];
            } catch(ArrayIndexOutOfBoundsException e) {
                System.out.println("Int i is: " + i + " and window looks like: ");
                for(double[] curr : currInput) { 
                    for(double elem : curr)
                        System.out.print(elem + " ");
                    System.out.println();
                }

            }
            inputLayer.get(i).activateInput(currInputPos);
        }
        //feed through hidden layer
        for(int i=0; i<hiddenLayer.size(); i++) {
            Perceptron curr = hiddenLayer.get(i);
            curr.activate();
        }
        //feed through output layer
        for(int i=0; i<outputLayer.size(); i++) {
            outputLayer.get(i).activate();
        }

        return maxOutputIndex();
    }

    private static void recordTime(PrintWriter file, String timeName) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println(timeName + " Time: " + dtf.format(now));
        file.println(timeName + "Time: " + dtf.format(now));
    }

    public static void main(String[] args) {

        if(args.length>1){
            System.out.println("Just enter file name");
            System.exit(1);
        }

        PrintWriter timeTrack = null;
        try {
            timeTrack = new PrintWriter("timeTrack", "UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //recordTime(timeTrack, "Start");

        handleInputFile(args[0]);

        // __letsTrain__();
        
        
        //_letsTrain_();
        
        
        createANN();
        letsTrain();
        //recordTime(timeTrack, "End");

        timeTrack.close();
    }

}
