package com.pvnsys.ttts.tttsGwtClient.shared.ta;

import java.io.Serializable;
import java.util.List;

public class MacdVO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8614965832705917110L;
	private List<Double> outMACD;
    private List<Double> outMACDSignal;
    private List<Double> outMACDHist;
    private int begin;
    private int length;
    
    public MacdVO() {
    }
    
    public MacdVO(List<Double> outMACD, List<Double> outMACDSignal,
			List<Double> outMACDHist, int begin, int length) {
		super();
		this.outMACD = outMACD;
		this.outMACDSignal = outMACDSignal;
		this.outMACDHist = outMACDHist;
		this.begin = begin;
		this.length = length;
	}
    
    public List<Double> getOutMACD() {
		return outMACD;
	}
	public void setOutMACD(List<Double> outMACD) {
		this.outMACD = outMACD;
	}
	public List<Double> getOutMACDSignal() {
		return outMACDSignal;
	}
	public void setOutMACDSignal(List<Double> outMACDSignal) {
		this.outMACDSignal = outMACDSignal;
	}
	public List<Double> getOutMACDHist() {
		return outMACDHist;
	}
	public void setOutMACDHist(List<Double> outMACDHist) {
		this.outMACDHist = outMACDHist;
	}
	public int getBegin() {
		return begin;
	}
	public void setBegin(int begin) {
		this.begin = begin;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}

	
}
