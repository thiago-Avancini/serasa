package br.com.serasa.serasa.common;

import java.util.Locale;

/**
 * Brazilian plates are 7 alphanumeric characters (old format AAA9999 or
 * Mercosul AAA9A99), sometimes written with a hyphen after the letters
 * (AAA-9999). Normalizing to a hyphen-free uppercase form keeps truck
 * registration and scale-reading correlation consistent regardless of
 * which way a given plate was typed or read by an LPR camera.
 */
public final class PlateNormalizer {

    private PlateNormalizer() {
    }

    public static String normalize(String plate) {
        return plate.toUpperCase(Locale.ROOT).replace("-", "");
    }
}