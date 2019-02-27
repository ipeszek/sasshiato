/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;

import com.btcisp.utils.NonAsciiScan;
import com.btcisp.utils.StringUtil;


public class SasshiatoMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	       String xmldoc = null;
	       String out_dir = null;
	       String watermark_doc = null;
	       String log2 = null;
	       String log2f = null;
	       String log2lev = null;

	       String cpropsStr = System.getProperty("cprops");
	       Properties cprops = null;
		  if(cpropsStr!=null){
			  try{
				  FileInputStream fis = new FileInputStream(cpropsStr);
				  cprops = new Properties();
				  cprops.load(fis);
				  fis.close();		
			  }catch(Exception e){
				  System.err.println("ERROR: could on load properties from " + cpropsStr);
				  e.printStackTrace();
				  try{Thread.sleep(10000);}catch(Exception ex){}
				  return;
			  }
		  } else {
	          cprops = System.getProperties();
		  }
		  
		  xmldoc = cprops.getProperty("xml_in_file");
	      out_dir = cprops.getProperty("out_dir");
	      watermark_doc = cprops.getProperty("watermark_file");
	      log2 = cprops.getProperty("log2", "file");
	      log2f = cprops.getProperty("log2f");
	      log2lev = cprops.getProperty("log2lev", "0");
	      //String home = cprops.getProperty("sasshiato_home");
	      //if(home!=null) System.setProperty("sasshiato_home", home);
	      
	      
	      
	       int traceLevel = Integer.parseInt(log2lev);
	        
	       if(xmldoc!=null && (xmldoc.trim().equals("") || xmldoc.trim().equals("NONE"))){
	    	   xmldoc=null;
	       }
	       if(out_dir!=null && (out_dir.trim().equals("")|| out_dir.trim().equals("NONE"))){
	    	   out_dir=null;
	       }
	       if(watermark_doc!=null && (watermark_doc.trim().equals("")|| watermark_doc.trim().equalsIgnoreCase("NONE"))){
	    	   watermark_doc=null;
	       }
	       if(log2f!=null && (log2f.trim().equals("")|| log2f.trim().equals("NONE"))){
	    	   log2f=null;
	       }
	       
	        File trace2 = null;
	        String tracePath=null;
	        if(log2f!=null) {
	        	trace2 = new File(log2f);
	        	tracePath = trace2.getAbsolutePath();
	        } else if("file".equals(log2)){
	        	System.out.println("Usage: provide -Dlog2f if -Dlog2=file");
				  try{Thread.sleep(10000);}catch(Exception ex){}
				  return;
	        }
	       
	        SasshiatoTrace.initTrace(log2, tracePath, traceLevel);
	       
	        SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "Sashiato invoked with properties " + cprops);
	        if(xmldoc==null){
	        	SasshiatoTrace.logError("Missing xml_in_file property");
	        	SasshiatoTrace.close();
				  return;

	        }
			
	        File xmlf = new File(xmldoc);
	        if(!xmlf.exists()){
	        	SasshiatoTrace.logError("Specified xml file does not exist: " + xmlf.getAbsolutePath());
	        	SasshiatoTrace.close();
	        	return;	        	
	        }
	        
	        String xmlfname = xmlf.getName();
	        File xmldir = xmlf.getAbsoluteFile().getParentFile();
	        
	        String logname = StringUtil.replace(xmlfname, ".xml", ".str");
	        if(!logname.endsWith(".str"))
	        	logname = logname + ".str";
	        
	        if(out_dir!=null) {
		        File outDir = new File(out_dir);
		        if(outDir.exists() && !outDir.isDirectory()) {
		        	SasshiatoTrace.logError("Specified output directory is not a directory");
		        	SasshiatoTrace.close();
		        	return;
		        }
		        if(!outDir.exists()){
		        	if(!outDir.mkdirs()){
		        		SasshiatoTrace.logError("Failed creating directories " + outDir.getAbsolutePath());
		        		SasshiatoTrace.close();
		        		return;
		        	}
		        }
	        }

	        String watermarkmsg=watermark_doc==null?"<none>": watermark_doc;
			SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "INFO: SASSHIATO invoked with xml_in_file="+ xmldoc +  ", water_mark="+ watermarkmsg + ", out_dir="+ out_dir + ", log2lev=" + log2lev);

			try{
				//SasshiatoS sse = new SasshiatoS();
				//sse.check(sec);
				
				if(traceLevel == 101){
					NonAsciiScan scan = new NonAsciiScan();
					ArrayList ampers = new ArrayList();
					SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "(101) Sasshiato file pre-scan requested\n");
					String[] non_ascii_lines = scan.findNonAscii(xmldoc, ampers, SasshiatoTrace.getDirectContent());
					if(non_ascii_lines.length ==0){
						SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "(101) No non-ASCII characters found");
					}
//					else {
//							StringBuffer sb = new StringBuffer();
//							sb.append("(101) List of lines with non-ASCII characters:\n");
//							for (int i = 0; i < non_ascii_lines.length; i++) {
//								sb.append(non_ascii_lines[i]).append("\n");
//							}
//							SasshiatoTrace.logError(sb.toString());
//					}
					if(ampers.size()==0){
						SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "(101) No & characters found");
					} else {
						StringBuffer sb = new StringBuffer();
						sb.append("(101) List of lines with & characters:\n");
						for (int i = 0; i < ampers.size(); i++) {
							sb.append(ampers.get(i)).append("\n");
						}
						SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, sb.toString());					
					}
				}
				Xml_2_Report creator = new Xml_2_Report();
				creator.createReport(xmldoc, watermark_doc, out_dir);
			}catch(Throwable e){
				SasshiatoTrace.logError("processing " + xmldoc + ":", e);
			}finally{
				SasshiatoTrace.close();
			}

			//try{ Thread.sleep(5000);}catch(Exception e){}

	}

}
