package dsk.tweet_a_gram.core.service;

public interface AuthService<SERVICE> {
	SERVICE getAuthenticateTwitter();

	void deleteAccessToken();
}
