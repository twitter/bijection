
import com.twitter.util.{Bijection => UtilBijection}
import com.twitter.bijection.{AbstractBijection, Bijection}

/**
 * Converts betwen twitter/util Bijections[1] and twitter/bijection Bijections[2]
 *
 * [1] https://github.com/twitter/util/blob/master/util-core/src/main/scala/com/twitter/util/Bijection.scala
 * [2] https://github.com/twitter/bijection
 */
class BijectionBijection[A,B] extends AbstractBijection[UtilBijection[A,B],Bijection[A,B]] {
  def apply(u: UtilBijection[A,B]) = new AbstractBijection[A,B] {
    def apply(a: A) = u(a)
    override def invert(b: B) = u.invert(b)
  }
  override def invert(bj: Bijection[A,B]) = new UtilBijection[A,B] {
    def apply(a: A) = bj(a)
    def invert(b: B) = bj.invert(b)
  }
}
