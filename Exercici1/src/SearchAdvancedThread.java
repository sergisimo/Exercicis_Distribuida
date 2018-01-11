public class SearchAdvancedThread extends Thread {

    private int start;
    private int end;
    private int[] array;
    private int toFind;
    private int indexOfNumber;

    SearchAdvancedThread (int start, int end, int[] array, int toFind) {

        this.start = start;
        this.end = end;
        this.array = array;
        //this.array = new int[array.length];
        //System.arraycopy(array, 0, this.array, 0, array.length);
        this.toFind = toFind;
        this.indexOfNumber = -1;
    }

    @Override
    public void run() {

        for (int i = start; i < end && i < array.length; i++) {

            if (array[i] == toFind) {
                indexOfNumber = i;
                break;
            }
        }
    }

    public int getIndexOfNumber() {

        return this.indexOfNumber;
    }
}
