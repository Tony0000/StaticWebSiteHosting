## Purpose

This project is for the purpose of obtaining grades in Software Scalability. 

by Antonio Manoel and Lucas Antonio 


##How to configure a static website hosting with custom domain in Amazon Web Services

On a static website, individual webpages include static content. They might also contain client-side scripts.
By contrast, a dynamic website relies on server-side processing, including server-side scripts such as PHP, 
JSP, or ASP.NET. Amazon S3 does not support server-side scripting.
###Before Starting

###Step 1 - Your credentials
It's required to have an IAM account's public and secret key with S3FullAccess and Route53FullAcess.
These keys must be passed as arguments in the correct order PUBLIC KEY and then SECRET KEY. 

###Step 2 - Register your domain
You must have registered a domain, for the purposed of testing this example will assume you have the domain
www.escalabilidade2017.tk .

Note that even without the domain the code will work and configure both services (S3 and Route53),
but it won't be possible to access your website through the link (www.escalabilidade2017.tk). 

###Step 3 - Your website files
The website folder with all required subdirectories must be placed in the root folder of this project.
There is a website example folder if you do not have one at hand. 
The default values for main page and error page are index.html and 404.html.

###Step 4 -  Compile and execute
From the following parameters, at least the keys are required to proceed as it will use default values and 
try to upload the files in 'website' folder. 

* Public key
* Secret Key
* Website Name - e.g. escalabilidade2017.com (do not include "www.")
* Website folder name
* Website main page
* Website error page

Command to compile 
       
    mvn package

Command to run

    java -jar target/Hosting-1.0-SNAPSHOT.jar public_key secret_key optional_params...