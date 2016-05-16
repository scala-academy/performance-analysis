package periphas.server

import periphas.administrator.Administrator
import periphas.logreceiver.LogReceiver

object Main extends App {

  val logReceiver = new LogReceiver
  val administrator = new Administrator(logReceiver.logReceiverActor)
}
