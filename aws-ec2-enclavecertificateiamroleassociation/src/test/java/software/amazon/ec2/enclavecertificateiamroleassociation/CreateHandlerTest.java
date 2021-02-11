package software.amazon.ec2.enclavecertificateiamroleassociation;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AssociateEnclaveCertificateIamRoleRequest;
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
public class CreateHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<Ec2Client> proxyClient;

    @Mock
    private Ec2Client ec2Client;

    private CreateHandler handler;

    @BeforeEach
    public void setup() {
        handler = spy(new CreateHandler());
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS,
                () -> Duration.ofSeconds(600).toMillis());
        ec2Client = mock(Ec2Client.class);
        proxyClient = MOCK_PROXY(proxy, ec2Client);
    }

    @Test
    public void testAssociateEnclaveCertIamRoleSuccess() {
        final ResourceModel model = ResourceModel.builder()
                .certificateArn(TestUtils.CERTIFICATE_ARN)
                .roleArn(TestUtils.ROLE_ARN)
                .build();
        final ResourceHandlerRequest<ResourceModel> request =
                TestUtils.createAssociationRequest(model);

        when(proxyClient.client()
                .associateEnclaveCertificateIamRole(any(AssociateEnclaveCertificateIamRoleRequest.class)))
                .thenReturn(TestUtils.createAssociationResponse());

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, null, proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        ResourceModel returnModel = response.getResourceModel();
        assertThat(returnModel.getCertificateS3BucketName()).isEqualTo(TestUtils.CERTIFICATE_S3_BUCKET_NAME);
        assertThat(returnModel.getCertificateS3ObjectKey()).isEqualTo(TestUtils.CERTIFICATE_S3_OBJECT_KEY);
        assertThat(returnModel.getEncryptionKmsKeyId()).isEqualTo(TestUtils.ENCRYPTION_KMS_KEY_ID);
    }

    @Test
    public void testAssociateEnclaveCertIamRoleWithoutCert() {
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
    public void testAssociateEnclaveCertIamRoleWithoutIamRole() {
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
    public void testAssociateEnclaveCertIamRoleMalformedCertArn() {
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
                .associateEnclaveCertificateIamRole(any(AssociateEnclaveCertificateIamRoleRequest.class)))
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
    public void testAssociateEnclaveCertIamRoleMalformedRoleArn() {
        final ResourceModel model = ResourceModel.builder()
                .certificateArn(TestUtils.CERTIFICATE_ARN)
                .roleArn(TestUtils.MALFORMED_ROLE_ARN)
                .build();
        final ResourceHandlerRequest<ResourceModel> request =
                TestUtils.createAssociationRequest(model);

        final String ERROR_MESSAGE = "Malformed role arn message";
        final AwsServiceException exception =
                TestUtils.getException(400, BaseHandlerStd.ERROR_CODE_ROLE_ARN_MALFORMED,
                        ERROR_MESSAGE);

        when(proxyClient.client()
                .associateEnclaveCertificateIamRole(any(AssociateEnclaveCertificateIamRoleRequest.class)))
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
    public void testAssociateEnclaveCertIamRoleTooManyAssociatedRoles() {
        final ResourceModel model = ResourceModel.builder()
                .certificateArn(TestUtils.CERTIFICATE_ARN)
                .roleArn(TestUtils.ROLE_ARN)
                .build();
        final ResourceHandlerRequest<ResourceModel> request =
                TestUtils.createAssociationRequest(model);

        final String ERROR_MESSAGE = "Too many associated roles message";
        final AwsServiceException exception =
                TestUtils.getException(400,
                        BaseHandlerStd.ERROR_CODE_CERTIFICATE_TOO_MANY_ASSOCIATED_ROLES,
                        ERROR_MESSAGE);

        when(proxyClient.client()
                .associateEnclaveCertificateIamRole(any(AssociateEnclaveCertificateIamRoleRequest.class)))
                .thenThrow(exception);

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, null, proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).contains(ERROR_MESSAGE);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.ServiceLimitExceeded);
    }

    @Test
    public void testAssociateEnclaveCertIamRoleUnauthorizedOperation() {
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
                .associateEnclaveCertificateIamRole(any(AssociateEnclaveCertificateIamRoleRequest.class)))
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
    public void testAssociateEnclaveCertIamRoleServerInternalError() {
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
                .associateEnclaveCertificateIamRole(any(AssociateEnclaveCertificateIamRoleRequest.class)))
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
    public void testAssociateEnclaveCertIamRoleServiceUnavailableError() {
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
                .associateEnclaveCertificateIamRole(any(AssociateEnclaveCertificateIamRoleRequest.class)))
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
