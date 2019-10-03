package com.kit.filetransmitter.entity;

import java.util.Date;

/**
 * Created by Kit
 * Time: 2019/9/30
 */
public class MessageItem {

	private final String message;
	private final Date date;
	private final String time;
	private final int type;

	public MessageItem(String message, Date date, int type) {
		this.message = message;
		this.date = (Date)date.clone();
		this.time = this.date.toString();
		this.type = type;
	}

	public String getMessage() {
		return message;
	}

	public Date getDate() {
		return date;
	}

	public String getTime() {
		return time;
	}

	public int getType() {
		return type;
	}

}
