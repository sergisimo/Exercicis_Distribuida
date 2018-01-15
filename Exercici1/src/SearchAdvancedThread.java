/*
 * Author: Sergi Simó Bosquet - ls30685
 * Date: January 11th 2018
 */

/**
 * Class that implements a multithreated search in an array.
 */
public class SearchAdvancedThread extends Thread {

    private int start;
    private int end;
    private int[] array;
    private int toFind;
    private int indexOfNumber;

    /**
     * Constructor of the searcher.
     * @param start Where the thread has to start searching.
     * @param end Where the thread has to finish searching.
     * @param array Array where the thread has to search.
     * @param toFind Number that has to be find.
     */
    SearchAdvancedThread (int start, int end, int[] array, int toFind) {

        this.start = start;
        this.end = end;
        this.array = array;
        this.toFind = toFind;
        this.indexOfNumber = -1;
    }

    /**
     * Method that search the number in the array.
     */
    @Override
    public void run() {

        for (int i = start; i < end && i < array.length; i++) {

            if (array[i] == toFind) {
                indexOfNumber = i;
                break;
            }
        }
    }

    /**
     * Getter of IndexOfNumber
     * @return The index where the array found the number.
     */
    public int getIndexOfNumber() {

        return this.indexOfNumber;
    }
}
