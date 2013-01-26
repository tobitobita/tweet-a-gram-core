package dsk.tweet_a_gram.core.twitter;

import static dsk.tweet_a_gram.core.Const.AUTH_ERROR;
import static dsk.tweet_a_gram.core.Const.NO_CREATE_IMAGE_FILE;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.File;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import dsk.common.exception.DskException;
import dsk.common.exception.DskWarningException;
import dsk.tweet_a_gram.core.delegates.MediaDelegate;
import dsk.tweet_a_gram.core.delegates.TweetDelegate;
import dsk.tweet_a_gram.core.service.AuthService;
import dsk.tweet_a_gram.core.service.TweetService;

public class TweetTwitter implements TweetService<Twitter> {
	private static final Logger LOG = LoggerFactory.getLogger(TweetTwitter.class);

	private TweetDelegate tweetDelegate;
	private MediaDelegate mediaDelegate;
	private AuthService<Twitter> twitterAuthService;

	@Inject
	public TweetTwitter(TweetDelegate tweetDelegate, MediaDelegate mediaDelegate,
			AuthService<Twitter> twitterAuthService) {
		super();
		this.tweetDelegate = tweetDelegate;
		this.mediaDelegate = mediaDelegate;
		this.twitterAuthService = twitterAuthService;
	}

	@Override
	public AuthService<Twitter> getAuthService() {
		return this.twitterAuthService;
	}

	@Override
	public void tweet() throws DskException {
		Twitter twitter = this.twitterAuthService.getAuthenticateTwitter();
		if (null == twitter) {
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
				StatusUpdate statusUpdate = new StatusUpdate(tweet);
				statusUpdate.setMedia(new File(filepath));
				twitter.updateStatus(statusUpdate);
			} else {
				LOG.info("つぶやきませんでした");
			}
		} catch (TwitterException e) {
			throw new DskWarningException(e);
		} finally {
			if (!isEmpty(filepath)) {
				new File(filepath).delete();
			}
		}
	}

	@Override
	public void deleteTweet(long statusId) throws TwitterException, DskException {
		Twitter twitter = this.twitterAuthService.getAuthenticateTwitter();
		if (null == twitter) {
			throw new DskException("認証エラー", AUTH_ERROR);
		}
		twitter.destroyStatus(statusId);
	}
}
