package com.pvnsys.ttts.tttsGwtClient.shared.dto;

import java.io.Serializable;
import java.util.List;

import com.pvnsys.ttts.tttsGwtClient.shared.StockVO;
import com.pvnsys.ttts.tttsGwtClient.shared.ta.MacdVO;

public class ChartsDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3322290116237656994L;

	private List<StockVO> data;
	
	public ChartsDTO(List<StockVO> data, MacdVO macd) {
		super();
		this.data = data;
		this.macd = macd;
	}

	private MacdVO macd;
	
    public ChartsDTO() {
    }

	public List<StockVO> getData() {
		return data;
	}

	public void setData(List<StockVO> data) {
		this.data = data;
	}

	public MacdVO getMacd() {
		return macd;
	}

	public void setMacd(MacdVO macd) {
		this.macd = macd;
	}

	
}
