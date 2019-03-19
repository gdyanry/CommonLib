package yanry.lib.java.util;

import yanry.lib.java.interfaces.StreamTransferHook;

import java.io.*;

public class IOUtil {
    @SuppressWarnings("unchecked")
    public static <T> T deepCopy(T source) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(buf);
        oos.writeObject(source);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buf.toByteArray()));
        Object o = ois.readObject();
        ois.close();
        return (T) o;
    }

    public static byte[] object2Bytes(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        return baos.toByteArray();
    }

    public static Object bytes2Object(byte[] bytes) throws ClassNotFoundException, IOException {
        return new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
    }

    public static byte[] fileToByteArray(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        return inputStreamToBytes(fis);
    }

    public static byte[] inputStreamToBytes(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        transferStream(is, bos);
        return bos.toByteArray();
    }

    public static long transferStream(InputStream is, OutputStream os) throws IOException {
        byte[] buf = new byte[4096];
        long length = 0;
        int i;
        while ((i = is.read(buf)) != -1) {
            os.write(buf, 0, i);
            length += i;
        }
        os.flush();
        is.close();
        return length;
    }

    public static void transferStream(InputStream is, OutputStream os, StreamTransferHook hook) throws IOException {
        long time = System.currentTimeMillis();
        byte[] buf = new byte[hook.getBufferSize() > 0 ? hook.getBufferSize() : 4096];
        long length = 0;
        int i = 0;
        while (!hook.isStop() && (i = is.read(buf)) != -1) {
            os.write(buf, 0, i);
            length += i;
            long currentTime = System.currentTimeMillis();
            if (currentTime - time >= hook.getUpdateInterval()) {
                hook.onUpdate(length);
                time = currentTime;
            }
        }
        os.flush();
        is.close();
        hook.onFinish(i != -1);
    }

    public static void bytesToOutputStream(byte[] bytes, OutputStream os, StreamTransferHook hook) throws IOException {
        long time = System.currentTimeMillis();
        byte[] buf = new byte[hook.getBufferSize() > 0 ? hook.getBufferSize() : 4096];
        int length = 0;
        while (!hook.isStop() && length < bytes.length) {
            int len = Math.min(buf.length, bytes.length - length);
            os.write(buf, length, len);
            length += len;
            long currentTime = System.currentTimeMillis();
            if (currentTime - time >= hook.getUpdateInterval()) {
                hook.onUpdate(length);
                time = currentTime;
            }
        }
        os.flush();
        hook.onFinish(length < bytes.length);
    }

    /**
     * @param callerType
     * @param path       "/" represents "bin" directory; "" represents the caller
     *                   type's package directory.
     * @param charset
     * @return
     * @throws IOException
     */
    public static String resourceToString(Class<?> callerType, String path, String charset) throws IOException {
        InputStream is = callerType.getResourceAsStream(path);
        return streamToString(is, charset);
    }

    /**
     * @param filePath "/" represents current disk root; "" represents current path.
     * @param charset
     * @return
     * @throws IOException
     */
    public static String fileToString(String filePath, String charset) throws IOException {
        return fileToString(new File(filePath), charset);
    }

    public static String fileToString(File sourceFile, String charset) throws IOException {
        return streamToString(new FileInputStream(sourceFile), charset);
    }

    public static void stringToFile(String sourceStr, String targetPath, String charset, boolean append)
            throws IOException {
        stringToFile(sourceStr, new File(targetPath), charset, append);
    }

    public static void stringToFile(String sourceStr, File targetFile, String charset, boolean append)
            throws IOException {
        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }
        FileOutputStream fos = new FileOutputStream(targetFile, append);
        fos.write(sourceStr.getBytes(charset));
        fos.flush();
        fos.close();
    }

    public static String streamToString(InputStream is, String charset) throws IOException {
        if (is == null) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        transferStream(is, out);
        return out.toString(charset);
    }

    /**
     * @param filePath "" represents the directory containing this jar file.
     * @return
     */
    public static File getAppRelativeFile(String filePath) {
        String classPath = System.getProperty("java.class.path");
        int separatorIndex = classPath.indexOf(File.pathSeparatorChar);
        if (separatorIndex != -1) {
            classPath = classPath.substring(0, separatorIndex);
        }
        File parentFile = new File(classPath).getAbsoluteFile().getParentFile();
        return new File(parentFile, filePath);
    }

    public static void copyFile(File src, File dest) throws IOException {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(src));
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
        IOUtil.transferStream(in, out);
        out.close();
    }
}
