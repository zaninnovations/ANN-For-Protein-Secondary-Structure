import java.util.*;

public class Rand {
        private static final long seed = 638*638;
        private static Random randObj = null;
        
        public Rand() {
            randObj = new Random(seed);
        }
        
        public double getNextRand() {
            return (randObj.nextDouble() * 2) - 1;
        }
        
        public int getNextInt(int index) {
            return randObj.nextInt(index);
        }
        
}
