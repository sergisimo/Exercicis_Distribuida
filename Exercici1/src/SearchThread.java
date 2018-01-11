import java.util.LinkedList;

public class SearchThread extends Thread {

    private boolean start;
    private int toFind;
    private LinkedList<Integer> list;

    public SearchThread (boolean start, int toFind, LinkedList<Integer> list) {

        this.start = start;
        this.toFind = toFind;
        this.list = list;
    }

    @Override
    public void run() {

        if (start) {
            for (int i = 0; i < list.size() / 2; i++) {
                if (list.get(i) == toFind) {
                    System.out.println("TROBAT THREAD 1");
                    break;
                }
            }
        } else {
            for (int i = list.size() - 1; i >= list.size() / 2; i--) {
                if (list.get(i) == toFind) {
                    System.out.println("TROBAT THREAD 2");
                    break;
                }
            }
        }
    }
}
