/**
 * 
 */
package lib.common.model.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.util.Iterator;

import lib.common.util.ConsoleUtil;

/**
 * @author yanry
 *
 *         2014年9月23日 上午11:10:15
 */
public abstract class UdpClient {
	private DatagramChannel dc;
	private ByteBuffer receiveBuf;
	private ByteBuffer sendBuf;
	private Charset charset;
	private Selector s;
	private boolean start;
	private boolean exit;
	private boolean restart;
	private InetSocketAddress server;
	private InetSocketAddress bindAddr;

	/**
	 * 
	 * @param localPort
	 *            A valid port value is between 0 and 65535. A port number of
	 *            zero will let the system pick up an ephemeral port in a bind
	 *            operation.
	 * @param host
	 * @param remotePort
	 * @param charset
	 * @param bufSize
	 * @throws IOException
	 */
	public UdpClient(int localPort, String host, int remotePort,
			String charset, int bufSize) throws IOException {
		this.charset = Charset.forName(charset);
		receiveBuf = ByteBuffer.allocate(bufSize);
		sendBuf = ByteBuffer.allocate(bufSize);
		server = new InetSocketAddress(host, remotePort);
		bindAddr = new InetSocketAddress(localPort);
	}

	private synchronized void prepareChannel() throws IOException {
		if (dc == null) {
			ConsoleUtil.debug(getClass(), "open datagram channel.");
			dc = DatagramChannel.open();
			dc.configureBlocking(false);
		}
		if (!dc.isConnected()) {
			ConsoleUtil.debug(getClass(), "connect to server " + server);
			dc.socket().bind(bindAddr);
			dc.connect(server);
		}
	}

	/**
	 * 
	 * @param listener
	 *            This listener is processed synchronously.
	 * @return
	 */
	public boolean startListening(final ClientListener listener) {
		if (start) {
			return false;
		}
		start = true;
		exit = false;
		execute(new Runnable() {

			@Override
			public void run() {
				try {
					prepareChannel();
					if (s == null) {
						s = Selector.open();
						dc.register(s, SelectionKey.OP_READ);
					}

					ConsoleUtil.debug(UdpClient.this.getClass(),
							"start listening...");
					while (!exit && start && s.select() > 0) {
						Iterator<SelectionKey> it = s.selectedKeys().iterator();
						while (it.hasNext()) {
							it.next();
							it.remove();
							receiveBuf.clear();
							int i = dc.read(receiveBuf);
							receiveBuf.flip();
							if (i > 0) {
								String text = UdpClient.this.charset.decode(
										receiveBuf).toString();
								ConsoleUtil.debug(UdpClient.this.getClass(),
										"<<< " + text);
								listener.onServerRequest(text);
							}
						}
					}
					ConsoleUtil.debug(UdpClient.this.getClass(),
							"stop listening.");
					if (exit) {
						ConsoleUtil.debug(UdpClient.this.getClass(), "exit.");
						s.close();
						dc.close();
						s = null;
						dc = null;
						if (restart) {
							startListening(listener);
							restart = false;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		return true;
	}

	public void send(String text) throws IOException {
		sendBuf.clear();
		sendBuf.put(charset.encode(text));
		sendBuf.flip();
		prepareChannel();
		ConsoleUtil.debug(getClass(),
				String.format("%s>>> %s", dc.socket().getLocalPort(), text));
		dc.write(sendBuf);
	}

	public void stoplistening() {
		start = false;
		if (s != null) {
			s.wakeup();
		}
	}

	public void exit() {
		exit = true;
		stoplistening();
	}

	public boolean restart() {
		if (start) {
			restart = true;
			exit();
			return true;
		} else {
			return false;
		}
	}

	protected abstract void execute(Runnable r);

	public interface ClientListener {
		void onServerRequest(String text);
	}
}
