package com.myorg.myapp.service;

public class TextRenderException extends RuntimeException {
  public TextRenderException(String message) {
    super(message);
  }

  public TextRenderException(String message, Throwable cause) {
    super(message, cause);
  }
}
