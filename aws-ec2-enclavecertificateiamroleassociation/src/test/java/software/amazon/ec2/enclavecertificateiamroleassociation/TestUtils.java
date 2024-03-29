package software.amazon.ec2.enclavecertificateiamroleassociation;

import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.ec2.model.AssociateEnclaveCertificateIamRoleResponse;
import software.amazon.awssdk.services.ec2.model.AssociatedRole;
import software.amazon.awssdk.services.ec2.model.DisassociateEnclaveCertificateIamRoleResponse;
import software.amazon.awssdk.services.ec2.model.GetAssociatedEnclaveCertificateIamRolesResponse;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.ArrayList;
import java.util.List;

public class TestUtils {
    static final String ROLE_ARN = "arn:aws:iam::123456789012:role/acm-role";
    static final String ROLE_ARN_2 = "arn:aws:iam::123456789092:role/acm-role-2";
    static final String MALFORMED_ROLE_ARN = "arn:aws:ia::123456789012:role/acm-role";
    static final String CERTIFICATE_ARN = "arn:aws:acm:us-east-1:123456789012:certificate/" +
            "d4c3b2a1-e5d0-4d51-95d9-1927fEXAMPLE";
    static final String MALFORMED_CERTIFICATE_ARN = "arn:aws:ac:us-east-1:123456789012" +
            ":certificate/d4c3b2a1-e5d0-4d51-95d9-1927fEXAMPLE";
    static final String CERTIFICATE_S3_BUCKET_NAME = "aws-ec2-enclave-certificate-us-east-1";
    static final String CERTIFICATE_S3_OBJECT_KEY = "arn:aws:iam::123456789012:role/acm-role/" +
            "arn:aws:acm:us-east-1:123456789012:certificate/d4c3b2a1-e5d0-4d51-95d9-1927fEXAMPLE";
    static final String ENCRYPTION_KMS_KEY_ID = "a1b2c3d4-354d-4e51-9190-b12ebEXAMPLE";
    static final String CERTIFICATE_S3_OBJECT_KEY_2 = "arn:aws:iam::123456789092:role/" +
            "acm-role-2/arn:aws:acm:us-east-1:123456789012:certificate/d4c3b2a1-e5d0-4d51-95d9-1927fEXAMPLE";
    static final String ENCRYPTION_KMS_KEY_ID_2 = "a1b2c3d4-354d-4e51-9190-b13ebEXAMPLE";
    static final String LOGICAL_RESOURCE_IDENTIFIER = "localTestResource";
    static final String CLIENT_REQUEST_TOKEN = "46d512c3-06ab-4354-b33b-a6179ad";
    static final String REQUEST_ID = "4cft6cb5-1de0-2840-8d9r-c3b9c7sjde9c";

    public static ResourceHandlerRequest<ResourceModel> createAssociationRequest(final ResourceModel model) {
        return ResourceHandlerRequest.<ResourceModel>builder()
                .logicalResourceIdentifier(LOGICAL_RESOURCE_IDENTIFIER)
                .clientRequestToken(CLIENT_REQUEST_TOKEN)
                .desiredResourceState(model)
                .build();
    }

    public static AssociateEnclaveCertificateIamRoleResponse createAssociationResponse() {
        return AssociateEnclaveCertificateIamRoleResponse
                .builder()
                .certificateS3BucketName(CERTIFICATE_S3_BUCKET_NAME)
                .certificateS3ObjectKey(CERTIFICATE_S3_OBJECT_KEY)
                .encryptionKmsKeyId(ENCRYPTION_KMS_KEY_ID)
                .build();
    }

    public static DisassociateEnclaveCertificateIamRoleResponse createDisassociateResponse() {
        return DisassociateEnclaveCertificateIamRoleResponse.builder().build();
    }

    public static GetAssociatedEnclaveCertificateIamRolesResponse createGetAssociationsResponse() {
        return GetAssociatedEnclaveCertificateIamRolesResponse
                .builder()
                .associatedRoles(createAssociatedRoles())
                .build();
    }

    public static GetAssociatedEnclaveCertificateIamRolesResponse createGetAssociationsResponseRoleNotFound() {
        return GetAssociatedEnclaveCertificateIamRolesResponse
                .builder()
                .associatedRoles(createAssociatedRolesRoleNotFound())
                .build();
    }

    public static GetAssociatedEnclaveCertificateIamRolesResponse createGetAssociationsEmptyResponse() {
        return GetAssociatedEnclaveCertificateIamRolesResponse
                .builder()
                .associatedRoles(new ArrayList<>())
                .build();
    }

    public static List<AssociatedRole> createAssociatedRoles() {
        ArrayList<AssociatedRole> roles = new ArrayList<>();
        roles.add(AssociatedRole.builder()
                .associatedRoleArn(TestUtils.ROLE_ARN)
                .certificateS3BucketName(TestUtils.CERTIFICATE_S3_BUCKET_NAME)
                .certificateS3ObjectKey(TestUtils.CERTIFICATE_S3_OBJECT_KEY)
                .encryptionKmsKeyId(TestUtils.ENCRYPTION_KMS_KEY_ID)
                .build());
        roles.add(AssociatedRole.builder()
                .associatedRoleArn(TestUtils.ROLE_ARN_2)
                .certificateS3BucketName(TestUtils.CERTIFICATE_S3_BUCKET_NAME)
                .certificateS3ObjectKey(TestUtils.CERTIFICATE_S3_OBJECT_KEY_2)
                .encryptionKmsKeyId(TestUtils.ENCRYPTION_KMS_KEY_ID_2)
                .build());

        return roles;
    }

    public static List<AssociatedRole> createAssociatedRolesRoleNotFound() {
        ArrayList<AssociatedRole> roles = new ArrayList<>();
        roles.add(AssociatedRole.builder()
                .associatedRoleArn(TestUtils.ROLE_ARN_2)
                .certificateS3BucketName(TestUtils.CERTIFICATE_S3_BUCKET_NAME)
                .certificateS3ObjectKey(TestUtils.CERTIFICATE_S3_OBJECT_KEY_2)
                .encryptionKmsKeyId(TestUtils.ENCRYPTION_KMS_KEY_ID_2)
                .build());

        return roles;
    }

    public static AwsServiceException getException(final int statusCode, final String errorCode, final String errorMessage) {
        return AwsServiceException.builder()
                .awsErrorDetails(AwsErrorDetails.builder()
                        .sdkHttpResponse(SdkHttpResponse.builder()
                                .statusCode(statusCode)
                                .build())
                        .errorCode(errorCode)
                        .serviceName("Ec2")
                        .errorMessage(errorMessage)
                        .build())
                .statusCode(statusCode)
                .requestId(REQUEST_ID)
                .build();
    }
}
