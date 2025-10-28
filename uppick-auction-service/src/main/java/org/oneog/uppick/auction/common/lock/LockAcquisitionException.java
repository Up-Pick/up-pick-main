package org.oneog.uppick.auction.common.lock;

public class LockAcquisitionException extends RuntimeException {

    public LockAcquisitionException() {

        super("Failed to acquire lock");
    }

}
