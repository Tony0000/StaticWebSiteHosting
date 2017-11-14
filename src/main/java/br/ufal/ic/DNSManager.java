package br.ufal.ic;

import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.model.*;

import br.ufal.ic.util.SessionBuilder;

import java.util.List;

/**
 * Class responsible for the Route 53 Client side of the website hosting.
 * It has methods to create host zones and change record sets so it may
 * serve as the DNS for a AWS S3 bucket.
 */
public class DNSManager {

  /**
   * This method will use the {@link AmazonRoute53} client to create a host zone and configure it to be the DNS for a bucket in AWS S3
   *
   * @param r53          Route 53 Client to execute the operations
   * @param hostZoneName The host zone name must be the same name of the AWS S3 bucket without prefix "www"
   * @param domainName   The AWS S3 bucket name
   */
  public static void configureHostZone(AmazonRoute53 r53, String hostZoneName, String domainName) {

    CreateHostedZoneResult creationResult = hostZoneBuilder(r53, hostZoneName);
    ChangeResourceRecordSetsRequest request;
    ChangeResourceRecordSetsResult response;
    GetChangeRequest changeRequest;

    if (creationResult != null) {
      System.out.println("Host Zone created - " + creationResult.getChangeInfo());
      request = createRecordSet(domainName, creationResult.getHostedZone().getId());

    } else {
      System.out.println("Hosted zone already exists. Trying to change only the record set.");
      String hostZoneId = "";
      List<HostedZone> zones = r53.listHostedZones().getHostedZones();
      for (HostedZone zone : zones) {
        if (zone.getName().equals(hostZoneName))
          hostZoneId = zone.getId();
      }
      if (hostZoneId.equals("")) {
        System.out.println("Could not find the host zone id.");
        System.exit(0);
      } else {
        System.out.println("Host Zone found.");
      }
      request = createRecordSet(domainName, hostZoneId);
    }
    System.out.println("Record set change request created.");
    response = r53.changeResourceRecordSets(request);

    changeRequest = new GetChangeRequest();
    changeRequest.setId(response.getChangeInfo().getId());

    while (ChangeStatus.PENDING.toString().equals(r53.getChange(changeRequest).getChangeInfo().getStatus())) {
      System.out.println("Change is pending");
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    System.out.println("Change is complete");
  }

  /**
   * Using the {@link AmazonRoute53} client it will create a Host Zone with the given host zone name,
   * it will return Null if the host zone already exists.
   * Do not directly invoke this method
   *
   * @param r53          Route 53 Client
   * @param hostZoneName Name of the host zone to be created
   * @return A CreateHostedZoneResult with result of the request
   */
  private static CreateHostedZoneResult hostZoneBuilder(AmazonRoute53 r53, String hostZoneName) {
    List<HostedZone> zones = r53.listHostedZones().getHostedZones();
    for (HostedZone zone : zones) {
      if (zone.getName().equals(hostZoneName + "."))
        return null;
    }

    String timeReference = String.valueOf(System.currentTimeMillis());
    return r53.createHostedZone(
      new CreateHostedZoneRequest()
        .withName(hostZoneName)
        .withCallerReference(timeReference)
                               );
  }

  /**
   * Creates a change request to insert a new record set in the Host zone at Route 53
   *
   * @param domainName The AWS S3 bucket name to be linked to the given host zone id
   * @param hostZoneId The AWS Route 53 host zone Id to be linked
   * @return The change request object for the Route 53 client to execute
   */
  private static ChangeResourceRecordSetsRequest createRecordSet(String domainName, String hostZoneId) {

    ResourceRecordSet recordSet = new ResourceRecordSet()
      .withName(domainName)
      .withType(RRType.A)
      .withAliasTarget(
        new AliasTarget()
          // The S3 host zone can not be retrieved by the API.
          // The list of zones can be found bellow.
          // http://docs.aws.amazon.com/general/latest/gr/rande.html#s3_region
          .withHostedZoneId("Z7KQH4QJS55SO")
          .withEvaluateTargetHealth(true)
          .withDNSName(SessionBuilder.S3Endpoint));

    ChangeBatch batch = new ChangeBatch();
    batch.withChanges(new Change()
        .withResourceRecordSet(recordSet)
        .withAction(ChangeAction.CREATE)
                     );

    return new ChangeResourceRecordSetsRequest()
      .withHostedZoneId(hostZoneId)
      .withChangeBatch(batch);
  }
}
