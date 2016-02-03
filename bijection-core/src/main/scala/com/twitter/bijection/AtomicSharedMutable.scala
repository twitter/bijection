package com.twitter.bijection

import java.util.concurrent.atomic.AtomicReference

/**
 * This class is for sharing an object between threads
 *  when allocation of a new one is not that cheap, but
 *  better than blocking.
 */
private[bijection] class AtomicSharedState[T <: AnyRef](cons: () => T) {
  private[this] val ref = new AtomicReference[T](cons())

  def get: T = {
    val borrow = ref.getAndSet(null.asInstanceOf[T])
    if (null == borrow) cons()
    else borrow
  }
  def release(t: T): Unit = {
    ref.lazySet(t)
  }
}
