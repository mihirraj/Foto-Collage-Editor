package com.wisesharksoftware.app_photoeditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.pdfjet.A4;
import com.pdfjet.Image;
import com.pdfjet.ImageType;
import com.pdfjet.PDF;
import com.pdfjet.Page;

public class ConvertUtils {

	public static void generatePdf(String imageFileName, String pdfFileName) {
		List<String> imageFileNames = new ArrayList<String>();
		imageFileNames.add(imageFileName);
		generatePdf(imageFileNames, pdfFileName);
	}
	
	public static void generatePdf(List<String> imageFileNames, String pdfFileName) {
		float left_margin = 57;// 2cm
		float right_margin = 57;// 2cm
		float top_margin = 57;// 2cm
		float bottom_margin = 57;// 2cm
		float max_width_pt = A4.PORTRAIT[0] - left_margin - right_margin;
		float max_height_pt = A4.PORTRAIT[1] - top_margin - bottom_margin;
		float width_pt;
		float height_pt;
		float scale = 1;	
		float scale_width;
		float scale_height;
		final int dpi = 72;		
		try {
			FileOutputStream fos = new FileOutputStream(pdfFileName);
			PDF pdf = new PDF(fos);
			for (int i = 0; i < imageFileNames.size(); i++) {
				File file = new File(imageFileNames.get(i));
				if (!file.exists()) {
					continue;
				}
				FileInputStream fis = new FileInputStream(imageFileNames.get(i));
				Image image = new Image(pdf, fis, ImageType.JPG);
				
				width_pt = image.getWidth() / dpi * 72;
				height_pt = image.getHeight() / dpi * 72;				
				scale_width = 1;
				scale_height = 1;
				if (width_pt > max_width_pt) {
					scale_width = max_width_pt / width_pt;
				}
				if (height_pt > max_height_pt) {
					scale_height = max_height_pt / height_pt;
				}
				scale = scale_width;
				if (scale_width > scale_height) {
					scale = scale_height;
				}

				Page page = new Page(pdf, A4.PORTRAIT);

				image.setPosition(left_margin, top_margin);
				image.scaleBy(scale);
				image.drawOn(page);
			}
			pdf.flush();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
