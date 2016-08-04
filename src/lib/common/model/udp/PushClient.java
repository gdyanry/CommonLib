/**
 * 
 */
package lib.common.model.udp;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import lib.common.model.udp.UdpClient.ClientListener;

/**
 * @author yanry
 *
 *         2014年9月23日 下午4:07:46
 */
public abstract class PushClient {
	private UdpClient uc;
	private TimerTask tt;
	private boolean started;
	private Timer timer;
	private int pulsePeriod;

	public PushClient(Timer timer, int pulsePeriodSec) {
		this.timer = timer;
		this.pulsePeriod = pulsePeriodSec * 1000;
	}

	private void setup() throws IOException {
		started = true;
		uc = newUdpClient();
		uc.startListening(getClientListener());
		
		tt = new TimerTask() {
			
			@Override
			public void run() {
				String sessionId = getPulseContent();
				if (sessionId != null && isConnected() && uc != null) {
					try {
						uc.send(sessionId);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};
		timer.schedule(tt, 0, pulsePeriod);
	}

	/**
	 * start only if the network is connected, in which case if already started, then restart.
	 * @throws IOException
	 */
	public synchronized void start() throws IOException {
		destroy();
		if (isConnected()) {
			setup();
		}
	}

	public void destroy() {
		if (tt != null) {
			tt.cancel();
		}
		if (uc != null) {
			uc.exit();
		}
		started = false;
	}
	
	public boolean isStarted() {
		return started;
	}
	
	protected abstract String getPulseContent();
	
	protected abstract UdpClient newUdpClient();
	
	protected abstract ClientListener getClientListener();
	
	protected abstract boolean isConnected();
}
