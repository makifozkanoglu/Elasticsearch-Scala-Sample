import esclient.{EsClient, EsConf, EsReader, EsWriter}

import scala.concurrent._
import ExecutionContext.Implicits.global  // implicit execution context

object main extends App {


  var t1 = new EsWriterThread()
  var t2 = new EsReaderThread()

  t1.start()
  t2.start()
}


class EsWriterThread extends Thread
  with EsWriter
  with EsClient
  with EsConf{

   override def run() {
    main()
  }
}

class EsReaderThread extends Thread
  with EsReader
  with EsClient
  with EsConf{

  override def run() {
    main()
  }
}