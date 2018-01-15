/*
 * Author: Sergi Simó Bosquet - ls30685
 * Date: January 8th 2018
 */

/* ************** IMPORTS ************** */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
/* ************************************* */

/**
 * Implement de dedicated server in order to attend slaves.
 */
public class DedicatedServer extends Thread {

    /* ********** CONSTANTS ********** */
    public static final String ERROR_SENDING_TOKEN = "Error 001. The token has not been sent.";
    public static final String ERROR_RECEIVING_REQUEST = "Error 002. The request has not been received.";
    public static final String ERROR_SENDING_RESPONSE = "Error 003. The response has not been send.";
    public static final String ERROR_OPENING_STREAMS = "Error 004. The streams can not be opened.";
    public static final String ERROR_SEMAPHORE_BLOCK = "Error 005. The semaphore block has been interrupted.";
    public static final String ERROR_CONNECTING_SERVER = "Error 006. The slave can not be connected to the server.";
    public static final String ERROR_SLEEP_INTERRUPTED = "Error 007. The thread sleep has been interrupted.";

    /* ********** ATTRIBUTES ********** */
    private Server master;
    private int identifier;
    private boolean wantToken;
    private Socket slave;
    private DataOutputStream dataOut;
    private DataInputStream dataIn;

    /* ********** CONSTRUCTOR ********** */
    /**
     * Constructor that initialize a Dedicate Server before starting the threads.
     * @param master Reference to Master.
     * @param identifier Slave identifier.
     * @param slave Socket to communicate with the slave.
     */
    DedicatedServer (Server master, int identifier, Socket slave) {

        this.master = master;
        this.identifier = identifier;
        this.slave = slave;
        this.wantToken = false;

        try {
            dataOut = new DataOutputStream(slave.getOutputStream());
            dataIn = new DataInputStream(slave.getInputStream());
        } catch (IOException e) {
            System.err.println(ERROR_OPENING_STREAMS);
        }
    }

    /* ********** METHODS ********** */
    /**
     * Method that is executed when the thread is launched.
     */
    @Override
    public void run() {

        boolean isOn = true;
        String request, response;

        if (identifier == 0) {

            try {
                response = dataIn.readUTF();
                if (response.equals(Server.TOKEN_REQUEST)) dataOut.writeUTF(Server.TOKEN_REQUEST);
                master.setToken(false);
            } catch (IOException e) {
                System.err.println(ERROR_SENDING_TOKEN);
            }
        }

        while (isOn) {

            try {
                request = dataIn.readUTF();
                isOn = handleRequest(request);
            } catch (IOException e) {
                System.err.println(ERROR_RECEIVING_REQUEST);
            }
        }
    }

    /**
     * Method that selects what to do when a request is received.
     * @param request String that contains the request received.
     * @return true if the thread has to be end, false otherwise.
     */
    private boolean handleRequest(String request) {

        int number, nextSlave;
        DataOutputStream tokenOut;

        try {
            switch (request) {

                case Server.READ_REQUEST:
                    dataOut.writeInt(master.getNumber());
                    break;

                case Server.TOKEN_REQUEST:
                    try {

                        master.getListSemaphore().acquire();
                        this.wantToken = true;
                        if (master.isToken()) {
                            dataOut.writeUTF(Server.TOKEN_REQUEST);
                            master.setToken(false);
                        }
                        master.getListSemaphore().release();
                    } catch (InterruptedException e) {
                        System.err.println(ERROR_SEMAPHORE_BLOCK);
                    }
                    break;

                case Server.TOKEN_FREE:

                    this.wantToken = false;

                    nextSlave = searchNextSlave(master.getSlavesSockets());

                    if (nextSlave != -1) {
                        tokenOut = new DataOutputStream(getSocketByIdentifier(master.getSlavesSockets(), nextSlave).getOutputStream());
                        tokenOut.writeUTF(Server.TOKEN_REQUEST);
                    } else master.setToken(true);
                    break;

                case Server.WRITE_REQUEST:
                    number = dataIn.readInt();
                    master.setNumber(number);
                    break;

                case Server.DISCONNECT_REQUEST:
                    master.getSlavesSockets().remove(removeSlave(master.getSlavesSockets()));
                    return false;
            }
        } catch (IOException e) {
            System.err.println(ERROR_SENDING_RESPONSE);
        }

        return true;
    }

    /**
     * Method that search the next slave ho has to receive the token.
     * @param list List of slaves connected to the Master.
     * @return Next Slave identifier if it found one, -1 otherwise.
     */
    private int searchNextSlave(LinkedList<DedicatedServer> list) {

        DedicatedServer slave;
        int index, counter = 0;

        try {

            master.getListSemaphore().acquire();
            for (int i = 0; i < list.size(); i++) {

                slave = list.get(i);

                if (slave.getIdentifier() == identifier) {

                    if (i == list.size() - 1) index = 0;
                    else index = i + 1;

                    master.getListSemaphore().release();
                    master.getListSemaphore().acquire();
                    if (index >= list.size()) index = 0;
                    while (!list.get(index).isWantToken()) {

                        list = master.getSlavesSockets();
                        if (index == list.size() - 1) index = 0;
                        else index++;

                        master.getListSemaphore().release();
                        master.getListSemaphore().acquire();
                        if (index >= list.size()) index = 0;
                        counter++;
                        if (counter > list.size()) {
                            master.getListSemaphore().release();
                            return -1;
                        }
                    }
                    master.getListSemaphore().release();

                    return list.get(index).getIdentifier();
                }
                master.getListSemaphore().release();
                master.getListSemaphore().acquire();
            }
            master.getListSemaphore().release();

        } catch (InterruptedException e) {
            System.err.println(ERROR_SEMAPHORE_BLOCK);
        }

        return -1;
    }

    /**
     * Method that remove one Slave connection from the Master.
     * @param list List of slaves connected to the Master.
     * @return Index of the list where's the Slave to be removed.
     */
    private int removeSlave(LinkedList<DedicatedServer> list) {

        DedicatedServer slave;

        try {
            master.getListSemaphore().acquire();
            for (int i = 0; i < list.size(); i++) {

                slave = list.get(i);
                master.getListSemaphore().release();
                if (slave.getIdentifier() == identifier) return i;
                master.getListSemaphore().acquire();
            }
            master.getListSemaphore().release();

        } catch (InterruptedException e) {
            System.err.println(ERROR_SEMAPHORE_BLOCK);
        }

        return -1;
    }

    /**
     * Method that gets the Socket of the Slave that matches with the identifier.
     * @param list List of slaves connected to the Master.
     * @param identifier Identifier of the Slave we are searching.
     * @return Socket of the Slave that we are searching.
     */
    private Socket getSocketByIdentifier(LinkedList<DedicatedServer> list, int identifier) {

        DedicatedServer slave;

        for (int i = 0; i < list.size(); i++) {

            slave = list.get(i);

            if (slave.getIdentifier() == identifier) return slave.getSlave();
        }

        return null;
    }

    /* ********** GETTERS ********** */
    /**
     * Identifier Getter.
     * @return int
     */
    private int getIdentifier() {

        return identifier;
    }

    /**
     * Slave Socket Getter.
     * @return Socket
     */
    private Socket getSlave() {

        return slave;
    }

    /**
     * wantToken Getter.
     * @return true if the Slave wants the token, false otherwise.
     */
    private boolean isWantToken() {

        return wantToken;
    }
}
