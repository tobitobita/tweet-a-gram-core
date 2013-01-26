package dsk.tweet_a_gram.core.utils;

import java.io.File;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dsk.common.exception.DskRuntimeException;
import dsk.common.util.DesktopTools;

public final class TweetTool {
    private static final Logger LOG = LoggerFactory.getLogger(TweetTool.class);

    private TweetTool() {
    }

    /**
     * あすったーが使用するフォルダ
     */
    private final static String APP_DIR_NAME = ".asutter";

    public static String getAsutterDirectoryPath() {
        String dirPath = String.format("%s/%s", DesktopTools.getHomeDirectoryPath(), APP_DIR_NAME);
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dirPath;
    }

    /**
     * 暗号化・復号化
     */
    private static final String CIPHER_TYPE = "AES";

    public static byte[] encript(String encriptString) {
        byte[] bytes = null;
        try {
            bytes = createCipher(Cipher.ENCRYPT_MODE).doFinal(encriptString.getBytes());
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage());
            throw new DskRuntimeException(e);
        }
        return bytes;
    }

    public static String decript(byte[] decript) {
        String str = null;
        try {
            byte[] bytes = createCipher(Cipher.DECRYPT_MODE).doFinal(decript);
            str = new String(bytes);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage());
            throw new DskRuntimeException(e);
        }
        return str;
    }

    public static Cipher createCipher(int cipherMode) {
        SecretKey key = new SecretKeySpec(create128bits(), CIPHER_TYPE);
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(CIPHER_TYPE);
            cipher.init(cipherMode, key);
        } catch (InvalidKeyException e) {
            LOG.error(e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            LOG.error(e.getMessage(), e);
        } catch (NoSuchPaddingException e) {
            LOG.error(e.getMessage(), e);
        }
        return cipher;
    }

    private static byte[] create128bits() {
        byte[] bytes = new byte[128 / 8];
        byte[] strBytes = (TweetTool.class.getName()).getBytes();
        for (int i = 0; i < bytes.length; ++i) {
            bytes[i] = strBytes[i];
        }
        return bytes;
    }
}
