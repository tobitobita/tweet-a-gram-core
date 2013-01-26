package dsk.tweet_a_gram.core.delegates;

public interface TweetDelegate {
	boolean isTweet();

	String getTweet();

	void setMediaPath(String path);
}
