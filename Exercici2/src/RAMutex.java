import java.util.LinkedList;

public class RAMutex {

    private int myts;
    private int myId;
    private LamportClock c = new LamportClock();
    private LinkedList<Integer> pendingQ = new LinkedList<>();
    private int numOkay = 0;
    private ClientB client;

    public RAMutex (int id, ClientB client) {

        this.client = client;
        this.myId = id;
        myts = Integer.MAX_VALUE;
    }

    private void myWait() {

        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void requestCS() {

        c.tick();
        myts = c.getValue();
        client.broadcastMessage(myts, "request");
        numOkay = 0;
        while (numOkay < 1) myWait();
    }

    public synchronized void releaseCS () {

        myts = Integer.MAX_VALUE;
        while (!pendingQ.isEmpty()) {
            int pid = pendingQ.removeFirst();
            client.directMessage(pid, c.getValue(), "okay");
        }
    }

    public synchronized void handleMsg(int src, int timeStamp, String tag) {

        c.receiveAction(src, timeStamp);
        if (tag.equals("request")) {

            if ((myts == Integer.MAX_VALUE) || (timeStamp < myts) || ((timeStamp == myts) && (src < myId)))
                client.directMessage(src, c.getValue(), "okay");
            else pendingQ.add(src);
        } else if (tag.equals("okay")) {
            numOkay++;
            if (numOkay == 1) notify();
        }
    }
}
