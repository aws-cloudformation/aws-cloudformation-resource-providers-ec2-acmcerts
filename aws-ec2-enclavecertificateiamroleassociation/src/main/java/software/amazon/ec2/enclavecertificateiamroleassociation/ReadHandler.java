package software.amazon.ec2.enclavecertificateiamroleassociation;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AssociatedRole;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.HandlerErrorCode;

public class ReadHandler extends BaseHandlerStd {
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

            AssociatedRole associatedRole = Helper.getAssociatedRole(proxyClient,
                    model.getCertificateArn(), model.getRoleArn());
            if (associatedRole != null) {
                    model.setCertificateS3BucketName(associatedRole.certificateS3BucketName());
                    model.setCertificateS3ObjectKey(associatedRole.certificateS3ObjectKey());
                    model.setEncryptionKmsKeyId(associatedRole.encryptionKmsKeyId());

                    return ProgressEvent.<ResourceModel, CallbackContext>builder()
                            .resourceModel(model)
                            .status(OperationStatus.SUCCESS)
                            .build();
            }

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .message(String.format("No association found for certificate arn %s and" +
                                    " role arn %s",
                            model.getCertificateArn(), model.getRoleArn()))
                    .status(OperationStatus.FAILED)
                    .errorCode(HandlerErrorCode.NotFound)
                    .build();
        } catch (final Throwable e) {
            return handleException(e, logger);
        }
    }
}
