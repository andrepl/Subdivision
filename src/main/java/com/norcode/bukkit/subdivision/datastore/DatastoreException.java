package com.norcode.bukkit.subdivision.datastore;

public class DatastoreException extends Exception {
	public DatastoreException() {
	}

	public DatastoreException(String message) {
		super(message);
	}

	public DatastoreException(String message, Throwable cause) {
		super(message, cause);
	}

	public DatastoreException(Throwable cause) {
		super(cause);
	}

	public DatastoreException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
