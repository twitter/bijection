/*
Copyright 2012 Twitter, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.twitter.bijection.twitter_util

import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.{ Future => JFuture }

import com.twitter.util.{ Future, Promise, Try }

import scala.annotation.tailrec

/**
 * Utility class for converting Java futures to Twitter's
 *
 * @param waitTimeMs Time spent sleeping by the thread converting java futures to
 *                   twitter futures when there are no futures to convert in ms
 */
private[twitter_util] class JavaFutureToTwitterFutureConverter(waitTimeMs: Long = 1000L) {

  case class Link[T](future: JFuture[T], promise: Promise[T]) {
    def maybeUpdate: Boolean = future.isDone && {
      promise.update(Try(future.get()))
      true
    }

    def cancel(): Unit = {
      promise.setException(new Exception("Promise not completed"))
      future.cancel(true)
    }
  }

  sealed abstract class State
  case object Closed extends State
  case class Open(links: List[Link[_]]) extends State
  val EmptyState: State = Open(Nil)

  private val pollRun = new Runnable {
    override def run(): Unit =
      try {
        if (!Thread.currentThread().isInterrupted)
          loop(list.getAndSet(EmptyState))
      } catch {
        case e: InterruptedException =>
      }

    @tailrec
    def swapOpen(old: List[Link[_]]): Option[List[Link[_]]] = list.get match {
      case Closed => None
      case s @ Open(links) =>
        if (list.compareAndSet(s, Open(old))) Some(links)
        else swapOpen(links)
    }

    @tailrec
    def loop(state: State): Unit = state match {
      case s @ Open(links) =>
        val notDone = links.filterNot(_.maybeUpdate)
        if (links.isEmpty || notDone.nonEmpty) Thread.sleep(waitTimeMs)
        swapOpen(notDone) match {
          case None => notDone.foreach(_.cancel())
          case Some(next) => loop(Open(next))
        }
      case Closed =>
    }
  }
  private val list = new AtomicReference[State](EmptyState)
  private val thread = new Thread(pollRun)

  def apply[T](javaFuture: JFuture[T]): Future[T] = {
    val promise = new Promise[T]()
    poll(Link(javaFuture, promise))
    promise
  }

  def start(): Unit = {
    thread.setDaemon(true)
    thread.start()
  }

  def stop(): Unit = {
    list.getAndSet(Closed) match {
      case Closed => // already closed
      case s @ Open(links) => links.foreach(_.cancel())
    }
  }

  private def poll[T](link: Link[T]): Unit = list.get match {
    case Closed => link.cancel()
    case s @ Open(tail) =>
      if (list.compareAndSet(s, Open(link :: tail))) ()
      else poll(link)
  }
}
