/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.StringTokenizer;

public class SasshiatoSP {
	Properties props = null;
	private String sd2 = new String(new char[]{'c','h','p','h','2','2'}); 
	public Properties getSeProperties() {
		if(props==null){
			try{
				Properties p = new Properties();
				p.setProperty("companyName", "Public");
				p.setProperty("outputs", "pdf,rtf");
				p.setProperty("companyShortName", "Public");
				props = p;
			}catch(Exception e){
				SasshiatoTrace.logError("Error reading internal configuration, contact btcisp.com", e);
			}
		}
		return props;
	}
	
}
