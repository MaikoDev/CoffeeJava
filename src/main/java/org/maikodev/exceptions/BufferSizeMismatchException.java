package org.maikodev.exceptions;

public class BufferSizeMismatchException extends DisplayBufferException {
    public BufferSizeMismatchException() {
        super("Provided buffer cannot be allocated with provided size!");
    }
}
