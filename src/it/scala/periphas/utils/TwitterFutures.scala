package periphas.utils

import scala.language.implicitConversions
import com.twitter.util.{Return, Throw}
import org.scalatest.concurrent.Futures

/**
  * Created by Ruud on 13-04-16.
  */
trait TwitterFutures extends Futures {

  implicit def convertTwitterFuture[T](twitterFuture: com.twitter.util.Future[T]): FutureConcept[T] =
    new FutureConcept[T] {
      override def eitherValue: Option[Either[Throwable, T]] = {
        twitterFuture.poll.map {
          case Return(o) => Right(o)
          case Throw(e)  => Left(e)
        }
      }
      override def isCanceled: Boolean = false
      override def isExpired: Boolean = false
    }
}
