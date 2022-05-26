package net.shyshkin.study.batch.resilience.exception;

public class RetryableWriterException extends RuntimeException{
    public RetryableWriterException(String message) {
        super(message);
    }
}
