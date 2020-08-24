package com.example.commons.constants;

import java.util.Arrays;
import java.util.List;

public interface OcrConstants {
	int height = 60;
	int width = 200;//160;
	int channels = 1;
	
	int captchaLength = 5; //must be 5
	
	String textChars = "_234578acdefgmnpwxy";
	String modelFilePath = "model/captcha/ocrModel.json";
	

  //"0123456789abcdefghijklmnopqrstuvwxyz"
   static List<String> labelListAll =
      Arrays.asList(
	  "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H",
	  "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");
  
  // "234578acdefgmnpwxy"
   static List<String> labelListMini =
	      Arrays.asList(
		   "2", "3", "4", "5", "7", "8", "9", "a",  "c", "d", "e", "f", "g", 
		   "m", "n",  "p", "w", "x", "y");
   
   static List<String> labelList = labelListAll;
}
