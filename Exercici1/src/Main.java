/*
 * Author: Sergi Simó Bosquet - ls30685
 * Date: January 11th 2018
 */

/* ************** IMPORTS ************** */
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
/* ************************************* */

/**
 * Class that implements the Chapter 1 Exercises of Distributed Systems Subject.
 */
public class Main {

    private static final int[] INT_ARRAY = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };
    //private static int[] arrayToMerge = { 10, 23, 1, 3, 11, 6, 32, 25, 22, 101, 8, 14, 26, 44, 50, 12, 17, 16, 43, 27 };

    /**
     * Main function that test the different exercises depending on the arg[0] received.
     * @param args
     *          arg[0]: int with the number that match with the exercicse you want to execute.
     *             0 -> Exercise 1.2
     *             1 -> Exercise 1.3
     *             2 -> Exercise 1.6
     *             3 -> Exercise 1.7
     */
    public static void main(String[] args) {

        long start, end;
        int[] arrayToMergeAux = createRandomArray(1000);

        switch (Integer.parseInt(args[0])) {

            case 0:
                /* ************** EXERCICI 1.2 ************** */

                LinkedList<Integer> list = new LinkedList<>();
                SearchThread thread1, thread2;

                for (int i = 0; i < 1000; i++) list.add(i);

                thread1 = new SearchThread(true, 990, list);
                thread2 = new SearchThread(false, 990, list);

                thread1.start();
                thread2.start();
                break;

            case 1:
            case 4:
                /* ************** EXERCICI 1.3 ************** */
                int [] array = new int[1004];
                int exercise = Integer.parseInt(args[0]);
                for (int i = 0; i < 1004; i++) array[i] = i;
                System.out.println("INDEX OF THE NUMBER 1003: " + parallelSearch(1003, array, 10, exercise));
                System.out.println("INDEX OF THE NUMBER 10: " + parallelSearch(10, INT_ARRAY, 1, exercise));
                System.out.println("INDEX OF THE NUMBER 14: " + parallelSearch(14, INT_ARRAY, 7, exercise));
                System.out.println("INDEX OF THE NUMBER 1: " + parallelSearch(1, INT_ARRAY, 13, exercise));
                System.out.println("INDEX OF THE NUMBER 18: " + parallelSearch(18, INT_ARRAY, 19, exercise));
                System.out.println("INDEX OF THE NUMBER 20: " + parallelSearch(20, INT_ARRAY, 20, exercise));
                break;

            case 2:
                /* ************** EXERCICI 1.6 ************** */
                System.out.println("Array to merge:");
                System.out.println(Arrays.toString(arrayToMergeAux));
                MergeThread thread = new MergeThread(arrayToMergeAux);
                start = System.currentTimeMillis();

                thread.start();

                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                end = System.currentTimeMillis();

                System.out.println("Merged Array:");
                System.out.println(Arrays.toString(arrayToMergeAux));
                System.out.println("Execution Time: " + (end - start) + " ms.");

                break;

            case 3:
                /* ************** EXERCICI 1.7 ************** */
                System.out.println("Array to merge:");
                System.out.println(Arrays.toString(arrayToMergeAux));

                start = System.currentTimeMillis();
                MergeThread.mergeSort(arrayToMergeAux);
                end = System.currentTimeMillis();

                System.out.println("Merged Array:");
                System.out.println(Arrays.toString(arrayToMergeAux));
                System.out.println("Execution Time: " + (end - start) + " ms.");
                break;
        }
    }

    /**
     * Function that search the index of the array where is the value we are searching.
     * @param toSearch Number to search inside the array.
     * @param array Array where to find the number.
     * @param numThreads Number of threads that have to search the number.
     * @return Index of the array where the toSearch number is situated, -1 otherwise.
     */
    private static int parallelSearch (int toSearch, int[] array, int numThreads, int exercise) {

        SearchAdvancedThread[] threads;
        int increment, nextStartPosition = 0, nextEndPosition;
        boolean decremented = false;
        int j = 0;

        if (numThreads <= 0) {
            System.err.println("Error! The number of threads has to be positive.");
            return -1;
        }
        if (array.length == 0) {

            System.err.println("Error! The minimum array length has to be 1.");
            return -1;
        }

        if (numThreads > array.length) numThreads = array.length;

        increment = array.length / numThreads;

        if (array.length % numThreads != 0 ) increment++;
        nextEndPosition = increment;

        threads = new SearchAdvancedThread[numThreads];

        for (int i = 0; i < numThreads; i++) {

            threads[i] = new SearchAdvancedThread(nextStartPosition, nextEndPosition, array, toSearch, exercise);
            threads[i].start();
            nextStartPosition += increment;
            if (((numThreads - i - 1) * (increment - 1)) == array.length - nextEndPosition && !decremented && increment != 1) {
                increment--;
                decremented = true;
            }
            nextEndPosition += increment;
        }

        for (SearchAdvancedThread thread: threads) {

            try {
                thread.join();
                if (thread.getIndexOfNumber() != -1) return thread.getIndexOfNumber();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            j++;
        }

        return -1;
    }

    /**
     * Function that creates a random array of the length passed by parameter.
     * @param length Length that will have the new array.
     * @return The array created full with random numbers that go from 0 to length * 10.
     */
    private static int[] createRandomArray(int length) {

        int[] array = new int[length];
        Random rand = new Random();
        for (int i = 0; i < array.length; i++) array[i] = rand.nextInt(length * 10);

        return array;
    }
}
