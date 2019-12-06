package com.example.deeplearning.ocr;

import java.io.IOException;

import com.example.commons.constants.OcrConstants;

public class OcrTrain {
	public static void main(String[] args) {
		Ocr ocr = new Ocr.Builder()
				.setEpochs(10)
				.setBatchSize(15)
				.setDataSetType("train")
				.setTimeSeriesLength(16)
				.setMaxLabelLength(8)
				.setTextChars(OcrConstants.textChars)
				.setDirPath("data/captcha/")
				.setModelFileName(OcrConstants.modelFilePath)
				.toOcr();
		try {
			ocr.train();
			
			ocr.predict();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
