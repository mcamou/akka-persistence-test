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

import akka.actor.Props
import akka.persistence._
import com.tecnoguru.akka.persistence.TestActor.{ Add, GetHistory, Snapshot }

/**
  * Created by mario on 4/10/16.
  */
class TestActor(id: String) extends PersistentActor {
  var sequence: Int         = 0
  var history: List[String] = Nil

  override def receiveRecover: Receive = {
    case s: Int =>
      println(s"restoring sequence $id - $s")
      sequence = s

    case Add(newValue) =>
      history = newValue :: history
      sequence += 1
      println(s"restoring value $id - $newValue")

    case RecoveryCompleted =>
      println(s"recovery completed $id")

    case SnapshotOffer(metadata, state: Snapshot) =>
      println(s"received snapshot $id - $state ($metadata)")
      sequence = state.sequence
      history = state.history

    case x => println(s"Unexpected recovery message $id: $x")
  }

  override def receiveCommand: Receive = {
    case GetHistory =>
      sequence += 1
      println(s"received sequence $id - $sequence")
      persist(sequence) { x =>
        println(s"persisted sequence $id - $x")
      }
      sender ! history

    case ev @ Add(newValue) =>
      history = newValue :: history
      sequence += 1
      println(s"received $id - $sequence: $ev")
      if (sequence % 10 == 0) {
        val lastSequenceNumber = lastSequenceNr
        println(s"snapshotting $id - $sequence")
        saveSnapshot(Snapshot(sequence, history))
        println(s"deleting old events $id - $lastSequenceNumber")
        deleteMessages(lastSequenceNumber)
        deleteSnapshots(SnapshotSelectionCriteria(lastSequenceNumber))
      } else {
        persist(ev) { x =>
          println(s"persisted $id - $sequence: $x")
        }
      }

    case DeleteMessagesSuccess(sequenceNumber) =>
      println(s"deleted messages $id - $sequenceNumber")

    case DeleteMessagesFailure(cause, sequenceNumber) =>
      println(s"FAILURE deleting messages $id - $sequenceNumber ($cause)")

    case SaveSnapshotSuccess(metadata) =>
      println(s"saved snapshot $id - $metadata")

    case SaveSnapshotFailure(metadata, cause) =>
      println(s"FAILURE deleting snapshots $id - $metadata ($cause)")

    case DeleteSnapshotsSuccess(criteria) =>
      println(s"deleted snapshot $id - $criteria")

    case DeleteSnapshotFailure(metadata, cause) =>
      println(s"FAILURE deleting snapshots $id - $metadata ($cause)")

    case x => println(s"Unexpected message $id: $x")
  }

  override val persistenceId: String = id
}

object TestActor {
  case class Add(newValue: String)
  case object GetHistory
  case class Snapshot(sequence: Int, history: List[String])

  def props(id: String) = Props(classOf[TestActor], id)
}
