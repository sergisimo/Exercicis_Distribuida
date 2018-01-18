import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class ClientB {

    private static final int PORT = 5002;
    private static final int N = 2;
    private static final String IP = "127.0.0.1";

    private DataOutputStream dataOut;
    private int myId;
    private RAMutex mutex;
    private Semaphore semaphore = new Semaphore(1, true);

    private ClientB (int id) {

        this.myId = id;
        mutex = new RAMutex(myId, this);
        Socket socket;
        HandleMessageBThread thread;
        DataInputStream dataIn;

        try {

            socket = new Socket(IP, PORT);
            dataOut = new DataOutputStream(socket.getOutputStream());
            dataOut.writeInt(id);
            dataIn = new DataInputStream(socket.getInputStream());
            dataIn.readUTF();
            System.out.println("GO!");
            thread = new HandleMessageBThread(dataIn, mutex);
            thread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendToScreen() {

        try {
            semaphore.acquire();
            dataOut.writeUTF("print");
            dataOut.writeUTF("Sóc el procés lightweight B" + (myId + 1));
            semaphore.release();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void broadcastMessage(int timeStamp, String tag) {

        try {
            semaphore.acquire();
            dataOut.writeUTF("broadcast");
            dataOut.writeInt(timeStamp);
            dataOut.writeUTF(tag);
            semaphore.release();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void directMessage(int destination, int timeStamp, String tag) {

        try {
            semaphore.acquire();
            System.out.println(destination + " - " + timeStamp + " - " + tag);
            dataOut.writeUTF("message");
            dataOut.writeInt(destination);
            dataOut.writeInt(timeStamp);
            dataOut.writeUTF(tag);
            semaphore.release();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void requestToken() {

        if (myId == 0) {
            try {
                semaphore.acquire();
                dataOut.writeUTF("request");
                semaphore.release();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void releaseToken() {

        if (myId == 1) {
            try {
                semaphore.acquire();
                dataOut.writeUTF("release");
                semaphore.release();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private RAMutex getMutex() {

        return mutex;
    }

    public static void main (String[] args) {

        ClientB process = new ClientB(Integer.parseInt(args[0]));

        while (true) {

            process.getMutex().requestCS();
            process.requestToken();

            for (int i = 0; i < 10; i++) {
                process.sendToScreen();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            process.getMutex().releaseCS();
            process.releaseToken();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class HandleMessageBThread extends Thread {

    private DataInputStream dataIn;
    private RAMutex raMutex;

    HandleMessageBThread(DataInputStream dataIn, RAMutex raMutex) {

        this.dataIn = dataIn;
        this.raMutex = raMutex;
    }

    @Override
    public void run() {

        int src;
        int timeStamp;
        String tag;

        while (true) {

            try {

                src = dataIn.readInt();
                timeStamp = dataIn.readInt();
                tag = dataIn.readUTF();

                raMutex.handleMsg(src, timeStamp, tag);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
