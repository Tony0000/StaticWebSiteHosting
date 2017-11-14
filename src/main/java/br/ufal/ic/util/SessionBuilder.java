package br.ufal.ic.util;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53ClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

/**
 * This class will handle the Route 53 and S3 Client objects creation
 * with default Region set to Sao Paulo.
 */
public class SessionBuilder {

    public final static String S3Endpoint = "s3-website-sa-east-1.amazonaws.com";

    /**
     * Creates the S3 client with the credentials provided and the Region set to Sao Paulo.
     *
     * @return The S3 Client
     */
    public static AmazonS3 createS3Session(String publicKey, String secretKey) {

        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(publicKey, secretKey)))
                .withRegion(Regions.SA_EAST_1)
                .build();
    }

    /**
     * Creates the Route 53 client with the credentials provided and the Region set to Sao Paulo.
     *
     * @return The Route53 Client
     */
    public static AmazonRoute53 createRoute53Session(String publicKey, String secretKey) {

        return AmazonRoute53ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(publicKey, secretKey)))
                .withRegion(Regions.SA_EAST_1)
                .build();
    }
}
