package com.test;

public class Money {
	long id = -1;
	long consumerId;
	long number;
	
	

	public Money(long consumerId, long number) {
		this.consumerId = consumerId;
		this.number = number;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getConsumerId() {
		return consumerId;
	}

	public void setConsumerId(long consumerId) {
		this.consumerId = consumerId;
	}

	public long getNumber() {
		return number;
	}

	public void setNumber(long number) {
		this.number = number;
	}

}
