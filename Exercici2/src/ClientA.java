import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class ClientA {

    private static final int PORT = 5001;
    private static final int TOKEN_PORT = 5004;
    private static final int N = 3;
    private static final String IP = "127.0.0.1";

    private DataOutputStream dataOut;
    private int myId;
    private LamportMutex mutex;
    private boolean firstTime = true;
    private Semaphore semaphore = new Semaphore(1, true);

    private ClientA (int id) {

        this.myId = id;
        mutex = new LamportMutex(myId, this);
        Socket socket;
        HandleMessageThread thread;
        DataInputStream dataIn;

        try {

            socket = new Socket(IP, PORT);
            dataOut = new DataOutputStream(socket.getOutputStream());
            dataOut.writeInt(id);
            dataIn = new DataInputStream(socket.getInputStream());
            dataIn.readUTF();
            System.out.println("GO!");
            thread = new HandleMessageThread(dataIn, mutex);
            thread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendToScreen() {

        try {
            semaphore.acquire();
            dataOut.writeUTF("print");
            dataOut.writeUTF("Sóc el procés lightweight A" + (myId + 1));
            semaphore.release();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void broadcastMessage(int timeStamp, String tag) {

        try {
            semaphore.acquire();
            System.out.println("BROADCAST - " + timeStamp + " - " + tag);
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

        Socket socket;
        DataOutputStream dataO;
        DataInputStream dataI;

        if (myId == 0 && !firstTime) {
            try {
                socket = new Socket("localhost", TOKEN_PORT);

                dataO = new DataOutputStream(socket.getOutputStream());
                dataI = new DataInputStream(socket.getInputStream());
                dataO.writeUTF("request");
                dataI.readUTF();

                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        firstTime = false;
    }

    private void releaseToken() {

        Socket socket;
        DataOutputStream dataO;

        if (myId == 2) {
            try {

                socket = new Socket("localhost", TOKEN_PORT);

                dataO = new DataOutputStream(socket.getOutputStream());
                dataO.writeUTF("release");

                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private LamportMutex getMutex() {

        return mutex;
    }

    public static void main (String[] args) {

        ClientA process = new ClientA(Integer.parseInt(args[0]));

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

            process.releaseToken();
            process.getMutex().releaseCS();


            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class HandleMessageThread extends Thread {

    private DataInputStream dataIn;
    private LamportMutex lamportMutex;

    HandleMessageThread (DataInputStream dataIn, LamportMutex lamportMutex) {

        this.dataIn = dataIn;
        this.lamportMutex = lamportMutex;
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

                lamportMutex.handleMsg(timeStamp, src, tag);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}