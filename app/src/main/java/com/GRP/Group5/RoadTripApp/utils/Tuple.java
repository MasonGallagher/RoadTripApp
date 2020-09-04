package com.GRP.Group5.RoadTripApp.utils;

/**
 * Created by psybc4 on 07/03/2018.
 */

public class Tuple<A, B> {

    public A a;
    public B b;

    public Tuple(A a, B b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        if (this.a != null) {
            hash = 3 * hash + this.a.hashCode();
        }
        if (this.b != null) {
            hash = 3 * hash + this.b.hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tuple) {
            if (((Tuple) obj).a != null && ((Tuple) obj).a.equals(this.a)) {
                if (((Tuple) obj).b != null && ((Tuple) obj).b.equals(this.b)) {
                    return obj.hashCode() == this.hashCode();
                }
            }
        }
        return false;
    }
}
