package cs.escaperoomhub.common.exceptionstarter;

public final class CommonErrors {
    private CommonErrors() {}

    public static ClientErrorException notFound(String message) {
        return new ClientErrorException(CommonErrorCode.RESOURCE_NOT_FOUND, message);
    }

    public static ClientErrorException lockAcquisitionFailed(String key) {
        return new ClientErrorException(
                CommonErrorCode.LOCK_ACQUISITION_FAILED,
                String.format("락을 획득하지 못했습니다. key=%s", key)
        );
    }
}