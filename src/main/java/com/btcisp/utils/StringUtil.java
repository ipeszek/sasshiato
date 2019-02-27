/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.utils;

import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.text.StyledEditorKit.StyledTextAction;

/**
* string service provides PowerBuilder behaviour of conversion
* to int, etc. And other needed string utilities such as wrapping,
* checking empty, checking "Y" = "Yes", etc. 
* <p>
* See method descriptions.
* @author = Robert Peszek
*/

public class StringUtil  {
	public static int MAX_LINE_END_BLANKS = 7;
	public static boolean areEqual(String one, String two) {
		if (isEmpty(one)) {
			return isEmpty(two);
		} else {
			return one.equals(two);
		}

	}
	public static int countOccurrences(String str, String subStr) {
		int ret = 0;
		int len = subStr.length();
		int pos = -len;
		while ((pos = str.indexOf(subStr, pos + len)) != -1) {
			ret++;
		}
		return ret;
	}
	
	public static String nonEmpty(String s){
		if(s==null) return "";
		else return s;
	}
	/**
	 * This method was created in VisualAge by Henry.
	 * returns String with same n characters
	 * @param n int
	 */
	public static String fill(String aChar, int n) {
		char[] ret = new char[n];
		for (int i = 0; i < n; i++) {
			ret[i] = aChar.charAt(0);
		}
		return new String(ret);
	}
	public static String fill(char aChar, int n) {
		if(n<0) return "";
		char[] ret = new char[n];
		for (int i = 0; i < n; i++) {
			ret[i] = aChar;
		}
		return new String(ret);
	}
    public static String leftPadWithBlanks(String txt, int totLength) throws Exception{
    	if(txt ==null) txt = "";
    	if(txt.length()> totLength) throw new Exception("Size of "+ txt + " exceeds total allowed length "+ totLength);
    	return fill(' ', totLength-txt.length()) + txt;
    }
	/**
	* Ecapsultes RF logic for checking if String is empty.
	*/
	public static boolean isEmptyAfterTrim(String s) {
		if (s == null || s.trim().equals(""))
			return true;
		else
			return false;
	}
	
	public static boolean isEmpty(String s) {
		if (s == null || s.trim().equals(""))
			return true;
		else
			return false;
	}
	
	/**
	* Trims on the left
	* This method was created in VisualAge by Henry.
	* @param s java.lang.String
	* @param l int
	*/
	public static String leftString(String s, int l) {
		String ss = new String(s);
		if (ss.length() <= l)
			return ss;
		return ss.substring(0, l);
	}
	public static int lineCount(String s) {
		return countOccurrences(s, "\n");
	}


	
	public static String[] separateDelimitedText(String txt, String delimiter, boolean trim){
		if(txt==null) return new String[0];
		Vector resultV = new Vector();
		StringTokenizer tokenizer = new StringTokenizer(txt, delimiter);
		while(tokenizer.hasMoreTokens()){
			String el = tokenizer.nextToken();
			if(el!=null) {
				if(trim) 
					el = el.trim();
				resultV.add(el);
			}
		}
		String[] result= new String[resultV.size()];
		resultV.copyInto(result);
		return result;
	}
	public static String leftTrim(String s) {
		if(s==null) return s;
		int position = 0;
		for(int i=0; i<s.length(); i++){
			if(s.charAt(i) == ' ') {
				position = i +1;
			} else {
				break;
			}
		}
		if(position >= s.length())
			return "";
		else
			return s.substring(position);
		
	}
	
	public static String rightTrim(String s) {
		if(s==null) return s;
		int position = s.length();
		for(int i=s.length()-1; i> -1; i--){
			if(s.charAt(i) == ' ') {
				position = i;
			} else {
				break;
			}
		}
		if(position ==s.length()){
			return s;
		}
		else if(position < 1)
			return "";
		else
			return s.substring(0, position);
		
	}
	/**
	* removes specified leading characters
	*/
	public static String OLD_removeLeadingCharacters(String s, char newc) {
		char[] c = s.toCharArray();
		char[] empty = { ' ' };
		int len = s.length();
		StringBuffer sb = new StringBuffer("");

		for (int i = 0; i < len; i++) {
			if (c[i] == newc) {
				c[i] = empty[0];
			} else
				break;
		}
		for (int i = 0; i < len; i++) {
			sb = sb.append(c[i]);
		}
		return sb.toString();
	}
	/**
	* Old implementation of VAJ had a buggy behavior of replace.
	* This is replacement method for String replace method.
	* This method was created in VisualAge by Herny.
	*/
	public static String replace(String s, char oldc, char newc) {
		char[] c = s.toCharArray();
		int len = s.length();
		for (int i = 0; i < len; i++) {
			if (c[i] == oldc)
				c[i] = newc;
		}
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < len; i++) {
			sb = sb.append(c[i]);
		}
		return sb.toString();
	}
	/**
	* Old implementation of VAJ had a buggy behavior of replace.
	* This is replacement method for String replace method.
	* @return java.lang.String
	* @param oldStr java.lang.String
	* @param newStr java.lang.String
	*/
	public static String replace(
		String origStr,
		int start,
		int len,
		String newStr) {
		String ls_return = new String(origStr);
		//
		ls_return =
			leftString(ls_return, start)
				+ newStr
				+ rightString(ls_return, ls_return.length() - start - len);
		return ls_return;
	}
	/**
	* Old implementation of VAJ had a buggy behavior of replace.
	* This is replacement method for String replace method.
	 * This method was created in VisualAge by Henry.
	 * @return java.lang.String
	 * @param oldStr java.lang.String
	 * @param newStr java.lang.String
	 */
	public static String replace(
		String origStr,
		String oldStr,
		String newStr) {
		/*** Bad implementation: execution results in an infinite loop when oldStr is a substring of newStr
			String ls_return = new String(origStr);
			//
			int li_pos, li_len;
			//
			li_len = oldStr.length();
			//
			do {
			li_pos = ls_return.indexOf(oldStr);
			if (li_pos == -1)
			break;
			//
			ls_return = leftString(ls_return, li_pos) + newStr + rightString(ls_return, ls_return.length() - li_pos - li_len);
			} while (true);
			//
			return ls_return;
		***/
		if (origStr == null)
			return null;
		if (oldStr == null)
			return origStr;
		if (newStr == null)
			newStr = "";
		String ret = origStr;
		int oldLen = oldStr.length();
		int newLen = newStr.length();
		for (int pos = 0;
			(pos = ret.indexOf(oldStr, pos)) != -1;
			pos = pos + newLen)
			ret = ret.substring(0, pos) + newStr + ret.substring(pos + oldLen);
		return ret;
	}
	/**
	 * 
	 */
	public static String replaceNth(
		String origStr,
		String oldStr,
		String newStr,
		int n) {
		String ret = origStr;
		int oldLen = oldStr.length();
		int occCount = 0;
		for (int pos = 0;
			(pos = ret.indexOf(oldStr, pos)) != -1;
			pos = pos + oldLen)
			if (++occCount == n)
				ret =
					ret.substring(0, pos)
						+ newStr
						+ ret.substring(pos + oldLen);
		return ret;
	}
	/**
	 * Trims on the right.
	 * This method was created in VisualAge by Henry.
	 * @param s java.lang.String
	 * @param l int
	 */
	public static String rightString(String s, int l) {
		String ls_return;
		ls_return = s.substring(s.length() - l, s.length());
		return ls_return;

	}
	/**
	* provides PowerBuilder behaviour of conversion
	* to int, Returns 0 if String is not valid integer
	 * This method was created in VisualAge by Herny.
	*/
	public static int roundToInt(String in) {
		Long L = new Long(roundToLong(in));
		return L.intValue();
	}
	/**
	* provides PowerBuilder behaviour of conversion
	* to int, Returns 0 if String is not valid integer
	 * This method was created in VisualAge by Herny.
	*/
	public static long roundToLong(String in) {
		double a = 0;
		long l = 0;
		try {
			a = (new Double(in)).doubleValue();
			l = Math.round(a);
		} catch (Exception e) {
			return 0;
		}
		return l;
	}
	public static String sortLines(String text) {
		String s;
		boolean inserted = false;
		StringTokenizer st =
			new StringTokenizer(text, "\n");
		Vector strings = new Vector();
		while (st.hasMoreTokens()) {
			s = st.nextToken();
			inserted = false;
			if (strings.size() == 0)
				strings.addElement(s);
			else {
				for (int i = 0; i < strings.size(); i++)
					if (s.compareTo((String) strings.elementAt(i)) < 0) {
						strings.insertElementAt(s, i);
						inserted = true;
						break;
					}
				if (!inserted)
					strings.addElement(s);
			}
		}
		StringBuffer ret = new StringBuffer();
		for (int i = 0; i < strings.size(); i++) {
			s = (String) strings.elementAt(i);
			ret.append(s).append("\n");
		}
		return ret.toString();
	}
	/**
	* Creates n-long empty String.
	* This method was created in VisualAge by Herny.
	* @param n int
	*/
	public static String space(int n) {
		return fill(" ", n);
	}
	/**
	* The opposite to wordWrap. Makes sure that output String do not start/end with blanks
	* (other than first and last). It may end or start with blanks if part of the text for text has each character separated by a blank. 
	* Does not make sure that width is exact. Output width may be smaller
	* than argument width. The trim is done on argument input first.
	*
	*/

	public static String[] trimSafeWrap(String input, int width) {
		String[] returnS = null;
		if (input == null) {
			returnS = new String[1];
			returnS[0] = null;
			return returnS;
		}

		if (input.length() == 0) {
			returnS = new String[1];
			returnS[0] = input;
			return returnS;
		}

		Vector resultLines = new Vector();
		input = input.trim();
		String resultLine = null;
		while (true) {
			if (input.length() > width) {
				int cut_point = width;
				while (cut_point > 0) {
					if (input.charAt(cut_point - 1) == ' '
						|| input.charAt(cut_point) == ' ') {
						cut_point = cut_point - 1;
					} else {
						break;
					}
				}
				if (cut_point == 0)
					cut_point = width;
				//possible only for text where each character is separated by a blank
				resultLine = input.substring(0, cut_point);
				input = input.substring(cut_point);
				resultLines.addElement(resultLine.trim());
			} else {
				resultLines.addElement(input);
				break;
			}
		}

		returnS = new String[resultLines.size()];
		resultLines.copyInto(returnS);
		return returnS;
	}
	public static String[] trimSafeWrap2(String input, int width, int upper) {
		int cnt  = 0;
		String[] returnS = null;
		if (input == null) {
			returnS = new String[1];
			returnS[0] = null;
			return returnS;
		}

		if (input.length() == 0) {
			returnS = new String[1];
			returnS[0] = input;
			return returnS;
		}

		Vector resultLines = new Vector();
		input = input.trim();
		String resultLine = null;
		while (true) {
			if (input.length() > width && cnt < upper) {
				int cut_point = width;
				while (cut_point > 0) {
					if (input.charAt(cut_point - 1) == ' '
						|| input.charAt(cut_point) == ' ') {
						cut_point = cut_point - 1;
					} else {
						break;
					}
				}
				if (cut_point == 0)
					cut_point = width;
				//possible only for text where each character is separated by a blank
				resultLine = input.substring(0, cut_point);
				input = input.substring(cut_point);
				resultLines.addElement(resultLine.trim());
				cnt++;
			} else {
				resultLines.addElement(input);
				break;
			}
		}

		returnS = new String[resultLines.size()];
		resultLines.copyInto(returnS);
		return returnS;
	}	

	/**
	* Wraps argument String and returns array of Strings with width =< then second argument
	* Tries not to split words so if a word crosses lines and starts <MAX_LINE_END_BLANKS=5 characters before the line end
	* it is moved to the next line.
	* Usage: see error screen or wrapping in the DbScreenPresistence
	*
	*/

	public static String[] wordWrap(String input, int width) {

		if (width < MAX_LINE_END_BLANKS + 1) {
			return wrap(input, width);
		}

		String[] returnS = null;
		if (input == null) {
			returnS = new String[1];
			returnS[0] = null;
			return returnS;
		}

		if (input.length() == 0) {
			returnS = new String[1];
			returnS[0] = input;
			return returnS;
		}

		Vector resultLines = new Vector();
		input = input.trim();

		while (true) {
			if (input.length() > width) {
				String fullLine = input.substring(0, width + 1);
				String resultLine = "";
				int last_blank = fullLine.lastIndexOf(" ");
				if (last_blank > width - MAX_LINE_END_BLANKS) {
					resultLine = input.substring(0, last_blank);
					input = input.substring(last_blank).trim();
				} else {
					resultLine = input.substring(0, width);
					input = input.substring(width).trim();
				}
				resultLines.addElement(resultLine.trim());
			} else {
				resultLines.addElement(input);
				break;
			}
		}

		returnS = new String[resultLines.size()];
		resultLines.copyInto(returnS);
		return returnS;
	}
	/**
	* Wraps argument String and returns array of Strings with width =< then second argument
	* Usage: see error screen or wrapping in the DbScreenPresistence
	*
	*/

	public static String[] wrap(String input, int width) {
		String[] returnS = null;
		if (input == null) {
			returnS = new String[1];
			returnS[0] = null;
			return returnS;
		}

		int size = 0;
		int length = input.length();

		if (length == 0) {
			returnS = new String[1];
			returnS[0] = input;
			return returnS;
		}

		size = length / width;
		size += size * width == length ? 0 : 1;
		returnS = new String[size];
		int currentpointer = 0;

		for (int i = 0; i < size; i++) {
			if (currentpointer + width < length) {
				returnS[i] =
					input.substring(currentpointer, currentpointer + width);
				currentpointer += width;
			} else {
				returnS[i] = input.substring(currentpointer);
				break;
			}
		}

		return returnS;

	}
	
	public static String trim(String s){
		if(s==null) return s;
		else return s.trim();
	}
	
	public static int indexOfAnyOfChars(String txt, int fromIndex, String chars){
		int found = -1;
		for (int i = 0; i < chars.length(); i++) {
			char ch = chars.charAt(i);
			int inx = txt.indexOf(ch, fromIndex);
			if(inx != -1){
				if(found == -1)
					found = inx;
				else if(inx < found){
					found = inx;
				}
			}
		}
		return found;
	}
	
	public static boolean isInteger(String num){
		   try{
			   Integer.parseInt(num);
			   return true;
		   }catch(Exception e){
			   //ignore this index
			   return false;
		   }
	}
}
