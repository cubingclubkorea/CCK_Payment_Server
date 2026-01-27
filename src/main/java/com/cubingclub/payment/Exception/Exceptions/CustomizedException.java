package com.cubingclub.payment.Exception.Exceptions;

import com.cubingclub.payment.Exception.Entity.ErrorCode;
import lombok.Getter;

@Getter
public class CustomizedException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String detailMessage;

    public CustomizedException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detailMessage = errorCode.getMessage();
    }

    public CustomizedException(ErrorCode errorCode, String detailMessage) {
        super(
                (detailMessage != null && !detailMessage.isBlank())
                        ? detailMessage
                        : errorCode.getMessage()
        );
        this.errorCode = errorCode;
        this.detailMessage = getMessage();
    }
}
