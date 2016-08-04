/**
 * 
 */
package lib.common.t;

import java.io.IOException;
import java.util.Scanner;

import lib.common.model.udp.UdpClient;

/**
 * @author yanry
 *
 *         2014年9月23日 下午3:28:50
 */
public class UdpClientTest extends UdpClient {

	public UdpClientTest() throws IOException {
		super(0, "58.67.150.131", 60000, "gbk", 1024);
	}

	@Override
	protected void execute(Runnable r) {
		new Thread(r).start();
	}

	public void receiveFromUser() {
		Scanner sc = new Scanner(System.in);
		String line = null;
		while (!(line = sc.nextLine()).equals("bye")) {
			try {
				send(line);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		sc.close();
		exit();
	}

	public static void main(String[] args) {
		try {
			UdpClientTest uct = new UdpClientTest();
			uct.startListening(new ClientListener() {

				@Override
				public void onServerRequest(String text) {
					// TODO Auto-generated method stub

				}
			});
			uct.receiveFromUser();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
