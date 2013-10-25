package com.plotter.data;


public class Maths {
	
	public static double getDistance(double[] pos1, double[] pos2){
		return Math.sqrt( Math.pow( (pos1[0] - pos2[0]) ,2) + Math.pow( (pos1[1] - pos2[1]) ,2) );
	}
	
	public static double getDistance(int[] pos1, int[] pos2) {
		return Math.sqrt( Math.pow( (pos1[0] - pos2[0]) ,2) + Math.pow( (pos1[1] - pos2[1]) ,2) );
	}
	
	public static double getDistance(int[] pos1, double[] pos2) {
		return Math.sqrt( Math.pow( (pos1[0] - pos2[0]) ,2) + Math.pow( (pos1[1] - pos2[1]) ,2) );
	}
	
	public static double getDistance(double x1, double y1, double x2, double y2){
		return Math.sqrt( Math.pow( (x1 - x2) ,2) + Math.pow( (y1 - y2) ,2) );
	}
	
	public static double getRads(double[] pos1, double[] pos2){
		return Math.atan2(pos2[1] - pos1[1], pos2[0] - pos1[0]);
	}
	
	public static double getRads(double[] pos1, int[] pos2) {
		return Math.atan2(pos2[1] - pos1[1], pos2[0] - pos1[0]);
	}
	
	public static double getRads(int[] pos1, double[] pos2) {
		return Math.atan2(pos2[1] - pos1[1], pos2[0] - pos1[0]);
	}
	
	public static double getRads(int[] pos1, int[] pos2) {
		return Math.atan2(pos2[1] - pos1[1], pos2[0] - pos1[0]);
	}
	
	public static double getRads(double x1, double y1, double x2, double y2){
		return Math.atan2(y2 - y1, x2 - x1);
	}
	
	public static double getDegrees(double[] pos1, double[] pos2){
		return Math.toDegrees(getRads(pos1,pos2));
	}
	
	public static double getDegrees(double[] pos1, int[] pos2) {
		return Math.toDegrees(getRads(pos1,pos2));
	}
	
	public static double getDegrees(double x1, double y1, double x2, double y2){
		return Math.toDegrees(getRads(x1,y1,x2,y2));
	}
	
	public static double getDegrees(int[] pos1, int[] pos2) {
		return Math.toDegrees(getRads(pos1, pos2));
	}
	
	public static double angleDifference(double angle1, double angle2){

		double difference = Math.abs(angle1 - angle2) % 360;

		if(difference > 180){
			difference = 360 - difference;
		}

	    return difference;
	}
	
	public static double angleDifferenceOneWay(double angle1, double angle2){

		double difference = Math.abs(angle1 - angle2) % 360;

	    return difference;
	}
	
	public static int POM(){
		
		if(Math.random() > 0.5){
			return 1;
		}
		else{
			return -1;
		}
	}
	
	public static double crop(double input, double lowerBound, double upperBound){
		if(input > upperBound){
			input = upperBound;
		}
		if(input < lowerBound){
			input = lowerBound;
		}
		return input;
	}
	
	public static boolean estimate(double trueValue, double targetValue, double errorMargin){
		if(targetValue + errorMargin >= trueValue && targetValue - errorMargin <= trueValue){
			return true;
		}
		return false;
	}
	
	public static double mod(double number){
		return Math.sqrt(number * number);
	}
	
	public static boolean contains(Object object, Object[] array){
		for(int i = 0; i < array.length; i++){
			if(array[i] == object){
				return true;
			}
		}
		return false;
	}

}
