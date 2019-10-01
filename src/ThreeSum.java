import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.io.*;

public class ThreeSum {

    static ThreadMXBean bean = ManagementFactory.getThreadMXBean( );

    /* define constants */
    static long MAXVALUE =  200000000;
    static long MINVALUE = -200000000;
    static int numberOfTrials = 100;
    static int MAXINPUTSIZE  = (int) Math.pow(2,12);
    static int MININPUTSIZE  =  1;
    // static int SIZEINCREMENT =  10000000; // not using this since we are doubling the size each time

    static String ResultsFolderPath = "/home/sethowens/Results/"; // pathname to results folder
    static FileWriter resultsFile;
    static PrintWriter resultsWriter;


    public static void main(String[] args) {

        // run the whole experiment at least twice, and expect to throw away the data from the earlier runs, before java has fully optimized
        System.out.println("Running first full experiment...");
        runFullExperiment("ThreeSumShort-Exp1-ThrowAway.txt");
        System.out.println("Running second full experiment...");
        runFullExperiment("ThreeSumShort-Exp2.txt");
        System.out.println("Running third full experiment...");
        runFullExperiment("ThreeSumShort-Exp3.txt");
    }

    static void runFullExperiment(String resultsFileName){

        testThreeSum();
        try {
            resultsFile = new FileWriter(ResultsFolderPath + resultsFileName);
            resultsWriter = new PrintWriter(resultsFile);
        } catch(Exception e) {
            System.out.println("*****!!!!!  Had a problem opening the results file "+ResultsFolderPath+resultsFileName);
            return; // not very foolproof... but we do expect to be able to create/open the file...
        }

        ThreadCpuStopWatch BatchStopwatch = new ThreadCpuStopWatch(); // for timing an entire set of trials
        ThreadCpuStopWatch TrialStopwatch = new ThreadCpuStopWatch(); // for timing an individual trial

        resultsWriter.println("#InputSize    AverageTime"); // # marks a comment in gnuplot data
        resultsWriter.flush();
        /* for each size of input we want to test: in this case starting small and doubling the size each time */
        for(int inputSize=MININPUTSIZE;inputSize<=MAXINPUTSIZE; inputSize*=2) {
            // progress message...
            System.out.println("Running test for input size "+inputSize+" ... ");

            /* repeat for desired number of trials (for a specific size of input)... */
            long batchElapsedTime = 0;
            // generate a list of randomly spaced integers in ascending sorted order to use as test input
            // In this case we're generating one list to use for the entire set of trials (of a given input size)
            // but we will randomly generate the search key for each trial
            System.out.print("    Generating test data...");
            long[] testList = createAscendingList(inputSize);
            System.out.println("...done.");
            System.out.print("    Running trial batch...");

            /* force garbage collection before each batch of trials run so it is not included in the time */
            System.gc();


            // instead of timing each individual trial, we will time the entire set of trials (for a given input size)
            // and divide by the number of trials -- this reduces the impact of the amount of time it takes to call the
            // stopwatch methods themselves
            BatchStopwatch.start(); // comment this line if timing trials individually

            // run the trials
            for (long trial = 0; trial < numberOfTrials; trial++) {

                /* force garbage collection before each trial run so it is not included in the time */
                System.gc();

                //TrialStopwatch.start(); // *** uncomment this line if timing trials individually
                /* run the function we're testing on the trial input */
                threeSumShort(testList);
                // batchElapsedTime = batchElapsedTime + TrialStopwatch.elapsedTime(); // *** uncomment this line if timing trials individually
            }
            batchElapsedTime = BatchStopwatch.elapsedTime(); // *** comment this line if timing trials individually
            double averageTimePerTrialInBatch = (double) batchElapsedTime / (double)numberOfTrials; // calculate the average time per trial in this batch

            /* print data for this size of input */
            resultsWriter.printf("%12d  %15.2f \n",inputSize, averageTimePerTrialInBatch); // might as well make the columns look nice
            resultsWriter.flush();
            System.out.println(" ....done.");
        }
    }

    //This is the one we built in class
    public static int binarySearch(long key, long[] list) {
        int i = 0;
        int j= list.length-1;
        if (list[i] == key) return i;
        if (list[j] == key) return j;
        int k = (i+j)/2;
        while(j-i > 1){
            //resultsWriter.printf("%d %d %d %d %d %d\n",i,k,j, list[0], key, list[list.length-1]); resultsWriter.flush();
            if (list[k]== key) return k;
            else if (list[k] < key) i=k;
            else j=k;
            k=(i+j)/2;
        }
        return -1;
    }

    public static int threeSumLong(long[] list){
        //gets length of list
        int len = list.length;
        //Counts number of threes
        int threes = 0;
        //for each value we have to add it to every other value and then add it to every other value O(n^3)
        for(int iii = 0; iii < len; iii++){
            for(int jjj = iii + 1; jjj < len; jjj++){
                for(int hhh = jjj + 1; hhh < len; hhh++){
                    if(list[iii] + list[jjj] +list[hhh] == 0){
                        threes++;
                    }
                }
            }
        }
        return threes;
    }

    public static int threeSumMed(long[] list){
        //gets length of list
        int len = list.length;
        int threes  = 0;
        //for each value we add it to every other value and then binary search for the opposite
        for(int iii = 0; iii <  len - 1; iii++){
            for (int jjj = iii + 1; jjj < len; jjj++){
                //Binary search for the opposite
                int binSearch = binarySearch(-(list[iii] + list[jjj]), list);
                //The value exists and we haven't checked it already.
                if(binSearch != -1 && binSearch > jjj){
                    threes++;
                }
            }
        }
        return threes;
    }

    public static int threeSumShort(long[] list){
        //gets length of list
        int len = list.length;
        int threes = 0;
        //We only need to go to len - 1 since we check the last value every time.
        for(int iii = 0; iii < len - 1; iii++){
            int firstIndex = iii + 1; //We've checked the values before
            int lastIndex = len - 1; //Start at the last value;
            long firstValue = list[iii]; //The value to check

            while (firstIndex < lastIndex){
                //These two will slowly work towards each other
                long secondValue = list[firstIndex];
                long thirdValue = list[lastIndex];

                //Check to see if the sum is zero
                if (firstValue + secondValue + thirdValue == 0){
                    //If zero, both second and third can move, since they won't match with any other values and first
                    firstIndex++;
                    lastIndex--;
                    //Increment threes
                    threes++;
                }
                else if (firstValue + secondValue +thirdValue > 0){
                    //In this case, the last value is too great and needs to shrink
                    lastIndex--;
                }
                else{
                    //In this case, the first value is too small and needs to grow
                    firstIndex++;
                }
            }
        }
        return threes;
    }

    public static void testThreeSum(){
        long[] list = {-5, -3, 0, 2, 3, 4, 5};
        long[] listB = {2, 3, 4};

        if(threeSumLong(list) != 3 || threeSumMed(list) != 3 || threeSumShort(list) != 3){
            System.out.println("Did not read accurate value."); //first list should have 3
            return;
        }
        if (threeSumLong(listB) != 0 || threeSumMed(listB) != 0 || threeSumShort(listB) != 0){
            System.out.println("Read threes when none existed."); //Should have none
            return;
        }
        System.out.println("ThreeSum working correctly.");
        return;
    }

    public static long[] createAscendingList(long size){
        long[] newList = new long[(int)size];
        long startValue = (long)(MAXVALUE + Math.random() * (MAXVALUE - MINVALUE));
        newList[0] = startValue;
        for(int i = 1; i < size; i++){
            //Adds a small random number to each to create sorted list with no repeats
            newList[i] = newList[i-1] + (long)(Math.random() * 10 + 1);
        }
        return newList;
    }

    public static long getCpuTime(){
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        return bean.isCurrentThreadCpuTimeSupported() ?
                bean.getCurrentThreadCpuTime() : 0L;
    }
}