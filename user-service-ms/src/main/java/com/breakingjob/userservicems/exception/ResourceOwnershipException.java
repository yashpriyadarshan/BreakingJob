package com.breakingjob.userservicems.exception;

/**
 * Thrown when a user attempts to modify or delete a resource
 * that does not belong to them.
 */
public class ResourceOwnershipException extends RuntimeException {

    private final String resourceName;
    private final Long resourceId;
    private final Long userId;

    public ResourceOwnershipException(String resourceName, Long resourceId, Long userId) {
        super(String.format("%s with id '%d' does not belong to user '%d'", resourceName, resourceId, userId));
        this.resourceName = resourceName;
        this.resourceId = resourceId;
        this.userId = userId;
    }
}
