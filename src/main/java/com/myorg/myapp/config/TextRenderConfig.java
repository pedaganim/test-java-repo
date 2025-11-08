package com.myorg.myapp.config;

import com.myorg.myapp.service.TextRenderService;
import com.myorg.myapp.service.impl.Java2DTextRenderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TextRenderConfig {

  @Bean
  public TextRenderService textRenderService() {
    // Defaults: max 4096x4096
    return new Java2DTextRenderService();
  }
}
