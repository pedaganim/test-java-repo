package com.myorg.myapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.myorg.myapp.service.impl.Java2DTextRenderService;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class Java2DTextRenderServiceTest {

  private final TextRenderService service = new Java2DTextRenderService();

  @Test
  void rendersPngBytesWithBuiltinFont() throws Exception {
    byte[] png = service.renderText("builtin:SansSerif", "Hello World", 24f, 10);
    assertThat(png).isNotNull();
    assertThat(png.length).isGreaterThan(100);

    try (ByteArrayInputStream bais = new ByteArrayInputStream(png)) {
      BufferedImage image = ImageIO.read(bais);
      assertThat(image).isNotNull();
      assertThat(image.getWidth()).isGreaterThan(0);
      assertThat(image.getHeight()).isGreaterThan(0);
    }
  }

  @Test
  void rejectsBlankText() {
    assertThatThrownBy(() -> service.renderText("builtin:SansSerif", "  ", 24f, 10))
        .isInstanceOf(TextRenderException.class);
  }

  @Test
  void rejectsNonPositiveFontSize() {
    assertThatThrownBy(() -> service.renderText("builtin:SansSerif", "x", 0f, 10))
        .isInstanceOf(TextRenderException.class);
  }

  @Test
  void rejectsNegativePadding() {
    assertThatThrownBy(() -> service.renderText("builtin:SansSerif", "x", 12f, -1))
        .isInstanceOf(TextRenderException.class);
  }
}
