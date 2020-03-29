/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.io.FileOutputStream;
import java.io.PrintStream;

import java.io.OutputStream;
import java.util.Date;

import javax.xml.stream.events.EndDocument;

import com.btcisp.utils.StringUtil;
import java.text.SimpleDateFormat;

public class SasshiatoTrace {

	public static final String DIR_STDOUT = "stdout";
	public static final String DIR_FILE = "file";
	
	public static final int LEV_REQUIRED = 0;
	public static final int LEV_INFO = 5;
	public static final int LEV_DETAIL = 98;
	public static final int LEV_ALL_EXTRA_DEBUG = 999;
	
	private static int trace_level = LEV_REQUIRED;
	private static String traceDir ;

	private static PrintStream out = System.out;
	private static OutputStream outs;
	private static boolean printExceptionTrace = true;
	public static boolean is2File = false;
	public static boolean includeStdOutAlways = false;
	public static boolean traceDate = true;

	public static void initTrace(String direction, String fileName, int level){
		trace_level = level;
		String printExcTraceS = System.getProperty("logExc");
		if(!StringUtil.isEmpty(printExcTraceS)){
			printExceptionTrace = Boolean.valueOf(printExcTraceS).booleanValue();
		}
		traceDir = direction;
		if(DIR_STDOUT.equals(direction)) {
			out = System.out;
		} else if(DIR_FILE.equals(direction)){
			try {
				outs = new FileOutputStream(fileName);
				out = new PrintStream(outs, true);
				is2File = true;
				System.out.println("Trace is redirected to " + fileName);
			}catch(Exception e){
				System.out.println("ERROR initializing trace");
				e.printStackTrace();
				out = System.out;
			}
		} else {
			out = System.out;
			logError("Trace configuration error, invalid direction specified: " + direction);
		}
		if(printExceptionTrace) log(SasshiatoTrace.LEV_INFO, "Exception Tracing Enabled");
	}
	
	public static String displayDuration(long milisec){
		int hours = (int) Math.floor(milisec / (60* 60*1000));
		milisec = milisec - (hours *60* 60*1000);
		int minutes= (int) Math.floor(milisec / (60*1000));
		milisec = milisec - (minutes *60*1000);
		int seconds = (int) Math.floor(milisec/1000);
		return StringUtil.fill('0', 2-(""+hours).length()) + hours + ":" +StringUtil.fill('0', 2-(""+minutes).length())+ minutes + ":" +StringUtil.fill('0', 2-(""+seconds).length())+ seconds;
	}
	private static java.util.Date startDate = new Date();
	public static void markStart(){
		startDate = new java.util.Date();
	}
	public static void markStart(String what){
		startDate = new java.util.Date();
		log(LEV_REQUIRED, "Processing of "+ what + " started"); 
		if(out!=System.out && !includeStdOutAlways){
			System.out.println("Processing of "+ what + " started");
		}
	}
	
	public static void markFinish(String what){
		Date end = new Date();
		long duration = end.getTime() - startDate.getTime();
		log(LEV_REQUIRED, "Processing of "+ what + " has finished, processing took " + displayDuration(duration));
		if(out!=System.out && !includeStdOutAlways){
			System.out.println("Processing of "+ what + " has finished, processing took " + displayDuration(duration));
		}
	}

	public static final int PROGRESS_DONE= -1;
	public static final int PROGRESS_PAGE=0;
	public static final int PROGRESS_STARTED=-2;
	private static int page_count = 0;
	private static int page_displ_coutn = 0;
	public static void displayProgress(String phase, int page_progress) {
		if(page_progress>0){
			Date end = new Date();
			long duration = end.getTime() - startDate.getTime();
			System.out.println(displayDuration(duration) + " " + phase + " processed " + page_progress + " pages");
		} else if (page_progress == PROGRESS_DONE){
			
			Date end = new Date();
			long duration = end.getTime() - startDate.getTime();
			System.out.println(displayDuration(duration) + " " + phase + " finished" + (page_count>0?(" (" + page_count +" pages)"):""));			
		} else if (page_progress == PROGRESS_STARTED){
			Date end = new Date();
			long duration = end.getTime() - startDate.getTime();
			System.out.println(displayDuration(duration) + " " + phase + " started");						
		} else if (page_progress == PROGRESS_PAGE){
			page_count++;
			page_displ_coutn ++;
			if(page_displ_coutn ==100){
				page_displ_coutn =0;
				displayProgress(phase, page_count);
			}
		}
	}
	public static void displayProgressMessage(String phase, String message) {
		Date end = new Date();
		long duration = end.getTime() - startDate.getTime();
		System.out.println(displayDuration(duration) + " " + phase + " " + message);						
	}
	public static void displayFinalMessage(String message) {
		System.out.println(message);
	}
	public static PrintStream getDirectContent(){
		return out;
	}
	public static boolean isTracing(int level){
		return trace_level >=level;
	}
	public static void close(){
		if(outs!=null){
			try{
				out.close();
				outs.close();
			}catch(Exception e){
				System.out.println("ERROR closing trace");
				e.printStackTrace();
			}
		}
	}
	public static void log(int level, String msg, boolean wrap){
		if(level<=trace_level) {
			println(msg, wrap);
		}
	}
	public static void log(int level, String msg){
		if(level<=trace_level) {
			println(msg, true);
		}
	}


	public static void logError(String msg){
		println("ERROR: " + msg, true);
	}
	
	public static void logError(String msg, Throwable t){
		if(printExceptionTrace) {
			println("ERROR: " + msg, true);
			t.printStackTrace(out);
		}
		else {
			println("ERROR: " + msg, true);
			println("ERROR DETAILS: " + t.getMessage() + " :" + t.getClass().getName(), true);
		}
	}
	
	public static void logWarning(String msg, Throwable t){
		println("WARNING: " + msg, true);
		if(printExceptionTrace) t.printStackTrace(out);
		else
			println("DETAILS: " + t.getMessage() + " :" + t.getClass().getName(), true);
			
	}

	public static void logWarning(String msg){
		println("WARNING: " + msg, true);
	}


	static SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

	private static void println(String s0, boolean wrap){
		String s;
		if (traceDate)
		{
		  Date now = new Date();
		  s = "[" + timeFormatter.format(now) + "] " + s0;
		} else {
			s = s0;
		}
		if(wrap){
		boolean spawnLines = false;
			while(s!=null && s.length()>110){
				String line = s.substring(0, 110);
				s = "->  " + s.substring(110);
				out.println(line);
				if (includeStdOutAlways && out != System.out)
				   System.out.println(line);
				spawnLines = true;
			}
			if(!StringUtil.isEmpty(s)) out.println(s);
			if(spawnLines) {
				out.println();
				if (includeStdOutAlways && out != System.out)
					System.out.println(s);
			}
		} else {
			out.println(s);
			if (includeStdOutAlways && out != System.out)
				System.out.println(s);

		}
		out.flush();
	}
}
