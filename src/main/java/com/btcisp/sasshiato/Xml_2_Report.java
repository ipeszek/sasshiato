/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.security.acl.LastOwnerException;
import java.util.*;


import com.btcisp.rrg.rcdparser.RcdInfo;
import com.btcisp.rrg.rcdparser.RcdRow;
import com.btcisp.rrg.rcdparser.RcdXmlParser;
import com.btcisp.rtf.RtfDocFormat;
import com.btcisp.rtf.RtfParagraphFormat;
import com.btcisp.rtf.RtfRowFormat;
import com.btcisp.rtf.RtfStamper;
import com.btcisp.rtf.RtfWriter;
import com.btcisp.rtf.Sasshiato2RTFUtil;
import com.btcisp.sasshiato.DocumentFactory;
import com.btcisp.sasshiato.PageGroup;
import com.btcisp.sasshiato.PageVerticalSplitCalculator;
import com.btcisp.sasshiato.ReportLaF;
import com.btcisp.sasshiato.ReportSetup;
import com.btcisp.sasshiato.TableWidthCalculator;
import com.btcisp.sasshiato.TableWidths;
import com.btcisp.sasshiato.rtf.RtfPrinterUtil;
import com.btcisp.sasshiato.simple_info.InfoGenerator_Simple;
import com.btcisp.sasshiato.simple_info.ReportPadListener_SimpleInfoGenerator;
import com.btcisp.utils.StringUtil;
import com.btcisp.utils.sasxml.SasParserException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageLabels;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.RandomAccessFileOrArray;

public class Xml_2_Report {
    SasshiatoSP sp = new SasshiatoSP();
	/**
	 * If 
	 * @param xmlDocument
	 * @param outdir - if null the output folder is read from RcdInfo
	 * @throws Exception
	 */
	public List<File> createReport(String xmlDocument, String watermark_doc, String outdir) throws Throwable {
		// HeaderTablet: gives us header columns: column text and alignment and
		// span;
		// pages: gives us column groups for pages, with column ID and width,
		// including pageNumber
		// which indicates page numbers if columns do not fit on one page (this
		// is like sub-page number);
		// fontsize, papersize font_tn are properties of Main;
		// tableWidth, tableHeight are properties of Main;
		SasshiatoTrace.markStart();
		SasshiatoTrace.displayProgress("Document Preparation",  SasshiatoTrace.PROGRESS_STARTED);
        List<File> result = new ArrayList<File>();
		File watermarkF = null;
        if(watermark_doc!=null){
        	watermarkF = new File(watermark_doc);
        	if(!watermarkF.exists()){
        		throw new Exception("Watermark file " + watermarkF.getAbsolutePath() + " does not exist");
        	}		
        }

        
        Properties spprops = sp.getSeProperties();
        if(spprops==null) {
        	SasshiatoTrace.logError("Missing internal configuration");
        	return result;
        }
        String compName = spprops.getProperty("companyName", "btcisp.com");
         
		RcdXmlParser p = null;
		DocumentHolder documentholder = null;
		String outDocPath=null;
		String outDocPathTmpAppended=null;
		String outDocPathBase=null;
		String outDocPathFinal = null;
		RcdRow lastParsed = null;
		boolean isEmptyTable = false;
		boolean isPdfOut = false;
		Date start = new Date();
		try {
			SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "SASSHIATO "+ SasshiatoVersion.ver +" (BTCISP product, www.btcisp.com) \n    powered by iText v2.1.4"); 
			if(!"btcisp.com".equals(compName)) 
				SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "This product is licensed to " + compName + "\n"); 
			else 
				SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "SASSHIATO is the property of Izabella Peszek" + "\n"); 
				
			SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "INFO: Processing " + xmlDocument + " started");


			int numberOfColumns = 0;
			// Document doc2 = new Document();
			// when scanning for actual printing, print header if
			// foundtableBody=true;

			// FIRST SCAN
			p = new RcdXmlParser(xmlDocument);
			p.open();
			RcdRow firstLine = p.nextRow();
			lastParsed = firstLine;
			if(firstLine==null) {
				isEmptyTable = true;
				numberOfColumns = 1;
				SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "INFO: xml contains no data body rows - table is empty"); 
			} else {
				numberOfColumns = firstLine.getColSize();
			}
			// get __fontsize;
			RcdInfo info = p.getRcdInfo();
			SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "INFO: " + info); 
		
			ReportLaF laf = new ReportLaF(info, numberOfColumns);
			
			//static preconfigure setup (this introduces no thread safe logic)
			Properties props = info.getSystemProps();
			String pgsepparfs = props.getProperty("rtf_pgsepparfs", "");
			if(!StringUtil.isEmpty(pgsepparfs)){
				try{
					int ppfs = Integer.parseInt(pgsepparfs);
					if(ppfs==-1){
						RtfWriter.setPageParagraphSeparator("{\\pard\\par}");
					} else {
						RtfWriter.setPageParagraphSeparator("{\\fs"+ ppfs + "\\pard\\par}");
					}
				}catch(Exception e){
					SasshiatoTrace.logError("Invalid rtf_pgsepparfs passed in sprops:" + pgsepparfs, e);
				}
				
			} else {
				RtfWriter.setPageParagraphSeparator("{\\fs0\\pard\\par}"); //default
			}
			//end preconfigure


			int numberOfHeaderColumns = info.getLastcheadid() + 1;
			ReportSetup reportSetup = new ReportSetup(
					numberOfColumns, numberOfHeaderColumns,
					info, laf, isEmptyTable, spprops);

			isPdfOut = reportSetup.isPdfOut();
			String fname = info.getFilename();
			if(outdir==null) outdir = info.getPath();
			File outdirF = new File(outdir);
			 
			if(!outdirF.exists()){
				SasshiatoTrace.log(SasshiatoTrace.LEV_INFO,"INFO: " +outdir + " directory does not exist, creating ...");
				if(outdirF.mkdirs())
					SasshiatoTrace.log(SasshiatoTrace.LEV_INFO,"INFO: created " + outdirF.getAbsolutePath());
			}
			
			if(!outdirF.isDirectory()) throw new Exception(outdirF + " not a directory");
			outDocPathBase = outdir + "/" + fname;
			outDocPath = outDocPathBase + ".tmp";
			outDocPathTmpAppended = outDocPathBase + ".tm2";
			outDocPathFinal = outDocPathBase + ".pdf";
			
			//old location of document creation

			if(!isEmptyTable) {
				ArrayList specialListeners = new ArrayList();
				TableWidthCalculator widthCalc = new TableWidthCalculator(reportSetup);
				widthCalc.init(laf, numberOfColumns);
	
				for (RcdRow line = firstLine; line != null; line = p.nextRow()) {
					lastParsed = line;
					widthCalc.inspect(line);
				}
	
				
				TableWidths tableWidths = widthCalc.getTableWidths();
	
				StringBuffer traceInfo = new StringBuffer();
				float tots = 0;
				for (int i = 0; i < tableWidths.getActualWidths().length; i++) {
					traceInfo.append("size(col_" +i).append(")=").append( tableWidths.getActualWidths()[i]).append(",");
					tots = tots + tableWidths.getActualWidths()[i];
				}
				traceInfo.append("TOT= " + tots);
				SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED,"Computed table sizes=" + traceInfo);
								
				PageVerticalSplitCalculator pageCalc = new PageVerticalSplitCalculator(reportSetup, tableWidths);
				PageGroup[] pages = pageCalc.calculatePageAssignment();
				for (int i = 0; i < pages.length; i++) {
					pages[i].adjustToStetch(reportSetup);
				}
				// this is number of pages needed to display all columns;
				// start printing table;
				
				if(reportSetup.isGenerateSimpleSizingInfo()){
					InfoGenerator_Simple infogen = new InfoGenerator_Simple();
					if(infogen.init(laf, reportSetup, outDocPathBase, tableWidths, pages)) {
						infogen.generateSizingInfo(reportSetup, info, laf);
						ReportPadListener_SimpleInfoGenerator listener = new ReportPadListener_SimpleInfoGenerator(infogen);
						specialListeners.add(listener);
					}
				}
				/*
				 * PdfWriter writer2 = PdfWriter.getInstance(doc2, new
				 * FileOutputStream("f:/pdfs/"+"test01"+".pdf"));
				 */
				documentholder = DocumentFactory.getSelf().createDocument(
						reportSetup, info, laf, pages, tableWidths, "xmldoc=" + xmlDocument, outDocPath, outDocPathBase, false, watermarkF, (ReportPadListener[]) specialListeners.toArray(new ReportPadListener[0]));
				configurePdf(documentholder.getDocument(), documentholder.getPdfWriter(), reportSetup);
				if(reportSetup.isRtfOut()) configureRtf(documentholder);

				Document document = documentholder.getDocument();
				document.open();
				// doc2.open();
				
				
				if(laf.isInText()){
					ReportBuilder_InText builder = new ReportBuilder_InText();
					builder.init(pages, reportSetup, laf, documentholder, tableWidths, fname + ".pdf");
					builder.buildTitles(info, laf);
					builder.buildFootnotes(info, laf);
			
					// System.out.println("numberOfSubpages" + numberOfSubpages);
					for (int i = 0; i < pages.length; i++) {
						builder.setCurrentPageGroup(pages[i], i==pages.length-1);
						p.close();
						p.reopen();
						firstLine = p.nextRow();
						for (RcdRow line = firstLine; line != null; line = p.nextRow()) {
							builder.buildRow(line, info, document, laf);
						}
				
				        builder.finish(document, laf);						
					}
					// SECOND SCAN
				
				} else {
					ReportBuilder builder = new ReportBuilder();
					builder.init(pages, reportSetup, laf, documentholder, tableWidths, fname + ".pdf");
					builder.buildTitles(info, laf);
					builder.buildFootnotes(info, laf);
			
					// System.out.println("numberOfSubpages" + numberOfSubpages);
			
					// SECOND SCAN
					p.close();
					p.reopen();
					firstLine = p.nextRow();
					for (RcdRow line = firstLine; line != null; line = p.nextRow()) {
						builder.buildRow(line, info, document, laf);
					}
			
			        builder.finish(document, laf);
				}
		        
			} else {
				//print empty table
				documentholder = DocumentFactory.getSelf().createDocument(
						reportSetup, info, laf, null, null, "xmldoc=" + xmlDocument, outDocPath, outDocPathBase, true, watermarkF, null);
				configurePdf(documentholder.getDocument(), documentholder.getPdfWriter(), reportSetup);
				if(reportSetup.isRtfOut()) configureRtf(documentholder);

				if(reportSetup.isGenerateSimpleSizingInfo()){
					InfoGenerator_Simple infogen = new InfoGenerator_Simple();
					if(infogen.initForEmptyTable(laf, reportSetup, outDocPathBase)){
						infogen.generateSizingInfo(reportSetup, info, laf);
						try{
						infogen.generateHeaderInfo(null);
						}catch(Exception e){
							SasshiatoTrace.logError("Simple Info Generation Error", e);
						}
						//infogen.generateHeaderSplitPointsInfo(null);
					}
				}

				float padding= processEmptyTableDocument_PDF(documentholder, reportSetup, laf, info, watermark_doc);
				if(reportSetup.isRtfOut()) processEmptyTableDocument_RTF(outDocPathBase, documentholder, reportSetup, laf, info, watermarkF, padding);
			}
			if(reportSetup.isPdfOut() && reportSetup.isBookmarksEnabled() && reportSetup.getBookmarksPdf() !=null) {
				(new PdfBookmarkService()).handleBookmarks(documentholder.getPdfWriter(), info, reportSetup, laf);
			}
			//long duration =  (new Date()).getTime() - start.getTime();
			//SasshiatoTrace.log(SasshiatoTrace.LEV_INFO,"INFO: before page Stamping duration=" + duration + "(ms)");
			
			p.close();
			p=null;
			documentholder.close(reportSetup.isPdfOut());
			SasshiatoTrace.displayProgress("Document Preparation",  SasshiatoTrace.PROGRESS_DONE);

			//documentholder=null;
			if(reportSetup.isPdfOut()){
				String currentFileName = outDocPath;
				if(reportSetup.isAppend()){
					combinePDFs(outDocPathFinal, outDocPath, outDocPathTmpAppended, reportSetup);
			        File f = new File(currentFileName);
					SasshiatoTrace.log(SasshiatoTrace.LEV_DETAIL, "File " + f.getAbsolutePath() + " deleted for cleanup");
			        f.delete();
					currentFileName = outDocPathTmpAppended;						
				}
				if(!reportSetup.isAllowFutureAppend()) {
					int startFromPage=1;
					SasshiatoTrace.displayProgress("PDF Stamping Page Numbers",  SasshiatoTrace.PROGRESS_STARTED);

					while(startFromPage !=-1){
						startFromPage = restampPages(currentFileName, outDocPathFinal, reportSetup, laf, info, watermarkF, startFromPage);
				        if(startFromPage != -1) {
							SasshiatoTrace.displayProgressMessage("PDF Stamping Page Numbers",  "Stamping Chunk Completed");
						    File f = new File(currentFileName);
							SasshiatoTrace.log(SasshiatoTrace.LEV_DETAIL, "File " + f.getAbsolutePath() + " deleted for cleanup");
					        f.delete();
					        File outFile = new File(outDocPathFinal);
					        outFile.renameTo(f);
				        }
					}
			        File f = new File(currentFileName);
					SasshiatoTrace.log(SasshiatoTrace.LEV_DETAIL, "File " + f.getAbsolutePath() + " deleted for cleanup");
			        f.delete();
					SasshiatoTrace.displayProgress("PDF Stamping Page Numbers",  SasshiatoTrace.PROGRESS_DONE);
				} else {
					File dest = new File(outDocPathFinal);
			        dest.delete();
			        File gen = new File(currentFileName);
			        gen.renameTo(dest);					
				}
				result.add(new File(outDocPathFinal));
			}
			if(reportSetup.isRtfOut() && !reportSetup.isAllowFutureAppend() && laf.isRestampingRequired()){
				String totPagesS = (String) documentholder.getStratchBoard().get("rtf.totPages");
				int totPagesI = Integer.parseInt(totPagesS);
				File f = new File(outDocPathBase + ".rtf");
				File fback = new File(outDocPathBase + ".rtbk");
				if(fback.exists())
					if(!fback.delete()) {
						throw new Exception("Cannot delete file " + fback.getAbsolutePath());
					} else {
						SasshiatoTrace.log(SasshiatoTrace.LEV_DETAIL, "File " + fback.getAbsolutePath() + " deleted in preparation for replacement");			
					}
				if(!f.renameTo(fback)){
					throw new Exception("Error renaming " + f.getAbsolutePath() + " to " + fback.getAbsolutePath());
				}
				FileOutputStream fw = null;
				PrintStream pw = null;
				BufferedReader br = null;
				FileReader fr = null;
				try{
				   fr = new FileReader(fback);
				   br = new BufferedReader(fr);
				   fw = new FileOutputStream(f);
				   pw = new PrintStream(fw);
				   RtfStamper stamper = new RtfStamper(pw);
				   stamper.restampDocument(totPagesI, totPagesI>1 || laf.isRftCombinable(), br, Sasshiato2RTFUtil.getSelf().convert2FontSize(laf.getFontSize()), true, outDocPathBase);
				} finally{
					if(pw!=null) pw.close();
					if(fw!=null) fw.close();
					if(br !=null) br.close();
					if(fr != null) fr.close();
					if(fback.exists() )
						if(!fback.delete()) {
						} else {
							SasshiatoTrace.log(SasshiatoTrace.LEV_DETAIL, "File " + fback.getAbsolutePath() + " deleted for cleanup");			
						}
				}
				SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "Restamping of rtf document " + outDocPathBase + " has finished");
				result.add(f);
			}
			if(isEmptyTable) SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED,"INFO: document " + outDocPathFinal + " generated as empty table");
			//duration =  (new Date()).getTime() - start.getTime();
			if(documentholder !=null) {
				if(reportSetup.isAppend()) SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED,"INFO: document(s) " + documentholder.getDocumentsInfo() + " have been appended to previously existing " + outDocPathBase);
				else SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED,"INFO: document(s) " + documentholder.getDocumentsInfo() + " have been created");
			}
			if(reportSetup.isAllowFutureAppend()) SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED,"INFO: this document(s) is(are) not complete, other documents will be appended");
			else SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED,"INFO: this document(s) is(are) final");
			//SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED,"INFO: Processing " + xmlDocument + " ended, duration=" + duration + "(ms)");
			//SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED,"INFO: Processing " + xmlDocument + " ended");
			SasshiatoTrace.markFinish(xmlDocument);
			return result;
			// doc2.close();
		} catch(Throwable e){
			if(e instanceof SasParserException && lastParsed !=null) {
				SasshiatoTrace.logError("XML parsing error (re-run with debug=101 for more info), last successfully parsed record=" + lastParsed, e);				
			} else 
				SasshiatoTrace.logError("when generating PDF file, xml input=" + xmlDocument, e);
			throw e;
		} finally {
			if (p != null)
				p.close();
            if(documentholder!=null)
            	documentholder.close(isPdfOut);
		}
	}
	
    public List<File> moveToFinalDest (String finalFolder, List<File> files) {
		List<File> res = new ArrayList<File>();
		for (File f : files) {
			File dest = new File (finalFolder, f.getName());
			if(f.exists()) {
				if(dest.exists()) {
					if (dest.delete()) 
						SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "deleted old file " + dest.getAbsolutePath());
					else 	
					    SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "error deleting old file " + dest.getAbsolutePath());
				} 
				if(f.renameTo(dest))
					res.add(dest);
				else 
				    SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "ERROR moving " + f.getAbsolutePath());
			} else {
				SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "ERROR No working file " + f.getAbsolutePath());
			}
		}
		return res;
	}

	private void configureRtf(DocumentHolder documentholder){
		//logic perfomed by listener
	}
	private void configurePdf(Document document, PdfWriter writer, ReportSetup setup){
        Properties spprops = sp.getSeProperties();
        if(spprops==null) {
        	SasshiatoTrace.logError("Missing internal configuration");
        	return;
        }
        String compName = spprops.getProperty("companyName", "btcisp.com");
        String compShortName = spprops.getProperty("companyShortName", "btcisp");
		writer.setPdfVersion(PdfWriter.VERSION_1_3);
        String lic_2= "";
        if(!"btcisp.com".equals(compName))
        	lic_2= " licensed to " + compShortName;
        else 
        	lic_2 = " this software is the property of Izabella Peszek";

		document.addCreator("Sasshiato " + SasshiatoVersion.ver +  " (by btcisp.com)" + lic_2);
		if(!setup.isAllowFutureAppend()) document.addAuthor(compName);
	}
	
	private void processEmptyTableDocument_RTF(String baseFilePath, DocumentHolder documentholder, ReportSetup reportSetup, ReportLaF laf, RcdInfo info, File watermark_file, float padding) throws Exception{
		String[] streamTypes = documentholder.getOtherStreamTypes();
		PrintStream rtf = null;
		documentholder.getStratchBoard().put("rtf.totPages", "1");
		for (int i = 0; i < streamTypes.length; i++) {
			String type = streamTypes[i];
			if("rtf".equals(type)){
				rtf = documentholder.getOtherPrintStreams()[i];
				RtfWriter writer = new RtfWriter(rtf);
				Sasshiato2RTFUtil convUtil = Sasshiato2RTFUtil.getSelf();
				RtfDocFormat df = convUtil.convert(info, reportSetup, laf);
				if(reportSetup.isAppend()) {
					Map infoM = writer.openWithAppend(baseFilePath + ".rtftmp", df, reportSetup.isAllowFutureAppend(), laf.useSectionBreaksWhenAppending(), !laf.isRtfPageStartsWithParagraph());
					SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "Appending to rtf document " + infoM);
					String totPagesS = (String) infoM.get("rtf.totPages");
					int totPages = Integer.parseInt(totPagesS) + 1;
					documentholder.getStratchBoard().put("rtf.totPages", totPages + "");
					File fx = new File(baseFilePath + ".rtftmp");
					if(fx.delete())
						SasshiatoTrace.log(SasshiatoTrace.LEV_DETAIL, "File " + baseFilePath + ".rtftmp" + " deleted for cleanup");
					else 
						SasshiatoTrace.logError("Cannot delete file " + fx.getAbsolutePath());
				} else {
					writer.open(df, reportSetup.isAllowFutureAppend());
				}

				if(laf.isSystemHeaderInRtfHeader() || watermark_file !=null){
					writer.beginHeader(convUtil.convert2FontSize(laf.getFontSize("systemHeader")));
					if(laf.isSystemHeaderInRtfHeader()) RtfPrinterUtil.getSelf().printSystemHeaders(writer, laf, info, reportSetup);
					if(watermark_file !=null) RtfPrinterUtil.getSelf().printBackgroundImage(writer, laf, reportSetup, watermark_file);
					writer.endHeader();
				}

				if(laf.isSystemFooterInRtfFooter()){
					writer.beginFooter(convUtil.convert2FontSize(laf.getFontSize("systemFootnote")));
					writer.beginNewFontSize(convUtil.convert2FontSize(laf.getFontSize("systemFootnote")));
					RtfPrinterUtil.getSelf().printSystemFootnotes(writer, laf, info, reportSetup, 0, laf.isSeparatorAboveFootnotes());
					writer.endNewFontSize();
					writer.endFooter();
				}

				if(laf.isSystemHeaderPartOfTable()){
					RtfPrinterUtil.getSelf().printSystemHeaders(writer, laf, info, reportSetup);				
				}
				Map oldTitleIndexes = new HashMap();
				StyledText[] titles = RrgPrinterUtil.getSelf().prepareTitles(info, oldTitleIndexes, laf);
				RtfPrinterUtil.getSelf().printTiltesRows(writer, titles, oldTitleIndexes, info, reportSetup, laf,  reportSetup.getTableWidth(), true, null);
				RtfRowFormat rf = new RtfRowFormat();
				String border = RtfRowFormat.BORDER_TOP;
				if(laf.isSeparatorBelowTable()) border = RtfRowFormat.BORDER_BOTTOM + border;
				rf.configure(new int[]{convUtil.convert2Twips(reportSetup.getTableWidth())}, RtfRowFormat.TABLE_ALIGN_CENTER, null, border, false);
				writer.beginRow(rf);
				RtfParagraphFormat pformat = new RtfParagraphFormat();
				pformat.configure(RtfParagraphFormat.ALIGN_LEFT, 0, 0, 0, 0, convUtil.convert2Twips(laf.getParagraphLeading("body")), convUtil.convert2Twips(laf.getIndentSize()), convUtil.convertFont2FFInstructions(laf.getFont("body")));
				 String txt = info.getNodatamsg();
				 if(StringUtil.isEmpty(txt))
					 txt = "No Data";
				 
				StyledText txtMsg = StyledText.withConfiguredStyle(txt, ReportLaF.STYLE_TYPE_BODY, laf);
				RtfPrinterUtil.getSelf().formatTextAsCell(writer, txtMsg, laf, reportSetup, ReportLaF.STYLE_TYPE_BODY, pformat, 0);
				writer.endRow(false);
				
				
				if(laf.isSystemFooterPartOfTable()){
					writer.beginNewFontSize(convUtil.convert2FontSize(laf.getFontSize("systemFootnote")));
					RtfPrinterUtil.getSelf().printSystemFootnotes(writer, laf, info, reportSetup, padding -0, laf.isSeparatorAboveFootnotes());					
					writer.endNewFontSize();
				}
				SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED, "Closing rft document with " + documentholder.getStratchBoard());
				writer.close(documentholder.getStratchBoard());
			}
			
		}
	}
	
	private float processEmptyTableDocument_PDF(DocumentHolder documentholder, ReportSetup reportSetup, ReportLaF laf, RcdInfo info, String watermark_file) throws Exception{
		Document document = documentholder.getDocument();
		PdfWriter writer = documentholder.getPdfWriter();
		document.open();
		PdfPTable shead = SHeader_PdfTableStorage.getSelf().buildSTable_FirstRun(info, laf, reportSetup);
		//PdfPTable shead = SHeader_PdfTableStorage.getSelf().buildSTable_Stamping(1, 1, laf, info, reportSetup);
 		document.add(shead);
 		Map oldTitleIndexes = new HashMap();
 		StyledText[] titlesA = RrgPrinterUtil.getSelf().prepareTitles(info, oldTitleIndexes, laf);
		PdfPTable titles = PdfPrinterUtil.getSelf().printTiltesRows(titlesA, info, reportSetup, laf, reportSetup.getTableWidth(), null);
		 document.add(titles);
		 PdfPTable body = new PdfPTable(1);
		 body.setTotalWidth(reportSetup.getTableWidth());
		 body.setWidthPercentage(100f);
		 String txt = info.getNodatamsg();
		 if(StringUtil.isEmpty(txt))
			 txt = "No Data";
		 PdfPCell emptycell = new PdfPCell();
		 emptycell.setBorder(PdfPCell.NO_BORDER);
		 emptycell.setFixedHeight(laf.getAfterTitlePadding() + laf.getPdfPaddingAdjustement());
		 body.addCell(emptycell);
		 StyledText txtMsg = StyledText.withConfiguredStyle(txt, ReportLaF.STYLE_TYPE_BODY, laf);
		 PdfPCell cell = PdfPrinterUtil.getSelf().formatTextAsCell(txtMsg, laf, "body", 0f, Element.ALIGN_LEFT);
		 if(laf.isSeparatorBelowTable())
			 cell.setBorder(PdfPCell.TOP + PdfPCell.BOTTOM);
		 else
			 cell.setBorder(PdfPCell.TOP);
		 cell.setPaddingBottom(laf.getAfterBodyPadding() + laf.getPdfPaddingAdjustement());
		 cell.setPaddingLeft(0f);
		 body.addCell(cell);
		 document.add(body);
		 
		 PdfPTable sftable = PdfPrinterUtil.getSelf().printSystemFootnotes(laf, info, reportSetup);
		 //system footer
		 float padding =0;
		if(laf.isBottomFooter()){
				//footer placing with setYLine requires a go() this has to be done in the finalizePage to avoid introducing incorrect behavior with vertical page splits
				
		    float currentLine = writer.getVerticalPosition(true);
		    padding = currentLine - (reportSetup.getBottomMargin() + sftable.getTotalHeight());
		    //System.out.println("currentLine=" + currentLine + ",adjusted="+ missingSize);
			PdfPTable emptyT = new PdfPTable(1);
				emptyT.setTotalWidth(reportSetup.getTableWidth());
				emptyT.setWidthPercentage(100f);
				//creates narrow cell spacing out the body from the footer
			PdfPCell topcell = new PdfPCell();
			//topcell.setPadding(laf.getAfterBodyPadding());
			topcell.setFixedHeight(padding);
			if(laf.isSeparatorAboveFootnotes())
				topcell.setBorder(PdfPCell.BOTTOM);
			else
				topcell.setBorder(PdfPCell.NO_BORDER);
			emptyT.addCell(topcell);
            document.add(emptyT);
            
            document.add(sftable);
		
		} else {
            document.add(sftable);
		}
		float extraLinesSize = (laf.getParagraphLeading("body")) * laf.getPdfExtraLinesForRtfCompatibility();
		float diff = padding - extraLinesSize;
		if(!laf.isBottomFooter()) diff = laf.getBeforeFootnotePadding();
		if(reportSetup.isRtfOut() && diff<0) SasshiatoTrace.logError("Unexpected condition: footnote padding(" + padding + ") < rtf compatibility space (" + extraLinesSize +")");

		return diff;
	}
	
	private void combinePDFs(String firstFile, String secondFile, String destinationFile, ReportSetup reportSetup) throws Exception{
		PdfCopyUtil.getSelf().combineFiles(firstFile, secondFile, destinationFile, sp.getSeProperties(), reportSetup.isAllowFutureAppend(), reportSetup.isBookmarksEnabled());
	}
	
	private int restampPages(String in, String out, ReportSetup setup, ReportLaF laf, RcdInfo info, File watermark, int startFrom) throws Exception {
		if(watermark==null && laf.isInText()) return -1; //no stamping for intext tables
		//if(1==1) return -1;//temp
		String chunkSize=System.getProperty("pdf_stamp_chunk_size", "1000");
		int RESTAMP_CHUNK_SIZE = Integer.parseInt(chunkSize);
		SasshiatoTrace.log(SasshiatoTrace.LEV_DETAIL, "PDF stamp Chunk Size is " + RESTAMP_CHUNK_SIZE);
		PdfReader reader = new PdfReader(new RandomAccessFileOrArray(in), null);
		File f = new File(out);
		if(f.exists()) {
			SasshiatoTrace.log(SasshiatoTrace.LEV_DETAIL, "File " + f.getAbsolutePath() + " deleted in preparation for restamp");
			f.delete();
		}
		FileOutputStream outs = new FileOutputStream(out);
		PdfStamper stamper = new PdfStamper(reader,
		 outs);
		int newStartFrom = -1;
		try{
			
			Image wim = null;
			if(watermark !=null){
				wim = ImageUtil.getSelf().getImage(watermark, setup);
			}

			//Image img = Image.getInstance("watermark.jpg");
			//img.setAbsolutePosition(200, 400);
			PdfContentByte over;
			//TODO check timing and memory usage
			int total = reader.getNumberOfPages();
			int restampTo = total;
			
			if(total-startFrom> RESTAMP_CHUNK_SIZE +1){
				restampTo= startFrom + RESTAMP_CHUNK_SIZE;
				newStartFrom = restampTo + 1;
			}
			int count = 0;
			for (int i = startFrom; i <= restampTo; i++) {
			  count ++;
			  if(count==100) {
				  SasshiatoTrace.displayProgress("PDF Stamping Page Numbers",  i);
				  count = 0;
			  }
			  Rectangle rect =  reader.getPageSize(i);
	      	  float y= rect.getTop(setup.getTopMargin()) ;
	     	  float x = rect.getLeft(setup.getLeftMargin());
			  //System.out.println("page size=" + rect);
			  over = stamper.getOverContent(i);
			  
			  /*
			  over.setLineWidth(0f);
			  over.moveTo(setup.getLeftMargin(), setup.getBottomMargin());
			  over.lineTo(setup.getLeftMargin() + setup.getTableWidth(), setup.getBottomMargin());
			  over.stroke();
			  over.moveTo(setup.getLeftMargin(), setup.getBottomMargin() + setup.getTableHeight());
			  over.lineTo(setup.getLeftMargin() + setup.getTableWidth(), setup.getBottomMargin() + setup.getTableHeight());
			  over.stroke();
			  */
			  
			  //these settings have to match the ReportPad_SinglePage.printSystemHeaderRows() spacing
				
			  if(!laf.isInText()){
				 PdfPTable table = SHeader_PdfTableStorage.getSelf().buildSTable_Stamping(i, total, laf, info, setup);
				 table.writeSelectedRows(0, -1, x , y, over); //x -5f
			  }
	
	         	//over.beginText();
	        	//over.setFontAndSize(bf, fontSize);
	        	//over.setLeading(0f);
	        	//over.setTextMatrix(x, y);
	        	//System.out.println("stamping at " + x + "/" + y);
	        	//over.showText(txt);
	        	//over.endText();
				if(wim!=null){
					PdfContentByte under = stamper.getUnderContent(i);
					under.addImage(wim);
				}
		}
			//  SasshiatoTrace.displayProgressMessage("PDF Stamping Page Numbers",  "Stamping Chunk Completed");


		} finally {
			stamper.close();
			reader.close();
			outs.close();
		}
		return newStartFrom;
	}
	

	
}
