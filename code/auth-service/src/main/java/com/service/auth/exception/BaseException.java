package com.service.auth.exception;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BaseException extends RuntimeException{
    private final String subjectString;

    public String toString() {
        String res = super.toString();
        res = res + "\n" + "subject: " + subjectString;
        return res;
    }
}
