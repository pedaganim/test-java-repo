package com.myorg.myapp.service.impl;

import com.myorg.myapp.service.TextRenderException;
import com.myorg.myapp.service.TextRenderService;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import javax.imageio.ImageIO;

public class Java2DTextRenderService implements TextRenderService {

  private static final int DEFAULT_CONNECT_TIMEOUT_MS = 5000;
  private static final int DEFAULT_READ_TIMEOUT_MS = 5000;

  private final int maxWidth;
  private final int maxHeight;

  public Java2DTextRenderService() {
    this(4096, 4096);
  }

  public Java2DTextRenderService(int maxWidth, int maxHeight) {
    this.maxWidth = maxWidth;
    this.maxHeight = maxHeight;
    System.setProperty("java.awt.headless", "true");
  }

  @Override
  public byte[] renderText(String fontUrl, String text, float fontSize, int padding) {
    if (text == null || text.isBlank()) {
      throw new TextRenderException("text must not be blank");
    }
    if (fontSize <= 0) {
      throw new TextRenderException("fontSize must be > 0");
    }
    if (padding < 0) {
      throw new TextRenderException("padding must be >= 0");
    }

    Font font = loadFont(fontUrl, fontSize);

    // Measure text (support for manual newlines only)
    BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2dMeasure = tmp.createGraphics();
    try {
      g2dMeasure.setFont(font);
      FontMetrics fm = g2dMeasure.getFontMetrics();
      String[] lines = text.split("\\r?\\n", -1);

      int lineHeight = fm.getAscent() + fm.getDescent() + fm.getLeading();
      int textHeight = Math.max(lineHeight, lineHeight * lines.length);
      int textWidth = 0;
      for (String line : lines) {
        int w = fm.stringWidth(line);
        if (w > textWidth) {
          textWidth = w;
        }
      }

      int width = safeSize(textWidth + padding * 2, maxWidth, "width");
      int height = safeSize(textHeight + padding * 2, maxHeight, "height");

      BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      Graphics2D g2d = img.createGraphics();
      try {
        // White background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // Rendering quality hints
        g2d.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Draw text
        g2d.setColor(Color.BLACK);
        g2d.setFont(font);
        int y = padding + fm.getAscent();
        for (String line : lines) {
          g2d.drawString(line, padding, y);
          y += lineHeight;
        }
      } finally {
        g2d.dispose();
      }

      try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        if (!ImageIO.write(img, "png", baos)) {
          throw new TextRenderException("Failed to encode image as PNG");
        }
        return baos.toByteArray();
      } catch (IOException e) {
        throw new TextRenderException("I/O error while writing PNG", e);
      }
    } finally {
      g2dMeasure.dispose();
    }
  }

  private int safeSize(int value, int max, String dimName) {
    if (value <= 0) {
      throw new TextRenderException(dimName + " computed size <= 0");
    }
    if (value > max) {
      throw new TextRenderException(dimName + " exceeds max " + max + ": " + value);
    }
    return value;
  }

  private Font loadFont(String fontUrl, float fontSize) {
    if (fontUrl == null || fontUrl.isBlank()) {
      // Default logical font
      return new Font("SansSerif", Font.PLAIN, Math.round(fontSize));
    }
    if (fontUrl.startsWith("builtin:")) {
      String logical = fontUrl.substring("builtin:".length());
      if (logical.isBlank()) {
        logical = "SansSerif";
      }
      // Validate availability; fall back to logical even if custom not registered
      if (!isFontAvailable(logical)) {
        throw new TextRenderException("Requested builtin font not available: " + logical);
      }
      return new Font(logical, Font.PLAIN, Math.round(fontSize));
    }

    URL url;
    try {
      url = URI.create(fontUrl).toURL();
    } catch (IllegalArgumentException | MalformedURLException e) {
      throw new TextRenderException("Invalid fontUrl: " + fontUrl, e);
    }

    try (InputStream is = openWithTimeouts(url)) {
      Font base = Font.createFont(Font.TRUETYPE_FONT, is);
      return base.deriveFont(Font.PLAIN, fontSize);
    } catch (FontFormatException e) {
      throw new TextRenderException("Unsupported font format from URL: " + fontUrl, e);
    } catch (IOException e) {
      throw new TextRenderException("Failed to load font from URL: " + fontUrl, e);
    }
  }

  private boolean isFontAvailable(String name) {
    String[] families =
        GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    for (String fam : families) {
      if (fam.equalsIgnoreCase(name)) {
        return true;
      }
    }
    return false;
  }

  private InputStream openWithTimeouts(URL url) throws IOException {
    if ("http".equalsIgnoreCase(url.getProtocol()) || "https".equalsIgnoreCase(url.getProtocol())) {
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT_MS);
      conn.setReadTimeout(DEFAULT_READ_TIMEOUT_MS);
      conn.setInstanceFollowRedirects(true);
      int code = conn.getResponseCode();
      if (code >= 400) {
        throw new IOException("HTTP error " + code + " loading font");
      }
      return conn.getInputStream();
    }
    return url.openStream();
  }
}
