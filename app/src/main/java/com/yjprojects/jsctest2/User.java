package com.yjprojects.jsctest2;

/**
 * Created by jyj on 2017-10-03.
 */

public class User {
    private final static boolean[] _NOT_BLINDNESS = {false, false, false, false};
    private final static boolean[] _RED_BLINDNESS = {true, true, false, false};
    private final static boolean[] _GREEN_BLINDESS = {false, true, true, false};
    private final static boolean[] _BLUE_BLINDNESS = {false, false, true, true};
    private final static boolean[] _ALL_BLINDNESS = {true, true, true, true};

    public final static int MODE_NORMAL = 0;
    public final static int MODE_RED = 1;
    public final static int MODE_GREEN = 2;
    public final static int MODE_BLUE = 3;
    public final static int MODE_ALL = 4;


    private static String name = "Cortana";
    private static int mode = MODE_NORMAL;
    private static int density = 1500; //lower is high density
    private static int quality = 750; //higher is high quality


    public static String getModeName(){
        String out = "Unknown";
        switch (mode){
            case MODE_NORMAL :
                out = "정상";
                break;
            case MODE_RED :
                out = "적색맹";
                break;
            case MODE_GREEN :
                out = "녹색맹";
                break;
            case MODE_BLUE:
                out = "청색맹";
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
            case MODE_NORMAL :
                out = _NOT_BLINDNESS;
                break;
            case MODE_RED :
                out = _RED_BLINDNESS;
                break;
            case MODE_GREEN:
                out = _GREEN_BLINDESS;
                break;
            case MODE_BLUE:
                out = _BLUE_BLINDNESS;
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

    public static void setDensity1(String s){
        if(s.equals("0")) setDensity(3000);
        else if(s.equals("1")) setDensity(2500);
        else if(s.equals("2")) setDensity(2000);
        else if(s.equals("3")) setDensity(1500);
    }
}
