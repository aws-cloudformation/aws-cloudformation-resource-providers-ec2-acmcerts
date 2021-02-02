package software.amazon.ec2.enclavecertificateiamroleassociation;

import software.amazon.awssdk.services.ec2.model.AssociateEnclaveCertificateIamRoleRequest;
import software.amazon.awssdk.services.ec2.model.AssociateEnclaveCertificateIamRoleResponse;
import software.amazon.awssdk.services.ec2.Ec2Client;

import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;

public class CreateHandler extends BaseHandlerStd {
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

            final AssociateEnclaveCertificateIamRoleRequest associateEnclaveCertificateIamRoleRequest =
                    AssociateEnclaveCertificateIamRoleRequest
                            .builder()
                            .certificateArn(model.getCertificateArn())
                            .roleArn(model.getRoleArn())
                            .build();
            final AssociateEnclaveCertificateIamRoleResponse response =
                    proxyClient.injectCredentialsAndInvokeV2(associateEnclaveCertificateIamRoleRequest,
                            proxyClient.client()::associateEnclaveCertificateIamRole);
            model.setCertificateS3BucketName(response.certificateS3BucketName());
            model.setCertificateS3ObjectKey(response.certificateS3ObjectKey());
            model.setEncryptionKmsKeyId(response.encryptionKmsKeyId());

            logger.log(String.format("%s %s created successfully with " +
                        "CertificateS3BucketName %s, CertificateS3ObjectKey %s, " +
                        "EncryptionKmsKeyId %s",
                    ResourceModel.TYPE_NAME,
                    model.getPrimaryIdentifier(),
                    model.getCertificateS3BucketName(),
                    model.getCertificateS3ObjectKey(),
                    model.getEncryptionKmsKeyId()));

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(OperationStatus.SUCCESS)
                    .build();
        } catch (final Throwable e) {
            return handleException(e, logger);
        }
    }
}
