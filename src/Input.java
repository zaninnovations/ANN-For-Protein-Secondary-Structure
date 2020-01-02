
public class Input {

    final static int WINDOW_SZ = 17;
    String input;
    int[][] line;
    double[][] window = new double[WINDOW_SZ][2];
    int targetIndex;
    int frontWindow;

    public Input(String strLine){
        input = strLine;
        targetIndex = -8;

        String[] splitted = input.split(",");
        line = new int[splitted.length][2];

        //get the elements and actual labels
        for(int i=0; i<splitted.length;i++){
            String[] elem = splitted[i].split(" ");
            line[i][0] = letterIndex(elem[0]);

            if(line[i][0]==-1)
                System.err.println("Unknown Amino Acid");

            line[i][1] = outputVal(elem[1]);
            if(line[i][1]==-1)
                System.err.println("Uknown 2nd structure");
        }

        frontWindow = 8;
    }


    public double[][] getWindow(){

        if(targetIndex + 8 == line.length){
            return null;
        }

        int index = 0;
        int temp;

        //front padding
        if(frontWindow>0 && targetIndex<line.length) {
            temp = frontWindow;
            while(temp>0) {
                window[index][0] = 20;
                window[index][1] = 3;
                temp--;
                index++;
            }
            frontWindow--;
        }

        int tempLine; 
        if(targetIndex>=0)
            tempLine = targetIndex;
        else
            tempLine = 0;
        //fill in the proteins
        while(index<window.length && tempLine!=line.length) {
            window[index][0] = line[tempLine][0];
            window[index][1] = line[tempLine][1];
            tempLine++;
            index++;
        }

        //fill rear padding
        if (index<window.length) {
            temp = 0;
            while(temp<8 && index<window.length) {
                window[index][0] = 20;
                window[index][1] = 3;
                temp++;
                index++;
            }
        }

        targetIndex++;
        return window;

    }


    int letterIndex(String elem){
        switch(elem){
        case "A":
            return 0;
        case "C":
            return 1;
        case "D":
            return 2;
        case "E":
            return 3;
        case "F":
            return 4;
        case "G":
            return 5;
        case "H":
            return 6;
        case "I":
            return 7;
        case "K":
            return 8;
        case "L":
            return 9;
        case "M":
            return 10;
        case "N":
            return 11;
        case "P":
            return 12;
        case "Q":
            return 13;
        case "R":
            return 14;
        case "S":
            return 15;
        case "T":
            return 16;
        case "V":
            return 17;
        case "W":
            return 18;
        case "Y":
            return 19;
        default:
            return -1;
        }
    }
    
    public static String letterIndex(double elem){
        switch((int)elem){
        case 0:
            return "A";
        case 1:
            return "C";
        case 2:
            return "D";
        case 3:
            return "E";
        case 4:
            return "F";
        case 5:
            return "G";
        case 6:
            return "H";
        case 7:
            return "I";
        case 8:
            return "K";
        case 9:
            return "L";
        case 10:
            return "M";
        case 11:
            return "N";
        case 12:
            return "P";
        case 13:
            return "Q";
        case 14:
            return "R";
        case 15:
            return "S";
        case 16:
            return "T";
        case 17:
            return "V";
        case 18:
            return "W";
        case 19:
            return "Y";
        default:
            return "NaN";
        }
    }

    int outputVal(String output){
        switch(output){
        case "h":
            return 0;
        case "_":
            return 1;
        case "e":
            return 2;
        default:
            return -1;
        }
    }
    
    public static String outputVal(int prediction) {
        switch(prediction) {
        case 0:
            return "h";
        case 1:
            return "_";
        case 2:
            return "e";
        default:
            return "NaN";
        }
    }
}
