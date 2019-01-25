package lib.common.t;

import lib.common.util.AesUtil;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class AesTest {
    public static void main(String[] args) throws NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException {
        String content = "527512505";
        String password = "tcl_push";
        System.out.println("加密之前：" + content);
        System.out.println(content.getBytes().length);

        byte[] key = AesUtil.generateKey(password.getBytes());
        System.out.println(Arrays.toString(key));

        byte[] iv = new byte[]{65, 20, -91, 123, -102, 126, 105, -28, -15, 13, 51, 32, 53, 45, -97, -40};

//         加密
        byte[] encrypt = AesUtil.encrypt(iv, key, content.getBytes());
        String base64 = Base64.getEncoder().encodeToString(encrypt);
        System.out.println("加密后的内容：" + base64);
        System.out.println(base64.length());
        System.out.println(encrypt.length);

//         解密
        byte[] decrypt = AesUtil.decrypt(iv, key, encrypt);
        System.out.println("解密后的内容：" + new String(decrypt));
    }
}
