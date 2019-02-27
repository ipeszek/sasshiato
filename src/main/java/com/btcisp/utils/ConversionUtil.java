/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.utils;


public class ConversionUtil {
private static ConversionUtil self = new ConversionUtil();

public static ConversionUtil getSelf(){
	return self;
}

 	public float parseFloatWithPoints(String s, float defCharWidth) throws Exception{
		float unitsize = 1;
		s = s.toLowerCase();
		boolean charRequest = false;
		if(s.endsWith("in")){
			unitsize= 72f;
			s = s.substring(0, s.indexOf("in"));
		} else if(s.endsWith("cm")){
			unitsize= 72f/2.54f ;
			s = s.substring(0, s.indexOf("cm"));
		} else if(defCharWidth != -1f && s.endsWith("ch")){
			unitsize=defCharWidth;
			s = s.substring(0, s.indexOf("ch"));
			charRequest =true;
		}
			float reqSize = Float.parseFloat(s);
			//TODO strict mod
			//if(charRequest) reqSize = reqSize + 0.3f;
			reqSize = reqSize * unitsize;
			return reqSize;
	}
}
