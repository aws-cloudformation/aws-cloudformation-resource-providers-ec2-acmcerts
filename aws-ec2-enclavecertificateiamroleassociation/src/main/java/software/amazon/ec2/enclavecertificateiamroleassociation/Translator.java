package software.amazon.ec2.enclavecertificateiamroleassociation;

import software.amazon.awssdk.services.ec2.model.GetAssociatedEnclaveCertificateIamRolesResponse;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Translator {
    private Translator() {}

    static List<ResourceModel> translateForList(final GetAssociatedEnclaveCertificateIamRolesResponse response, final String certificateArn) {
        return streamOfOrEmpty(response.associatedRoles())
            .map(associatedRole -> ResourceModel.builder()
                    .certificateArn(certificateArn)
                    .roleArn(associatedRole.associatedRoleArn())
                    .certificateS3BucketName(associatedRole.certificateS3BucketName())
                    .certificateS3ObjectKey(associatedRole.certificateS3ObjectKey())
                    .encryptionKmsKeyId(associatedRole.encryptionKmsKeyId())
                    .build())
            .collect(Collectors.toList());
    }

    static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
            .map(Collection::stream)
            .orElseGet(Stream::empty);
    }
}
