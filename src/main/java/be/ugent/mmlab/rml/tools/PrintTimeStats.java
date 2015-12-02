package be.ugent.mmlab.rml.tools;

public class PrintTimeStats {
	private static double total_duration=0.0;

	public static void printTime(String text, double time_milliseconds) {
		//System.out.println("Time: " + text + " took " + time_milliseconds + " msec");
	}

	public static void addDuration(double duration) {
		total_duration+=duration;
	}
	public static double getDuration(){
		return total_duration;
	}
}
