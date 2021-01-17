/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.StringTokenizer;

import com.btcisp.utils.StringUtil;
import com.lowagie.text.pdf.BaseFont;

public class StyledTextSizingUtil {
	 public static StyledTextSizingUtil self = new StyledTextSizingUtil();
	 
     public float calcStyledChunkSize(StyledChunk ch, String styleType, ReportLaF laf, boolean includeIndentInstruction){
		 RrgPrinterUtil util = RrgPrinterUtil.getSelf();
    	 String txt = ch.getText();
    	 String escape = laf.getEscapeCharString();
    	 //from RrgPrinterUtil
 		 if(txt.indexOf("_DATE_") !=-1){
			txt = StringUtil.replace(txt, "_DATE_", util.getDateReplacement(laf));
		 }
		 if(txt.indexOf("\\\\") !=-1){
			txt = StringUtil.replace(txt, "\\\\", "\\");
		 }
		 if(txt.indexOf("#") != -1){
		   	 txt =  util.replaceSpecialChars(txt, escape, "W");
	  		 txt =  util.replaceSymbols(escape, txt);			 
	 		 txt =  util.replaceAsciInstructions(escape, txt, -1);
		 }
  		 txt =  util.removeSuperScript(txt);
   	 
     	 String encodedStyles= ch.getEncodedNoIndentingStyles();
     	 BaseFont bf = laf.getBaseFont(styleType, encodedStyles);
       	 float fontSize = laf.getFontSize();   	 
    	 float result = bf.getWidthPoint(txt, fontSize);
    	 
    	 String indentIntruction = ch.getIndentInstruction();
    	 if(includeIndentInstruction && indentIntruction != null){
    		 int indent = 0;
    		 String s0 = indentIntruction.substring(2,3);
			 try {
				 indent = Integer.parseInt(s0);
			 } catch (NumberFormatException ex) {

			 }
    		 result += indent * laf.getIndentSize();
    	 }
    		 
    	 return result;
     }
     
     //if chunk representing several lines is passed the mid linebreaks are ignored. 
     public float calcLineSize(StyledText line, String styleType, ReportLaF laf){
    	StyledChunk[] chunks = line.getChunks();
    	float size = 0;
    	for (int i = 0; i < chunks.length; i++) {
			StyledChunk styledChunk = chunks[i];
			boolean includeIndentInstruction = (i==0);
			size += calcStyledChunkSize(styledChunk, styleType, laf, includeIndentInstruction);
		}
    	return size;
     }
     
     public int calcCharacterSize(StyledText line, ReportLaF laf){
     	  String txt = line.getText();
     	  int indent = line.getIndent();
     	  return txt.length() + (indent * laf.getIndentSizeCh());
     }
     
     //replaces superscript with string xxxx of the same face length (~{super +} is replaced with xxxxxxxxx)
 	private String replaceSuperScript(String s){
		if(s == null) return s;
		String res = s;
		int inx = res.indexOf("~{super ");
		if(inx > -1) {
			int toInx = res.indexOf('}', inx);
			if(toInx > -1){
				res = res.substring(0, inx) + StringUtil.fill('x', toInx -inx)  + res.substring(toInx + 1);
			}
			if(res.indexOf("~{super ") > -1){
				res = replaceSuperScript(res);
			}
		}
		return res;
	}

    private String replaceSubScript(String s){
        if(s == null) return s;
        String res = s;
        int inx = res.indexOf("~{sub ");
        if(inx > -1) {
            int toInx = res.indexOf('}', inx);
            if(toInx > -1){
                res = res.substring(0, inx) + StringUtil.fill('x', toInx -inx)  + res.substring(toInx + 1);
            }
            if(res.indexOf("~{sub ") > -1){
                res = replaceSuperScript(res);
            }
        }
        return res;
    }
    
     //adds ' ' in the position that PDF would break line (unless '-' is encountered right after
     public StyledText adjustForRtfLineBreaks(float maxSize, StyledText line, String styleType, ReportLaF laf){
    	 float size = calcLineSize(line, styleType, laf);
    	 
    	 if(size < maxSize){
    		 return line;
    	 } else {
		    String pureText = line.getText();
		    String pureTextWithXcriptReplaced0 = replaceSuperScript(pureText);
		    String pureTextWithXcriptReplaced = replaceSubScript(pureTextWithXcriptReplaced0);
		    String allSplits = laf.getSplitChars();
		    if(allSplits.indexOf(' ') == -1){
		    	allSplits = allSplits + " ";
		    }
  	    	ArrayList<Integer> possiblePdfOnlySplitPositions = new ArrayList<Integer>();
			int inx = StringUtil.indexOfAnyOfChars(pureTextWithXcriptReplaced, -1, allSplits);
			while(inx != -1){
  				possiblePdfOnlySplitPositions.add(inx + 1);
				inx = StringUtil.indexOfAnyOfChars(pureTextWithXcriptReplaced, inx+1, allSplits);				
			}
			if(possiblePdfOnlySplitPositions.size() == 0){
				return line; //PDF will break only on spaces
			}  else {
				ArrayList<StyledChunk> adjustedChunks = new ArrayList();
				StyledTextUtil util = StyledTextUtil.getInstance(laf);
				int hitPosition = 0;
				StyledText workingText= line;
				int workingTextLine0 = 0;
				while(hitPosition < possiblePdfOnlySplitPositions.size()) {
					int splitPossitionIndex = -1;
					StyledText largestFitting = null;
					StyledText rest = null;
					for (int i = hitPosition; i < possiblePdfOnlySplitPositions.size(); i++) {
						int splitAt = possiblePdfOnlySplitPositions.get(i);
						Pair<StyledText, StyledText> split = StyledTextUtil.splitAtPureTextPosition(workingText, splitAt - workingTextLine0, false);
						float currentsize = calcLineSize(split.left, styleType, laf);
						if(currentsize < maxSize) {
							largestFitting = split.left;
							rest = split.right;
							splitPossitionIndex = i;
						} else {
							break;
						}
					}
					if(largestFitting == null){
		    	  		 SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED,
		  						"Part of sentence " + pureText
		  								+ " may not fit");
		     	  		 return line;						
					}
					StyledChunk[] fittingChunks = largestFitting.getChunks();
					String largestFittingText = largestFitting.getText();
					if(largestFittingText.endsWith(" ") || largestFittingText.endsWith("-")){
						//RFS will split on " " or "-", nothing to do
						adjustedChunks.addAll(Arrays.asList(fittingChunks));
					} else {
						//add space to the last chunk
						for (int i = 0; i < fittingChunks.length; i++) {
							if(i < fittingChunks.length -1){
								adjustedChunks.add(fittingChunks[i]);
							} else {
								StyledChunk newChunk = new StyledChunk(fittingChunks[i].getStyles(), fittingChunks[i].getText() + " ");
								adjustedChunks.add(newChunk);
							}
						}
					}
					
					workingTextLine0 = possiblePdfOnlySplitPositions.get(splitPossitionIndex);
					hitPosition = splitPossitionIndex + 1;
					workingText = rest;
				}
				adjustedChunks.addAll(Arrays.asList(workingText.getChunks()));
				return new StyledText((StyledChunk[]) adjustedChunks.toArray(new StyledChunk[0]));				
			}
 		 }
     }
    
     //NOT USED, Remove
     //creates internal line break on multi-line text
 
     //creates internal line breaks
     StyledText adjustToFitSingleLine(float maxSize, StyledText line, String styleType, ReportLaF laf){
 		float size = StyledTextSizingUtil.self.calcLineSize(line, styleType, laf);
 		if(size < maxSize){
 			return line;
 		} else {
 	    	ArrayList<StyledChunk> adjustedChunks = new ArrayList<StyledChunk>();
 	    	Pair<StyledText, StyledText> split = splitToFit(maxSize, line, styleType, laf);
    		adjustedChunks.addAll(Arrays.asList(split.left.getChunks()));
    		while(!split.right.isEmpty()){
	    		split = splitToFit(maxSize, split.right, styleType, laf);
 	    		adjustedChunks.addAll(Arrays.asList(split.left.getChunks()));
 	    	}
	    	return new StyledText((StyledChunk[]) adjustedChunks.toArray(new StyledChunk[0]));
 		}		
     }
     //+t instruction is added to the result.right 
     private Pair<StyledText, StyledText> splitToFit(float maxSize, StyledText line, String styleType, ReportLaF laf) {
    	 StyledText sanityBack = StyledText.withConfiguredStyle("WWWWWWWWWWWWWWWWWWWW", styleType, laf);
    	 String pureText = line.getText();
//    	 if(pureText.indexOf("_DATE_") != -1){
//    		 sanityBack = StyledText.withConfiguredStyle(RrgPrinterUtil.getSelf().getDateReplacement(laf), styleType, laf);
//    	 }
    	 float start_position = maxSize - calcCharacterSize(sanityBack, laf);
     	 if(start_position < 0) {
    		 SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED,
						"Part of sentence " + pureText
								+ " may not fit");
    		 start_position = 0;
     	 }
    	 
 		 //look for first " " or "-"
    	StyledChunk[] chunks = line.getChunks();
    	ArrayList<StyledChunk> leftChunks = new ArrayList<StyledChunk>();
    	ArrayList<StyledChunk> rightChunks = new ArrayList<StyledChunk>();
    	float currentChunkSize= 0;
    	float sizeSoFar = 0;
    	int breakingChunkIndex = -1;
    	for (int i = 0; i < chunks.length; i++) {
			StyledChunk styledChunk = chunks[i];
			boolean includeIndentInstruction = (i==0);
			currentChunkSize = calcStyledChunkSize(styledChunk, styleType, laf, includeIndentInstruction);
			sizeSoFar += currentChunkSize;
			if(sizeSoFar < start_position){
				leftChunks.add(styledChunk);
			} else {
				breakingChunkIndex = i;
				break;
			}
		}
    	
    	if(sizeSoFar < maxSize) {
    		//whole chunk fits in, treat this as breaking point
    		leftChunks.add(chunks[breakingChunkIndex]);
    		if(breakingChunkIndex +1 < chunks.length) {
    			//there is more chunks, first needs to be adjusted
    			StyledChunk nextChunk = chunks[breakingChunkIndex +1];
				ArrayList<String> styles = new ArrayList<String>();
				styles.addAll(nextChunk.getStyles());
				styles.add(0, "+t" + line.getIndent()); //it is 0 if  not indent and this is what we want
	   			rightChunks.add(new StyledChunk(styles, nextChunk.getText()));   				
     		}
    	} else {
    		//current chunk needs to be split
    		StyledChunk breakingChunk = chunks[breakingChunkIndex];
 			String breakingChunkText = breakingChunk.getText();
 			
 			//find split char index
 			int indexOfBreakingChar = -1;
    		String splitChars = laf.getSplitChars() + " ";
			StringTokenizer sth = new StringTokenizer(breakingChunkText, splitChars); //make sure this lists maches SplitChar implementation
			while(sth.hasMoreTokens()){
				String s0 = sth.nextToken();
				if(s0.length() < breakingChunkText.length()) {
					indexOfBreakingChar = s0.length();
				}
			}

			if(breakingChunkIndex == 0 && indexOfBreakingChar != -1) {
    			//first chunk is already too big
    	  		 SasshiatoTrace.log(SasshiatoTrace.LEV_REQUIRED,
 						"Part of sentence " + pureText
 								+ " may not fit");
    	  		 return new Pair<StyledText, StyledText>(line, StyledText.EMPTY);
     		} else if(indexOfBreakingChar > 0){
     			String text = breakingChunkText;
     			String txt1 = text.substring(0, indexOfBreakingChar);
     			String txt2 = text.substring(indexOfBreakingChar);
   	   			leftChunks.add(new StyledChunk(breakingChunk.getStyles(), txt1));   				     				

   				ArrayList<String> styles = new ArrayList<String>();
				styles.addAll(breakingChunk.getStyles());
				styles.add(0, "+t" + line.getIndent());
	   			rightChunks.add(new StyledChunk(styles, txt2));   				

	   			rightChunks.add(chunks[breakingChunkIndex +1]); 
     		} else {
   				ArrayList<String> styles = new ArrayList<String>();
				styles.addAll(breakingChunk.getStyles());
				styles.add(0, "+t" + line.getIndent());
	   			rightChunks.add(new StyledChunk(styles, breakingChunkText));   				
  			
	   			rightChunks.add(chunks[breakingChunkIndex +1]);
     		}
   	    }
    	
    	for (int i = breakingChunkIndex + 2; i < chunks.length; i++) {
    		rightChunks.add(chunks[i]);			
		}
    	
		StyledText left = new StyledText((StyledChunk[]) leftChunks.toArray(new StyledChunk[0]));
		StyledText right = new StyledText((StyledChunk[]) rightChunks.toArray(new StyledChunk[0]));
		return new Pair<StyledText, StyledText>(left, right);   	 
     }
} 
