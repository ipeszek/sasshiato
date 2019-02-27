/*
 * Sasshiato: PDF and RTF document generation for RRG (https://github.com/ipeszek/RRG) reporting system.
 *
 * This file is part of sasshiato project (https://github.com/ipeszek/sasshiato) which is released under GNU General Public License v3.0.
 * See the LICENSE file in the root directory or go to https://www.gnu.org/licenses/gpl-3.0.en.html for full license details.
 */
package com.btcisp.sasshiato;

import java.awt.Color;
import java.io.File;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;

public class ImageUtil {
	private static ImageUtil self = new ImageUtil();
	public static ImageUtil getSelf() {
		return self;
	}
	
	//TODO not thread safe
	File current_watermark;
	ReportSetup current_setup;
	Image currentImage = null;
	float current_width = 0;
	float current_height = 0;
	
	public Image getImage(File watermark, ReportSetup setup) throws Exception{
		if(current_watermark == watermark && current_setup == setup && currentImage!=null)
			return currentImage;
		Image wim = null;
		float wim_height = 0;
		float wim_width = 0;
			if(!watermark.exists()){
				throw new Exception("Watermark file " + watermark.getAbsolutePath() + " does not exist");
			}
			
			//get watermark image
			try{
				wim = Image.getInstance(watermark.getAbsolutePath());
				wim_height= wim.getHeight();
				wim_width = wim.getWidth();
				
				SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "watermark image sizes: h," + wim_height +",w=" + wim_width);
				if(setup.getTableHeight()<(wim_height * 1.4) || setup.getTableWidth()<(wim_height * 1.4)){
					//scale image
					float ratioh = (float) (wim_height/setup.getTableHeight());
					float ratiow = (float) (wim_height/setup.getTableWidth());
					float ratio = Math.max(ratioh, ratiow) * 1.4f;
					SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "Scaling watermark image for PDF by factor of " + ratio);
					wim_height = wim_height / ratio ;
					wim_width  = wim_width / ratio ;
					wim.scaleAbsolute(wim_width, wim_height);
					//wim.setBorder(Rectangle.BOX);
					//wim.setBorderColor(Color.BLACK);
					SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "watermark image sizes after scaling: h," + wim.getScaledHeight() +",w=" + wim.getScaledWidth());
				}

				//wim.setBorder(Image.LEFT + Image.RIGHT + Image.TOP + Image.BOTTOM);
				//wim.setBorderWidth(2f);
			}catch(Exception e){
				e.printStackTrace();
				throw new Exception("Cannot parse image, image maybe corrupted " + watermark.getAbsolutePath());
			}
	    current_height = wim_height;
	    current_width = wim_width;
	    currentImage = wim;
	    current_watermark = watermark;
	    current_setup = setup;
	    wim.setAbsolutePosition((setup.getLeftMargin() + setup.getTableWidth() - current_width) /2, (setup.getBottomMargin() + setup.getTableHeight() - current_height) /2);
		SasshiatoTrace.log(SasshiatoTrace.LEV_INFO, "watermark absolute position at x=" + ((setup.getTableWidth() - current_width) /2) + ",y=" + ((setup.getTableHeight() - current_height) /2));
	    return wim;
	}
	
}
