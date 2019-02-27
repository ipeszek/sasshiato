/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.btcisp.rrg.rcdparser.RcdInfo;
import com.btcisp.utils.StringUtil;
import com.lowagie.text.Element;

public class RrgPrinterUtil {
	private static RrgPrinterUtil self = new RrgPrinterUtil();
	
	public static RrgPrinterUtil getSelf(){
		return self;
	}
	
	Map hint2SymbolAscii = new HashMap();
	String dateReplacement = null;
	
	private RrgPrinterUtil(){
		hint2SymbolAscii.put("LE", new Integer(163));
		hint2SymbolAscii.put("GE", new Integer(179));
		hint2SymbolAscii.put("PM", new Integer(177));
		
	}
	public String getDateReplacement(ReportLaF laf){
		//store the formatted date so each page shows the same minute
		if(dateReplacement==null){
			SimpleDateFormat formatter = new SimpleDateFormat(laf.getDateFormat());
			Date date = new Date();
			dateReplacement = formatter.format(date);
			if(laf.isDateUpperCase()) dateReplacement = dateReplacement.toUpperCase();
		}
		return dateReplacement;
	}

	private int findDelimiterLocationWithNumericAfter(String text, String delimiterText, int from){
		   int index0 = text.indexOf(delimiterText, from);
		   if(index0==-1)
			   return index0;
		   if(index0 + delimiterText.length() ==text.length()) {
			   //no numbers after delimiter ignore this one
			   return -1;
		   } else {
			   //check if is followed by number
			   String num=text.substring(index0 + delimiterText.length());
			   if(num.length()>0) num = num.substring(0,1);
			   try{
				   Integer.parseInt(num);
			   }catch(Exception e){
				   //ignore this index
				   index0 = findDelimiterLocationWithNumericAfter(text, delimiterText, index0+ delimiterText.length());
			   }
		   }	
		   return index0;
	}
	public String[] splitWithNumericAfterDelimiter(String text, String delimiterText ){
		ArrayList res = new ArrayList();
		if (text==null) {
			return null;
		}
		while(text.length()>0){
		   int index0 = findDelimiterLocationWithNumericAfter(text, delimiterText, 0);
		   if (index0==0){
			   String t0="";
			   res.add(t0);
			   text = text.substring(delimiterText.length());
		   } else if (index0>-1){
			   String t0 = text.substring(0, index0);
			   res.add(t0);
			   text = text.substring(index0+delimiterText.length());
		   } else{
			   res.add(text);
			   text="";
		   }
		}
		if (res!=null){
		String[] result = (String[]) res.toArray(new String[0]);
		return result;
		} else return null;
		
	}

	public String[] lineSpitText_Prep(String txt, ReportLaF laf) throws Exception{
		//if(txt.startsWith("/i")){
		//	System.out.println();
		//}
		if(txt==null) txt= "";
		String ESCAPE = laf.getEscapeCharString();
		txt =  StringUtil.replace(txt, laf.getDoubleEscapeCharString(), ESCAPE +"t0");
		txt =  StringUtil.replace(txt, "\n", ESCAPE + "t0");
		txt =  StringUtil.replace(txt, "\r", "");
		String[] ss = splitWithNumericAfterDelimiter(txt,
		 ESCAPE + "t");
		return ss;
	}
	//this populates indents table
	public String[] lineSplitText_Perf(String[] ss, int[] indents, ReportLaF laf, boolean trim){ 
		String[] result = new String[ss.length];
		
		int indent = 0;
		for (int k = 0; k < ss.length; k++) {
			//note first token will never be after /t so should not be indented, also first token can be empty and
			//then it should be skipped.
			String s = ss[k];
			int indentD = 0;
			if(k>0) {
				if(s.length()>0){
					String s0 = s.substring(0, 1);
					try {
						indentD = Integer.parseInt(s0);
						s = s.substring(1);
						if(trim) s = s.trim(); //StringUtil.leftTrim(s);
					} catch (NumberFormatException ex) {
		
					}
				} 
			} else {
				//i=0
				if(s.startsWith(laf.getEscapeCharString() + "i"))	{
					if(s.length()>2){
						String s0 = s.substring(2, 3);
						try {
							indentD = Integer.parseInt(s0);
							s = s.substring(3);
							if(trim) s = s.trim(); //StringUtil.leftTrim(s);
						} catch (NumberFormatException ex) {
			
						}
					} 					
				}
			}
			indent = indent + indentD; //indents add adding up for single cell
			indents[k] = indent;
			result[k] = s;
		}
		
    	return result;
	}

	private float maxFtSize= -1;
	private float maxTlSize= -1;
	public float maxSpecialFooterTitleSize(StyledText[] footnotesOrTitltes, ReportLaF laf, String bodyType) {
		float maxSize;
		String leftTab, rightTab;
		if(StringUtil.areEqual(ReportLaF.STYLE_TYPE_FOOTNOTE, bodyType)){
			maxSize = maxFtSize;
			leftTab = RcdConstants.LEFT_FOOTNOTE_TAB;
			rightTab = RcdConstants.RIGHT_FOOTNOTE_TAB;
		} else {
			maxSize = maxTlSize;
			leftTab = RcdConstants.LEFT_TITLE_TAB;
			rightTab = RcdConstants.RIGHT_TITLE_TAB;
		}
		if(maxSize==-1){
			for (int j = 0; j < footnotesOrTitltes.length; j++) {
				StyledText ts = (StyledText) footnotesOrTitltes[j];
				if (!ts.isEmpty()) {
					//String ts = StringUtil.replace(footnotes[j],
					//		"//", "\n");
					String tsTxt = ts.getText();
					int idxftl = tsTxt.indexOf(leftTab);
					int idxftr = tsTxt.indexOf(rightTab);
					if(idxftl !=-1 || idxftr!=-1){
						int align = idxftl !=-1 ? Element.ALIGN_LEFT: Element.ALIGN_RIGHT;
						int inx = idxftl !=-1 ? idxftl: idxftr;
						Pair<StyledText, StyledText> split = StyledTextUtil.splitAtPureTextPosition(ts, inx, false);

						String delim = idxftl !=-1 ? leftTab: rightTab;
						StyledTextUtil styledTextUtil = StyledTextUtil.getInstance(laf);
						StyledTextSizingUtil sizingUtil = StyledTextSizingUtil.self;
						StyledText footL= split.left;
						StyledText footR = split.right;
						footR = footR.substring(delim.length());
						footR = footR.trim();
						if(align==Element.ALIGN_LEFT) footL=footL.rightTrim();
						else if(align==Element.ALIGN_RIGHT) footL = footL.leftTrim();
	
						float size = sizingUtil.calcLineSize(footL, bodyType, laf);//laf.getBaseFont("footnote").getWidthPoint(footL, laf.getFontSize("footnote"));
						if(size > 0) size = size + 2; // fix for RTF sometimes having not enough space
						//System.out.println("size of ["+footL + "]=" + size);
						if(size>maxSize) maxSize = size;
					}	
				}
			}
			//store lazy computed results
			if(StringUtil.areEqual(ReportLaF.STYLE_TYPE_FOOTNOTE, bodyType)){
				maxFtSize = maxSize;
			} else {
				maxTlSize = maxSize;
			}
		}
		return maxSize;
	}
	
	public String redoForWidthCalculation(String escapeChar, String s){
		if(s == null) return s;
		String res = s;
		res = removeSuperScript(res);
		res = replaceSymbols(escapeChar, res);
		res = replaceAsciInstructions(escapeChar, res, -1);
		return res;			
	}
	
	public String removeSuperScript(String s){
		if(s == null) return s;
		String res = s;
		int inx = res.indexOf("~{super ");
		if(inx > -1) {
			int toInx = res.indexOf('}', inx);
			if(toInx > -1){
				res = res.substring(0, inx) + res.substring(inx + "~{super ".length(), toInx)  + res.substring(toInx + 1);
			}
			if(res.indexOf("~{super ") > -1){
				res = removeSuperScript(res);
			}
		}
		return res;
	}
	
	public String replaceSymbols(String escapeChar, String s){
		if(s == null) return s;
		String res = s;
		int inx = res.indexOf(escapeChar + "s#");
		if(inx > -1) {
			int toInx = res.indexOf(' ', inx);
			if(toInx > -1){
				res = res.substring(0, inx) + 'W' + res.substring(toInx + 1);
			} else {
				res = res.substring(0, inx) + 'W'; //working on a chunk that that ends with symbol so there is no delimiting space
			}
			if(res.indexOf(escapeChar + "s#") > 0){
				res = replaceSymbols(escapeChar, res);
			}
		}
		return res;
	}
	
	public String replaceAsciInstructions(String escapeChar, String s, int searchFrom){
		if(s == null) return s;
		String res = s;
		int inx = res.indexOf(escapeChar + "#", searchFrom);
		if(inx > -1) {
			int toInx = res.indexOf(' ', inx);
			int asci = -1;
			try{
				asci = Integer.parseInt(res.substring(inx + ((escapeChar + "#").length()), toInx));
			}catch(Exception e){
			}
			if(toInx > -1 && asci > 31){
				res = res.substring(0, inx) + 'W' + res.substring(toInx + 1);
			}

			int nextInx = res.indexOf(escapeChar + "#", toInx);
			if(nextInx > -1) {
				res = replaceAsciInstructions(escapeChar, res, toInx);				
			}
		}
		return res;
	}
	
	public StyledText[] prepareTitles(RcdInfo info, Map oldTitleIndexes, ReportLaF laf) throws Exception{
		StyledText[] titles = info.getTitles(laf);
		if(titles==null) titles = new StyledText[0];
		ArrayList<StyledText> titlesAl = new ArrayList<StyledText>();
		for (int j = 0; j < titles.length; j++) {
			if (!titles[j].isEmpty()) {
				//System.out.println("got " + j + ":" + titles[j]);
				titlesAl.add(titles[j]);
				//the index reflects X in __titleX 
				if(oldTitleIndexes !=null) oldTitleIndexes.put(Integer.valueOf(titlesAl.size()-1), Integer.valueOf(j +1));
			}
		}
		if(titlesAl.size()==0) titlesAl.add(StyledText.withConfiguredStyle(" ", ReportLaF.STYLE_TYPE_TITLE, laf)); //make sure there is at least one title (needed for RTF).
		StyledText[] newTitles= (StyledText[]) titlesAl.toArray(new StyledText[0]);
		return newTitles;
	}
	
	public int convertSymbolHintToAsciiCode(String hint){
		if(hint==null) return  -1;
		hint = hint.toUpperCase().trim();
		Integer code = (Integer) this.hint2SymbolAscii.get(hint);
		if(code==null){
			SasshiatoTrace.logError("Invalid /s hint =" + hint);
			return -1;
		} else {
			return code.intValue();
		}

	}
	
	public String replaceSpecialChars(String txt, ReportLaF laf){
		return replaceSpecialChars(txt, laf.getEscapeCharString(), null);
	}
	
	public String replaceSpecialChars(String txt, String ESCAPE, String replaceWith){
		String prefix = ESCAPE+ "#";
		if(txt.indexOf(prefix) != -1){
			StringBuffer sb= new StringBuffer();
    		int i_start=txt.indexOf(prefix);
    		while(i_start !=-1){
     			//System.out.println("loop: " + txt + ":" + i_start);
    			int i_stop = txt.indexOf(" ", i_start);
    			if(i_stop==-1) {
    				//automatic trimming may impact this calculation
       				i_stop = txt.length();
    			}
    			if(i_stop==-1) {
    				SasshiatoTrace.logError("Malformed "+prefix+" instruction found in line:" + txt);
    				break;
    			} else {
    				String txt_before = txt.substring(0, i_start);
    				String txt_after = "";
    				if(i_stop +1<txt.length()) txt_after = txt.substring(i_stop+1 );
    				String scommand = txt.substring(i_start + prefix.length(), i_stop);
    				scommand = scommand.trim();
    				int it = 0;
    				try{
    					it = Integer.parseInt(scommand);
    				}catch(NumberFormatException e){
    					it = -1;					
    				}
    				sb.append(txt_before);
    				if(replaceWith != null){
    					sb.append(replaceWith);
    				} else if(it!=-1) {
    					sb.append((char) it);
    				} else {
    					SasshiatoTrace.logError("Invalid "+prefix+" command, txt=" + txt);	
    					sb.append(" ");
    				}
    				txt = txt_after;
    	   			i_start=txt.indexOf(prefix);
   			}
     			//System.out.println("loopend: " + txt + ":" + i_start);
   		}
    	sb.append(txt);
    	txt = sb.toString();
        }
		return txt;
	}
	//may need work
	private boolean isSpecialCharacter(char c, int position, String txt, ReportLaF laf){
		char ESCAPE = laf.getEscapeChar();
		if(position + 2>=txt.length()) return false;
		if(c==ESCAPE) {
			char c2 =txt.charAt(position+1);
			if(c2=='#'){
				return true;
			} else if(c2=='s' && txt.charAt(position +2)=='#'){
				return true;
			}
			else
				return false;
		} else if (c=='~'){
			if(txt.substring(position).startsWith("~super{"))
				return true;
			else 
				return false;
		}
		else
			return false;
	}
	
	public boolean isSplitCharacter_RTFSyntax(char c, int position, String txt, ReportLaF laf, String splitChars){
		if(splitChars==null) splitChars = laf.getSplitChars();
		char ESCAPE = laf.getEscapeChar();
		if(c!=ESCAPE && c!= '\\' && splitChars.indexOf(c) != -1){
			return true;
		}
		//NOTE ~super is handled before
		if(c==ESCAPE) {
			if(position + 2 >= txt.length()) return splitChars.indexOf(c) != -1;
			char c2 =txt.charAt(position+1);
			if(c2=='#'){
		    		int i_start=position;
			    	int i_stop = txt.indexOf(" ", i_start);
		    		if(i_stop==-1) {
		    			//automatic trimming may impact this calculation
		       			i_stop = txt.length();
		    		}
    				String scommand = txt.substring(i_start + 2, i_stop);
    				scommand = scommand.trim();
    				int it = 0;
    				try{
    					it = Integer.parseInt(scommand);
    				}catch(NumberFormatException e){
    					SasshiatoTrace.logWarning("unexpected escaped sequence in " +txt);
    					return splitChars.indexOf(c) != -1;
    				}
    				//char newc = (char) it;
    				//return splitChars.indexOf(newc) != -1;
    				return false;
			} 
			else if(c2=='s'){
				if(position + 3 >= txt.length()) return splitChars.indexOf(c) != -1;
				char c3 = txt.charAt(position +2);
				if(c3=='#') return false;
				else return splitChars.indexOf(c) != -1;
			}else 
				return splitChars.indexOf(c) != -1;
		}  else if (c=='\\'){
			if(position +1 >= txt.length()) return false;
		    char c2 =txt.charAt(position+1);
		    if(c2=='\\' && splitChars.indexOf(c2) != -1)
		    	return true;
		    else 
		    	return false;
		}
		else
			return false;
	}
	
	
	
	//replaced with StyledTextSizingUtil.splitToFit
	@Deprecated
	public String[] adjustForRtfLineBreaking(String[] txt, int[] indents, float max_size, ReportLaF laf, String styletype) {
		String[] res = new String[txt.length];  
		for (int i = 0; i < txt.length; i++) {
			String txtn = txt[i];
			float indentF = indents[i] * laf.getIndentSize();
			txtn = adjustForRtfLineBreaking(txtn, max_size - indentF, laf, styletype);
			res[i] = txtn;
		  }
		  return res;
	  }
	  
	  private float _measureLength(String txt, ReportLaF laf, String styletype){
		  
		  String oldtxt = txt;
			if(txt.indexOf("_DATE_") !=-1){
				txt = StringUtil.replace(txt, "_DATE_", RrgPrinterUtil.getSelf().getDateReplacement(laf));
			}
			if(txt.indexOf("\\\\") !=-1){
				txt = StringUtil.replace(txt, "\\\\", "\\");
			}
			String ESCAPE = laf.getEscapeCharString();
			txt= replaceSpecialChars(txt, ESCAPE, "W");
			//replace symbols with X
			String prefix = ESCAPE+ "s#";
			int ssidx_start0=txt.indexOf(prefix);
			while(ssidx_start0 !=-1){
				int ssidx_stop0 = txt.indexOf(" ", ssidx_start0);
				String txt_before = txt.substring(0, ssidx_start0);
				if(ssidx_stop0!=-1){
					String txt_after = "";
					if(ssidx_stop0 +1 < txt.length()) txt_after = txt.substring(ssidx_stop0 +1);
					txt = txt_before+"X" + txt_after;
				}else {
					txt = txt_before+"X";
				}
				ssidx_start0=txt.indexOf(prefix);
			}
			
			//replaces superscripts with normal text.
			int ssidx_start=txt.toLowerCase().indexOf("~{super");
			while(ssidx_start!=-1){
				int ssidx_stop = txt.indexOf("}", ssidx_start);
				if(ssidx_stop!=-1){
					String superscript = txt.substring(ssidx_start + "~{super".length(), ssidx_stop);
					superscript = superscript.trim();				
					txt = txt.substring(0, ssidx_start) + "X" + txt.substring(ssidx_stop +1);
					ssidx_start=txt.toLowerCase().indexOf("~{super");
				} else {
					break;
				}
			}
			float cw0 =  laf.getBaseFont(styletype).getWidthPoint(txt,laf.getFontSize(styletype));
			//if(!oldtxt.equals(txt)){
			//	System.out.println("Measurement change:");
			//	System.out.println(oldtxt);
			//	System.out.println(txt);
			//}
		    return cw0;
	  }
	  
	  private boolean isEscaped = false;
	  private char escapeEndChar = ' ';
	  public boolean _isEscaped(char c, int position, String txt, char ESCAPE){
		  if(!isEscaped) {
			  if(c==ESCAPE && (txt.length()>position +1 && (txt.charAt(position+1)=='#' || txt.charAt(position+1)=='s'))){
				 isEscaped= true;
				 escapeEndChar=' ';
			  } else if (c=='~' && txt.indexOf("~{super", position-1) == position){
				  isEscaped = true;
				  escapeEndChar = '}';
			  }
			  return isEscaped;
		  } else {
			  if(c==escapeEndChar){
				  isEscaped = false;
				  return true; //last character keep escaped for it
			  } else
				  return isEscaped;
		  }
		  
	  }
	  
	  //insert a space in the place where PDF will break (laf.getSplitChars() + ' ' if line overflows unless there is '-' encountered
	  //
	  @Deprecated
	  public String adjustForRtfLineBreaking(String txt, float max_size, ReportLaF laf, String styletype) {
		   if(txt ==null) return txt;
		   StringBuffer newTxt = new StringBuffer();
			String allSpits = laf.getSplitChars();
			int i=0;
			StringBuffer runningWord = new StringBuffer();
			StringBuffer fittingWord = new StringBuffer();
			//boolean debug = txt.indexOf("DRAFT")!=-1;
			//if(debug) System.out.println("max_size=" + max_size + ",font=" + laf.getFontSize() + ":" + txt);
			char ESCAPE= laf.getEscapeChar();
			while(i < txt.length()) {
				char c = txt.charAt(i);
				runningWord.append(c);
				if((allSpits.indexOf(c) != -1 && !_isEscaped(c, i, txt, ESCAPE)) || i==txt.length() -1){
					float cw0 =  _measureLength(runningWord.toString(),laf, styletype);
					if(cw0 < max_size) {
						fittingWord.append(runningWord.substring(fittingWord.length()));
						//if(debug) System.out.println("still fitting:" + fittingWord);
					} else {
						if(fittingWord.length()==0){
							//word is longer than max
							SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "Part of sentence " + txt + ": word " + runningWord.toString() + " may not fit");
							newTxt.append(runningWord);
							fittingWord = new StringBuffer();
							runningWord= new StringBuffer();						
						} else {
							//there was a smaller fitting word at the begining
							String leftOver = runningWord.substring(fittingWord.length());
							char lastChar = fittingWord.charAt(fittingWord.length()-1);
							newTxt.append(fittingWord);
							char nextc = ' ';
							if(leftOver.length()>0) nextc = leftOver.charAt(0);
							//System.out.println(i + " nextc=" + nextc + ":" + fittingWord.toString());
							if(lastChar!=' ' && lastChar != '-' && nextc !=' ' && nextc != '-'){
								newTxt.append(' ');
							} 
							runningWord= new StringBuffer();
							runningWord.append(leftOver);
							cw0 =  _measureLength(runningWord.toString(),laf, styletype);
							if(cw0 >= max_size) {
								//the end is not fitting
								SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "Part of sentence " + txt + ": word " + runningWord.toString() + " may not fit");
								newTxt.append(runningWord);
								char lastChar2 = runningWord.charAt(runningWord.length()-1);
								char nextc2 = ' ';
								if(i<txt.length()-1) nextc2 = txt.charAt(i+1);
								if(lastChar2!=' ' && lastChar2 != '-' && nextc2 !=' ' && nextc2 != '-'){
									newTxt.append(' ');
								} 
								runningWord= new StringBuffer();													
								fittingWord = new StringBuffer();
							} else {
								fittingWord = new StringBuffer();
								fittingWord.append(leftOver);
							}
						}
					}
				} 				
				i++;
			}
			newTxt.append(runningWord);
			//if(debug) System.out.println("appending rest:" + fittingWord);
		   String res = newTxt.toString();
		    return res;
	   }
	  
	  public String adjustForProcRtfAsciLineBreaks(String txt, float max_size, ReportLaF laf, String styletype, char splitChar) {
		   if(txt ==null) return txt;
		   StringBuffer newTxt = new StringBuffer();
			String allSpits = laf.getSplitChars();
			int i=0;
			StringBuffer runningWord = new StringBuffer();
			StringBuffer fittingWord = new StringBuffer();
			//boolean debug = txt.indexOf("DRAFT")!=-1;
			//if(debug) System.out.println("max_size=" + max_size + ",font=" + laf.getFontSize() + ":" + txt);
			char ESCAPE= laf.getEscapeChar();
			while(i < txt.length()) {
				char c = txt.charAt(i);
				runningWord.append(c);
				if((allSpits.indexOf(c) != -1 && !_isEscaped(c, i, txt, ESCAPE)) || i==txt.length() -1){
					float cw0 =  _measureLength(runningWord.toString(),laf, styletype);
					if(cw0 < max_size) {
						fittingWord.append(runningWord.substring(fittingWord.length()));
						//if(debug) System.out.println("still fitting:" + fittingWord);
					} else {
						if(fittingWord.length()==0){
							//word is longer than max
							SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "Part of sentence " + txt + ": WORD:" + runningWord.toString() + " may not fit");
							newTxt.append(runningWord);
							fittingWord = new StringBuffer();
							runningWord= new StringBuffer();						
						} else {
							//there was a smaller fitting word at the begining
							String leftOver = runningWord.substring(fittingWord.length());
							//char lastChar = fittingWord.charAt(fittingWord.length()-1);
							newTxt.append(fittingWord);
							//char nextc = ' ';
							//if(leftOver.length()>0) nextc = leftOver.charAt(0);
							//if(lastChar!=' ' && lastChar != '-' && nextc !=' ' && nextc != '-'){
							//	newTxt.append(' ');
							//} 
							newTxt.append(splitChar);
							runningWord= new StringBuffer();
							runningWord.append(leftOver);
							cw0 =  _measureLength(runningWord.toString(),laf, styletype);
							if(cw0 >= max_size) {
								//the end is not fitting
								SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "Part of sentence " + txt + ": word:" + runningWord.toString() + " may not fit");
								newTxt.append(runningWord);
								//char lastChar2 = runningWord.charAt(runningWord.length()-1);
								//char nextc2 = ' ';
								//if(i<txt.length()-1) nextc2 = txt.charAt(i+1);
								//if(lastChar2!=' ' && lastChar2 != '-' && nextc2 !=' ' && nextc2 != '-'){
								//	newTxt.append(' ');
								//} 
								newTxt.append(splitChar);
								runningWord= new StringBuffer();													
								fittingWord = new StringBuffer();
							} else {
								fittingWord = new StringBuffer();
								fittingWord.append(leftOver);
							}
						}
					}
				} 				
				i++;
			}
			newTxt.append(runningWord);
			//if(debug) System.out.println("appending rest:" + fittingWord);
		   String res = newTxt.toString();
		    return res;
	   }
	  

	  public float getColumnTotalSeparation(int column, ReportLaF laf){
		  float paddingLeft = laf.getColumnSeparation();
		  //if(column==0 && laf.isCharacterStrict()) paddingLeft=0;
		  float paddingRight = laf.getColumnSeparation();
		  if(ReportLaF.COLUMN_SEPARATION_USE_DEFAULT != laf.getColumnSeparationOverrideToNext(column)){
			  paddingRight = laf.getColumnSeparationOverrideToNext(column);
		  }
		  if(column>0 && ReportLaF.COLUMN_SEPARATION_USE_DEFAULT != laf.getColumnSeparationOverrideToNext(column-1)){
			  paddingLeft = laf.getColumnSeparationOverrideToNext(column-1);
		  }
		  return paddingLeft + paddingRight;
	  }
	  
	  
		public boolean isEmptyHeader(HeaderTablet tablet){
			HeaderRow[] rows = tablet.getHeaderRows();
			if(rows==null) return true;
			else if(rows.length ==0) return true;
			else if(rows.length >1) return false;
			else if(rows.length==1){
				HeaderRow row = rows[0];
				StyledText[] cells = row.getHeaderCells();
				if(cells ==null) return true;
				for (int i = 0; i < cells.length; i++) {
					StyledText cell = cells[i];
					if (!cell.isEmpty()) {
						return false;
					}
				}
				return true;
			}
			return false;
			
		}
}
