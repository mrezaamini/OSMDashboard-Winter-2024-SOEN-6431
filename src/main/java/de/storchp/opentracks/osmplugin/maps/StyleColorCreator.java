package de.storchp.opentracks.osmplugin.maps;

import org.oscim.backend.canvas.Color;

/**
 * Creates continues distinguished colors via golden ration.
 * Adapted from: <a href="https://github.com/dennisguse/TheKarte/blob/master/src/StyleColorCreator.js">...</a>
 * Code for color generation was taken partly from <a href="https://martin.ankerl.com/2009/12/09/how-to-create-random-colors-programmatically/">...</a>
 */
public class StyleColorCreator {

    public static final double GOLDEN_RATIO_CONJUGATE = 0.618033988749895;
    private double h;

    public StyleColorCreator(double start) {
        this.h = start;
    }

    private int convertHSVtoColorRGB(double hue, double saturation, double value) {
        double i = Math.floor(hue * 6);
        double f = hue * 6 - i;
        double p = value * (1 - saturation);
        double q = value * (1 - f * saturation);
        double t = value * (1 - (1 - f) * saturation);
        double red = 0;
        double green = 0;
        double blue = 0;
        switch ((int) (i % 6)) {
            case 0 -> {
                red = value;
                green = t;
                blue = p;
            }
            case 1 -> {
                red = q;
                green = value;
                blue = p;
            }
            case 2 -> {
                red = p;
                green = value;
                blue = t;
            }
            case 3 -> {
                red = p;
                green = q;
                blue = value;
            }
            case 4 -> {
                red = t;
                green = p;
                blue = value;
            }
            case 5 -> {
                red = value;
                green = p;
                blue = q;
            }
            default -> {
                red = 1.0;
                green = 1.0;
                blue = 1.0;
            }
        }

        return Color.get((int) (red * 255), (int) (green * 255), (int) (blue * 255));
    }

    /**
     * Go to next color.
     *
     * @return The color.
     */
    public int nextColor() {
        this.h += GOLDEN_RATIO_CONJUGATE;
        this.h %= 1;

        return convertHSVtoColorRGB(this.h, 0.99, 0.99);
    }

    /**
     *  generate next color with specified saturation and value
     * @param saturation current saturarion
     * @param value specified value
     * @return nextColor
     */
    public int nextColor(double saturation, double value) {
        this.h += GOLDEN_RATIO_CONJUGATE;
        this.h %= 1;

        return convertHSVtoColorRGB(this.h, saturation, value);
    }

    /**
     * generate the complementary colors for map to visualize better
     * @param baseColor first color
     * @return complementary color
     */
    public int generateComplementaryColor(int baseColor) {
        int red = (baseColor >> 16) & 0xFF; // Extract red component
        int green = (baseColor >> 8) & 0xFF; // Extract green component
        int blue = baseColor & 0xFF; // Extract blue component

        double[] hsv = rgbToHSV(red, green, blue);
        double complementaryHue = (hsv[0] + 180) % 360; // Add 180 for complementary hue

        return convertHSVtoColorRGB(complementaryHue / 360.0, hsv[1], hsv[2]);
    }

    /**
     * convert rgb to hsv function
     * @param red amount of red
     * @param green amount of green
     * @param blue amount of blue
     * @return hsv value of color
     */
    private double[] rgbToHSV(int red, int green, int blue) {
        double[] hsv = new double[3];

        double min = Math.min(Math.min(red, green), blue);
        double max = Math.max(Math.max(red, green), blue);
        double delta = max - min;

        // Hue calculation
        if (delta == 0) {
            hsv[0] = 0;
        } else if (max == red) {
            hsv[0] = (green - blue) / delta % 6;
        } else if (max == green) {
            hsv[0] = (blue - red) / delta + 2;
        } else {
            hsv[0] = (red - green) / delta + 4;
        }
        hsv[0] *= 60;
        if (hsv[0] < 0) hsv[0] += 360;

        // Saturation calculation
        if (max == 0) {
            hsv[1] = 0;
        } else {
            hsv[1] = delta / max;
        }

        // Value calculation
        hsv[2] = max / 255.0;

        return hsv;
    }

}