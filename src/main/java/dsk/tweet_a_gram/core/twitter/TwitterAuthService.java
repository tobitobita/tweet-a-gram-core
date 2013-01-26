package dsk.tweet_a_gram.core.twitter;

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

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import dsk.common.exception.DskRuntimeException;
import dsk.common.util.IoTools;
import dsk.tweet_a_gram.core.auth.Consumer;
import dsk.tweet_a_gram.core.delegates.AuthDelegate;
import dsk.tweet_a_gram.core.service.AuthService;
import dsk.tweet_a_gram.core.utils.TweetTool;

public class TwitterAuthService implements AuthService<Twitter> {
	private final static Logger LOG = LoggerFactory.getLogger(TwitterAuthService.class);
	private final static String AUTH_FILENAME = ".auth3";
	public final static String CONSUMER_FILENAME = "consumer";

	private Twitter twitter;
	private AuthDelegate authDelegate;

	@Inject
	public TwitterAuthService(Twitter twitter, AuthDelegate authDelegate) {
		super();
		this.twitter = twitter;
		this.authDelegate = authDelegate;
	}

	@Override
	public Twitter getAuthenticateTwitter() {
		try {
			Consumer consumer = new Consumer();
			consumer.loadConsumer(CONSUMER_FILENAME);
			this.twitter.setOAuthConsumer(consumer.getConsumerKey(), consumer.getConsumerSecret());
		} catch (IllegalStateException e) {
			LOG.info(e.getLocalizedMessage());
		}
		try {
			AccessToken accessToken = this.loadAccessToken();
			if (null == accessToken) {
				LOG.debug("accessToken is null.");
				RequestToken requestToken = this.twitter.getOAuthRequestToken();
				if (null != requestToken) {
					String pin = this.authDelegate.doAuthTwitter(requestToken.getAuthorizationURL());
					if (null == pin) {
						return null;
					}
					accessToken = this.twitter.getOAuthAccessToken(requestToken, pin);
					this.saveAccessToken(accessToken);
				}
			}
			this.twitter.setOAuthAccessToken(accessToken);
			return twitter;
		} catch (TwitterException e) {
			this.twitter = new TwitterFactory().getInstance();
			throw new DskRuntimeException(e);
		} catch (IOException e) {
			return null;
		}
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

	protected void saveAccessToken(AccessToken accessToken) throws IOException {
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

	protected AccessToken loadAccessToken() throws IOException {
		File f = this.getAccessTokenFile();
		if (!f.exists()) {
			return null;
		}
		AccessToken accessToken = null;
		ObjectInputStream is = null;
		try {
			is = new ObjectInputStream(new FileInputStream(f));
			SealedObject sealedObject = (SealedObject) is.readObject();
			accessToken = (AccessToken) sealedObject.getObject(TweetTool.createCipher(Cipher.DECRYPT_MODE));
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
