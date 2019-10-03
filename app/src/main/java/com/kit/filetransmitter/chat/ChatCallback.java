package com.kit.filetransmitter.chat;

/**
 * Created by Kit
 * Time: 2019/9/30
 */
public interface ChatCallback {

	void onMessageArrive(String topic, byte[] payload);

	void onDeliveryComplete(String topic, byte[] payload);

}
