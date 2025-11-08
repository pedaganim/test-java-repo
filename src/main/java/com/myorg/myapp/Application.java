package com.myorg.myapp;

import com.myorg.myapp.service.TextRenderService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Application {
  public static void main(String[] args) {
    ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
    TextRenderService svc = ctx.getBean(TextRenderService.class);
    byte[] png =
        svc.renderText(
            "https://font-public.canva.com/YAFdJkVWBPo/0/MoreSugar-Regular.62992e429acdec5e01c3db.6f7a950ef2bb9f1314d37ac4a660925e.otf",
            "Hello Renderer",
            24f,
            12);
    String b64 = Base64.getEncoder().encodeToString(png);
    System.out.println("Rendered PNG bytes: " + png.length);
    System.out.println(
        "Base64 (first 80): " + b64.substring(0, Math.min(80, b64.length())) + " End of Output");

    try {
      Path out = Path.of("render-output.png");
      Files.write(out, png);
      System.out.println("Saved PNG to: " + out.toAbsolutePath());
    } catch (IOException e) {
      System.err.println("Failed to save PNG: " + e.getMessage());
    }
  }
}
