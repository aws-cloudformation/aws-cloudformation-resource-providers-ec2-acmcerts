package software.amazon.ec2.enclavecertificateiamroleassociation;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;

import java.util.Arrays;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {
    static final String ERROR_CODE_CERTIFICATE_ARN_MALFORMED = "InvalidCertificateArn.Malformed";
    static final String ERROR_CODE_CERTIFICATE_ARN_NOT_FOUND = "InvalidCertificateArn.NotFound";
    static final String ERROR_CODE_ROLE_ARN_MALFORMED = "InvalidRoleArn.Malformed";
    static final String ERROR_CODE_ROLE_ARN_NOT_FOUND = "InvalidRoleArn.NotFound";
    static final String ERROR_CODE_CERTIFICATE_TOO_MANY_ASSOCIATED_ROLES =
            "TooManyAssociatedRoles";
    static final String ERROR_CODE_UNAUTHORIZED_OPERATION = "UnauthorizedOperation";
    static final String ERROR_CODE_SERVER_INTERNAL = "InternalError";
    static final String ERROR_CODE_SERVICE_UNAVAILABLE = "Unavailable";
    static final String PROPERTY_CANNOT_BE_EMPTY = "Property %s cannot be empty.";

    enum Properties {
        CertificateArn, RoleArn
    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(AmazonWebServicesClientProxy proxy,
                                                                       ResourceHandlerRequest<ResourceModel> request,
                                                                       CallbackContext callbackContext,
                                                                       Logger logger) {
        return handleRequest(proxy, request, callbackContext != null ? callbackContext : new CallbackContext(),
                proxy.newProxy(ClientBuilder::getClient), logger);
    }

    protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(final AmazonWebServicesClientProxy proxy,
                                                                                   final ResourceHandlerRequest<ResourceModel> request,
                                                                                   final CallbackContext callbackContext,
                                                                                   final ProxyClient<Ec2Client> proxyClient,
                                                                                   final Logger logger);

    protected ProgressEvent<ResourceModel, CallbackContext> handleException(final Throwable e,
                                                                            final Logger logger) {
        logger.log(String.format("Error during operation: %s, Error message: %s",
                this.getClass().getSimpleName(),
                e.getMessage()));
        logger.log(Arrays.toString(e.getStackTrace()));

        if (e instanceof AwsServiceException) {
            final String errorCode = ((AwsServiceException) e).awsErrorDetails().errorCode();

            switch (errorCode) {
                case ERROR_CODE_CERTIFICATE_TOO_MANY_ASSOCIATED_ROLES:
                    return ProgressEvent.defaultFailureHandler(new CfnServiceLimitExceededException(e),
                            HandlerErrorCode.ServiceLimitExceeded);
                case ERROR_CODE_CERTIFICATE_ARN_MALFORMED:
                case ERROR_CODE_CERTIFICATE_ARN_NOT_FOUND:
                case ERROR_CODE_ROLE_ARN_MALFORMED:
                case ERROR_CODE_ROLE_ARN_NOT_FOUND:
                    return ProgressEvent.defaultFailureHandler(new CfnInvalidRequestException(e),
                            HandlerErrorCode.InvalidRequest);
                case ERROR_CODE_SERVER_INTERNAL:
                    return ProgressEvent.defaultFailureHandler(new CfnInternalFailureException(e),
                            HandlerErrorCode.InternalFailure);
                case ERROR_CODE_SERVICE_UNAVAILABLE:
                    return ProgressEvent.defaultFailureHandler(new CfnServiceInternalErrorException(e),
                            HandlerErrorCode.ServiceInternalError);
                case ERROR_CODE_UNAUTHORIZED_OPERATION:
                    return ProgressEvent.defaultFailureHandler(new CfnAccessDeniedException(e),
                            HandlerErrorCode.AccessDenied);
            }

            // For all the exceptions not captured by error code above
            final int statusCode = ((AwsServiceException) e).statusCode();
            if (statusCode >= 400 && statusCode < 500) {
                return ProgressEvent.defaultFailureHandler(new CfnInvalidRequestException(e),
                        HandlerErrorCode.InvalidRequest);
            } else if (statusCode >= 500 && statusCode < 600) {
                return ProgressEvent.defaultFailureHandler(new CfnServiceInternalErrorException(e),
                        HandlerErrorCode.ServiceInternalError);
            }

        } else if (e instanceof CfnInvalidRequestException) {
            return ProgressEvent.defaultFailureHandler(new CfnInvalidRequestException(e),
                    HandlerErrorCode.InvalidRequest);
        }

        return ProgressEvent.defaultFailureHandler(new CfnGeneralServiceException(e),
                HandlerErrorCode.GeneralServiceException);
    }

    protected static void validateNotNull(final Object param, Properties property) {
        if (param == null) {
            throw new CfnInvalidRequestException(String.format(PROPERTY_CANNOT_BE_EMPTY, property));
        }
    }
}
