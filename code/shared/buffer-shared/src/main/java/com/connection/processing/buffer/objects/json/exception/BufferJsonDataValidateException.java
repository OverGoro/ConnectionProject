// BufferJsonDataValidateException.java
package com.connection.processing.buffer.objects.json.exception;

public class BufferJsonDataValidateException extends BaseBufferJsonDataException {
    String descriptionString;

    public BufferJsonDataValidateException(String dataUid, String description) {
        super(dataUid);
        this.descriptionString = description;
    }

    public String toString() {
        String res = super.toString();
        res += "\n" + "description: buffer JSON data is invalid";
        res += "\n" + descriptionString;
        return res;
    }
}