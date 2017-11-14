package br.ufal.ic;

import br.ufal.ic.util.MgrProgress;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.policy.Principal;
import com.amazonaws.auth.policy.Resource;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.actions.S3Actions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.BucketWebsiteConfiguration;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * This class will handle the logic to create and configure a bucket to serve the purpose of hosting a static website.
 * <ol>
 * <li>Create a bucket.</li>
 * <li>Upload the website files such as html, css and images.</li>
 * <li>Set the entry point and the error page of the website (e.g., index.html and notfound.html respectively).</li>
 * <li>Change the bucket policy to turn it public</li>
 * </ol>
 */
public class WebsiteManager {

    /**
     * Set the entry point and error page of the website to be hosted in the AWS S3 bucket.
     *
     * @param s3         AWS S3 Client.
     * @param bucketName it must be in the following format - www.your-website-name.domain(.com/.org/.etc)
     * @param indexFile  main page of the website.
     * @param errorFile  default error page of the website.
     */
    public static void setWebsiteConfig(AmazonS3 s3, String bucketName, String indexFile, String errorFile) {
        BucketWebsiteConfiguration websiteConfig;
        if (indexFile == null) {
            websiteConfig = new BucketWebsiteConfiguration();
        } else if (errorFile == null) {
            websiteConfig = new BucketWebsiteConfiguration(indexFile);
        } else {
            websiteConfig = new BucketWebsiteConfiguration(indexFile, errorFile);
        }

        try {
            s3.setBucketWebsiteConfiguration(bucketName, websiteConfig);

        } catch (AmazonServiceException e) {
            System.out.println("Error creating website configuration");
            System.err.println(e.getErrorMessage());
        }

        s3.setBucketPolicy(bucketName, declarePolicy(bucketName));

    }

    /**
     * Creates the policy to make a bucket public
     *
     * @param bucketName the bucket name.
     * @return the JSON policy in String format to be send to AWS.
     */
    private static String declarePolicy(String bucketName) {
        Policy policyText = new Policy().withStatements(
                new Statement(Statement.Effect.Allow)
                        .withPrincipals(Principal.All)
                        .withActions(S3Actions.GetObject)
                        .withResources(new Resource(
                                "arn:aws:s3:::" + bucketName + "/*"
                        ))
        );
        return policyText.toJson();
    }

    /**
     * Reads the policy to make a bucket public from a file
     *
     * @param policyFile File with the policy content in valid JSON format.
     * @return the JSON policy in String format to be send to AWS.
     */
    private static String getBucketPolicyFromFile(String policyFile) {
        StringBuilder file_text = new StringBuilder();
        try {
            List<String> lines = Files.readAllLines(
                    Paths.get(policyFile), Charset.forName("UTF-8"));
            for (String line : lines) {
                file_text.append(line);
            }
        } catch (IOException e) {
            System.out.format("Problem reading file: \"%s\"", policyFile);
            System.out.println(e.getMessage());
        }

        // Verify the policy by trying to load it into a Policy object.
        Policy bucket_policy = null;
        try {
            bucket_policy = Policy.fromJson(file_text.toString());
        } catch (IllegalArgumentException e) {
            System.out.format("Invalid policy text in file: \"%s\"",
                    policyFile);
            System.out.println(e.getMessage());
        }

        return bucket_policy.toJson();
    }

    /**
     * Uploads the directory with all the website content, it will also print the current upload progress.
     *
     * @param s3         AWS S3 Client
     * @param bucketname The bucket to receive the files
     * @param dirPath    Path of the folder with the website files
     * @param recursive  If there are multiple subdirectories then set it to True
     */
    public static void uploadDir(AmazonS3 s3, String bucketname, String dirPath, boolean recursive) {

        TransferManagerBuilder builder = TransferManagerBuilder.standard();
        builder.setS3Client(s3);
        TransferManager manager = builder.build();

        try {
            MultipleFileUpload uploader = manager.uploadDirectory(bucketname, "", new File(dirPath), recursive);
            MgrProgress.showTransferProgress(uploader);

        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        //manager.shutdownNow();

    }

    /**
     * Creates the bucket with the given name
     *
     * @param s3          AWS S3 client
     * @param bucket_name bucket name
     * @return The Bucket object
     */
    public static Bucket createBucket(AmazonS3 s3, String bucket_name) {

        Bucket b = null;
        if (s3.doesBucketExistV2(bucket_name)) {
            b = s3.listBuckets().get(0);
            System.out.format("Bucket %s already exists.\n", bucket_name);
            b = null;
        } else {
            try {
                b = s3.createBucket(bucket_name);
            } catch (AmazonS3Exception e) {
                System.err.println(e.getErrorMessage());
            }
        }
        return b;
    }
}
