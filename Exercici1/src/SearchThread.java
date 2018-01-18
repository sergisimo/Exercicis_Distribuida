/*
 * Author: Sergi Simó Bosquet - ls30685
 * Date: January 11th 2018
 */

/* ************** IMPORTS ************** */
import java.util.LinkedList;
/* ************************************* */

/**
 * Class that implements the thread that has to search in the half of the array.
 */
public class SearchThread extends Thread {

    private boolean start;
    private int toFind;
    private LinkedList<Integer> list;

    /**
     * SearchThread constructor.
     * @param start True if it is the first half thread, false otherwise.
     * @param toFind Number to be found in the array.
     * @param list LinkedList where to find the number.
     */
    SearchThread (boolean start, int toFind, LinkedList<Integer> list) {

        this.start = start;
        this.toFind = toFind;
        this.list = list;
    }

    /**
     * Main method of the thread that searches the number in the list.
     */
    @Override
    public void run() {

        if (start) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) == toFind) {
                    System.out.println("Thread 1 find the number in the index " + i + ".");
                    break;
                }
            }
        } else {
            for (int i = list.size() - 1; i >= 0; i--) {
                if (list.get(i) == toFind) {
                    System.out.println("Thread 2 find the number in the index " + i + ".");
                    break;
                }
            }
        }
    }
}
