package lib.common.t;

import lib.common.interfaces.StreamTransferHook;
import lib.common.util.IOUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by rongyu.yan on 3/3/2017.
 */
public class TcpDownload {
    public static void main(String... args) throws IOException {
        final ServerSocket ss = new ServerSocket(800);
        System.out.println("listening at port 800...");
        while (true) {
            final Socket socket = ss.accept();
            new Thread(() -> {
                try {
                    System.out.println("receive request from " + socket.getRemoteSocketAddress());
                    FileInputStream fileInputStream = new FileInputStream("C:\\Users\\rongyu.yan\\Documents\\pmp_exam.pdf");
                    OutputStream outputStream = socket.getOutputStream();
                    IOUtil.transferStream(fileInputStream, outputStream, new StreamTransferHook() {
                        @Override
                        public boolean isStop() {
                            return false;
                        }

                        @Override
                        public int getUpdateInterval() {
                            return 3000;
                        }

                        @Override
                        public void onUpdate(long transferredBytes) {
                            System.out.println(String.valueOf(transferredBytes >> 10));
                        }

                        @Override
                        public void onFinish(boolean isStopped) {
                            System.out.println(socket.getRemoteSocketAddress() + " finish.");
                        }

                        @Override
                        public int getBufferSize() {
                            return 0;
                        }
                    });
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
