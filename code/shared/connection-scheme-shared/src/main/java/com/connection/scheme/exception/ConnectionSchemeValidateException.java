
package com.connection.scheme.exception;

/** . */
public class ConnectionSchemeValidateException
        extends BaseConnectionSchemeException {
    private final String description;

    /** . */
    public ConnectionSchemeValidateException(String schemeUid,
            String description) {
        super(schemeUid);
        this.description = description;
    }

    /** . */
    public String toString() {
        return super.toString() + "\ndescription: " + description;
    }
}
