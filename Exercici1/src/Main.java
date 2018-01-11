import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

public class Main {

    private static final int[] INT_ARRAY = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };
    //private static int[] arrayToMerge = { 10, 23, 1, 3, 11, 6, 32, 25, 22, 101, 8, 14, 26, 44, 50, 12, 17, 16, 43, 27 };

    public static void main(String[] args) {

        long start, end;
        int[] arrayToMergeAux = createRandomArray(1000);

        switch (Integer.parseInt(args[0])) {

            case 0:
                /* ************** EXERCICI 1.2 ************** */

                LinkedList<Integer> list = new LinkedList<>();
                SearchThread thread1, thread2;

                for (int integer: INT_ARRAY) list.add(integer);

                thread1 = new SearchThread(true, 8, list);
                thread2 = new SearchThread(false, 8, list);

                thread1.start();
                thread2.start();
                break;

            case 1:
                /* ************** EXERCICI 1.3 ************** */

                System.out.println("INDEX OF THE NUMBER 102: " + cercaParellela(102, INT_ARRAY, 24));
                System.out.println("INDEX OF THE NUMBER 10: " + cercaParellela(10, INT_ARRAY, 1));
                System.out.println("INDEX OF THE NUMBER 14: " + cercaParellela(14, INT_ARRAY, 7));
                System.out.println("INDEX OF THE NUMBER 1: " + cercaParellela(1, INT_ARRAY, 13));
                System.out.println("INDEX OF THE NUMBER 18: " + cercaParellela(18, INT_ARRAY, 19));
                System.out.println("INDEX OF THE NUMBER 20: " + cercaParellela(20, INT_ARRAY, 20));
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

            default:

                break;
        }
    }

    private static int cercaParellela (int aBuscar, int[] array, int numThreads) {

        SearchAdvancedThread[] threads;
        int increment, nextStartPosition = 0, nextEndPosition;
        boolean decremented = false;

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

            threads[i] = new SearchAdvancedThread(nextStartPosition, nextEndPosition, array, aBuscar);
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
        }

        return -1;
    }

    private static int[] createRandomArray(int length) {

        int[] array = new int[length];
        Random rand = new Random();
        for (int i = 0; i < array.length; i++) array[i] = rand.nextInt(length * 10);

        return array;
    }
}
