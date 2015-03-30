package akka.stream.impl

import akka.stream.actor.ActorSubscriber
import akka.actor.ActorRef
import akka.stream.actor.ActorSubscriberMessage
import akka.actor.Status
import akka.stream.actor.WatermarkRequestStrategy
import akka.actor.Props
import akka.actor.Terminated

/**
 * INTERNAL API
 */
private[akka] object TellSinkActor {
  def props(ref: ActorRef, highWatermark: Int, onCompleteMessage: Any): Props =
    Props(new TellSinkActor(ref, highWatermark, onCompleteMessage))
}

/**
 * INTERNAL API
 */
private[akka] class TellSinkActor(ref: ActorRef, highWatermark: Int, onCompleteMessage: Any) extends ActorSubscriber {
  import ActorSubscriberMessage._

  override val requestStrategy = WatermarkRequestStrategy(highWatermark)

  context.watch(ref)

  def receive = {
    case OnNext(elem)      ⇒ ref ! elem
    case OnError(cause)    ⇒ ref ! Status.Failure(cause)
    case OnComplete        ⇒ ref ! onCompleteMessage
    case Terminated(`ref`) ⇒ context.stop(self) // will cancel upstream
  }

}
