package software.amazon.ec2.enclavecertificateiamroleassociation;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DisassociateEnclaveCertificateIamRoleRequest;
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
public class DeleteHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<Ec2Client> proxyClient;

    @Mock
    private Ec2Client ec2Client;

    private DeleteHandler handler;

    @BeforeEach
    public void setup() {
        handler = spy(new DeleteHandler());
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        ec2Client = mock(Ec2Client.class);
        proxyClient = MOCK_PROXY(proxy, ec2Client);
    }

    @Test
    public void testDisassociateEnclaveCertIamRoleSuccess() {
        final ResourceModel model = ResourceModel.builder()
                .certificateArn(TestUtils.CERTIFICATE_ARN)
                .roleArn(TestUtils.ROLE_ARN)
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(proxyClient.client()
                .disassociateEnclaveCertificateIamRole(any(DisassociateEnclaveCertificateIamRoleRequest.class)))
                .thenReturn(TestUtils.createDisassociateResponse());

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testDisassociateEnclaveCertIamRoleWithoutCert() {
        final ResourceModel model = ResourceModel.builder()
                .roleArn(TestUtils.ROLE_ARN)
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
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
    public void testDisassociateEnclaveCertIamRoleWithoutIamRole() {
        final ResourceModel model = ResourceModel.builder()
                .certificateArn(TestUtils.CERTIFICATE_ARN)
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
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
}
