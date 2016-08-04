/**
 * 
 */
package lib.common.model.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;

import lib.common.util.ConsoleUtil;

/**
 * @author yanry
 *
 * 2015年2月3日 下午5:32:08
 */
public abstract class Udp {
	private DatagramChannel dc;
	private ByteBuffer receiveBuf;
	private ByteBuffer sendBuf;
	private Charset charset;
	private boolean exit;

	/**
	 * 
	 * @param port A valid port value is between 0 and 65535. A port number of zero will let the system pick up an ephemeral port in a bind operation.
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
			SocketAddress localAddr = new InetSocketAddress(port);
			ConsoleUtil.debug(getClass(), "bind: " + localAddr);
			dc.bind(localAddr);
		}
	}

	/**
	 * 
	 * @param listener This listener is processed synchronously.
	 * @return
	 */
	public boolean start(final UdpListener listener) {
		if (exit) {
			return false;
		}
		execute(new Runnable() {

			@Override
			public void run() {
				try {
					ConsoleUtil.debug(Udp.class, "start listening...");
					while (!exit) {
						receiveBuf.clear();
						SocketAddress client = dc.receive(receiveBuf);
						receiveBuf.flip();
						if (client != null) {
							String text = Udp.this.charset.decode(
									receiveBuf).toString();
							ConsoleUtil.debug(Udp.class, String.format("%s >>> %s", client, text));
							listener.onReceive((InetSocketAddress) client, text);
						}
					}
					ConsoleUtil.debug(Udp.class, "stop listening.");
				} catch (IOException e) {
					e.printStackTrace();
				}
				ConsoleUtil.debug(Udp.class, "exit.");
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

	public void exit() throws IOException {
		exit = true;
		dc.disconnect();
		dc.close();
	}

	protected abstract void execute(Runnable r);
}
