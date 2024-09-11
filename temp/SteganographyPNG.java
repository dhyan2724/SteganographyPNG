import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class SteganographyPNG {

    // Method to convert text to binary string
    public static String toBinary(String text) {
        StringBuilder binary = new StringBuilder();
        for (char ch : text.toCharArray()) {
            binary.append(String.format("%8s", Integer.toBinaryString(ch)).replaceAll(" ", "0"));
        }
        return binary.toString();
    }

    // Method to convert binary string to text
    public static String toText(String binary) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < binary.length(); i += 8) {
            String byteString = binary.substring(i, i + 8);
            char ch = (char) Integer.parseInt(byteString, 2);
            text.append(ch);
        }
        return text.toString();
    }

    // Encryption: Hide the text inside the image using LSB algorithm
    public static void encrypt(BufferedImage image, String text) throws IOException {
        String binaryText = toBinary(text) + "00000000"; // Add delimiter to indicate end of text
        int index = 0;

        outerLoop:
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);

                if (index < binaryText.length()) {
                    // Extracting RGB channels
                    int red = (pixel >> 16) & 0xFF;
                    int green = (pixel >> 8) & 0xFF;
                    int blue = pixel & 0xFF;

                    // Modify the LSB of the blue channel to hide the binary data
                    blue = (blue & 0xFE) | (binaryText.charAt(index) - '0');
                    index++;

                    // Combine the new RGB values back into the pixel
                    int newPixel = (red << 16) | (green << 8) | blue;
                    image.setRGB(x, y, newPixel);
                } else {
                    break outerLoop;
                }
            }
        }

        // Save the modified image as a PNG file (lossless format)
        File outputFile = new File("encrypted_image.png");
        ImageIO.write(image, "png", outputFile);
        System.out.println("Encryption completed. Image saved as encrypted_image.png");
    }

    // Decryption: Extract the text from the image using LSB algorithm
    public static String decrypt(BufferedImage image) {
        StringBuilder binaryText = new StringBuilder();

        outerLoop:
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                int blue = pixel & 0xFF;

                binaryText.append(blue & 1); // Extract LSB from blue channel

                // Stop when the delimiter is found (8 zero bits)
                if (binaryText.length() % 8 == 0 && binaryText.substring(binaryText.length() - 8).equals("00000000")) {
                    break outerLoop;
                }
            }
        }

        // Convert binary string back to text
        return toText(binaryText.substring(0, binaryText.length() - 8)); // Remove delimiter
    }

    public static void main(String[] args) {
        try {
            // Input PNG file (check if the file exists)
            File inputFile = new File("imgbin_coffee-bean-tea-cafe-png.png");

            if (!inputFile.exists()) {
                System.out.println("Error: Input file not found!");
                return; // Exit if file does not exist
            }

            // Read the image
            BufferedImage image = ImageIO.read(inputFile);
            if (image == null) {
                System.out.println("Error: Unable to read the image. Please check if the file is a valid image format.");
                return;
            }

            // Encrypt routine: Use PNG file as input
            String textToEncrypt = "Hello123"; // Text to encrypt (around 8-10 chars)
            encrypt(image, textToEncrypt);

            // Decrypt routine: Read the encrypted PNG file
            File encryptedFile = new File("encrypted_image.png");
            if (!encryptedFile.exists()) {
                System.out.println("Error: Encrypted image file not found!");
                return; // Exit if encrypted image does not exist
            }

            BufferedImage encryptedImage = ImageIO.read(encryptedFile);
            if (encryptedImage == null) {
                System.out.println("Error: Unable to read the encrypted image.");
                return;
            }

            // Decrypt the hidden text from the image
            String decryptedText = decrypt(encryptedImage);

            // Display decrypted text and verify
            System.out.println("Decrypted Text: " + decryptedText);
            if (textToEncrypt.equals(decryptedText)) {
                System.out.println("Success: The original and decrypted texts match!");
            } else {
                System.out.println("Error: The original and decrypted texts do not match.");
            }

        } catch (IOException e) {
            System.out.println("Error: Could not read the image file.");
            e.printStackTrace();
        }
    }
}
