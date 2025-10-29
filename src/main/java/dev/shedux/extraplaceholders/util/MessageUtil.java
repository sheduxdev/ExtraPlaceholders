package dev.shedux.extraplaceholders.util;

import lombok.experimental.UtilityClass;
import net.j4c0b3y.api.config.message.Message;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class for message formatting and color processing.
 *
 * <p>Supports both legacy Minecraft color codes (&a, &c) and modern hex colors
 * with MiniMessage-like syntax (<#RRGGBB>). Also includes gradient support
 * for creating smooth color transitions in text.
 *
 * <p>Features:
 * <ul>
 *   <li>Legacy color code translation (&-codes)</li>
 *   <li>Hex color support for MC 1.16+ (<#RRGGBB>)</li>
 *   <li>Gradient color application (<gradient:#START:#END>text</gradient>)</li>
 *   <li>Automatic version detection and compatibility</li>
 * </ul>
 *
 * @author sheduxdev
 * @since 1.0.0
 */
@UtilityClass
@SuppressWarnings("unused")
public final class MessageUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient:#([A-Fa-f0-9]{6}):#([A-Fa-f0-9]{6})>(.*?)</gradient>");
    private static final char COLOR_CHAR = '&';

    private static final boolean HEX_SUPPORTED = isHexSupported();

    /**
     * Colorizes text with both legacy codes and hex colors.
     *
     * @param text the text to colorize
     * @return colorized text, or original if null/empty
     */
    public String colorize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        if (HEX_SUPPORTED) {
            text = applyGradients(text);
            text = applyHexColors(text);
        }

        return applyLegacyColors(text);
    }

    /**
     * Colorizes a list of strings.
     *
     * @param texts the list of texts to colorize
     * @return colorized list, or null if input is null
     */
    public List<String> colorize(List<String> texts) {
        if (texts == null) {
            return null;
        }
        return texts.stream()
                .map(MessageUtil::colorize)
                .collect(Collectors.toList());
    }

    /**
     * Sends a colorized message to a command sender.
     *
     * @param sender the recipient
     * @param message the message to send
     */
    public void sendMessage(CommandSender sender, String message) {
        if (sender == null || message == null || message.isEmpty()) {
            return;
        }
        sender.sendMessage(colorize(message));
    }

    /**
     * Sends a multi-line message with placeholder replacements.
     *
     * @param sender the recipient
     * @param message the message object containing lines
     * @param replacer the function to apply replacements
     */
    public void sendMessage(CommandSender sender, Message message, UnaryOperator<String> replacer) {
        if (sender == null || message == null) {
            return;
        }

        message.getLines().stream()
                .map(replacer)
                .map(MessageUtil::colorize)
                .forEach(sender::sendMessage);
    }

    /**
     * Applies hex color codes using MiniMessage-like format.
     *
     * @param text the text to process
     * @return text with hex colors applied
     */
    private String applyHexColors(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String hexCode = matcher.group(1);
            String replacement = convertHexToColorCode(hexCode);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Applies gradient color effects to text.
     *
     * @param text the text to process
     * @return text with gradients applied
     */
    private String applyGradients(String text) {
        Matcher matcher = GRADIENT_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String startHex = matcher.group(1);
            String endHex = matcher.group(2);
            String content = matcher.group(3);

            String gradientText = createGradient(content, startHex, endHex);
            matcher.appendReplacement(result, Matcher.quoteReplacement(gradientText));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Creates a smooth color gradient across text.
     *
     * @param text the text to apply gradient to
     * @param startHex the starting hex color
     * @param endHex the ending hex color
     * @return gradient-colored text
     */
    private String createGradient(String text, String startHex, String endHex) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        RGB start = RGB.fromHex(startHex);
        RGB end = RGB.fromHex(endHex);

        StringBuilder result = new StringBuilder();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);

            if (Character.isWhitespace(c)) {
                result.append(c);
                continue;
            }

            double ratio = length > 1 ? (double) i / (length - 1) : 0;
            RGB interpolated = start.interpolate(end, ratio);

            result.append(convertRgbToColorCode(interpolated)).append(c);
        }

        return result.toString();
    }

    /**
     * Applies legacy Minecraft color codes.
     *
     * @param text the text to process
     * @return text with legacy colors applied
     */
    private String applyLegacyColors(String text) {
        return ChatColor.translateAlternateColorCodes(COLOR_CHAR, text);
    }

    /**
     * Converts hex color to Minecraft color code.
     *
     * @param hex the hex color (without #)
     * @return color code string
     */
    private String convertHexToColorCode(String hex) {
        try {
            net.md_5.bungee.api.ChatColor chatColor = net.md_5.bungee.api.ChatColor.of("#" + hex);
            return chatColor.toString();
        } catch (Exception | NoClassDefFoundError e) {
            return "";
        }
    }

    /**
     * Converts RGB values to Minecraft color code.
     *
     * @param rgb the RGB color
     * @return color code string
     */
    private String convertRgbToColorCode(RGB rgb) {
        try {
            String hex = String.format("%02x%02x%02x", rgb.r(), rgb.g(), rgb.b());
            return convertHexToColorCode(hex);
        } catch (Exception | NoClassDefFoundError e) {
            return "";
        }
    }

    /**
     * Checks if the server supports hex colors (1.16+).
     *
     * @return true if hex colors are supported
     */
    private static boolean isHexSupported() {
        try {
            Class.forName("net.md_5.bungee.api.ChatColor");
            String version = org.bukkit.Bukkit.getVersion();
            return version.matches(".*(1\\.(1[6-9]|2[0-9])).*");
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Immutable record representing RGB color.
     */
    private record RGB(int r, int g, int b) {

        /**
         * Creates RGB from hex string.
         */
        static RGB fromHex(String hex) {
            int value = Integer.parseInt(hex, 16);
            return new RGB(
                    (value >> 16) & 0xFF,
                    (value >> 8) & 0xFF,
                    value & 0xFF
            );
        }

        /**
         * Interpolates between this RGB and another.
         */
        RGB interpolate(RGB other, double ratio) {
            int newR = (int) (r + ratio * (other.r - r));
            int newG = (int) (g + ratio * (other.g - g));
            int newB = (int) (b + ratio * (other.b - b));
            return new RGB(newR, newG, newB);
        }
    }
}