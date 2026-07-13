package com.example.demo.image;

import com.example.demo.PojaGenerated;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Function;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Component;

/** Convertit les octets d'une image jpeg/png en niveaux de gris. */
@PojaGenerated
@Component
public class GrayscaleConverter implements Function<GrayscaleConverter.Input, byte[]> {

    public record Input(byte[] imageBytes, String formatName) {}

    @Override
    public byte[] apply(Input input) {
        try {
            BufferedImage source = ImageIO.read(new ByteArrayInputStream(input.imageBytes()));
            if (source == null) {
                throw new IllegalArgumentException("Impossible de lire l'image (format non supporté)");
            }

            BufferedImage grayscale =
                    new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            new ColorConvertOp(null).filter(source, grayscale);

            var output = new ByteArrayOutputStream();
            ImageIO.write(grayscale, input.formatName(), output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}