package com.handnote.backend.TextDetection.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.textract.model.DetectDocumentTextResult;
import com.handnote.backend.TextDetection.service.HandNoteService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/handnote")
public class HandNoteController {

	@Value("${s3.bucket.namei}")
	private String imagesBucket;
	
	@Value("${s3.bucket.namep}")
	private String pdfsBucket;
	
	@Autowired
	private HandNoteService handNoteService;

	@GetMapping("/health-check")
	public String healthCheck()
	{
	  return "it's working";
	}
	
	@PostMapping("/extract-text")
	public String extractText(@RequestParam("image") MultipartFile[] multipartFile) {
		List<DetectDocumentTextResult> result = new ArrayList<>();

		//MultipartFile multipartFile[] = {multipartFile1, multipartFile2};
		
		for (int i = 0; i < multipartFile.length; i++)
		{
			System.out.println(multipartFile[i].getOriginalFilename());
			result.add(this.handNoteService.extractText(multipartFile[i]));
		}
		File file = this.handNoteService.convertTextToPdf(result);
		return this.handNoteService.storeIntoS3(file,pdfsBucket);

		//ResponseEntity<Resource>
//		Path path = Paths.get(file.getAbsolutePath());
//		ByteArrayResource resource = null;
//		try {
//			resource = new ByteArrayResource(Files.readAllBytes(path));
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		ResponseEntity<Resource> finalResource = ResponseEntity.ok().contentLength(file.length())
//				.contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);

//		try {
//			Files.delete(file.toPath());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

	//	return finalResource;
	}

	@PostMapping("/store-s3")
	public String storeInS3(@RequestParam("image") MultipartFile multipartFile) {
		System.out.println("inside store");
		return (this.handNoteService.storeIntoS3(this.handNoteService.convertMultiPartFileToFile(multipartFile, null),imagesBucket));
	}
}
