package lib.common.model.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.util.Iterator;

import lib.common.util.ConsoleUtil;

/**
 * 
 */

/**
 * @author yanry
 *
 *         2014年9月19日 下午5:20:25
 */
public abstract class UdpServer {
	private DatagramChannel dc;
	private ByteBuffer receiveBuf;
	private ByteBuffer sendBuf;
	private Charset charset;
	private Selector s;
	private boolean start;
	private boolean exit;

	/**
	 * 
	 * @param port A valid port value is between 0 and 65535. A port number of zero will let the system pick up an ephemeral port in a bind operation.
	 * @param charset
	 * @param bufSize
	 * @throws IOException
	 */
	public UdpServer(int port, String charset, int bufSize)
			throws IOException {
		this.charset = Charset.forName(charset);
		receiveBuf = ByteBuffer.allocate(bufSize);
		sendBuf = ByteBuffer.allocate(bufSize);
		dc = DatagramChannel.open();
		dc.configureBlocking(false);
		if (port > 0) {
			SocketAddress localAddr = new InetSocketAddress(port);
			ConsoleUtil.debug(getClass(), "bind: " + localAddr);
			dc.bind(localAddr);
		}
		s = Selector.open();
		dc.register(s, SelectionKey.OP_READ);
	}

	/**
	 * 
	 * @param listener This listener is processed synchronously.
	 * @return
	 */
	public boolean start(final UdpListener listener) {
		if (start) {
			return false;
		}
		start = true;
		execute(new Runnable() {

			@Override
			public void run() {
				try {
					ConsoleUtil.debug(UdpServer.this.getClass(), "start listening...");
					while (!exit && start && s.select() > 0) {
						Iterator<SelectionKey> it = s.selectedKeys().iterator();
						while (it.hasNext()) {
							it.next();
							it.remove();
							receiveBuf.clear();
							SocketAddress client = dc.receive(receiveBuf);
							receiveBuf.flip();
							if (client != null) {
								String text = UdpServer.this.charset.decode(
										receiveBuf).toString();
								ConsoleUtil.debug(UdpServer.this.getClass(), String.format("%s >>> %s", client, text));
								listener.onReceive((InetSocketAddress) client, text);
							}
						}
					}
					ConsoleUtil.debug(UdpServer.this.getClass(), "stop listening.");
					if (exit) {
						ConsoleUtil.debug(UdpServer.this.getClass(), "exit.");
						s.close();
						dc.disconnect();
						dc.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		return true;
	}

	public synchronized void send(SocketAddress target, String text) throws IOException {
		sendBuf.clear();
		ByteBuffer content = charset.encode(text);
		sendBuf.put(content);
		sendBuf.flip();
		ConsoleUtil.debug(getClass(), String.format(String.format("%s <<< %s", target, text)));
		dc.send(sendBuf, target);
	}

	public void stop() {
		start = false;
		s.wakeup();
	}

	public void exit() throws IOException {
		exit = true;
		stop();
		dc.close();
	}

	protected abstract void execute(Runnable r);
}
