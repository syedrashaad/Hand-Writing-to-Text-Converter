package com.handnote.backend.TextDetection.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.model.DetectDocumentTextRequest;
import com.amazonaws.services.textract.model.DetectDocumentTextResult;
import com.amazonaws.services.textract.model.Document;
import com.amazonaws.util.IOUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HandNoteServiceImpl implements HandNoteService {

	@Autowired
	private AmazonTextract amazonTextract;

	@Autowired
	private AmazonS3 amazonS3;

	@Value("${s3.bucket.namei}")
	private String imagesBucket;
	
	@Value("${s3.bucket.namep}")
	private String pdfsBucket;

	@Override
	public File convertMultiPartFileToFile(MultipartFile multipartFile, String service) {
		final File file = new File(this.nameModification(multipartFile.getOriginalFilename(), service));
		try (final FileOutputStream outputStream = new FileOutputStream(file)) {
			outputStream.write(multipartFile.getBytes());
		} catch (IOException e) {
			log.error("Error {} occurred while converting the multipart file", e.getLocalizedMessage());
		}
		return file;
	}

	@Override
	public ByteBuffer convertFileToBytes(File file) {

		ByteBuffer imageBytes = null;

		try (InputStream inputStream = new FileInputStream(file)) {

			imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return imageBytes;
	}

	@Override
	public DetectDocumentTextResult extractText(MultipartFile file) {

		final File output = this.convertMultiPartFileToFile(file, "DETECT");
		ByteBuffer imageBytes = this.convertFileToBytes(output);

		DetectDocumentTextRequest request = new DetectDocumentTextRequest()
				.withDocument(new Document().withBytes(imageBytes));

		DetectDocumentTextResult result = amazonTextract.detectDocumentText(request);
		try {
			Files.delete(output.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;

	}

	@Override
	public void liquidizeDocumentTextResult(DetectDocumentTextResult result) {

		System.out.println(result);

		result.getBlocks().forEach(block -> {
			if (block.getBlockType().equals("PAGE"))
				System.out.println("page is " + block.getPage() + " confidence is " + block.getConfidence());

			if (block.getBlockType().equals("LINE"))
				System.out.println("text is " + block.getText() + " confidence is " + block.getConfidence());
		});
	}

	@Override
	public String storeIntoS3(File file, String bucketName) {
		//MultipartFile multipartFile
		//final File file = this.convertMultiPartFileToFile(multipartFile, "S3");
		final String fileName = file.getName();
		final PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, file);
		amazonS3.putObject(putObjectRequest); //PutObjectResult result = 
		String url = amazonS3.getUrl(bucketName, fileName).toString();
		System.out.println(url);
		try {
			Files.delete(file.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return url;

	}

	@Override
	public String nameModification(String s, String service) {

		String alpha = "abcdefghijklmnopqrstuvwxyz1234";
		String random = "";
		Random obj = new Random();
		int index = s.lastIndexOf('.');
		String temp = s.substring(0, index);
		String temp2 = s.substring(index);

		for (int i = 0; i < 8; i++) {
			int ind = obj.nextInt(alpha.length());
			random = random + alpha.charAt(ind);
		}

		temp = temp + random;
		temp = temp + temp2;
		System.out.println(temp);
		return temp;

	}

	@Override
	public File convertTextToPdf(List<DetectDocumentTextResult> result) {

		PDDocument doc =  new PDDocument();
		//File file = new File("src/main/resources/temporary pdfs/" + this.generateUnique() + ".pdf");
		File file = new File(this.generateUnique() + ".pdf");
		for (int i = 0; i < result.size(); i++) {
			List<String> list = new ArrayList<>();
			String text = "";
			
			result.get(i).getBlocks().forEach(block -> {

				if (block.getBlockType().equals("LINE")) {
					String s = block.getText();
					list.add(s);
				}

			});

			for (String z : list)
				text = text + " "+z;
			
			
			try
			{
			    PDPage page = new PDPage();
			    doc.addPage(page);
			    PDPageContentStream contentStream = new PDPageContentStream(doc, page);

			    PDFont pdfFont = PDType1Font.HELVETICA;
			    float fontSize = 25;
			    float leading = 1.5f * fontSize;

			    PDRectangle mediabox = page.getMediaBox();
			    float margin = 72;
			    float width = mediabox.getWidth() - 2*margin;
			    float startX = mediabox.getLowerLeftX() + margin;
			    float startY = mediabox.getUpperRightY() - margin;

			   
			    List<String> lines = new ArrayList<String>();
			    int lastSpace = -1;
			    while (text.length() > 0)
			    {
			        int spaceIndex = text.indexOf(' ', lastSpace + 1);
			        if (spaceIndex < 0)
			            spaceIndex = text.length();
			        String subString = text.substring(0, spaceIndex);
			        float size = fontSize * pdfFont.getStringWidth(subString) / 1000;
			        System.out.printf("'%s' - %f of %f\n", subString, size, width);
			        if (size > width)
			        {
			            if (lastSpace < 0)
			                lastSpace = spaceIndex;
			            subString = text.substring(0, lastSpace);
			            lines.add(subString);
			            text = text.substring(lastSpace).trim();
			            System.out.printf("'%s' is line\n", subString);
			            lastSpace = -1;
			        }
			        else if (spaceIndex == text.length())
			        {
			            lines.add(text);
			            System.out.printf("'%s' is line\n", text);
			            text = "";
			        }
			        else
			        {
			            lastSpace = spaceIndex;
			        }
			    }

			    contentStream.beginText();
			    contentStream.setFont(pdfFont, fontSize);
			    contentStream.newLineAtOffset(startX, startY);
			    for (String line: lines)
			    {
			        contentStream.showText(line);
			        contentStream.newLineAtOffset(0, -leading);
			    }
			    contentStream.endText(); 
			    contentStream.close();

			}
			catch(IOException e)
			{
				
			}
			
			
		}
		try {
			doc.save(file);
			doc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;

	}

	@Override
	public String generateUnique() {
		String alpha = "abcdefghijklmnopqrstuvwxyz1234";
		Random obj = new Random();
		String unique = "";
		for (int i = 0; i < 12; i++) {
			unique = unique + alpha.charAt(obj.nextInt(alpha.length()));
		}
		return unique;
	}

}
