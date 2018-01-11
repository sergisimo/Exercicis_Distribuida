import java.util.Arrays;

public class MergeThread extends Thread {

    private int[] array;

    MergeThread (int[] array) {

        this.array = array;
    }

    @Override
    public void run() {

        if (array.length > 1) {
            int[] left = Arrays.copyOfRange(array, 0, array.length / 2);
            int[] right = Arrays.copyOfRange(array, array.length / 2, array.length);

            MergeThread lThread = new MergeThread(left);
            MergeThread rThread = new MergeThread(right);
            lThread.start();
            rThread.start();

            try {
                lThread.join();
                rThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // merge them back together
            merge(left, right, array);
        }
    }

    public static void mergeSort(int[] a) {

        if (a.length > 1) {

            int[] left  = Arrays.copyOfRange(a, 0, a.length / 2);
            int[] right = Arrays.copyOfRange(a, a.length / 2, a.length);

            mergeSort(left);
            mergeSort(right);

            merge(left, right, a);
        }
    }

    private static void merge(int[] left, int[] right, int[] a) {

        int i1 = 0;
        int i2 = 0;

        for (int i = 0; i < a.length; i++) {
            if (i2 >= right.length || (i1 < left.length && left[i1] < right[i2])) {
                a[i] = left[i1];
                i1++;
            } else {
                a[i] = right[i2];
                i2++;
            }
        }
    }
}
