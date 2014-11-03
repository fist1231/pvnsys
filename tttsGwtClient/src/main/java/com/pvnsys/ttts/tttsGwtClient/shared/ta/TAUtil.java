package com.pvnsys.ttts.tttsGwtClient.shared.ta;

import org.apache.commons.lang.ArrayUtils;

import com.pvnsys.ttts.tttsGwtClient.shared.common.ListUtils;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;

public class TAUtil {

	public static MacdVO getMacd(Double[] close) {

		int size = close.length;

		MInteger begin = new MInteger();
//		begin.value = 33;
		MInteger length = new MInteger();
//		length.value = -1;

		double[] outMACD = new double[size];
		double[] outMACDSignal = new double[size];
		double[] outMACDHist = new double[size];
		double[] close_d =  ArrayUtils.toPrimitive(close);
		
		
		/*
		 *  begin is not a index in the out array, it's an indicator that out[0] corresponds to closePrice[begin]
		 */

		Core core = new Core();
//		RetCode rc = core.macdFix(0, close.length - 1, close_d, 9, begin, length, outMACD, outMACDSignal, outMACDHist);
//		if(rc.Success.equals(rc));
		core.macd(0, close.length - 1, close_d, 12, 26, 9, begin, length, outMACD, outMACDSignal, outMACDHist);
//		core.macdExt(0, close.length - 1, close_d, 12, MAType.Ema, 26, MAType.Ema, 9, MAType.Ema, begin, length, outMACD, outMACDSignal, outMACDHist);

		MacdVO result = new MacdVO(
				ListUtils.toListOfDoubles(outMACD),
				ListUtils.toListOfDoubles(outMACDSignal),
				ListUtils.toListOfDoubles(outMACDHist),
				begin.value,
				length.value
				);

		return result;
	}
	
	
}
