package com.example.demo.image;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class GrayscaleConverterTest {

  private final GrayscaleConverter converter = new GrayscaleConverter();

  @Test
  void apply_convertsColoredImageToGrayscale() throws Exception {
    // image 4x4 entièrement rouge
    BufferedImage colored = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
    for (int x = 0; x < 4; x++) {
      for (int y = 0; y < 4; y++) {
        colored.setRGB(x, y, Color.RED.getRGB());
      }
    }
    var input = new ByteArrayOutputStream();
    ImageIO.write(colored, "png", input);

    byte[] grayscaleBytes =
        converter.apply(new GrayscaleConverter.Input(input.toByteArray(), "png"));

    BufferedImage result = ImageIO.read(new ByteArrayInputStream(grayscaleBytes));
    assertThat(result).isNotNull();

    // en niveaux de gris, R = G = B pour chaque pixel
    int rgb = result.getRGB(0, 0);
    int r = (rgb >> 16) & 0xff;
    int g = (rgb >> 8) & 0xff;
    int b = rgb & 0xff;
    assertThat(r).isEqualTo(g);
    assertThat(g).isEqualTo(b);
  }
}
