// Autogenerated code DO NOT EDIT BY HAND
package com.twitter.bijection.clojure
import clojure.lang.{AFn, IFn}
import com.twitter.bijection.{AbstractBijection, Bijection}

trait GeneratedIFnBijections {
  implicit def function0ToIFn[A]: Bijection[Function0[A], IFn] =
    new AbstractBijection[Function0[A], IFn] {
      def apply(fn: Function0[A]) = new AFn {
        override def invoke(): AnyRef = fn.apply().asInstanceOf[AnyRef]
      }
      override def invert(fn: IFn) = { () =>
        fn.invoke().asInstanceOf[A]
      }
    }
  implicit def function1ToIFn[A, B]: Bijection[Function1[A, B], IFn] =
    new AbstractBijection[Function1[A, B], IFn] {
      def apply(fn: Function1[A, B]) = new AFn {
        override def invoke(a: AnyRef): AnyRef = fn.apply(a.asInstanceOf[A]).asInstanceOf[AnyRef]
      }
      override def invert(fn: IFn) = { (a) =>
        fn.invoke(a).asInstanceOf[B]
      }
    }
  implicit def function2ToIFn[A, B, C]: Bijection[Function2[A, B, C], IFn] =
    new AbstractBijection[Function2[A, B, C], IFn] {
      def apply(fn: Function2[A, B, C]) = new AFn {
        override def invoke(a: AnyRef, b: AnyRef): AnyRef =
          fn.apply(a.asInstanceOf[A], b.asInstanceOf[B]).asInstanceOf[AnyRef]
      }
      override def invert(fn: IFn) = { (a, b) =>
        fn.invoke(a, b).asInstanceOf[C]
      }
    }
  implicit def function3ToIFn[A, B, C, D]: Bijection[Function3[A, B, C, D], IFn] =
    new AbstractBijection[Function3[A, B, C, D], IFn] {
      def apply(fn: Function3[A, B, C, D]) = new AFn {
        override def invoke(a: AnyRef, b: AnyRef, c: AnyRef): AnyRef =
          fn.apply(a.asInstanceOf[A], b.asInstanceOf[B], c.asInstanceOf[C]).asInstanceOf[AnyRef]
      }
      override def invert(fn: IFn) = { (a, b, c) =>
        fn.invoke(a, b, c).asInstanceOf[D]
      }
    }
  implicit def function4ToIFn[A, B, C, D, E]: Bijection[Function4[A, B, C, D, E], IFn] =
    new AbstractBijection[Function4[A, B, C, D, E], IFn] {
      def apply(fn: Function4[A, B, C, D, E]) = new AFn {
        override def invoke(a: AnyRef, b: AnyRef, c: AnyRef, d: AnyRef): AnyRef =
          fn.apply(a.asInstanceOf[A], b.asInstanceOf[B], c.asInstanceOf[C], d.asInstanceOf[D])
            .asInstanceOf[AnyRef]
      }
      override def invert(fn: IFn) = { (a, b, c, d) =>
        fn.invoke(a, b, c, d).asInstanceOf[E]
      }
    }
  implicit def function5ToIFn[A, B, C, D, E, F]: Bijection[Function5[A, B, C, D, E, F], IFn] =
    new AbstractBijection[Function5[A, B, C, D, E, F], IFn] {
      def apply(fn: Function5[A, B, C, D, E, F]) = new AFn {
        override def invoke(a: AnyRef, b: AnyRef, c: AnyRef, d: AnyRef, e: AnyRef): AnyRef =
          fn.apply(
              a.asInstanceOf[A],
              b.asInstanceOf[B],
              c.asInstanceOf[C],
              d.asInstanceOf[D],
              e.asInstanceOf[E]
            )
            .asInstanceOf[AnyRef]
      }
      override def invert(fn: IFn) = { (a, b, c, d, e) =>
        fn.invoke(a, b, c, d, e).asInstanceOf[F]
      }
    }
  implicit def function6ToIFn[A, B, C, D, E, F, G]: Bijection[Function6[A, B, C, D, E, F, G], IFn] =
    new AbstractBijection[Function6[A, B, C, D, E, F, G], IFn] {
      def apply(fn: Function6[A, B, C, D, E, F, G]) = new AFn {
        override def invoke(
            a: AnyRef,
            b: AnyRef,
            c: AnyRef,
            d: AnyRef,
            e: AnyRef,
            f: AnyRef
        ): AnyRef =
          fn.apply(
              a.asInstanceOf[A],
              b.asInstanceOf[B],
              c.asInstanceOf[C],
              d.asInstanceOf[D],
              e.asInstanceOf[E],
              f.asInstanceOf[F]
            )
            .asInstanceOf[AnyRef]
      }
      override def invert(fn: IFn) = { (a, b, c, d, e, f) =>
        fn.invoke(a, b, c, d, e, f).asInstanceOf[G]
      }
    }
  implicit def function7ToIFn[A, B, C, D, E, F, G, H]
      : Bijection[Function7[A, B, C, D, E, F, G, H], IFn] =
    new AbstractBijection[Function7[A, B, C, D, E, F, G, H], IFn] {
      def apply(fn: Function7[A, B, C, D, E, F, G, H]) = new AFn {
        override def invoke(
            a: AnyRef,
            b: AnyRef,
            c: AnyRef,
            d: AnyRef,
            e: AnyRef,
            f: AnyRef,
            g: AnyRef
        ): AnyRef =
          fn.apply(
              a.asInstanceOf[A],
              b.asInstanceOf[B],
              c.asInstanceOf[C],
              d.asInstanceOf[D],
              e.asInstanceOf[E],
              f.asInstanceOf[F],
              g.asInstanceOf[G]
            )
            .asInstanceOf[AnyRef]
      }
      override def invert(fn: IFn) = { (a, b, c, d, e, f, g) =>
        fn.invoke(a, b, c, d, e, f, g).asInstanceOf[H]
      }
    }
  implicit def function8ToIFn[A, B, C, D, E, F, G, H, I]
      : Bijection[Function8[A, B, C, D, E, F, G, H, I], IFn] =
    new AbstractBijection[Function8[A, B, C, D, E, F, G, H, I], IFn] {
      def apply(fn: Function8[A, B, C, D, E, F, G, H, I]) = new AFn {
        override def invoke(
            a: AnyRef,
            b: AnyRef,
            c: AnyRef,
            d: AnyRef,
            e: AnyRef,
            f: AnyRef,
            g: AnyRef,
            h: AnyRef
        ): AnyRef =
          fn.apply(
              a.asInstanceOf[A],
              b.asInstanceOf[B],
              c.asInstanceOf[C],
              d.asInstanceOf[D],
              e.asInstanceOf[E],
              f.asInstanceOf[F],
              g.asInstanceOf[G],
              h.asInstanceOf[H]
            )
            .asInstanceOf[AnyRef]
      }
      override def invert(fn: IFn) = { (a, b, c, d, e, f, g, h) =>
        fn.invoke(a, b, c, d, e, f, g, h).asInstanceOf[I]
      }
    }
  implicit def function9ToIFn[A, B, C, D, E, F, G, H, I, J]
      : Bijection[Function9[A, B, C, D, E, F, G, H, I, J], IFn] =
    new AbstractBijection[Function9[A, B, C, D, E, F, G, H, I, J], IFn] {
      def apply(fn: Function9[A, B, C, D, E, F, G, H, I, J]) = new AFn {
        override def invoke(
            a: AnyRef,
            b: AnyRef,
            c: AnyRef,
            d: AnyRef,
            e: AnyRef,
            f: AnyRef,
            g: AnyRef,
            h: AnyRef,
            i: AnyRef
        ): AnyRef =
          fn.apply(
              a.asInstanceOf[A],
              b.asInstanceOf[B],
              c.asInstanceOf[C],
              d.asInstanceOf[D],
              e.asInstanceOf[E],
              f.asInstanceOf[F],
              g.asInstanceOf[G],
              h.asInstanceOf[H],
              i.asInstanceOf[I]
            )
            .asInstanceOf[AnyRef]
      }
      override def invert(fn: IFn) = { (a, b, c, d, e, f, g, h, i) =>
        fn.invoke(a, b, c, d, e, f, g, h, i).asInstanceOf[J]
      }
    }
  implicit def function10ToIFn[A, B, C, D, E, F, G, H, I, J, K]
      : Bijection[Function10[A, B, C, D, E, F, G, H, I, J, K], IFn] =
    new AbstractBijection[Function10[A, B, C, D, E, F, G, H, I, J, K], IFn] {
      def apply(fn: Function10[A, B, C, D, E, F, G, H, I, J, K]) = new AFn {
        override def invoke(
            a: AnyRef,
            b: AnyRef,
            c: AnyRef,
            d: AnyRef,
            e: AnyRef,
            f: AnyRef,
            g: AnyRef,
            h: AnyRef,
            i: AnyRef,
            j: AnyRef
        ): AnyRef =
          fn.apply(
              a.asInstanceOf[A],
              b.asInstanceOf[B],
              c.asInstanceOf[C],
              d.asInstanceOf[D],
              e.asInstanceOf[E],
              f.asInstanceOf[F],
              g.asInstanceOf[G],
              h.asInstanceOf[H],
              i.asInstanceOf[I],
              j.asInstanceOf[J]
            )
            .asInstanceOf[AnyRef]
      }
      override def invert(fn: IFn) = { (a, b, c, d, e, f, g, h, i, j) =>
        fn.invoke(a, b, c, d, e, f, g, h, i, j).asInstanceOf[K]
      }
    }
  implicit def function11ToIFn[A, B, C, D, E, F, G, H, I, J, K, L]
      : Bijection[Function11[A, B, C, D, E, F, G, H, I, J, K, L], IFn] =
    new AbstractBijection[Function11[A, B, C, D, E, F, G, H, I, J, K, L], IFn] {
      def apply(fn: Function11[A, B, C, D, E, F, G, H, I, J, K, L]) = new AFn {
        override def invoke(
            a: AnyRef,
            b: AnyRef,
            c: AnyRef,
            d: AnyRef,
            e: AnyRef,
            f: AnyRef,
            g: AnyRef,
            h: AnyRef,
            i: AnyRef,
            j: AnyRef,
            k: AnyRef
        ): AnyRef =
          fn.apply(
              a.asInstanceOf[A],
              b.asInstanceOf[B],
              c.asInstanceOf[C],
              d.asInstanceOf[D],
              e.asInstanceOf[E],
              f.asInstanceOf[F],
              g.asInstanceOf[G],
              h.asInstanceOf[H],
              i.asInstanceOf[I],
              j.asInstanceOf[J],
              k.asInstanceOf[K]
            )
            .asInstanceOf[AnyRef]
      }
      override def invert(fn: IFn) = { (a, b, c, d, e, f, g, h, i, j, k) =>
        fn.invoke(a, b, c, d, e, f, g, h, i, j, k).asInstanceOf[L]
      }
    }
  implicit def function12ToIFn[A, B, C, D, E, F, G, H, I, J, K, L, M]
      : Bijection[Function12[A, B, C, D, E, F, G, H, I, J, K, L, M], IFn] =
    new AbstractBijection[Function12[A, B, C, D, E, F, G, H, I, J, K, L, M], IFn] {
      def apply(fn: Function12[A, B, C, D, E, F, G, H, I, J, K, L, M]) = new AFn {
        override def invoke(
            a: AnyRef,
            b: AnyRef,
            c: AnyRef,
            d: AnyRef,
            e: AnyRef,
            f: AnyRef,
            g: AnyRef,
            h: AnyRef,
            i: AnyRef,
            j: AnyRef,
            k: AnyRef,
            l: AnyRef
        ): AnyRef =
          fn.apply(
              a.asInstanceOf[A],
              b.asInstanceOf[B],
              c.asInstanceOf[C],
              d.asInstanceOf[D],
              e.asInstanceOf[E],
              f.asInstanceOf[F],
              g.asInstanceOf[G],
              h.asInstanceOf[H],
              i.asInstanceOf[I],
              j.asInstanceOf[J],
              k.asInstanceOf[K],
              l.asInstanceOf[L]
            )
            .asInstanceOf[AnyRef]
      }
      override def invert(fn: IFn) = { (a, b, c, d, e, f, g, h, i, j, k, l) =>
        fn.invoke(a, b, c, d, e, f, g, h, i, j, k, l).asInstanceOf[M]
      }
    }
  implicit def function13ToIFn[A, B, C, D, E, F, G, H, I, J, K, L, M, N]
      : Bijection[Function13[A, B, C, D, E, F, G, H, I, J, K, L, M, N], IFn] =
    new AbstractBijection[Function13[A, B, C, D, E, F, G, H, I, J, K, L, M, N], IFn] {
      def apply(fn: Function13[A, B, C, D, E, F, G, H, I, J, K, L, M, N]) = new AFn {
        override def invoke(
            a: AnyRef,
            b: AnyRef,
            c: AnyRef,
            d: AnyRef,
            e: AnyRef,
            f: AnyRef,
            g: AnyRef,
            h: AnyRef,
            i: AnyRef,
            j: AnyRef,
            k: AnyRef,
            l: AnyRef,
            m: AnyRef
        ): AnyRef =
          fn.apply(
              a.asInstanceOf[A],
              b.asInstanceOf[B],
              c.asInstanceOf[C],
              d.asInstanceOf[D],
              e.asInstanceOf[E],
              f.asInstanceOf[F],
              g.asInstanceOf[G],
              h.asInstanceOf[H],
              i.asInstanceOf[I],
              j.asInstanceOf[J],
              k.asInstanceOf[K],
              l.asInstanceOf[L],
              m.asInstanceOf[M]
            )
            .asInstanceOf[AnyRef]
      }
      override def invert(fn: IFn) = { (a, b, c, d, e, f, g, h, i, j, k, l, m) =>
        fn.invoke(a, b, c, d, e, f, g, h, i, j, k, l, m).asInstanceOf[N]
      }
    }
  implicit def function14ToIFn[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O]
      : Bijection[Function14[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O], IFn] =
    new AbstractBijection[Function14[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O], IFn] {
      def apply(fn: Function14[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O]) = new AFn {
        override def invoke(
            a: AnyRef,
            b: AnyRef,
            c: AnyRef,
            d: AnyRef,
            e: AnyRef,
            f: AnyRef,
            g: AnyRef,
            h: AnyRef,
            i: AnyRef,
            j: AnyRef,
            k: AnyRef,
            l: AnyRef,
            m: AnyRef,
            n: AnyRef
        ): AnyRef =
          fn.apply(
              a.asInstanceOf[A],
              b.asInstanceOf[B],
              c.asInstanceOf[C],
              d.asInstanceOf[D],
              e.asInstanceOf[E],
              f.asInstanceOf[F],
              g.asInstanceOf[G],
              h.asInstanceOf[H],
              i.asInstanceOf[I],
              j.asInstanceOf[J],
              k.asInstanceOf[K],
              l.asInstanceOf[L],
              m.asInstanceOf[M],
              n.asInstanceOf[N]
            )
            .asInstanceOf[AnyRef]
      }
      override def invert(fn: IFn) = { (a, b, c, d, e, f, g, h, i, j, k, l, m, n) =>
        fn.invoke(a, b, c, d, e, f, g, h, i, j, k, l, m, n).asInstanceOf[O]
      }
    }
  implicit def function15ToIFn[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P]
      : Bijection[Function15[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P], IFn] =
    new AbstractBijection[Function15[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P], IFn] {
      def apply(fn: Function15[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P]) = new AFn {
        override def invoke(
            a: AnyRef,
            b: AnyRef,
            c: AnyRef,
            d: AnyRef,
            e: AnyRef,
            f: AnyRef,
            g: AnyRef,
            h: AnyRef,
            i: AnyRef,
            j: AnyRef,
            k: AnyRef,
            l: AnyRef,
            m: AnyRef,
            n: AnyRef,
            o: AnyRef
        ): AnyRef =
          fn.apply(
              a.asInstanceOf[A],
              b.asInstanceOf[B],
              c.asInstanceOf[C],
              d.asInstanceOf[D],
              e.asInstanceOf[E],
              f.asInstanceOf[F],
              g.asInstanceOf[G],
              h.asInstanceOf[H],
              i.asInstanceOf[I],
              j.asInstanceOf[J],
              k.asInstanceOf[K],
              l.asInstanceOf[L],
              m.asInstanceOf[M],
              n.asInstanceOf[N],
              o.asInstanceOf[O]
            )
            .asInstanceOf[AnyRef]
      }
      override def invert(fn: IFn) = { (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o) =>
        fn.invoke(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o).asInstanceOf[P]
      }
    }
  implicit def function16ToIFn[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q]
      : Bijection[Function16[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q], IFn] =
    new AbstractBijection[Function16[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q], IFn] {
      def apply(fn: Function16[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q]) = new AFn {
        override def invoke(
            a: AnyRef,
            b: AnyRef,
            c: AnyRef,
            d: AnyRef,
            e: AnyRef,
            f: AnyRef,
            g: AnyRef,
            h: AnyRef,
            i: AnyRef,
            j: AnyRef,
            k: AnyRef,
            l: AnyRef,
            m: AnyRef,
            n: AnyRef,
            o: AnyRef,
            p: AnyRef
        ): AnyRef =
          fn.apply(
              a.asInstanceOf[A],
              b.asInstanceOf[B],
              c.asInstanceOf[C],
              d.asInstanceOf[D],
              e.asInstanceOf[E],
              f.asInstanceOf[F],
              g.asInstanceOf[G],
              h.asInstanceOf[H],
              i.asInstanceOf[I],
              j.asInstanceOf[J],
              k.asInstanceOf[K],
              l.asInstanceOf[L],
              m.asInstanceOf[M],
              n.asInstanceOf[N],
              o.asInstanceOf[O],
              p.asInstanceOf[P]
            )
            .asInstanceOf[AnyRef]
      }
      override def invert(fn: IFn) = { (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p) =>
        fn.invoke(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p).asInstanceOf[Q]
      }
    }
  implicit def function17ToIFn[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R]
      : Bijection[Function17[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R], IFn] =
    new AbstractBijection[Function17[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R], IFn] {
      def apply(fn: Function17[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R]) = new AFn {
        override def invoke(
            a: AnyRef,
            b: AnyRef,
            c: AnyRef,
            d: AnyRef,
            e: AnyRef,
            f: AnyRef,
            g: AnyRef,
            h: AnyRef,
            i: AnyRef,
            j: AnyRef,
            k: AnyRef,
            l: AnyRef,
            m: AnyRef,
            n: AnyRef,
            o: AnyRef,
            p: AnyRef,
            q: AnyRef
        ): AnyRef =
          fn.apply(
              a.asInstanceOf[A],
              b.asInstanceOf[B],
              c.asInstanceOf[C],
              d.asInstanceOf[D],
              e.asInstanceOf[E],
              f.asInstanceOf[F],
              g.asInstanceOf[G],
              h.asInstanceOf[H],
              i.asInstanceOf[I],
              j.asInstanceOf[J],
              k.asInstanceOf[K],
              l.asInstanceOf[L],
              m.asInstanceOf[M],
              n.asInstanceOf[N],
              o.asInstanceOf[O],
              p.asInstanceOf[P],
              q.asInstanceOf[Q]
            )
            .asInstanceOf[AnyRef]
      }
      override def invert(fn: IFn) = { (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q) =>
        fn.invoke(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q).asInstanceOf[R]
      }
    }
  implicit def function18ToIFn[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S]
      : Bijection[Function18[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S], IFn] =
    new AbstractBijection[Function18[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S], IFn] {
      def apply(fn: Function18[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S]) = new AFn {
        override def invoke(
            a: AnyRef,
            b: AnyRef,
            c: AnyRef,
            d: AnyRef,
            e: AnyRef,
            f: AnyRef,
            g: AnyRef,
            h: AnyRef,
            i: AnyRef,
            j: AnyRef,
            k: AnyRef,
            l: AnyRef,
            m: AnyRef,
            n: AnyRef,
            o: AnyRef,
            p: AnyRef,
            q: AnyRef,
            r: AnyRef
        ): AnyRef =
          fn.apply(
              a.asInstanceOf[A],
              b.asInstanceOf[B],
              c.asInstanceOf[C],
              d.asInstanceOf[D],
              e.asInstanceOf[E],
              f.asInstanceOf[F],
              g.asInstanceOf[G],
              h.asInstanceOf[H],
              i.asInstanceOf[I],
              j.asInstanceOf[J],
              k.asInstanceOf[K],
              l.asInstanceOf[L],
              m.asInstanceOf[M],
              n.asInstanceOf[N],
              o.asInstanceOf[O],
              p.asInstanceOf[P],
              q.asInstanceOf[Q],
              r.asInstanceOf[R]
            )
            .asInstanceOf[AnyRef]
      }
      override def invert(fn: IFn) = { (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r) =>
        fn.invoke(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r).asInstanceOf[S]
      }
    }
  implicit def function19ToIFn[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T]
      : Bijection[Function19[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T], IFn] =
    new AbstractBijection[
      Function19[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T],
      IFn
    ] {
      def apply(fn: Function19[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T]) =
        new AFn {
          override def invoke(
              a: AnyRef,
              b: AnyRef,
              c: AnyRef,
              d: AnyRef,
              e: AnyRef,
              f: AnyRef,
              g: AnyRef,
              h: AnyRef,
              i: AnyRef,
              j: AnyRef,
              k: AnyRef,
              l: AnyRef,
              m: AnyRef,
              n: AnyRef,
              o: AnyRef,
              p: AnyRef,
              q: AnyRef,
              r: AnyRef,
              s: AnyRef
          ): AnyRef =
            fn.apply(
                a.asInstanceOf[A],
                b.asInstanceOf[B],
                c.asInstanceOf[C],
                d.asInstanceOf[D],
                e.asInstanceOf[E],
                f.asInstanceOf[F],
                g.asInstanceOf[G],
                h.asInstanceOf[H],
                i.asInstanceOf[I],
                j.asInstanceOf[J],
                k.asInstanceOf[K],
                l.asInstanceOf[L],
                m.asInstanceOf[M],
                n.asInstanceOf[N],
                o.asInstanceOf[O],
                p.asInstanceOf[P],
                q.asInstanceOf[Q],
                r.asInstanceOf[R],
                s.asInstanceOf[S]
              )
              .asInstanceOf[AnyRef]
        }
      override def invert(fn: IFn) = { (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s) =>
        fn.invoke(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s).asInstanceOf[T]
      }
    }
  implicit def function20ToIFn[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U]
      : Bijection[Function20[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U], IFn] =
    new AbstractBijection[
      Function20[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U],
      IFn
    ] {
      def apply(fn: Function20[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U]) =
        new AFn {
          override def invoke(
              a: AnyRef,
              b: AnyRef,
              c: AnyRef,
              d: AnyRef,
              e: AnyRef,
              f: AnyRef,
              g: AnyRef,
              h: AnyRef,
              i: AnyRef,
              j: AnyRef,
              k: AnyRef,
              l: AnyRef,
              m: AnyRef,
              n: AnyRef,
              o: AnyRef,
              p: AnyRef,
              q: AnyRef,
              r: AnyRef,
              s: AnyRef,
              t: AnyRef
          ): AnyRef =
            fn.apply(
                a.asInstanceOf[A],
                b.asInstanceOf[B],
                c.asInstanceOf[C],
                d.asInstanceOf[D],
                e.asInstanceOf[E],
                f.asInstanceOf[F],
                g.asInstanceOf[G],
                h.asInstanceOf[H],
                i.asInstanceOf[I],
                j.asInstanceOf[J],
                k.asInstanceOf[K],
                l.asInstanceOf[L],
                m.asInstanceOf[M],
                n.asInstanceOf[N],
                o.asInstanceOf[O],
                p.asInstanceOf[P],
                q.asInstanceOf[Q],
                r.asInstanceOf[R],
                s.asInstanceOf[S],
                t.asInstanceOf[T]
              )
              .asInstanceOf[AnyRef]
        }
      override def invert(fn: IFn) = {
        (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t) =>
          fn.invoke(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t).asInstanceOf[U]
      }
    }
}
