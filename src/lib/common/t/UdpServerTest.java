/**
 * 
 */
package lib.common.t;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;

import lib.common.model.udp.UdpListener;
import lib.common.model.udp.UdpServer;

/**
 * @author yanry
 *
 *         2014年9月23日 上午9:21:04
 */
public class UdpServerTest extends UdpServer {
	private Timer t;

	public UdpServerTest() throws IOException {
		super(10006, "gbk", 2048);
		t = new Timer();
		start(new UdpListener() {

			@Override
			public void onReceive(final InetSocketAddress client,
					final String text) {
				TimerTask tt = new TimerTask() {
					int counter = 0;

					@Override
					public void run() {
						String echo = String.format("(%tT) %s",
								System.currentTimeMillis(), text);
						try {
							send(client, echo);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (++counter == 5) {
							cancel();
						}
					}
				};
				t.schedule(tt, 0, 30000);
			}
		});
	}

	@Override
	protected void execute(Runnable r) {
		new Thread(r).start();
	}

	public static void main(String[] args) {
		try {
			new UdpServerTest();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
