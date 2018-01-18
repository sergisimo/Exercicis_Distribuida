import java.io.IOException;
import java.util.concurrent.Semaphore;

public class LamportMutex {

    private static final int N = 3;

    private DirectClock v;
    private int[] q;
    private int myId;
    private ClientA client;
    private Semaphore semaphore = new Semaphore(1, true);

    LamportMutex(int myId, ClientA client) {

        this.myId = myId;
        this.client = client;
        v = new DirectClock(N, myId);
        q = new int[N];
        for (int i = 0; i < N; i++) q[i] = Integer.MAX_VALUE;
    }

    private void myWait() {

        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void requestCS() {

        System.out.println("REQUEST");
        v.tick();
        q[myId] = v.getValue(myId);
        client.broadcastMessage(q[myId], "request");

        while (!okayCS()) myWait();
    }

    public synchronized void releaseCS() {

        System.out.println("RELEASE");
        q[myId] = Integer.MAX_VALUE;
        client.broadcastMessage(v.getValue(myId), "release");
    }

    private boolean okayCS() {

        for (int j = 0; j < N; j++) {

            if (isGreater(q[myId], myId, q[j], j)) return false;
            if (isGreater(q[myId], myId, v.getValue(j), j)) return false;
        }

        return true;
    }

    private boolean isGreater(int entry1, int pid1, int entry2, int pid2) {

        if (entry2 == Integer.MAX_VALUE) return false;
        return ((entry1 > entry2) || ((entry1 == entry2) && (pid1 > pid2)));
    }

    public synchronized void handleMsg (int timeStamp, int src, String tag) {

        v.receiveAction(src, timeStamp);
        if (tag.equals("request")) {
            q[src] = timeStamp;
            client.directMessage(src, v.getValue(myId), "ack");
        } else if (tag.equals("release")) q[src] = Integer.MAX_VALUE;

        this.notify();
    }
}
