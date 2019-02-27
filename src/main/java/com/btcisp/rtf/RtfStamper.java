/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.rtf;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import com.btcisp.sasshiato.SasshiatoTrace;
import com.btcisp.utils.StringUtil;

public class RtfStamper {
	PrintStream newDocStream;
    //out.println("{\\*\\docrrginfo docstart}");
	public RtfStamper(PrintStream out_) throws Exception{
		newDocStream = out_;
	}
	
	public void restampDocument(int totPages, boolean addParagraphToDocStart, BufferedReader oldDocument, int fontSize, boolean finishWithEmptyParagraph, String debugInfo) throws Exception{
		SasshiatoTrace.displayProgress("RTF Stamping Page Numbers",  SasshiatoTrace.PROGRESS_STARTED);
		String line = oldDocument.readLine();
		int page = 1;
		while(line !=null){
			if(addParagraphToDocStart && line.startsWith("{\\*\\docrrginfo docstart}")){
				SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "Adding empty paragraph at the page top to achieve uniform page look");
				line = line + "\r\n" + RtfWriter.getPageParagraphSeparator();
			}
			
			if(line.indexOf("\\page") !=-1 || line.indexOf("\\sect") !=-1){
				page++;
			}
			
			if(line.indexOf("_PAGE_") !=-1){
				String newTxt = "Page " + page + " of " + totPages;
				line = StringUtil.replace(line, "_PAGE_", newTxt);
			}
			if(finishWithEmptyParagraph && line.startsWith("{\\*\\eofrrginfo ")){ 
				//end of document add empty paragraph
				newDocStream.println("\\pard\\fs"+fontSize+"\\par");
			}

			newDocStream.println(line);
			newDocStream.flush();
			try{
				line = oldDocument.readLine();
			}catch(Exception e){
				SasshiatoTrace.logError("Restamping " + debugInfo + " stopped unexpectedly when reading old document", e);
				line = null;
			}
		}
		SasshiatoTrace.displayProgress("RTF Stamping Page Numbers",  SasshiatoTrace.PROGRESS_DONE);
	}

}
