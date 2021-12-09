package software.amazon.ec2.enclavecertificateiamroleassociation;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AssociatedRole;
import software.amazon.awssdk.services.ec2.model.GetAssociatedEnclaveCertificateIamRolesRequest;
import software.amazon.awssdk.services.ec2.model.GetAssociatedEnclaveCertificateIamRolesResponse;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.HandlerErrorCode;

public class ListHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(AmazonWebServicesClientProxy proxy,
                                                                          ResourceHandlerRequest<ResourceModel> request,
                                                                          CallbackContext callbackContext,
                                                                          ProxyClient<Ec2Client> proxyClient,
                                                                          Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();


        try {
           validateNotNull(model.getCertificateArn(), Properties.CertificateArn);
           final GetAssociatedEnclaveCertificateIamRolesRequest associatedEnclaveCertificateIamRolesRequest =
                GetAssociatedEnclaveCertificateIamRolesRequest
                    .builder()
                    .certificateArn(model.getCertificateArn())
                    .build();

            final GetAssociatedEnclaveCertificateIamRolesResponse response =
                proxyClient.injectCredentialsAndInvokeV2(associatedEnclaveCertificateIamRolesRequest,
                    proxyClient.client()::getAssociatedEnclaveCertificateIamRoles);

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(Translator.translateForList(response, model.getCertificateArn()))
                .status(OperationStatus.SUCCESS)
                .build();
        } catch (final Throwable e) {
            return handleException(e, logger);
        }
    }
}
