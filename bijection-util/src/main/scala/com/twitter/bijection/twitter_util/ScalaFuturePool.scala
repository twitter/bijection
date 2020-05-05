package com.twitter.bijection.twitter_util

import scala.concurrent.ExecutionContext
import com.twitter.util.{FuturePool, Future, Try, Return, Throw, Promise}

/**
  * FuturePool adapter for ExecutionContext
  *
  * @author Moses Nakamura
  */
class ScalaFuturePool(context: ExecutionContext) extends FuturePool {
  override def apply[A](f: => A): Future[A] = {
    val p = Promise[A]()
    val runnable = new Runnable() {
      override def run(): Unit =
        Try(f) match {
          case Return(value) => p.setValue(value)
          case Throw(e) => {
            context.reportFailure(e)
            p.setException(e)
          }
        }
    }
    context.execute(runnable)
    p
  }
}
