package com.kit.filetransmitter.util;

/**
 * Created by Kit
 * Time: 2019/9/30
 */
public class Topics {

	public static final String PC = "pc";
	public static final String MOBILE = "mobile";
	public static final String TEXT = "text";
	public static final String FILE = "file";

	public static final String SEPARATOR = "/";
	public static final String EXTRA_ONE = "+";
	public static final String EXTRA_MORE = "#";

	public static final String PC_TEXT = PC + SEPARATOR + TEXT;
	public static final String PC_FILE = PC + SEPARATOR + FILE + SEPARATOR;

	public static final String MOBILE_TEXT = MOBILE + SEPARATOR + TEXT;
	public static final String MOBILE_FILE = MOBILE + SEPARATOR + FILE + SEPARATOR;

	public static final String RECV_HINT = " 接收成功";
	public static final String RECV_FAIL = " 接收失败";
	public static final String SEND_HINT = " 发送成功";
	public static final String SEND_FAIL = " 发送失败";

	public static final int RECV = 3;
	public static final int SEND = 12;

	public static final int RECV_TEXT = 1;
	public static final int RECV_FILE = 2;
	public static final int SEND_TEXT = 4;
	public static final int SEND_FILE = 8;
	public static final int ERROR = -1;

}
