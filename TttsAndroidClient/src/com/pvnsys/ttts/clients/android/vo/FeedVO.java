package com.pvnsys.ttts.clients.android.vo;

import java.io.Serializable;

public class FeedVO implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String id;
	private String msgType;
	private String client;
	private String payload;
	private String timestamp;
	private String sequenceNum;
	
	public FeedVO() {
	}

	public String getId() {
		return id;
	}

	public FeedVO(String id, String msgType, String client, String payload,
			String timestamp, String sequenceNum) {
		super();
		this.id = id;
		this.msgType = msgType;
		this.client = client;
		this.payload = payload;
		this.timestamp = timestamp;
		this.sequenceNum = sequenceNum;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getSequenceNum() {
		return sequenceNum;
	}

	public void setSequenceNum(String sequenceNum) {
		this.sequenceNum = sequenceNum;
	}

}