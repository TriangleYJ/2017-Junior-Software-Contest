package com.yjprojects.jsctest2;

/**
 * Created by jyj on 2017-10-03.
 */

public class User {
    public final static boolean[] _NOT_BLINDNESS = {false, false, false, false};
    public final static boolean[] _RED_GREEN_BLINDNESS = {true, true, false, false};
    public final static boolean[] _BLUE_YELLOW_BLINDNESS = {false, false, true, true};
    public final static boolean[] _ALL_BLINDNESS = {true, true, true, true};

    public final static int MODE_NORMAL = 0;
    public final static int MODE_RED_GREEN = 1;
    public final static int MODE_BLUE_YELLOW = 2;
    public final static int MODE_ALL = 3;


    private static String name = "Cortana";
    private static int mode = MODE_NORMAL;
    private static int density = 1500; //lower is high density
    private static int quality = 720; //higher is high quality


    public static String getModeName(){
        String out = "Unknown";
        switch (mode){
            case MODE_NORMAL :
                out = "정상";
                break;
            case MODE_RED_GREEN :
                out = "적록색맹";
                break;
            case MODE_BLUE_YELLOW :
                out = "청황색맹";
                break;
            case MODE_ALL:
                out = "전색맹";
                break;
        }
        return out;
    }

    public static boolean[] getModeDetail(){
        boolean[] out = _NOT_BLINDNESS;
        switch (mode){
            case MODE_RED_GREEN :
                out = _RED_GREEN_BLINDNESS;
                break;
            case MODE_BLUE_YELLOW :
                out = _BLUE_YELLOW_BLINDNESS;
                break;
            case MODE_ALL:
                out = _ALL_BLINDNESS;
                break;

        }
        return out;
    }

    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        User.name = name;
    }

    public static int getMode() {
        return mode;
    }

    public static void setMode(int mode) {
        User.mode = mode;
    }

    public static int getDensity() {
        return density;
    }

    public static void setDensity(int density) {
        User.density = density;
    }

    public static int getQuality() {
        return quality;
    }

    public static void setQuality(int quality) {
        User.quality = quality;
    }
}
