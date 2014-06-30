package com.dacer.androidcharts.palette;

/**
 * @author dector
 */
public interface ColorPalette {

    public static final int INFINITE = -1;

    public int getColorsCount();
    public boolean isEmpty();
    public int getColor(int index);
}
