/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.util.ArrayList;
import java.util.List;

import com.btcisp.utils.StringUtil;
import com.lowagie.text.Font;

public class StyledChunk implements Cloneable{
	public static final List<String> EMPTY_STYLES = new ArrayList();
   private List<String> styles;
   private String text;
  public static final String STYLE_INSTRUCTION_IT = "it";
  public static final String STYLE_INSTRUCTION_BF = "bf";
  
   
	public StyledChunk(List<String> styles, String text) {
		super();
		this.styles = styles;
		this.text = text;
    }
	public List<String> getStyles() {
		return styles;
	}
	
	public List<String> getNoIndentingStyles() {
		List<String> adjustedStyles = new ArrayList<String>();
		for(int i=0; i< styles.size(); i++){
			if(!styles.get(i).startsWith(StyledTextUtil.INTERNAL_STYLE_INSTRUCTION_INDENT_GEN_PREFIX)){
				adjustedStyles.add(styles.get(i));
			}
		}
		return adjustedStyles;
	}
	
	//TODO adjust if we add superscript etc
	public List<String> getFontFaceStyles() {
		List<String> adjustedStyles = new ArrayList<String>();
		for(int i=0; i< styles.size(); i++){
			if(!styles.get(i).startsWith(StyledTextUtil.INTERNAL_STYLE_INSTRUCTION_INDENT_GEN_PREFIX)){
				adjustedStyles.add(styles.get(i));
			}
		}
		return adjustedStyles;
	}
	
	public String getEncodedNoIndentingStyles() {
		StringBuffer sb = new StringBuffer();
		for(int i=0; i< StyledTextUtil.VALID_NON_INDENT_STYLE_INSTRUCTION_SEQ.length; i++){
			String style = StyledTextUtil.VALID_NON_INDENT_STYLE_INSTRUCTION_SEQ[i];
			if(styles.contains(style))
				sb.append(style).append(';');
		}
		String res = sb.toString();
		if(res.length() > 0){
			res = res.substring(0, res.length());
		}
		return res;
	}
	
	public String getRftFFInstructions(){
		StringBuffer sb = new StringBuffer();
		for(int i=0; i< styles.size(); i++){
			String style = styles.get(i);
			if(style.equals(STYLE_INSTRUCTION_BF)){
				sb.append("\\b");
			} else if(style.equals(STYLE_INSTRUCTION_IT)){
				sb.append("\\i");
			}
		}
		String res = sb.toString();
		if(res.length() > 0){
			res = res.substring(0, res.length());
		}
		return res;
	}
	
	//returns +iN or +tN if they are set
	public String getIndentInstruction() {
		if(styles.size() >0 && styles.get(0).startsWith(StyledTextUtil.INTERNAL_STYLE_INSTRUCTION_INDENT_GEN_PREFIX)){
			return styles.get(0);
		} else {
			return null;
		}
	}
	public String getText() {
		return text;
	}
   
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if(styles.size() > 0) {			
			sb.append("{\\__");
			for(int i=0; i< styles.size(); i++){
				String internalStyle = styles.get(i);
				sb.append(internalStyle);
				if(i != styles.size()-1){
					sb.append(";");
				}
			}
			sb.append("__\\}");
			sb.append(text);
			return sb.toString();
		} else {
			return text;
		}
	}
   
	public StyledChunk clone() {
		ArrayList<String> _styles = new ArrayList<String>();
		styles.addAll(this.styles);
		return new StyledChunk(_styles, this.text);
	}
}
