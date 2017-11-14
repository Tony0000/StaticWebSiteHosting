package br.ufal.ic;

import br.ufal.ic.util.SessionBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;

/**
 * Created by Antonio Manoel and Lucas Amaral
 * */

public class Main {

    public static void main(String[] args) {

        String publicKey = null;
        String secretKey = null;
        String hostZoneName = null;
        String bucketName;
        String errorPage = null;
        String mainPage = null;
        String folder = null;
        String dirPath;
        final String prefix = "www";

        if (args.length == 6) {
            publicKey = args[0];
            secretKey = args[1];
            hostZoneName = args[2];
            folder = args[3];
            mainPage = args[4];
            errorPage = args[5];
        } else if(args.length == 2){
            System.out.println("------Using default config------");
            publicKey = args[0];
            secretKey = args[1];
            folder = "website";
            mainPage = "index.html";
            errorPage = "404.html";
            hostZoneName = "escalabilidade2017.tk";
        }else{
            System.out.println("Public and Secret key not provided. Execution cannot proceed.");
            System.exit(1);
        }

        bucketName = prefix + "." + hostZoneName;
        AmazonS3 s3 = SessionBuilder.createS3Session(publicKey, secretKey);
        dirPath = System.getProperty("user.dir") + "\\" + folder;

        Bucket b = WebsiteManager.createBucket(s3, bucketName);
        if (b != null) {
            System.out.println("Bucket created.");
            WebsiteManager.uploadDir(s3, bucketName, dirPath, true);
            WebsiteManager.setWebsiteConfig(s3, bucketName, mainPage, errorPage);
        }

        try {
            System.out.println("Suspending thread to wait for the changes consolidation.");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        DNSManager.configureHostZone(SessionBuilder.createRoute53Session(publicKey, secretKey), hostZoneName, bucketName);
        System.exit(0);
    }
}
