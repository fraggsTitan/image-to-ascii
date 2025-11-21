package com.imageToAscii.Image.to.Ascii.DataClasses;

public class CharCountMap {
    public char ch;
    int freq;
    public CharCountMap(char ch, int freq) {this.ch=ch;this.freq=freq;}
    public void incrementCount(){freq++;}
}
