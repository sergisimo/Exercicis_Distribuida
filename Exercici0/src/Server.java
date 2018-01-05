import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

/**
 * Class that implements the Slave and the Master depending on first command line argument.
 */
public class Server {

    /* ********** CONSTANTS ********** */
    private static final int PORT = 5000;
    private static final String IP = "127.0.0.1";
    public static final String READ_REQUEST = "READ";
    public static final String WRITE_REQUEST = "WRITE";
    public static final String TOKEN_REQUEST = "TOKEN_REQUEST";
    public static final String TOKEN_FREE = "TOKEN_FREE";
    public static final String DISCONNECT_REQUEST = "DISCONNECT";

    /* ********** ENUMERATIONS ********** */
    private enum ServerType {

        MASTER,
        SLAVE_READ_WRITE,
        SLAVE_READ
    }

    /* ********** MASTER ATTRIBUTES ********** */
    private ServerType serverType;
    private ServerSocket masterSocket;
    private Socket slaveSocket;
    private int slaveNumber;
    private int number;
    private LinkedList<DedicatedServer> slavesSockets;
    private boolean token;
    private final Semaphore listSemaphore = new Semaphore(1, true);

    /* ********** SLAVE ATTRIBUTES ********** */
    private int identifier;
    private DataOutputStream dataOut;
    private DataInputStream dataIn;

    /* ********** CONSTRUCTOR ********** */
    /**
     * Constructor that initialize the Server depending on the ServerType we are invoking.
     * @param serverType First command line argument that decides the the server type.
     */
    private Server (int serverType) {

        switch (serverType) {
            case 0:
                this.serverType = ServerType.MASTER;
                slaveNumber = 0;
                number = 0;
                slavesSockets = new LinkedList<>();
                token = false;
                try {
                    masterSocket = new ServerSocket(PORT);
                } catch (IOException e) {
                    System.err.println(DedicatedServer.ERROR_CONNECTING_SERVER);
                }
                break;

            case 1:
                this.serverType = ServerType.SLAVE_READ_WRITE;
                token = false;
                break;

            case 2:
                this.serverType = ServerType.SLAVE_READ;
                token = false;
                break;
        }
    }

    /* ********** PUBLIC METHODS ********** */
    /**
     * Method that bind the Master server.
     */
    private void masterListener() {

        DataOutputStream dataOut;
        DedicatedServer dedicatedServer;

        while (true) {

            try {
                if (slaveNumber == 20) break;
                System.out.println("WAITING FOR CONNECTION!");
                slaveSocket = masterSocket.accept();

                dataOut = new DataOutputStream(slaveSocket.getOutputStream());
                dataOut.writeInt(slaveNumber);

                dedicatedServer = new DedicatedServer(this, slaveNumber, slaveSocket);
                listSemaphore.acquire();
                slavesSockets.add(dedicatedServer);
                listSemaphore.release();
                dedicatedServer.start();

                slaveNumber++;
            } catch (IOException | InterruptedException e) {
                System.err.println(DedicatedServer.ERROR_CONNECTING_SERVER);
            }
        }
    }

    /**
     * Method that connects the Slave to the Master.
     */
    private void connectToMaster() {

        try {
            slaveSocket = new Socket(IP, PORT);
            dataIn = new DataInputStream(slaveSocket.getInputStream());
            dataOut = new DataOutputStream(slaveSocket.getOutputStream());

            identifier = dataIn.readInt();
            System.out.println("IDENTIFIER:" + identifier);
        } catch (IOException e) {
            System.err.println(DedicatedServer.ERROR_CONNECTING_SERVER);
        }
    }

    /**
     * Method that implements the Read Only Server.
     */
    private void readServer() {

        int number;

        for (int i = 0; i < 10; i++) {

            //while (!token) token = waitForToken();

            number = read();

            //token = freeToken();

            System.out.println("SERVER READ ONLY nº " + identifier + " JUST READ " + number + ".");

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println(DedicatedServer.ERROR_SLEEP_INTERRUPTED);
            }
        }

        try {
            dataOut.writeUTF(DISCONNECT_REQUEST);
            dataOut.close();
        } catch (IOException e) {
            System.err.println(DedicatedServer.ERROR_SENDING_RESPONSE);
        }
    }

    /**
     * Method that implements the Read/Write Server.
     */
    private void readWriteServer() {

        int number;

        for (int i = 0; i < 10; i++) {

            while (!token) token = waitForToken();

            number = read();

            write(number + 1);

            System.out.println("SERVER READ/WRITE nº " + identifier + " JUST WRITED " + (number+1) + ".");

            freeToken();
            token = false;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println(DedicatedServer.ERROR_SLEEP_INTERRUPTED);
            }
        }

        try {
            dataOut.writeUTF(DISCONNECT_REQUEST);
            dataOut.close();
        } catch (IOException e) {
            System.err.println(DedicatedServer.ERROR_SENDING_RESPONSE);
        }
    }

    /**
     * Method that read the distributed variable.
     * @return int value of the distributed variable.
     */
    private int read () {

        try {

            dataOut.writeUTF(READ_REQUEST);
            return dataIn.readInt();
        } catch (IOException e) {
            System.err.println(DedicatedServer.ERROR_RECEIVING_REQUEST);
            return -1;
        }
    }

    /**
     * Method that write the distributed variable.
     * @param number int value to be written in the distributed variable.
     */
    private void write(int number) {

        try {

            dataOut.writeUTF(WRITE_REQUEST);
            dataOut.writeInt(number);
        } catch (IOException e) {
            System.err.println(DedicatedServer.ERROR_SENDING_RESPONSE);

        }
    }

    /**
     * Method that waits until the Master gives the token to the slave.
     * @return true if the slave has received the token, false otherwise.
     */
    private boolean waitForToken () {

        String response;

        try {

            dataOut.writeUTF(TOKEN_REQUEST);
            response = dataIn.readUTF();
            return response.equals(TOKEN_REQUEST);
        } catch (IOException e) {
            System.err.println(DedicatedServer.ERROR_SENDING_TOKEN);
            return false;
        }
    }

    /**
     * Method that communicates the Master that the slaves returns the token.
     */
    private void freeToken() {

        try {

            dataOut.writeUTF(TOKEN_FREE);

        } catch (IOException e) {
            System.err.println(DedicatedServer.ERROR_SENDING_TOKEN);
        }
    }

    /* ********** GETTERS ********** */
    /**
     * serverType Getter.
     * @return ServerType
     */
    private ServerType getServerType() {

        return serverType;
    }

    /**
     * number Getter.
     * @return int
     */
    public int getNumber() {

        return number;
    }

    /**
     * slavesSockets Getter.
     * @return LinkedList<DedicatedServer>
     */
    public LinkedList<DedicatedServer> getSlavesSockets() {

        return slavesSockets;
    }

    /**
     * listSemaphore Getter.
     * @return Semaphore
     */
    public Semaphore getListSemaphore() {

        return listSemaphore;
    }

    /**
     * token Getter.
     * @return boolean
     */
    public boolean isToken() {

        return token;
    }

    /* ********** SETTERS ********** */
    /**
     * number Setter.
     * @param number int
     */
    public void setNumber(int number) {

        this.number = number;
    }

    /**
     * token Setter.
     * @param token boolean
     */
    public void setToken(boolean token) {

        this.token = token;
    }

    /* ********** MAIN ********** */
    /**
     * Main function of the program.
     * @param args Command line arguments.
     *             arg[0] -> Type of Server
     *                    0 -> Master
     *                    1 -> Read/Write Slave
     *                    2 -> Read Only Slave
     */
    public static void main(String[] args) {

        Server server = new Server(Integer.parseInt(args[0]));

        switch (server.getServerType()) {

            case MASTER:
                server.masterListener();
                break;

            case SLAVE_READ:
                server.connectToMaster();
                server.readServer();
                break;

            case SLAVE_READ_WRITE:
                server.connectToMaster();
                server.readWriteServer();
                break;
        }
    }
}
