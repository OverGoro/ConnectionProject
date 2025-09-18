// FeignErrorDecoder.java
package com.service.bufferjsondata.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        switch (response.status()) {
            case 401:
                return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid access token");
            case 403:
                return new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            case 404:
                return new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
            default:
                return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "External service error: " + response.status());
        }
    }
}