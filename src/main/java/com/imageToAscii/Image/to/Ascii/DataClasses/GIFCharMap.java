package com.imageToAscii.Image.to.Ascii.DataClasses;

import java.util.List;

public class GIFCharMap {
    public int delay;
    public List<CharCountMap> charMaps;
    public GIFCharMap(int delay, List<CharCountMap> charMaps) {this.delay = delay;this.charMaps = charMaps;}
}
