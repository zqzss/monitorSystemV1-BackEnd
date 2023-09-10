package com.seewin.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenException extends RuntimeException{
    private Integer code;

    public TokenException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public TokenException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
