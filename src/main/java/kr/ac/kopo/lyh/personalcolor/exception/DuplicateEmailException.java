package kr.ac.kopo.lyh.personalcolor.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException() {
        super();
    }
    public DuplicateEmailException(String message) {
        super(message);
    }
}