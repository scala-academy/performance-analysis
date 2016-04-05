package performanceanalysis.server

import performanceanalysis.administrator.Administrator
import performanceanalysis.logreceiver.LogReceiver

object Main extends App {

  val logReceiver = new LogReceiver
  val administrator = new Administrator(logReceiver.logReceiverActor)
}
