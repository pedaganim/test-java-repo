package com.myorg.myapp.service;

public interface TextRenderService {
  byte[] renderText(String fontUrl, String text, float fontSize, int padding);
}
