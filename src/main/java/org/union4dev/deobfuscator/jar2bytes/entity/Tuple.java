package org.union4dev.deobfuscator.jar2bytes.entity;


public class Tuple<TF, TS> {

    private TF first;

    private TS second;

    public Tuple(TF first, TS second) {
        this.first = first;
        this.second = second;
    }

    public TF getFirst() {
        return first;
    }

    public TS getSecond() {
        return second;
    }
}
