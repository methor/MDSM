package nju.cs;

import nju.cs.timingservice.TimingService;
import nju.cs.timingservice.message.Message;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.*;

/**
 * Utility class for sending and waiting for messages via socket
 *
 * @author hengxin
 * @date Jul 15, 2014
 */
public enum SocketUtil {
    INSTANCE;

    private static final String TAG = SocketUtil.class.getName();

    private static final ExecutorService exec = Executors.newCachedThreadPool();

    public void sendMsg(final Message msg, final Socket host_socket) throws IOException {
        ObjectOutputStream oos;
        if (host_socket == null)
            return;


        oos = TimingService.INSTANCE.outputStream;
        oos.writeObject(msg);
        oos.flush();
    }

    public void sendMsgInNewThread(final Message msg, final Socket host_socket, ObjectOutputStream outputStream) {
        exec.execute(new SendMsgTask(msg, host_socket, outputStream));
    }

    public Message receiveMsg(final Socket host_socket) throws IOException {
        Message msg = null;

        if (host_socket == null)
            return null;

        try {
            final ObjectInputStream ois = TimingService.INSTANCE.inputStream;
            Object o = ois.readObject();
            msg = (Message) o;
        } catch (StreamCorruptedException sce) {
            sce.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }

        return msg;
    }

    public Message receiveMsgInNewThread(final Socket host_socket, ObjectInputStream inputStream) throws Throwable {
        Future<Message> future = exec.submit(new ReceiveMsgTask(host_socket, inputStream));
        try {
            return future.get();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        } catch (ExecutionException ee) {
            throw ee.getCause();
        } catch (CancellationException ce) {
            ce.printStackTrace();
        }

        return null;
    }

    /**
     * Send message via specified socket
     *
     * @author hengxin
     * @date Jul 15, 2014
     */
    final class SendMsgTask implements Runnable {
        // message to send
        private final Message msg;
        // send message via this socket
        private final Socket host_socket;
        private ObjectOutputStream outputStream;

        /**
         * Constructor of {@link SendMsgTask}
         *
         * @param msg         {@link Message} to send
         * @param host_socket send message via this socket
         */
        public SendMsgTask(final Message msg, final Socket host_socket, ObjectOutputStream outputStream) {
            this.msg = msg;
            this.host_socket = host_socket;
            this.outputStream = outputStream;
        }

        @Override
        public void run() {
            ObjectOutputStream oos;

            if (host_socket == null)
                return;

            try {

                outputStream.writeObject(msg);
                outputStream.flush();
            } catch (SocketTimeoutException stoe) {
                stoe.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    final class ReceiveMsgTask implements Callable<Message> {
        private final Socket host_socket;
        private ObjectInputStream inputStream = null;

        public ReceiveMsgTask(final Socket host_socket, ObjectInputStream inputStream) {
            this.host_socket = host_socket;
            this.inputStream = inputStream;
        }

        @Override
        public final Message call() throws IOException {
            Message msg = null;
            if (host_socket == null)
                return null;

            try {
                msg = (Message) inputStream.readObject();
            } catch (StreamCorruptedException sce) {
                sce.printStackTrace();
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }

            return msg;
        }

    }
}
