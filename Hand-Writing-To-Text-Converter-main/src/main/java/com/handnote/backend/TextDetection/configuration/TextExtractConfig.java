package com.handnote.backend.TextDetection.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;

@Configuration
public class TextExtractConfig {

	
		@Value("${access.key.id}")
	    private String accessKeyId;

	    @Value("${access.key.secret}")
	    private String accessKeySecret;

	    @Value("${s3.region.name}")
	    private String regionName;
	    
	    
	    @Bean
	    public AmazonTextract getAmazonTextractClient() {
	        final BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(accessKeyId, accessKeySecret);
	        return AmazonTextractClientBuilder
	                .standard()
	                .withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials))
	                .withRegion(regionName)
	                .build();
	    }
}
