package software.amazon.ec2.enclavecertificateiamroleassociation;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.GetAssociatedEnclaveCertificateIamRolesRequest;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<Ec2Client> proxyClient;

    @Mock
    private Ec2Client ec2Client;

    private ReadHandler handler;

    @BeforeEach
    public void setup() {
        handler = spy(new ReadHandler());
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        ec2Client = mock(Ec2Client.class);
        proxyClient = MOCK_PROXY(proxy, ec2Client);
    }

    @Test
    public void testGetEnclaveCertRoleArnAssociationSuccess() {
        final ResourceModel model = ResourceModel.builder()
                .certificateArn(TestUtils.CERTIFICATE_ARN)
                .roleArn(TestUtils.ROLE_ARN)
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(proxyClient.client()
                .getAssociatedEnclaveCertificateIamRoles(any(GetAssociatedEnclaveCertificateIamRolesRequest.class)))
                .thenReturn(TestUtils.createGetAssociationsResponse());

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getResourceModel()).isNotNull();

        ResourceModel returnModel = response.getResourceModel();
        assertThat(returnModel.getCertificateS3BucketName()).isEqualTo(TestUtils.CERTIFICATE_S3_BUCKET_NAME);
        assertThat(returnModel.getCertificateS3ObjectKey()).isEqualTo(TestUtils.CERTIFICATE_S3_OBJECT_KEY);
        assertThat(returnModel.getEncryptionKmsKeyId()).isEqualTo(TestUtils.ENCRYPTION_KMS_KEY_ID);
    }

    @Test
    public void testGetEnclaveCertRoleArnAssociationEmptyResponse() {
        final ResourceModel model = ResourceModel.builder()
                .certificateArn(TestUtils.CERTIFICATE_ARN)
                .roleArn(TestUtils.ROLE_ARN)
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(proxyClient.client()
                .getAssociatedEnclaveCertificateIamRoles(any(GetAssociatedEnclaveCertificateIamRolesRequest.class)))
                .thenReturn(TestUtils.createGetAssociationsEmptyResponse());

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        final String ERROR_MESSAGE = String.format("No association found for certificate arn %s and" +
                        " role arn %s", TestUtils.CERTIFICATE_ARN, TestUtils.ROLE_ARN);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).contains(ERROR_MESSAGE);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
    }

    @Test
    public void testGetEnclaveCertRoleArnAssociationNotFound() {
        final ResourceModel model = ResourceModel.builder()
                .certificateArn(TestUtils.CERTIFICATE_ARN)
                .roleArn(TestUtils.ROLE_ARN)
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(proxyClient.client()
                .getAssociatedEnclaveCertificateIamRoles(any(GetAssociatedEnclaveCertificateIamRolesRequest.class)))
                .thenReturn(TestUtils.createGetAssociationsResponseRoleNotFound());

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        final String ERROR_MESSAGE = String.format("No association found for certificate arn %s and" +
                " role arn %s", TestUtils.CERTIFICATE_ARN, TestUtils.ROLE_ARN);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).contains(ERROR_MESSAGE);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
    }

    @Test
    public void testGetEnclaveCertRoleArnAssociationWithoutCert() {
        final ResourceModel model = ResourceModel.builder()
                .roleArn(TestUtils.ROLE_ARN)
                .build();
        final ResourceHandlerRequest<ResourceModel> request =
                TestUtils.createAssociationRequest(model);
        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, null, proxyClient, logger);
        final String ERROR_MESSAGE = String.format(BaseHandlerStd.PROPERTY_CANNOT_BE_EMPTY,
                "CertificateArn");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).contains(ERROR_MESSAGE);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);
    }

    @Test
    public void testGetEnclaveCertIamRoleAssociationWithoutIamRole() {
        final ResourceModel model = ResourceModel.builder()
                .certificateArn(TestUtils.CERTIFICATE_ARN)
                .build();
        final ResourceHandlerRequest<ResourceModel> request =
                TestUtils.createAssociationRequest(model);
        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, null, proxyClient, logger);
        final String ERROR_MESSAGE = String.format(BaseHandlerStd.PROPERTY_CANNOT_BE_EMPTY,
                "RoleArn");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).contains(ERROR_MESSAGE);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);
    }

    @Test
    public void testGetEnclaveCertIamRoleAssociationMalformedCertArn() {
        final ResourceModel model = ResourceModel.builder()
                .certificateArn(TestUtils.MALFORMED_CERTIFICATE_ARN)
                .roleArn(TestUtils.ROLE_ARN)
                .build();
        final ResourceHandlerRequest<ResourceModel> request =
                TestUtils.createAssociationRequest(model);

        final String ERROR_MESSAGE = "Malformed certificate arn message";
        final AwsServiceException exception =
                TestUtils.getException(400,
                        BaseHandlerStd.ERROR_CODE_CERTIFICATE_ARN_MALFORMED,
                        ERROR_MESSAGE);

        when(proxyClient.client()
                .getAssociatedEnclaveCertificateIamRoles(any(GetAssociatedEnclaveCertificateIamRolesRequest.class)))
                .thenThrow(exception);

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, null, proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).contains(ERROR_MESSAGE);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);
    }

    @Test
    public void testGetEnclaveCertIamRoleAssociationUnauthorizedOperation() {
        final ResourceModel model = ResourceModel.builder()
                .certificateArn(TestUtils.CERTIFICATE_ARN)
                .roleArn(TestUtils.ROLE_ARN)
                .build();
        final ResourceHandlerRequest<ResourceModel> request =
                TestUtils.createAssociationRequest(model);

        final String ERROR_MESSAGE = "Unauthorized operation message";
        final AwsServiceException exception =
                TestUtils.getException(403,
                        BaseHandlerStd.ERROR_CODE_UNAUTHORIZED_OPERATION,
                        ERROR_MESSAGE);

        when(proxyClient.client()
                .getAssociatedEnclaveCertificateIamRoles(any(GetAssociatedEnclaveCertificateIamRolesRequest.class)))
                .thenThrow(exception);

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, null, proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).contains(ERROR_MESSAGE);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.AccessDenied);
    }

    @Test
    public void testGetEnclaveCertIamRoleAssociationServerInternalError() {
        final ResourceModel model = ResourceModel.builder()
                .certificateArn(TestUtils.CERTIFICATE_ARN)
                .roleArn(TestUtils.ROLE_ARN)
                .build();
        final ResourceHandlerRequest<ResourceModel> request =
                TestUtils.createAssociationRequest(model);

        final String ERROR_MESSAGE = "Server internal error message";
        final AwsServiceException exception =
                TestUtils.getException(500, BaseHandlerStd.ERROR_CODE_SERVER_INTERNAL,
                        ERROR_MESSAGE);

        when(proxyClient.client()
                .getAssociatedEnclaveCertificateIamRoles(any(GetAssociatedEnclaveCertificateIamRolesRequest.class)))
                .thenThrow(exception);

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, null, proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InternalFailure);
    }

    @Test
    public void testGetEnclaveCertIamRoleAssociationServiceUnavailableError() {
        final ResourceModel model = ResourceModel.builder()
                .certificateArn(TestUtils.CERTIFICATE_ARN)
                .roleArn(TestUtils.ROLE_ARN)
                .build();
        final ResourceHandlerRequest<ResourceModel> request =
                TestUtils.createAssociationRequest(model);

        final String ERROR_MESSAGE = "Service unavailable error message";
        final AwsServiceException exception =
                TestUtils.getException(500, BaseHandlerStd.ERROR_CODE_SERVICE_UNAVAILABLE,
                        ERROR_MESSAGE);

        when(proxyClient.client()
                .getAssociatedEnclaveCertificateIamRoles(any(GetAssociatedEnclaveCertificateIamRolesRequest.class)))
                .thenThrow(exception);

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, null, proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.ServiceInternalError);
    }
}
