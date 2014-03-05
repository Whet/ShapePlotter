package com.plotter.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// http://code.hammerpig.com/quartiles-java.html
public class Quartiles {

	public static double[] quartiles(List<Double> values) {
	 
	    double median = median(values);
	 
	    List<Double> lowerHalf = getValuesLessThan(values, median, true);
	    List<Double> upperHalf = getValuesGreaterThan(values, median, true);
	 
	    return new double[] {median(lowerHalf), median, median(upperHalf)};
	}
	
	public static double median(List<Double> values)
	{
	    Collections.sort(values);
	 
	    if (values.size() % 2 == 1)
		return values.get((values.size()+1)/2-1);
	    else
	    {
		double lower = values.get(values.size()/2-1);
		double upper = values.get(values.size()/2);
	 
		return (lower + upper) / 2.0;
	    }	
	}
	 
	public static List<Double> getValuesGreaterThan(List<Double> values, double limit, boolean orEqualTo)
	{
		List<Double> modValues = new ArrayList();
	 
	    for (double value : values)
	        if (value > limit || (value == limit && orEqualTo))
	            modValues.add(value);
	 
	    return modValues;
	}
	 
	public static List<Double> getValuesLessThan(List<Double> values, double limit, boolean orEqualTo)
	{
		List<Double> modValues = new ArrayList();
	 
	    for (double value : values)
	        if (value < limit || (value == limit && orEqualTo))
	            modValues.add(value);
	 
	    return modValues;
	}
	
	public static double interQuartileRange(List<Double> values) {
		double[] quartiles = quartiles(values);
		return quartiles[2] - quartiles[0];
    }
	
}
