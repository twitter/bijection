package com.twitter.bijection;

import org.junit.Test;
import scala.Function1;

import static junit.framework.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: jcoveney
 * Date: 1/11/13
 * Time: 2:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class HelloWorld {
    @Test
    public void test() {
        roundTrip(new StringToLong(), "21", 21L);
    }

    //TODO also test that composition works

    private <T1, T2> void roundTrip(Bijection<T1,T2> bij, T1 d1, T2 d2) {
        Bijection<T2,T1> inv = bij.inverse();

        assertEquals(d2, bij.apply(d1));
        assertEquals(d2, inv.invert(d1));

        assertEquals(d1, bij.invert(bij.apply(d1)));
        assertEquals(d1, inv.apply(bij.apply(d1)));





    }

    public static class StringToLong extends BijectionImpl<String,Long> {
        @Override
        public Long apply(String a) {
            return Long.parseLong(a);
        }

        @Override
        public String invert(Long b) {
            return b.toString();
        }
    }
}
