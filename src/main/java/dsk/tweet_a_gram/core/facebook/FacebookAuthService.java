package dsk.tweet_a_gram.core.facebook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SealedObject;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dsk.common.exception.DskRuntimeException;
import dsk.common.util.IoTools;
import dsk.tweet_a_gram.core.auth.Consumer;
import dsk.tweet_a_gram.core.delegates.AuthDelegate;
import dsk.tweet_a_gram.core.service.AuthService;
import dsk.tweet_a_gram.core.utils.TweetTool;

public class FacebookAuthService implements AuthService<String> {
	private final static Logger LOG = LoggerFactory.getLogger(FacebookAuthService.class);

	private static final String FACEBOOK_AUTH_URL = "https://www.facebook.com/dialog/oauth?client_id=%s&redirect_uri=http://www.facebook.com/connect/login_success.html&response_type=token&scope=publish_stream";

	private final static String AUTH_FILENAME = ".auth_fb";
	public final static String CONSUMER_FILENAME = "consumer_fb";

	private AuthDelegate authDelegate;

	@Inject
	public FacebookAuthService(AuthDelegate authDelegate) {
		super();
		this.authDelegate = authDelegate;
	}

	@Override
	public String getAuthenticateTwitter() {
		try {
			String accessToken = this.loadAccessToken();
			if (accessToken == null) {
				Consumer consumer = new Consumer();
				consumer.loadConsumer(CONSUMER_FILENAME);
				accessToken = authDelegate.doAuthTwitter(String.format(FACEBOOK_AUTH_URL, consumer.getConsumerKey()));
				this.saveAccessToken(accessToken);
			}
			return accessToken;
		} catch (IllegalStateException e) {
			LOG.info(e.getLocalizedMessage());
		} catch (IOException e) {
			LOG.error(e.getLocalizedMessage(), e);
		}
		return null;
	}

	@Override
	public void deleteAccessToken() {
		File f = this.getAccessTokenFile();
		if (f.exists()) {
			f.delete();
		}
	}

	protected File getAccessTokenFile() {
		return new File(TweetTool.getAsutterDirectoryPath() + "/" + AUTH_FILENAME);
	}

	protected void saveAccessToken(String accessToken) throws IOException {
		File f = this.getAccessTokenFile();
		ObjectOutputStream os = null;
		try {
			os = new ObjectOutputStream(new FileOutputStream(f));
			SealedObject sealedObject = new SealedObject(accessToken, TweetTool.createCipher(Cipher.ENCRYPT_MODE));
			os.writeObject(sealedObject);
		} catch (IOException e) {
			LOG.error("ファイル書き込みエラー");
			throw e;
		} catch (IllegalBlockSizeException e) {
			throw new DskRuntimeException(e);
		} finally {
			IoTools.close(os);
		}
	}

	protected String loadAccessToken() throws IOException {
		File f = this.getAccessTokenFile();
		if (!f.exists()) {
			return null;
		}
		String accessToken = null;
		ObjectInputStream is = null;
		try {
			is = new ObjectInputStream(new FileInputStream(f));
			SealedObject sealedObject = (SealedObject) is.readObject();
			accessToken = (String) sealedObject.getObject(TweetTool.createCipher(Cipher.DECRYPT_MODE));
		} catch (IOException e) {
			LOG.error("ファイル読み込みエラー");
			throw e;
		} catch (ClassNotFoundException e) {
			throw new DskRuntimeException(e);
		} catch (IllegalBlockSizeException e) {
			throw new DskRuntimeException(e);
		} catch (BadPaddingException e) {
			throw new DskRuntimeException(e);
		} finally {
			IoTools.close(is);
		}
		return accessToken;
	}
}
