package com.pvnsys.ttts.tttsGwtClient.shared.common;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {

	public static List<Double> toListOfDoubles(double[] in) {
		List<Double> out = new ArrayList<Double>(in.length);
		for(int i=0; i<in.length; i++) {
			out.add(new Double(in[i]));
		}
		return out;
	}

	public static double[] toArrayOfDoubles(List<Double> in) {
		
		double[] out = new double[in.size()];
		for(int i=0; i<in.size(); i++) {
			out[i] = in.get(i).doubleValue();
		}
		return out;
	}


}
