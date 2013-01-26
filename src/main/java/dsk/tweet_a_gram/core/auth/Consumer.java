package dsk.tweet_a_gram.core.auth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dsk.common.exception.DskRuntimeException;
import dsk.common.util.IoTools;
import dsk.tweet_a_gram.core.utils.TweetTool;

public class Consumer implements Serializable {
	private static final long serialVersionUID = 3454749638272763544L;
	private static final Logger LOG = LoggerFactory.getLogger(Consumer.class);

	private byte[] consumerKey;
	private byte[] consumerSecret;

	public String getConsumerKey() {
		return TweetTool.decript(this.consumerKey);
	}

	public String getConsumerSecret() {
		return TweetTool.decript(this.consumerSecret);
	}

	private void saveConsumer(String consumerKey, String consumerSecret, String filepath) throws IOException {
		System.out.println(filepath);
		this.consumerKey = TweetTool.encript(consumerKey);
		this.consumerSecret = TweetTool.encript(consumerSecret);
		// 保存
		File f = new File(filepath);
		if (f.exists()) {
			f.delete();
		}
		ObjectOutputStream os = null;
		try {
			os = new ObjectOutputStream(new FileOutputStream(f));
			os.writeObject(this);
		} finally {
			IoTools.close(os);
		}
	}

	public void loadConsumer(String path) {
		ObjectInputStream is = null;
		try {
			is = new ObjectInputStream(this.getInputStreamByConsumerObject(path));
			Consumer consumer = (Consumer) is.readObject();
			this.consumerKey = consumer.consumerKey;
			this.consumerSecret = consumer.consumerSecret;
		} catch (IOException e) {
			LOG.error("ファイルがありません。このファイルはあらかじめ作成しなければなりません。");
			throw new DskRuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new DskRuntimeException(e);
		} finally {
			IoTools.close(is);
		}
	}

	private InputStream getInputStreamByConsumerObject(String path) throws IOException {
		InputStream stream = Consumer.class.getClassLoader().getResourceAsStream(path);
		if (null == stream) {
			throw new IOException("consumer is null.");
		}
		return stream;
	}

	public static void main(String[] args) {
//		args = new String[3];
//		args[0] = "";
//		args[1] = "";
//		args[2] = "./";
		System.out.println(args[0]);
		System.out.println(args[1]);
		System.out.println(args[2]);
		try {
			new Consumer().saveConsumer(args[0], args[1], args[2] + "consumer_fb");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
