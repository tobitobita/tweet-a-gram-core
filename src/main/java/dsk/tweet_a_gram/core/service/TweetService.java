package dsk.tweet_a_gram.core.service;

import twitter4j.TwitterException;
import dsk.common.exception.DskException;

public interface TweetService<SERVICE> {
	AuthService<SERVICE> getAuthService();

	void tweet() throws DskException;

	void deleteTweet(long statusId) throws TwitterException, DskException;
}
