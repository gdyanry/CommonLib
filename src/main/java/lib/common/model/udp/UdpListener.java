/**
 * 
 */
package lib.common.model.udp;

import java.net.InetSocketAddress;

/**
 * @author yanry
 *
 * 2015年2月3日 下午5:35:08
 */
public interface UdpListener {
	void onReceive(InetSocketAddress addr, String text);
}
