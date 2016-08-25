package com.twitter.bijection.twitter_util

import scala.concurrent.ExecutionContext
import com.twitter.util.FuturePool

/**
  * ExecutionContext adapter for FuturePool
  *
  * @author Moses Nakamura
  */
class TwitterExecutionContext(pool: FuturePool, report: Throwable => Unit)
    extends ExecutionContext {

  def this(pool: FuturePool) = this(pool, TwitterExecutionContext.ignore)

  override def execute(runnable: Runnable): Unit = {
    pool(runnable.run())
    ()
  }

  override def reportFailure(t: Throwable): Unit = report(t)
}

private[twitter_util] object TwitterExecutionContext {
  private def ignore(throwable: Throwable): Unit = {} // do nothing
}
