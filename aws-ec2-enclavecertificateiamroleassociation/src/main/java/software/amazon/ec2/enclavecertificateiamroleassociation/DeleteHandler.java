package software.amazon.ec2.enclavecertificateiamroleassociation;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DisassociateEnclaveCertificateIamRoleRequest;
import software.amazon.awssdk.services.ec2.model.DisassociateEnclaveCertificateIamRoleResponse;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;

public class DeleteHandler extends BaseHandlerStd {
    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(AmazonWebServicesClientProxy proxy,
                                                                          ResourceHandlerRequest<ResourceModel> request,
                                                                          CallbackContext callbackContext,
                                                                          ProxyClient<Ec2Client> proxyClient,
                                                                          Logger logger) {
        final ResourceModel model = request.getDesiredResourceState();

        try {
            validateNotNull(model.getCertificateArn(), Properties.CertificateArn);
            validateNotNull(model.getRoleArn(), Properties.RoleArn);

            final DisassociateEnclaveCertificateIamRoleRequest disassociateRequest =
                    DisassociateEnclaveCertificateIamRoleRequest
                            .builder()
                            .certificateArn(model.getCertificateArn())
                            .roleArn(model.getRoleArn())
                            .build();
            final DisassociateEnclaveCertificateIamRoleResponse response = proxyClient
                    .injectCredentialsAndInvokeV2(disassociateRequest,
                            proxyClient.client()::disassociateEnclaveCertificateIamRole);

            logger.log(String.format("%s [%s] deleted successfully.",
                    ResourceModel.TYPE_NAME,
                    model.getPrimaryIdentifier()));

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.SUCCESS)
                    .build();
        } catch (final Throwable e) {
            return handleException(e, logger);
        }
    }
}
