package com.handnote.backend.TextDetection.service;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.textract.model.DetectDocumentTextResult;

public interface HandNoteService {

	
	public File convertMultiPartFileToFile(final MultipartFile multipartFile, String service);
	
	public ByteBuffer convertFileToBytes(final File file);
	
	public DetectDocumentTextResult extractText(final MultipartFile file);
	
	//after some time can be converted into this method becomes convert to pdf 
	//method name will also change
	//for time being this method just prints 
	
	public void liquidizeDocumentTextResult(DetectDocumentTextResult result);
	

	public File convertTextToPdf(List<DetectDocumentTextResult> result);
	
	public String storeIntoS3(final File file, final String bucketName);
	
	public String nameModification(String s, String service);
	
	public String generateUnique();
}
