package eu.pb4.lang.runtime;

import eu.pb4.lang.expression.Expression;
import eu.pb4.lang.object.XObject;

import java.util.Arrays;

public final class RuntimeStack {
    public Expression.Position info = Expression.Position.EMPTY;
    public ObjectScope scope;
    private XObject<?>[] ar = new XObject<?>[512];
    private int index = -1;
    public XObject<?> lastObject;
    public boolean finished = false;
    public boolean printTime = false;
    public boolean printOpcodes = false;
    public long instructionStartNanoTime = 0;

    public XObject<?> pop() {
        if (this.index == -1) {
            return XObject.NULL;
        }
        var obj = this.ar[this.index];
        this.ar[this.index--] = null;
        return obj;
    }

    public XObject<?> peek() {
        if (this.index == -1) {
            return XObject.NULL;
        }
        return this.ar[this.index];
    }

    public void push(XObject<?> object) {
        if (this.ar.length <= this.index + 1) {
            this.ar = Arrays.copyOf(this.ar, this.ar.length * 2);
        }
        this.lastObject = object;
        this.ar[++this.index] = object;
    }

    public void debugPrint() {
        System.out.println("== Debug: Current RuntimeStack | Index: " + this.index);
        for (int i = 0; i < this.index; i++) {
            System.out.println(i + " | " + ar[i].type() + " | " + ar[i].toString());
        }
    }

    public void clear() {
        this.index = -1;
        //Arrays.fill(this.ar, null);
    }

    public XObject<?>[] pop(final int read) {
        var arr = new XObject<?>[read];
        for (int i = 0; i < read; i++) {
            arr[i] = this.ar[this.index];
            this.ar[this.index--] = null;
        }
        return arr;
    }
}
