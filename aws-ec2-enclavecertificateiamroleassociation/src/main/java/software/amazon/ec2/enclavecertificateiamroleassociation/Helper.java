package software.amazon.ec2.enclavecertificateiamroleassociation;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AssociatedRole;
import software.amazon.awssdk.services.ec2.model.GetAssociatedEnclaveCertificateIamRolesRequest;
import software.amazon.awssdk.services.ec2.model.GetAssociatedEnclaveCertificateIamRolesResponse;
import software.amazon.cloudformation.proxy.ProxyClient;

public class Helper {
    public static AssociatedRole getAssociatedRole(ProxyClient<Ec2Client> proxyClient,
                                            String certificateArn,
                                            String roleArn) {
        final GetAssociatedEnclaveCertificateIamRolesRequest associatedEnclaveCertificateIamRolesRequest =
                GetAssociatedEnclaveCertificateIamRolesRequest
                        .builder()
                        .certificateArn(certificateArn)
                        .build();
        final GetAssociatedEnclaveCertificateIamRolesResponse getResponse =
                proxyClient.injectCredentialsAndInvokeV2(associatedEnclaveCertificateIamRolesRequest,
                        proxyClient.client()::getAssociatedEnclaveCertificateIamRoles);

        for (AssociatedRole associatedRole: getResponse.associatedRoles()) {
            if (associatedRole.associatedRoleArn().equals(roleArn)) {
                return associatedRole;
            }
        }

        return null;
    }
}
