package dsk.tweet_a_gram.core.facebook;

import static dsk.tweet_a_gram.core.Const.AUTH_ERROR;
import static dsk.tweet_a_gram.core.Const.NO_CREATE_IMAGE_FILE;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.TwitterException;
import dsk.common.exception.DskException;
import dsk.common.exception.DskWarningException;
import dsk.tweet_a_gram.core.delegates.MediaDelegate;
import dsk.tweet_a_gram.core.delegates.TweetDelegate;
import dsk.tweet_a_gram.core.service.AuthService;
import dsk.tweet_a_gram.core.service.TweetService;

public class TweetFacebook implements TweetService<String> {
	private static final Logger LOG = LoggerFactory.getLogger(TweetFacebook.class);

	private TweetDelegate tweetDelegate;
	private MediaDelegate mediaDelegate;
	private AuthService<String> authService;

	@Inject
	public TweetFacebook(TweetDelegate tweetDelegate, MediaDelegate mediaDelegate, AuthService<String> authService) {
		super();
		this.tweetDelegate = tweetDelegate;
		this.mediaDelegate = mediaDelegate;
		this.authService = authService;
	}

	@Override
	public AuthService<String> getAuthService() {
		return this.authService;
	}

	@Override
	public void tweet() throws DskException {
		String accessToken = this.authService.getAuthenticateTwitter();
		if (null == accessToken) {
			throw new DskException("認証エラー", AUTH_ERROR);
		}
		String filepath = null;
		try {
			filepath = this.mediaDelegate.getMediaPath();
			if (isEmpty(filepath)) {
				throw new DskWarningException("画像を作成できませんでした", NO_CREATE_IMAGE_FILE);
			}
			this.tweetDelegate.setMediaPath(filepath);
			String tweet = StringUtils.defaultString(this.tweetDelegate.getTweet(), "");
			if (this.tweetDelegate.isTweet()) {
				this.postMeMessage(accessToken, tweet, filepath);
			} else {
				LOG.info("つぶやきませんでした");
			}
		} catch (UnsupportedEncodingException e) {
			throw new DskException(e);
		} catch (ClientProtocolException e) {
			throw new DskException(e);
		} catch (ParseException | IOException e) {
			throw new DskWarningException(e);
		} finally {
			if (!isEmpty(filepath)) {
				new File(filepath).delete();
			}
		}
	}

	private void postMeMessage(String accessToken, String message, String filePath)
			throws UnsupportedEncodingException, ClientProtocolException, ParseException, IOException {
		HttpPost post = new HttpPost("https://graph.facebook.com/me/photos");
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		entity.addPart("access_token", new StringBody(accessToken));
		entity.addPart("message", new StringBody(message, Charset.forName("UTF-8")));
		entity.addPart("source", new FileBody(new File(filePath)));
		post.setEntity(entity);

		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse res = httpClient.execute(post);
		LOG.debug(Integer.toString(res.getStatusLine().getStatusCode()));
		LOG.debug(EntityUtils.toString(res.getEntity()));
	}

	@Override
	public void deleteTweet(long statusId) throws TwitterException, DskException {
		throw new UnsupportedOperationException("未対応です");
	}
}
