/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfWriter;


public class DocumentHolder {
	String debugInfo;
	String documentsInfo;
	private Document document;
	PdfWriter pdfWriter;
	private OutputStream pdfOutStream;
	private PrintStream[] otherPrintStreams;
	private OutputStream[] otherOutputStreams;
	String[] otherStreamTypes;
	private ReportPadListener[] reportPadListeners;
	private Map scratchBoard = new HashMap();
	
	public Map getStratchBoard(){
		return scratchBoard;
	}
	
	public String[] getOtherStreamTypes() {
		return otherStreamTypes;
	}


	public void setOtherStreamTypes(String[] otherStreamTypes) {
		this.otherStreamTypes = otherStreamTypes;
	}


	public String getDocumentsInfo() {
		return documentsInfo;
	}


	public void setDocumentsInfo(String documentsInfo) {
		this.documentsInfo = documentsInfo;
	}


	public PdfWriter getPdfWriter() {
		return pdfWriter;
	}


	public void setPdfWriter(PdfWriter pdfWriter) {
		this.pdfWriter = pdfWriter;
	}


	public String getDebugInfo() {
		return debugInfo;
	}


	public void setDebugInfo(String debugInfo) {
		this.debugInfo = debugInfo;
	}


	public Document getDocument() {
		return document;
	}


	public void setDocument(Document document) {
		this.document = document;
	}


	public OutputStream[] getOtherOutputStreams() {
		return otherOutputStreams;
	}


	public void setOtherOutputStreams(OutputStream[] otherOutputStreams) {
		this.otherOutputStreams = otherOutputStreams;
	}


	public PrintStream[] getOtherPrintStreams() {
		return otherPrintStreams;
	}


	public void setOtherPrintStreams(PrintStream[] otherPrintStreams) {
		this.otherPrintStreams = otherPrintStreams;
	}


	public OutputStream getPdfOutStream() {
		return pdfOutStream;
	}


	//expects any OutputStream for pdf generation or ByteArrayOutputStream for in-memory pdf measurements
	public void setPdfOutStream(OutputStream pdfOutStream, boolean isTempByteArrayStream) {
		this.pdfOutStream = pdfOutStream;
	}

	public void clearTemporaryStorage(){
		if(this.pdfOutStream instanceof ByteArrayOutputStream){
			((ByteArrayOutputStream) this.pdfOutStream).reset();
		}
	}

	public ReportPadListener[] getReportPadListeners() {
		return reportPadListeners;
	}


	public void setReportPadListeners(ReportPadListener[] reportPadListeners) {
		this.reportPadListeners = reportPadListeners;
	}

	boolean closed = false;

	public void close(boolean pdfOut) throws Throwable{
		if(closed) return;
		closed = true;
		if(document!=null){
			try{
				document.close();
			}
			catch(Throwable t){
				if(pdfOut) {
					SasshiatoTrace.logError("closing lowagie document for " + debugInfo, t);
				    throw t;
				} //expected error when closing with rtf only
			}				
		}
		if(pdfOutStream!=null){
			try{
				pdfOutStream.close();
			}
			catch(Throwable t){
				if(pdfOut) {
					SasshiatoTrace.logError("closing output stream for " + debugInfo, t);
					throw t;
				}
			}
		}	
		if(otherPrintStreams!=null){
			for (int i = 0; i < otherPrintStreams.length; i++) {
				PrintStream array_element = otherPrintStreams[i];
				if(array_element!=null) {
					try{ array_element.close();
				    }catch(Exception e){throw e;}
				}
			}
		}
		if(otherOutputStreams !=null){
			for (int i = 0; i < otherOutputStreams.length; i++) {
				OutputStream array_element = otherOutputStreams[i];
				if(array_element!=null) {
					try{ array_element.close();
				    }catch(Exception e){throw e;}
				}
			}
		}
	}
}
