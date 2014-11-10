package com.test;

public class Message {

	public Message(long id, byte[] data) {
		super();
		this.id = id;
		this.data = data;
	}

	long id;

	byte[] data;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
}
