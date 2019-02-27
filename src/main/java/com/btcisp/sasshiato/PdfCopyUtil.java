/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import com.btcisp.utils.StringUtil;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfDestination;
import com.lowagie.text.pdf.PdfOutline;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.SimpleBookmark;

public class PdfCopyUtil {
	private static PdfCopyUtil self = new PdfCopyUtil();
	public static PdfCopyUtil getSelf(){
		return self;
	}
	
	public void combineFiles(String firstFile, String secondFile, String outFile, Properties spprops, boolean isappendable, boolean copyBookmarks) throws Exception{
		File f = new File(firstFile);
		if(!f.exists()){
			throw new Exception("File " + firstFile + " does not exist, cannot append to it");
		}
		f = new File(secondFile);
		if(!f.exists()){
			throw new Exception("File " + secondFile + " does not exist, this file cannot be appended to " + firstFile);
		}
		f = new File(outFile);
		if(f.exists()){
			SasshiatoTrace.log(SasshiatoTrace.LEV_DETAIL, "Deleting " + f.getAbsolutePath() + " for recreation");
			f.delete();
		}
		PdfReader freader = null;
		PdfReader sreader = null;
		FileOutputStream fos = null;
		Document document = null;
		
		try{
			freader = new PdfReader(firstFile);
			//bookmarks
            //freader.consolidateNamedDestinations();
			Map finfo = freader.getInfo();
			if(!StringUtil.isEmpty((String) finfo.get("Author"))) {
				throw new Exception("Cannot append to PDF file, " + firstFile + ", this file was not generated as appendable");
			}
			sreader = new PdfReader(secondFile);
			//bookmarks
            //sreader.consolidateNamedDestinations();
			fos = new FileOutputStream(outFile);
			document = new Document(freader.getPageSizeWithRotation(1));
			
            
			PdfCopy copy = new PdfCopy(document,
			  fos);
			copy.setPdfVersion(PdfWriter.VERSION_1_3);
			if(spprops!=null){
		        String compName = spprops.getProperty("companyName", "btcisp.com");
		        String compShortName = spprops.getProperty("companyShortName", "btcisp");
				document.addCreator("Sasshiato " + SasshiatoVersion.ver +  " (by btcisp.com) licensed to " + compShortName);
				if(!isappendable) document.addAuthor(compName);
			}
			document.open();
			for (int i = 1; i <= freader.getNumberOfPages(); i++) {
				copy.addPage(copy.getImportedPage(freader, i));
			}
			//document.setPageSize(sreader.getPageSizeWithRotation(1)); //we assume same marigin and orient
			for (int i = 1; i <= sreader.getNumberOfPages(); i++) {
				copy.addPage(copy.getImportedPage(sreader, i));
			}


			//bookmarks
			if(copyBookmarks) {
				List oldBookmarks = SimpleBookmark.getBookmark(freader);
				//System.out.println("first =" + l);
	
				List newBookmarks = SimpleBookmark.getBookmark(sreader);
				//System.out.println("second =" + l2);
				
				if(oldBookmarks==null) oldBookmarks = new ArrayList();
				if(newBookmarks==null) newBookmarks = new ArrayList();
				if(oldBookmarks.size() >0 || newBookmarks.size()>0){
					this.markAsNew(newBookmarks);
					List allBookmarks = consolidateBookmarks(oldBookmarks, newBookmarks);
					//recreate old bookmarks
					PdfOutline root = copy.getDirectContent().getRootOutline();
		            recreateBookmarks(allBookmarks, root, freader.getNumberOfPages(), copy);
				}
			}
			SasshiatoTrace.log(SasshiatoTrace.LEV_DETAIL, "Combined document " + outFile + " created");
		}finally{
			if(freader!=null) freader.close();
			if(sreader!=null) sreader.close();
			if(document!=null) document.close();
			if(fos!=null) fos.close();
		}
	}
	
	
	private void markAsNew(List newb){
		if(newb==null) return;
		for(int i = 0; i < newb.size(); i++) {
			Map el = (Map) newb.get(i);
			el.put("new", "true");
			if(el.get("Kids") !=null) {
				List kids = (List) el.get("Kids");
				markAsNew(kids);
			}
		}
	}
	
	private List consolidateBookmarks(List oldb, List newb) {
	    if(oldb==null || oldb.size()==0) {
	    	return newb;
	    }
	    if(newb==null || newb.size()==0){
	    	return oldb;
	    }
		
		Map lastb = (Map) oldb.get(oldb.size() -1);
		Map firstb = (Map) newb.get(0);

		List result = new ArrayList();
		result.addAll(oldb);
		
		if(StringUtil.areEqual((String) lastb.get("Title"), (String) firstb.get("Title"))){
			SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "Conolidating bookmarks " + lastb + "\r\n with \r\n" + firstb);
			List lastl=(List)lastb.get("Kids");
			List fistl=(List)firstb.get("Kids");
			if(lastl !=null && fistl!=null) {
				List newkids = consolidateBookmarks(lastl, fistl);
				lastb.put("Kids", newkids);
			} else {
			}
		} else {
			result.add(firstb);
		}
		for (int i = 1; i < newb.size(); i++) {
			result.add(newb.get(i));
		}
		return result;
	}
	
	private void recreateBookmarks(List bookmarks, PdfOutline parent, int pageIncrement, PdfWriter copy) {
		if(bookmarks.size() >0) {
            for (int i = 0; i < bookmarks.size(); i++) {
				Map bookmark = (Map) bookmarks.get(i);
				String action = (String) bookmark.get("Action");
				int increament = 0;
				if(bookmark.get("new")!=null) increament = pageIncrement;
				SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "PdfCopier: working on old bookmark:" + bookmark);
				if("GoTo".equals(action)){
					PdfOutline outline = recreateBookmark(bookmark, parent, increament, copy);
					if(bookmark.get("Kids") !=null) {
						List kids = (List) bookmark.get("Kids");
						recreateBookmarks(kids, outline, pageIncrement, copy);
					}
				}
			}

		}		
	}
	private PdfOutline recreateBookmark(Map bookmark, PdfOutline parent, int pageIncrement, PdfWriter copy){
		String pageS= (String) bookmark.get("Page");
		String pageI = null;
		int page = -1;
		StringTokenizer st = new StringTokenizer(pageS);
		if(st.countTokens()>0) {
			pageI = st.nextToken();
			page = Integer.parseInt(pageI);
		}
		if(page != -1) {
			String title = (String) bookmark.get("Title");
			final PdfDestination DESTINATION = new PdfDestination(PdfDestination.FIT);
			//new PdfDestination(PdfDestination.XYZ, 0f, 0f, 1f);
			PdfAction actiono = PdfAction.gotoLocalPage(page + pageIncrement, DESTINATION, copy);
			PdfOutline outline = new PdfOutline(parent, actiono, title);
			return outline;
		} else {
			SasshiatoTrace.logError("PdfCopier iText returned invalid page info:" + pageS);
			return null;
		}
		
	}
}
