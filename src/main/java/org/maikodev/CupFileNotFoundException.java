package org.maikodev;

public class CupFileNotFoundException extends CupLoadException {
    public CupFileNotFoundException() {
        super("File to Cup display could not be found!");
    }
}
