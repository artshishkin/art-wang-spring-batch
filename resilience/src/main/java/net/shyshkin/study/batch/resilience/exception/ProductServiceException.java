package net.shyshkin.study.batch.resilience.exception;

public class ProductServiceException extends RuntimeException {
    public ProductServiceException(Throwable cause) {
        super(cause);
    }
}
