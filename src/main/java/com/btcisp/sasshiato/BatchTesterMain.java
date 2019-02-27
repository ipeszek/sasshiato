/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.io.File;
import java.io.FilenameFilter;

public class BatchTesterMain {
	Xml_2_Report pdfcreator = new Xml_2_Report();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String dir = "./test_inputs";
		dir =  System.getProperty("sasshiato.in", dir);
		String outdir = "./test_outputs";
		outdir = System.getProperty("sasshiato.out", outdir);
		if(args!=null) { 
          if(args.length>0)
			dir= args[0];
		  if(args.length>1)
			outdir = args[1];
		}
		BatchTesterMain tester = new BatchTesterMain();

		try{
			tester.runTests(dir, outdir);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void runTests(String dirName, String outdir) throws Exception{
		SasshiatoTrace.initTrace(SasshiatoTrace.DIR_STDOUT, null, SasshiatoTrace.LEV_INFO);
		SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "Test inputDir="+ dirName + ", outputDir="+ outdir);

		String watermark = System.getProperty("watermark_file");
		File f = new File(dirName);
		if(!f.exists()){
			throw new Exception("Directory " + dirName + " does not exist");
		}
		if(!f.isDirectory()){
			throw new Exception("" + dirName + " is not directory");
		}
		File[] xmlFiles = f.listFiles( new FilenameFilter(){
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				name= name.toLowerCase();
				if(name.endsWith(".xml"))
					return true;
				else
					return false;
			}
		});
		
		for (int i = 0; i < xmlFiles.length; i++) {
		//for (int i = 0; i < 1; i++) {
			String xmlFile = xmlFiles[i].getAbsolutePath();
			try{
			runTest(xmlFile, watermark, outdir);
			//SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "Processed XML " + xmlFile);
			}catch(Throwable e){
				SasshiatoTrace.logError("Failed on XML " + xmlFile, e);
			}
			if(i < xmlFiles.length-1) SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "");
		}
		SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "Test ended");
		SasshiatoTrace.close();
	}
	
	private void runTest(String xmlFile, String watermark, String outdir) throws Throwable{
		pdfcreator.createReport(xmlFile, watermark, outdir);
		
	}
	

}
