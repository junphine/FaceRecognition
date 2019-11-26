package com.example.deeplearning.ocr;

import java.io.IOException;

import com.example.commons.constants.OcrConstants;

public class OcrTrain {
	public static void main(String[] args) {
		Ocr ocr = new Ocr.Builder()
				.setEpochs(100)
				.setBatchSize(15)
				.setDataSetType("train")
				.setTimeSeriesLength(16)
				.setMaxLabelLength(16)
				.setTextChars(OcrConstants.textChars)
				.setDirPath("data/captcha-x/")
				.setModelFileName("ocrModel.json")
				.toOcr();
		try {
			ocr.train();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
