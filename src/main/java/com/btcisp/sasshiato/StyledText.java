/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.util.List;

import com.btcisp.utils.StringUtil;

public class StyledText {
	
	public static StyledText withNoStyle(String txt){
		StyledChunk ch = new StyledChunk(StyledChunk.EMPTY_STYLES, txt);
		StyledChunk[] chs = {ch};
		return new StyledText(chs);
	}
	public static StyledText withConfiguredStyle(String txt, String styleType, ReportLaF laf){
		StyledChunk ch = new StyledChunk(laf.getFontStylesForStyleType(styleType), txt);
		StyledChunk[] chs = {ch};
		return new StyledText(chs);
	}
   static final StyledChunk[] NO_CHUNKS = {};	 
   public static final StyledText EMPTY = new StyledText(NO_CHUNKS);
   private StyledChunk[] chunks;
   private String _text;
   private int[] chunkStartPositions;
	
   public StyledText(StyledChunk[] chunks) {
	 super();
	 this.chunks = chunks;
   }

   public StyledChunk[] getChunks() {
		return chunks;
   }
   
   //returns style-less text
   public String getText() {
		if(_text == null){
			chunkStartPositions = new int[chunks.length];
			StringBuffer sb = new StringBuffer();
			int chunkStartingPos = 0;
			for(int i=0; i<chunks.length; i++){
				sb.append(chunks[i].getText());
				chunkStartPositions[i] = chunkStartingPos;
				chunkStartingPos = sb.length();
			}
			
			_text = sb.toString();
		}
		return _text;
   }
   
   //given position in the styleless text returns chunk that the character is part of
   public StyledChunk getChunkAt(int i){
	   if(chunkStartPositions == null){
		   getText();
	   }
	   int lastIndex = -1;
	   for (int j = 0; j < chunkStartPositions.length; j++) {
		   int position = chunkStartPositions[j];
		   if(i < position){
			   break;
		   } else {
			   lastIndex = j;
		   }
	   }
	   return chunks[lastIndex];
   }
   
   public boolean isEmpty() {
	   if(chunks.length==0)
		   return true;
	   else if(chunks.length == 1){
		   if(StringUtil.isEmpty(chunks[0].getText()))
			   return true;
		   else
			   return false;
	   } else {
		   return false;
	   }
   }
    
   public int pureTextSize() {
	   return this.getText().length();
   }
   public StyledText substring(int i){
	   Pair<StyledText, StyledText> split = StyledTextUtil.splitAtPureTextPosition(this, i, true);
	   return split.right;
   }
   
   public StyledText trim() {
	   if(chunks.length == 0){
		   return this;
	   } else {
		   StyledChunk[] _chunks = new StyledChunk[chunks.length];
		   for (int i = 0; i < _chunks.length; i++) {
			   if(i==0 || i == _chunks.length -1){
				   String _text = chunks[i].getText();
				   if(i == 0){
					   _text = StringUtil.leftTrim(_text); 
				   }
				   if(i == _chunks.length -1){
					   _text = StringUtil.rightTrim(_text); 
				   }
				   _chunks[i] = new StyledChunk(chunks[i].getStyles(), _text);
			   } else {
				   _chunks[i] = chunks[i];  
			   }			    
		   }
		   return new StyledText(_chunks);
	   }
   }
   
   public StyledText rightTrim() {
	   if(chunks.length == 0){
		   return this;
	   } else {
		   StyledChunk[] _chunks = new StyledChunk[chunks.length];
		   for (int i = 0; i < _chunks.length; i++) {
			   if(i==_chunks.length -1 ){
				   String _text = chunks[i].getText();
					_text = StringUtil.rightTrim(_text); 
				   _chunks[i] = new StyledChunk(chunks[i].getStyles(), _text);
			   } else {
				   _chunks[i] = chunks[i];  
			   }			    
		   }
		   return new StyledText(_chunks);
	   }
   }
   
   public StyledText leftTrim() {
	   if(chunks.length == 0){
		   return this;
	   } else {
		   StyledChunk[] _chunks = new StyledChunk[chunks.length];
		   for (int i = 0; i < _chunks.length; i++) {
			   if(i== 0 ){
				   String _text = chunks[i].getText();
					_text = StringUtil.leftTrim(_text); 
				   _chunks[i] = new StyledChunk(chunks[i].getStyles(), _text);
			   } else {
				   _chunks[i] = chunks[i];  
			   }			    
		   }
		   return new StyledText(_chunks);
	   }

   }
   
   public int getIndent() {
	  int indent = 0;
	  if(chunks.length > 0){
   		 String indentIntruction = chunks[0].getIndentInstruction();
     	 if(indentIntruction != null){
     		 String s0 = indentIntruction.substring(2,3);
 			 try {
 				 indent = Integer.parseInt(s0);
 			 } catch (NumberFormatException ex) {

 			 }
      	 }
  	  }
	  return indent;
   }
   
   public String toString() {
	   return getText();
   }
   
   public int hashCode() {
	   return getText().hashCode(); 
   }
   
   //TODO this may need more thinking, seems like too relaxed
   public boolean equals(Object o){
	   if(!(o instanceof StyledText)) 
		   return false;
	   return ((StyledText)o).getText().equals(this.getText());
   }
   
   public boolean equalsAsText(StyledText o){
	   return this.getText().equals(o.getText());
   }
   
   public StyledText replace(String oldString, String newString){
	   if(this.getText().indexOf(oldString) == -1) {
		   return this;
	   } else {
		   StyledChunk[] chunks = this.chunks;
		   StyledChunk[] resChunks = new StyledChunk[chunks.length];
		   for (int i = 0; i < chunks.length; i++) {
			  StyledChunk styledChunk = chunks[i];
			  String oldText = chunks[i].getText();
			  if(oldText.indexOf(oldString) != -1){
				  String newText = StringUtil.replace(oldText, oldString, newString);
				  resChunks[i] = new StyledChunk(chunks[i].getStyles(), newText);
			  } else {
				  resChunks[i] = chunks[i]; 
			  }
		   }
		   return new StyledText(resChunks);
	   }
   }
   
   public StyledText appendToLastChunk(String txt, String defaultStyleType, ReportLaF laf){
	   StyledChunk[] chunks = this.chunks;
	   if(chunks.length == 0){
		   return withConfiguredStyle(txt, defaultStyleType, laf);
	   } else {
		   StyledChunk lastChunk = chunks[chunks.length -1];
		   List<String> styles = lastChunk.getStyles(); 
		   StyledChunk newChunk = new StyledChunk(styles, lastChunk.getText() + txt);
		   StyledChunk[] resch = new StyledChunk[chunks.length];
		   
		   for (int i = 0; i < resch.length -1; i++) {
			   resch[i] = chunks[i];			
		   }
		   resch[resch.length -1] = newChunk;
		   StyledText res = new StyledText(resch);
		   return res;
	   }
   }
   
   public StyledText appendUsingLastCharacterStyle(String txt, String defaultStyleType, ReportLaF laf){
	   StyledChunk[] chunks = this.chunks;
	   if(chunks.length == 0){
		   return withConfiguredStyle(txt, defaultStyleType, laf);
	   } else {
		   List<String> styles = chunks[chunks.length -1].getFontFaceStyles(); 
		   StyledChunk newChunk = new StyledChunk(styles, txt);
		   StyledChunk[] resch = new StyledChunk[chunks.length + 1];
		   
		   for (int i = 0; i < chunks.length; i++) {
			   resch[i] = chunks[i];			
		   }
		   resch[resch.length -1] = newChunk;
		   StyledText res = new StyledText(resch);
		   return res;
	   }
   }
}
