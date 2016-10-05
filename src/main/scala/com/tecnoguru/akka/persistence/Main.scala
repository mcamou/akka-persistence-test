/*
 * Copyright (c) 2016 Mario Camou
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tecnoguru.akka.persistence

import akka.actor.{ ActorSystem, PoisonPill }
import com.tecnoguru.akka.persistence.TestActorJava.{ Add, GetHistory }

import scala.util.Random

/**
  * Created by mario on 4/10/16.
  */
object Main {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("test-persistence")
    var actors = (1 to 10 map { n: Int =>
      system.actorOf(TestActorJava.props(n.toString), n.toString)
    }).toSet

    Thread.sleep(5000)

    val rnd = new Random

    1 to 200 foreach { n =>
      1 to 10 foreach { m =>
        randomMessage(n * 10 + m)
      }
      Thread.sleep(1000)
    }

    //system.terminate

    def randomMessage(n: Int) = {
      if (actors.nonEmpty) {
        val actor = actors.toList(rnd.nextInt(actors.size))
        rnd.nextInt(2) match {
          case 0 =>
            actor ! new Add(s"Element $n")

          case 1 =>
            actor ! new GetHistory()

          case 2 =>
            actors -= actor
            println(s"killing $actor")
            actor ! PoisonPill
        }
      } else {
        system.terminate
      }
    }
  }
}
