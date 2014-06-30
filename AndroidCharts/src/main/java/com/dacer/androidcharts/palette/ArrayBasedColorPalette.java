package com.dacer.androidcharts.palette;

/**
 * @author dector
 */
public class ArrayBasedColorPalette implements ColorPalette {

    private final int[] values;

    public ArrayBasedColorPalette(int[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Color palette must contain at least one color");
        }
        this.values = values;
    }

    @Override
    public int getColorsCount() {
        return values.length;
    }

    @Override
    public boolean isEmpty() {
        return getColorsCount() == 0;
    }

    @Override
    public int getColor(int index) {
        return values[index % values.length];
    }
}
