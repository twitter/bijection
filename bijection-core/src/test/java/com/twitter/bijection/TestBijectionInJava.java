package com.twitter.bijection;

import org.junit.Test;
import org.scalatest.junit.JUnitSuite;
import scala.util.Failure;
import scala.util.Success;
import scala.util.Try;
import static junit.framework.Assert.assertEquals;

/**
 * Bijection is as useful in Java as in Scala, so these tests ensure correct
 * functionality while providing an example of use and implementation from Java.
 */
public class TestBijectionInJava extends JUnitSuite {
    @Test
    public void testBasicBijection() {
        Bijection<String,Long> s2l = new StringToLong();

        for (long l = -1000; l < 1000; l++) {
            String s = Long.valueOf(l).toString();
            roundTrip(s2l, s, l);
            roundTrip(s2l.inverse(), l, s);

            roundTrip(s2l.andThen(s2l.inverse()).inverse(), s, s);
            roundTrip(s2l.inverse().compose(s2l), s, s);
            roundTrip(s2l.inverse().compose(s2l).inverse(), s, s);
        }
    }
    @Test
    public void testBasicInjection() {
      Injection<Long, String> l2s = new AbstractInjection<Long, String>() {
        @Override
        public String apply(Long in) { return in.toString(); }
        @Override
        public Try<Long> invert(String in) {
          try {
            return new Success(Long.valueOf(in));
          }
          catch(NumberFormatException nfe) {
            return new Failure(new InversionFailure(in, nfe));
          }
        }
      };
      assertEquals(Long.valueOf("123"), l2s.invert("123").get());
      assertEquals(true, l2s.invert("hello").isFailure());
    }

    //TODO include a more complete example using Base64 conversion, and GZip + Base64 version
    //TODO include a cleaner way to get to the scala Bijections than Bijection$.MODULE$.
    @Test
    public void testBase64Bijection() {
        // Note, value classes return the underlying types in Java. But Java users usually
        // don't care much about type safety, so punting on this for now
        Bijection<byte[], String> bytes2Base64 = Bijection$.MODULE$.bytes2Base64();
    }

    @Test
    public void testBase64BijectionGzip() {
        // Note, value classes return the underlying types in Java. But Java users usually
        // don't care much about type safety, so punting on this for now
        Bijection<byte[], String> bytes2GZippedBase64 = Bijection$.MODULE$.bytes2GZippedBase64();
    }

    // Instantiate a Bijection to String // Looks like the Long is erased
    @Test
    public void testStringRep() {
      Injection<Long, String> long2String = Injection$.MODULE$.jlong2String();
        for (long lv = -1000; lv < 1000; lv++) {
            Long l = Long.valueOf(lv);
            String s = l.toString();
            assertEquals(s, long2String.apply(l));
        }
    }

    private <T1, T2> void roundTrip(Bijection<T1,T2> bij, T1 d1, T2 d2) {
        Bijection<T2,T1> inv = bij.inverse();

        assertEquals(d2, bij.apply(d1));
        assertEquals(d2, inv.invert(d1));

        assertEquals(d1, bij.invert(bij.apply(d1)));
        assertEquals(d1, bij.invert(inv.invert(d1)));
        assertEquals(d1, inv.apply(bij.apply(d1)));
        assertEquals(d1, inv.apply(inv.invert(d1)));

        assertEquals(d1, bij.invert(d2));
        assertEquals(d1, inv.apply(d2));

        assertEquals(d2, bij.apply(bij.invert(d2)));
        assertEquals(d2, bij.apply(inv.apply(d2)));
        assertEquals(d2, inv.invert(bij.invert(d2)));
        assertEquals(d2, inv.invert(inv.apply(d2)));

        assertEquals(d1, bij.andThen(bij.inverse()).apply(d1));
        assertEquals(d1, bij.andThen(bij.inverse()).inverse().apply(d1));
        assertEquals(d1, bij.inverse().compose(bij).apply(d1));
        assertEquals(d1, (bij.inverse().compose(bij).inverse().apply(d1)));

        assertEquals(d2, inv.andThen(inv.inverse()).apply(d2));
        assertEquals(d2, inv.andThen(inv.inverse()).inverse().apply(d2));
        assertEquals(d2, inv.inverse().compose(inv).apply(d2));
        assertEquals(d2, (inv.inverse().compose(inv).inverse().apply(d2)));
    }

    public static class StringToLong extends AbstractBijection<String,Long> {
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
