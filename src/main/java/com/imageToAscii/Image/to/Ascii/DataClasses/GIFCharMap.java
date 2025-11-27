package com.imageToAscii.Image.to.Ascii.DataClasses;

import java.util.ArrayList;
import java.util.List;

public class GIFCharMap {
    public int delay;
    public List<CharCountMap> charMaps;
    public GIFCharMap(int delay, List<CharCountMap> charMaps) {this.delay = delay;this.charMaps = charMaps;}
    public int getDelay() {return delay;}
    public List<CharCountMap> getCharMaps() {
        return new ArrayList<>(charMaps);
    }

}
