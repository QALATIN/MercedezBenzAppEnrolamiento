package com.latinid.mercedes.ui.nuevosolicitante.facecapture.api.exceptions;

public class FaceCaptureApiException extends Exception {

    public enum ErrorCode {
        NoError,
        WorkflowInvalid,

        InvalidProperty,
        MinimumRecordingLengthTooShort
    }
    private final ErrorCode errorCode;

    public FaceCaptureApiException(ErrorCode var1) {
        super(var1.toString());
        this.errorCode = var1;
    }

    public ErrorCode getErrorCode() {
        return this.errorCode;
    }

   /* public String getMessage() {
        return this.errorCode.name() + ": " + this.errorCode.getDescription();
    }*/
}

