/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.btcisp.utils.StringUtil;

/**
 * Parses text into pre-digested 'styled chunks'.
 * Each chunk is a string starting with {\__set of comma delimited style instructions__\} followed by text.
 * Chunks are atomic (all text in the chunk after the initial set of instructions is instruction free.
 * 
 * Input text can contain {\bf terminated by \bf}, {\it terminated by \it} or combined 
 * {\bf\it terminated by \bf\it}
 * 
 * Example of input text would be:
 * "{\it This is italic {\bf Bold italic \bf} only italic \it}"
 * This corresponds to this text (white space is significant):
 * "This is italic Bold italic only italic"
 * {\bf is expected to have extra space after it to terminate and closing instruction \bf} also needs
 * to be preceded with blank.  These blanks are part of instruction parsing and are ignored.
 * 
 * The resulting chunks are:
 * "{\__it__\}This is italic "
 * "{\__bt;it__\}Bold italic"
 * "{\__it__\} only italic"
 * 
 * @author peszek
 *
 */
public class StyledTextUtil {
	static final List<String> EMPTY_STYLES = new ArrayList<String>();

	//defines proper order at the same time
	public static final String[] VALID_NON_INDENT_STYLE_INSTRUCTION_SEQ = {
		"bf",
		"it"
	};
	
	private String getValidStyleInstructionsMsg(){
		StringBuffer sb = new StringBuffer();
		for(int i=0; i< VALID_NON_INDENT_STYLE_INSTRUCTION_SEQ.length; i++){
			sb.append(escape);
			sb.append(VALID_NON_INDENT_STYLE_INSTRUCTION_SEQ[i]);
		}
		return sb.toString();
	}
	private static final String INTERNAL_STYLE_INSTRUCTION_END_PREFIX = "end_";
	public static final String INTERNAL_STYLE_INSTRUCTION_INDENT_GEN_PREFIX = "+";
	private static final String INTERNAL_STYLE_INSTRUCTION_INDENT_PREFIX = "+i";
	private static final String INTERNAL_STYLE_INSTRUCTION_LINE_INDENT_PREFIX = "+t";
	
	static StyledTextUtil instance;
	public static StyledTextUtil getInstance(ReportLaF laf){
		if(instance == null){
	       instance  = new StyledTextUtil();
	       instance.escape = laf.getEscapeCharString();
	       instance.doubleEscapeCharString = laf.getDoubleEscapeCharString();
		} else {
			assert instance.escape == laf.getEscapeCharString();
		}
		return instance;
	}
	
	public static StyledTextUtil getTestInstance(){
		
        String escape = "\\";
		return getTestInstance(escape);
	}
	
	public static StyledTextUtil getTestInstance(String escape){
		
		StyledTextUtil res=  new StyledTextUtil();
		res.escape = escape;
		return res;
	}
	
	String escape = "/";
    String doubleEscapeCharString = "//";
    //need to work with RrgPrinterUtil.lineSpitText_Prep / lineSplitText_Perf
	//styles: tN, iN (first character) {\b, {\i  or {\b\i
	public StyledText parseToStyledText(List<String> styleTypeStyles, String txt, boolean trim) throws InvalidStylingSyntaxException{
		if(styleTypeStyles == null)
			styleTypeStyles = EMPTY_STYLES;
		try{
			if(trim)
				txt = txt.trim();
			txt =  StringUtil.replace(txt, doubleEscapeCharString, escape +"t0");
			txt =  StringUtil.replace(txt, "\n", escape + "t0");
			txt =  StringUtil.replace(txt, "\r", "");
			
			if(!hasStyles(txt, 0, false)){
				StyledChunk[] chunks = { new StyledChunk(styleTypeStyles, txt) };
				return new StyledText(chunks);
			}
			ArrayList<StyledChunk> result = new ArrayList<StyledChunk>();
			
			//contains these: +iN, +tN (encoded to make them different), \bf, \it, \bf\it
			ArrayList<String> styleStack = new ArrayList<String>();
			int position = -1;
			
			while(hasStyles(txt, position, true)){
				Pair<String, Pair<Integer, Integer>> nextStyle = findNextStyleInstruction(txt, position);
				String nextStyleS = nextStyle.getLeft();
				Pair<Integer, Integer> positions = nextStyle.getRight();
				int styleInstructionPosition = positions.getLeft();
				int newPosition = positions.getRight();
				String interimText = txt.substring(position>-1?position:0, styleInstructionPosition);
				if(!"".equals(interimText)){
					StyledChunk newStyleChunk = formatStyleChunk(styleTypeStyles, styleStack, interimText);
					result.add(newStyleChunk);
					if(styleStack.size() > 0 && (styleStack.get(0).startsWith(INTERNAL_STYLE_INSTRUCTION_INDENT_PREFIX) || styleStack.get(0).startsWith(INTERNAL_STYLE_INSTRUCTION_LINE_INDENT_PREFIX))){
						styleStack.remove(0);
					}
				}
				if(nextStyleS.startsWith(INTERNAL_STYLE_INSTRUCTION_END_PREFIX)){
					//last style ended
					if(!StringUtil.areEqual(INTERNAL_STYLE_INSTRUCTION_END_PREFIX + styleStack.get(styleStack.size() - 1), nextStyleS)){
				    	throw new InvalidStylingSyntaxException("Invalid termination " + nextStyleS + " you need to close " + styleStack.get(styleStack.size() - 1) + " first: " + txt);
					}
					styleStack.remove(styleStack.size() - 1);

				} else if(nextStyleS.startsWith(INTERNAL_STYLE_INSTRUCTION_INDENT_PREFIX) || nextStyleS.startsWith(INTERNAL_STYLE_INSTRUCTION_LINE_INDENT_PREFIX)){
					//new indent/new line instruction
					//+i and +t encoded indent instructions,  it encodes italic
					if(styleStack.size() > 0 && (styleStack.get(0).startsWith(INTERNAL_STYLE_INSTRUCTION_INDENT_PREFIX) || styleStack.get(0).startsWith(INTERNAL_STYLE_INSTRUCTION_LINE_INDENT_PREFIX))){
						styleStack.set(0, nextStyleS);
					} else {
						styleStack.add(0, nextStyleS);
					}
				} else {
					//new style started (already normalized
					styleStack.add(nextStyleS);
				}
				position = newPosition;
			}
			if(position < txt.length() -1){
				if(styleStack.size()>0){
					if(styleStack.size() == 1 && (styleStack.get(0).startsWith(INTERNAL_STYLE_INSTRUCTION_INDENT_PREFIX) || styleStack.get(0).startsWith(INTERNAL_STYLE_INSTRUCTION_LINE_INDENT_PREFIX))){
					} else {
						throw new InvalidStylingSyntaxException("Not all styles terminated: " + txt);						
					}
				}
				result.add(formatStyleChunk(styleTypeStyles, styleStack, txt.substring(position)));
			} else if(position == txt.length() && styleStack.size() > 0){
				result.add(formatStyleChunk(styleTypeStyles, styleStack, ""));
			}
			StyledChunk[] chunks = (StyledChunk[]) result.toArray(new StyledChunk[0]);
			return new StyledText(chunks);
		}catch(InvalidStylingSyntaxException e){
			SasshiatoTrace.logError(e.getMessage());
			throw e;
//			StyledChunk[] res = {new StyledChunk(EMPTY_STYLES, "INVALID TEXT")};
//			return res;
		}
	}
	
	
	private boolean hasStyles(String txt, int position, boolean includeClosing){
		   String[] toTry = {
		    		"{" + escape + "bf",
		    		"{" + escape + "it",
		    	    escape + "t0", //priority is important it should be checked before i
		    	    escape + "t1",
		       	    escape + "t2",
		       	    escape + "t3",
		       	    escape + "t4",
		       	    escape + "t5",
		       	    escape + "t6",
		       	    escape + "t7",
		       	    escape + "t8",
		       	    escape + "t9",
		     	    escape + "i0",
		     	    escape + "i1",
		     	    escape + "i2",
		     	    escape + "i3",
		     	    escape + "i4",
		     	    escape + "i5",
		     	    escape + "i6",
		     	    escape + "i7",
		     	    escape + "i8",
		    	    escape + "i9"
		    };		

		   String[] toTryClosing = {
		    		escape + "bf" + "}",
		    		escape + "it" + "}"			   
		   };
		   
		boolean hit = false;
		for (int i = 0; i < toTry.length; i++) {
			if(txt.indexOf(toTry[i], position) != -1)
				hit = true;
		} 
		if(hit){
			return true;
		}
		if(includeClosing && txt.indexOf("}", position) != -1) {
			boolean closingHit = false;
			for (int i = 0; i < toTryClosing.length; i++) {
				if(txt.indexOf(toTryClosing[i], position) != -1)
					closingHit = true;
			} 
			return closingHit;
		} 
		return false;
	}
	
	
   // input instructions looks like \bf\it  it returns normalized set like 'bf_it' but it is extensible
	private String parseInstructionSequence(String instructions) throws InvalidStylingSyntaxException{
		StringBuffer sb = new StringBuffer();
		instructions = StringUtil.replace(instructions, escape, "/"); //just so escape have size 1
		for(int i=0; i< VALID_NON_INDENT_STYLE_INSTRUCTION_SEQ.length; i++){
			if(instructions.indexOf(VALID_NON_INDENT_STYLE_INSTRUCTION_SEQ[i]) != -1){
				sb.append(VALID_NON_INDENT_STYLE_INSTRUCTION_SEQ[i]).append("_");
			}
		}
		String res = sb.toString();
		if(res.length() != instructions.length())
		   throw new InvalidStylingSyntaxException("Combined style instruction can only contain subset of: " + getValidStyleInstructionsMsg() + " got " + instructions ); 
		return res.substring(0, res.length() -1);	
	}
	

    //return converted instructions: +iN, +tN (encoded to make them different), bf, it, bf_it  or end_bf or end_it or end_bf_it 
	private Pair<String, Pair<Integer, Integer>> findNextStyleInstruction(String txt, int position) throws InvalidStylingSyntaxException{
		   String[] toTry = {
		    		"{" + escape + "bf",
		    		"{" + escape + "it",
		    		escape + "bf" + "}",
		    		escape + "it" + "}",
		    	    escape + "t0", //priority is important it should be checked before i
		    	    escape + "t1",
		       	    escape + "t2",
		       	    escape + "t3",
		       	    escape + "t4",
		       	    escape + "t5",
		       	    escape + "t6",
		       	    escape + "t7",
		       	    escape + "t8",
		       	    escape + "t9",
		     	    escape + "i0",
		     	    escape + "i1",
		     	    escape + "i2",
		     	    escape + "i3",
		     	    escape + "i4",
		     	    escape + "i5",
		     	    escape + "i6",
		     	    escape + "i7",
		     	    escape + "i8",
		    	    escape + "i9"
		    };		
		int inx = txt.length() + 1;
		int foundInstructionInx = -1;
	    for(int i=0; i<toTry.length; i++){
	    	int hitIndex = txt.indexOf(toTry[i], position);
	    	if(hitIndex != -1 && hitIndex<inx){
	    		inx = hitIndex;
	    		foundInstructionInx = i;
	    	}
	    }
	    if(inx > txt.length()){
	    	//TODO this should be impossible
	    	throw new InvalidStylingSyntaxException("Unexpected internal error, no style instructions found in: " + txt);
	    } else {
	       String styleInstruction = null;
	       int instructionPosition = inx;
	       int newPosition = -1;
	       if(toTry[foundInstructionInx].startsWith("{")){
	    	   int instructionTerminationInx = txt.indexOf(' ', inx +2);
	    	   if(instructionTerminationInx == -1){
	    		   //TODO error handling throw exception and have whole instruction parsing aborted
	    		   throw new InvalidStylingSyntaxException(toTry[foundInstructionInx] + " needs to be terminated with blank: " + txt);
	    	   } else {
	    		   String txtIntruction = txt.substring(inx + 1, instructionTerminationInx);
	    		   if(txtIntruction.length() > 3){
	    			   //needs normalizing
	    			   String parsedInstructionSequence = parseInstructionSequence(txtIntruction);
		    		   styleInstruction = parsedInstructionSequence; 
	    		   } else {
	    			   styleInstruction = txtIntruction.substring(1);
	    		   }
    			   newPosition = instructionTerminationInx + 1;
	    	   }
	       } else if(toTry[foundInstructionInx].endsWith("}")){
	    	   newPosition = inx + toTry[foundInstructionInx].length();
	    	   inx = txt.lastIndexOf(' ', newPosition -1);
	    	   instructionPosition = inx;
	    	   if(inx == -1){
	    		   throw new InvalidStylingSyntaxException("Invalid style termination needs space before termination sequence: " + txt); 	    		   	    		   
	    	   }
	    	   if(inx < position){
	    		   throw new InvalidStylingSyntaxException("Invalid style termination needs space before termination sequence: " + txt.substring(inx, newPosition) + " :"  + txt); 	    		   
	    	   }
	    	   //TODO 
	    	   String allClosingInstructions = txt.substring(inx + 1, newPosition-1);
	    	   if(allClosingInstructions.length() > 3){
    			   String parsedInstructionSequence = parseInstructionSequence(allClosingInstructions);
	    		   styleInstruction = INTERNAL_STYLE_INSTRUCTION_END_PREFIX + parsedInstructionSequence; 
	    	   } else {
		    	   styleInstruction = INTERNAL_STYLE_INSTRUCTION_END_PREFIX + allClosingInstructions.substring(1);
	    	   }
	       } else {
	    	   //indentation instructions
	    	   styleInstruction = "+" + txt.substring(inx + 1, inx+3);
	    	   newPosition = inx + 3;
	       }
	       return new Pair<String, Pair<Integer, Integer>>(styleInstruction, new Pair<Integer, Integer>(instructionPosition, newPosition));
	    }
	}
	

	
	//TODO this is not extensible
	private StyledChunk formatStyleChunk(List<String> styleTypeStyles, List<String> styles, String txt){
		if(styles.size() == 0){
			return new StyledChunk(styleTypeStyles, txt);
		}
		List<String> adjustedStyles = new ArrayList<String>();
		if(styles.get(0).startsWith("+")){
			adjustedStyles.add(styles.get(0));
		}
		boolean hasBf = false;
		boolean hasIt = false;
		for(int i=0; i< styleTypeStyles.size(); i++){
		   if(styleTypeStyles.get(i).indexOf(StyledChunk.STYLE_INSTRUCTION_BF) != -1)
			   hasBf = true;
		   if(styleTypeStyles.get(i).indexOf(StyledChunk.STYLE_INSTRUCTION_IT) != -1)
			   hasIt = true;		   
		}
		for(int i=0; i< styles.size(); i++){
			   if(styles.get(i).indexOf(StyledChunk.STYLE_INSTRUCTION_BF) != -1)
				   hasBf = true;
			   if(styles.get(i).indexOf(StyledChunk.STYLE_INSTRUCTION_IT) != -1)
				   hasIt = true;		   
		}
		if(hasBf)
			adjustedStyles.add(StyledChunk.STYLE_INSTRUCTION_BF);
		if(hasIt)
			adjustedStyles.add(StyledChunk.STYLE_INSTRUCTION_IT);
		
		return new StyledChunk(adjustedStyles, txt);
	}
	
	public List<StyledText> splitIntoList(StyledText stext, String splitOnFirstOf, boolean keepIndentation, boolean includeEmpties) {
		ArrayList<StyledText> splits = new ArrayList<StyledText>();
		Pair<StyledText, StyledText> split = split(stext, splitOnFirstOf, keepIndentation);
		if(includeEmpties || !split.left.isEmpty())
			splits.add(split.left);
		while(split.right.pureTextSize() > 0){
			split = split(split.right.substring(1), splitOnFirstOf, keepIndentation);
			if(includeEmpties || !split.left.isEmpty())
				splits.add(split.left);
		}
		return splits;
	}
	
	//For decimal alignment, splits on first encountered character from the sequence
	//used tokenizer (matches on each of the chars in the string
	public Pair<StyledText, StyledText> split(StyledText stext, String splitOnFirstOf, boolean keepIndentation){
		String text = stext.getText();
		if (!StringUtil.isEmpty(text)) {
			
//			StringTokenizer stok = new StringTokenizer(text, splitOnFirstOf);
//			String s0 = null;
//			try {
//				s0 = stok.nextToken();
//			} catch (Exception exx) {
//				s0 = ""; // nextToken will exception there are no tokens
//				return new Pair<StyledText, StyledText> (StyledText.EMPTY, stext);
//			}
//			int position = s0.length();
			int position = StringUtil.indexOfAnyOfChars(text, -1, splitOnFirstOf);
			if(position == -1){
				return new Pair<StyledText, StyledText> (stext, StyledText.EMPTY);
			}
			ArrayList<StyledChunk> leftChunks = new ArrayList<StyledChunk>();
			ArrayList<StyledChunk> rightChunks = new ArrayList<StyledChunk>();
			
			int workingPosition = 0;
			StyledChunk[] inputChunks =stext.getChunks(); 
			for(int i=0; i<inputChunks.length; i++){
				StyledChunk ch = inputChunks[i];
				int size = ch.getText().length();
				if(workingPosition + size <= position) {
					leftChunks.add(ch);
				} else if(workingPosition < position && workingPosition + size > position){
					int split = position - workingPosition;
					String txt1 = ch.getText().substring(0, split);
					String txt2 = ch.getText().substring(split);
					leftChunks.add(new StyledChunk(ch.getStyles(), txt1));
					if(keepIndentation)
						rightChunks.add(new StyledChunk(ch.getStyles(), txt2));
					else
						rightChunks.add(new StyledChunk(ch.getNoIndentingStyles(), txt2));
				} else if(workingPosition >= position) {
					rightChunks.add(ch);					
				}
				workingPosition += size;
			}
			StyledText left = new StyledText((StyledChunk[]) leftChunks.toArray(new StyledChunk[0]));
			StyledText right = new StyledText((StyledChunk[]) rightChunks.toArray(new StyledChunk[0]));
			return new Pair<StyledText, StyledText>(left, right);
		} else {
			return new Pair<StyledText, StyledText>(stext, StyledText.EMPTY);
		}
	}
	
	public Pair<StyledText, StyledText> splitOnExactStringMatch(StyledText stext, String splitOnFirst, boolean keepIndentation){
		String pureText = stext.getText();
		int inx = pureText.indexOf(splitOnFirst);
		if(inx == -1){
			return new Pair<StyledText, StyledText>(stext, StyledText.EMPTY);
		} else {
			return splitAtPureTextPosition(stext, inx, keepIndentation);
		}
	}
	
	//For decimal alignment, splits on first encountered character from the sequence
	public static Pair<StyledText, StyledText> splitAtPureTextPosition(StyledText stext, int splitPosition, boolean keepIndentation){
		String text = stext.getText();
		if (!StringUtil.isEmpty(text)) {
			ArrayList<StyledChunk> leftChunks = new ArrayList<StyledChunk>();
			ArrayList<StyledChunk> rightChunks = new ArrayList<StyledChunk>();
			int position = splitPosition;
			
			int workingPosition = 0;
			StyledChunk[] inputChunks =stext.getChunks(); 
			for(int i=0; i<inputChunks.length; i++){
				StyledChunk ch = inputChunks[i];
				int size = ch.getText().length();
				if(workingPosition + size <= position) {
					leftChunks.add(ch);
				} else if(workingPosition < position && workingPosition + size > position){
					int split = position - workingPosition;
					String txt1 = ch.getText().substring(0, split);
					String txt2 = ch.getText().substring(split);
					leftChunks.add(new StyledChunk(ch.getStyles(), txt1));
					if(keepIndentation)
						rightChunks.add(new StyledChunk(ch.getStyles(), txt2));
					else
						rightChunks.add(new StyledChunk(ch.getNoIndentingStyles(), txt2));
				} else if(workingPosition >= position) {
					rightChunks.add(ch);					
				}
				workingPosition += size;
			}
			StyledText left = new StyledText((StyledChunk[]) leftChunks.toArray(new StyledChunk[0]));
			StyledText right = new StyledText((StyledChunk[]) rightChunks.toArray(new StyledChunk[0]));
			return new Pair<StyledText, StyledText>(left, right);
		} else {
			return new Pair<StyledText, StyledText>(stext, StyledText.EMPTY);
		}
	}
	
	public List<StyledText> groupIntoLines(StyledText stext){
		StyledChunk[] inputChunks =stext.getChunks();
		ArrayList<StyledText> lines = new ArrayList<StyledText>();
		ArrayList<StyledChunk> currentLine = new ArrayList<StyledChunk>();
		for (int i = 0; i < inputChunks.length; i++) {
			StyledChunk ch = inputChunks[i];
			if(ch.getIndentInstruction() != null && currentLine.size() > 0){
				StyledText line = new StyledText((StyledChunk[]) currentLine.toArray(new StyledChunk[0]));
				lines.add(line);
				currentLine.clear();
			} else if (ch.getIndentInstruction() != null && ch.getIndentInstruction().startsWith("+t") &&  currentLine.size() == 0) {
				StyledChunk extraLineChunk = new StyledChunk(ch.getStyles(), " ");
				StyledChunk[] extraLineChunks = {extraLineChunk};
				StyledText extraLine =  new StyledText(extraLineChunks);
				lines.add(extraLine);
			}
			currentLine.add(ch);
		}
		if(currentLine.size() > 0){
			StyledText line = new StyledText((StyledChunk[]) currentLine.toArray(new StyledChunk[0]));
			lines.add(line);
			currentLine.clear();
		} 
		return lines;
	}
}
