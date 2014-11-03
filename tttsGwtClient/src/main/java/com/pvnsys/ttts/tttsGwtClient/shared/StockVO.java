package com.pvnsys.ttts.tttsGwtClient.shared;

import java.io.Serializable;
import java.util.Date;

public class StockVO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1259478609627297669L;
	
	private Long id;
	private String name;
	private double open;
	private double close;
	private double high;
	private double low;
	private double adjClose;
	private Date stockDate;
	private long volume;


	public StockVO() {
	}
	
	public StockVO(Long id, String name, double open, double close,
			double high, double low, double adjClose, Date stockDate,
			long volume) {
		super();
		this.id = id;
		this.name = name;
		this.open = open;
		this.close = close;
		this.high = high;
		this.low = low;
		this.adjClose = adjClose;
		this.stockDate = stockDate;
		this.volume = volume;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getOpen() {
		return open;
	}
	public void setOpen(double open) {
		this.open = open;
	}
	public double getClose() {
		return close;
	}
	public void setClose(double close) {
		this.close = close;
	}
	public double getHigh() {
		return high;
	}
	public void setHigh(double high) {
		this.high = high;
	}
	public double getLow() {
		return low;
	}
	public void setLow(double low) {
		this.low = low;
	}
	public double getAdjClose() {
		return adjClose;
	}
	public void setAdjClose(double adjClose) {
		this.adjClose = adjClose;
	}
	public Date getStockDate() {
		return stockDate;
	}
	public void setStockDate(Date stockDate) {
		this.stockDate = stockDate;
	}
	public long getVolume() {
		return volume;
	}
	public void setVolume(long volume) {
		this.volume = volume;
	}

}
