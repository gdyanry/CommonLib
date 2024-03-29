package yanry.lib.java.model.udp;

import yanry.lib.java.model.log.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;

/**
 * @author yanry
 * <p>
 * 2015年2月3日 下午5:32:08
 */
public abstract class Udp {
    private DatagramChannel dc;
    private ByteBuffer receiveBuf;
    private ByteBuffer sendBuf;
    private Charset charset;
    private boolean exit;

    /**
     * @param port    A valid port value is between 0 and 65535. A port number of zero will let the system pick up an ephemeral port in a bind operation.
     * @param charset
     * @param bufSize
     * @throws IOException
     */
    public Udp(int port, String charset, int bufSize)
            throws IOException {
        this.charset = Charset.forName(charset);
        receiveBuf = ByteBuffer.allocate(bufSize);
        sendBuf = ByteBuffer.allocate(bufSize);
        dc = DatagramChannel.open();
        dc.configureBlocking(false);
        if (port > 0) {
            SocketAddress localAddress = new InetSocketAddress(port);
            Logger.getDefault().dd("bind: ", localAddress);
            dc.bind(localAddress);
        }
    }

    /**
     * @param listener This listener is processed synchronously.
     * @return
     */
    public boolean start(final UdpListener listener) {
        if (exit) {
            return false;
        }
        execute(() -> {
            try {
                Logger.getDefault().dd("start listening...");
                while (!exit) {
                    receiveBuf.clear();
                    SocketAddress client = dc.receive(receiveBuf);
                    receiveBuf.flip();
                    if (client != null) {
                        String text = Udp.this.charset.decode(
                                receiveBuf).toString();
                        Logger.getDefault().v("%s >>> %s", client, text);
                        listener.onReceive((InetSocketAddress) client, text);
                    }
                }
                Logger.getDefault().dd("stop listening.");
            } catch (IOException e) {
                Logger.getDefault().catches(e);
            }
            Logger.getDefault().dd("exit.");
        });
        return true;
    }

    public synchronized void send(SocketAddress target, String text) throws IOException {
        sendBuf.clear();
        ByteBuffer content = charset.encode(text);
        sendBuf.put(content);
        sendBuf.flip();
        Logger.getDefault().v("%s <<< %s", target, text);
        dc.send(sendBuf, target);
    }

    public void exit() throws IOException {
        exit = true;
        dc.disconnect();
        dc.close();
    }

    protected abstract void execute(Runnable r);
}
