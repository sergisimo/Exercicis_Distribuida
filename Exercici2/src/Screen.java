import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Screen {

    private static final int PORT = 5003;

    private ServerSocket serverSocket;

    private Screen() {

        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void waitForClients() {

        Socket client;
        boolean exit = false;
        ScreenThread screenThread;

        while (!exit) {

            try {

                client = serverSocket.accept();
                screenThread = new ScreenThread(new DataInputStream(client.getInputStream()));
                screenThread.start();
            } catch (IOException e) {
                exit = true;
            }
        }
    }

    public static void main(String[] args) {

        Screen screen = new Screen();

        screen.waitForClients();
    }
}

class ScreenThread extends Thread {

    private DataInputStream client;

    ScreenThread (DataInputStream client) {

        this.client = client;
    }

    @Override
    public void run() {

        boolean exit = false;
        String response;

        while (!exit) {

            try {
                response = client.readUTF();
                if (response.equals("EXIT")) exit = true;
                else System.out.println(response);
            } catch (IOException e) {
                exit = true;
            }
        }
    }
}
