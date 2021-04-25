package esclient

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.fields.IntegerField
import com.sksamuel.elastic4s.requests.count.CountResponse
import com.sksamuel.elastic4s.requests.searches.queries._
import com.sksamuel.elastic4s.{Hit, HitReader, RequestFailure, RequestSuccess, Response, SimpleFieldValue}
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import scala.util.Try


trait EsReader
  extends EsClient
    with EsConf {

  client.execute {
    createIndex("confusion_matrix").mapping(
      properties(
        IntegerField("id"),
        IntegerField("A_A"),
        IntegerField("A_B"),
        IntegerField("B_A"),
        IntegerField("B_B"),
      )
    )
  }.await


  case class InstancePredictedLabel(id: Int, A_A: Int, A_B: Int, B_A: Int, B_B: Int)

  implicit val HitReader: HitReader[InstancePredictedLabel] = new HitReader[InstancePredictedLabel] {
    implicit val weights: Map[String, Double] = Map("w1"-> 0.3,"w2"-> 0.3,"w3"-> 0.4)

    override def read(hit: Hit): Try[InstancePredictedLabel] = {

      val model_1_A:Double = hit.sourceField("model1_A").asInstanceOf[Double]
      val model_2_A:Double = hit.sourceField("model2_A").asInstanceOf[Double]
      val model_3_A:Double = hit.sourceField("model3_A").asInstanceOf[Double]
      val model_1_B:Double = hit.sourceField("model1_B").asInstanceOf[Double]
      val model_2_B:Double = hit.sourceField("model2_B").asInstanceOf[Double]
      val model_3_B:Double = hit.sourceField("model3_B").asInstanceOf[Double]

      val given_label = hit.sourceField("given_label")
      val id = hit.sourceField("id").asInstanceOf[Int]

      // val label_A = 'A'
      val pred_A = weights("w1") * model_1_A + weights("w2") * model_2_A + weights("w3") * model_3_A
      val pred_B = weights("w1") * model_1_B + weights("w2") * model_2_B + weights("w3") * model_3_B
      val label_char = 'A'
      if (pred_A>=pred_B) {
        if (given_label.toString == label_char.toString) {
          Try(InstancePredictedLabel(id, 1, 0, 0, 0))
        } else{
          Try(InstancePredictedLabel(id, 0, 0, 1, 0))
        }
      } else{
        if (given_label.toString == label_char.toString) {
          Try(InstancePredictedLabel(id, 0, 1, 0, 0))
        } else{
          Try(InstancePredictedLabel(id, 0, 0, 0, 1))
        }
      }
    }
  }

  def get_confusion_matrix(idx:Int): Map[String,Int]={
    // now we can search for the document we just indexed
    //val s = search("model_outputs").query(
    //  boolQuery().must(
    //    matchQuery("id", 103)
    //  )
    //)

    //while (_count > 1000)
    //val s = search("model_outputs").query("id").sourceFiltering().start(5).limit(10)

    val s = search("model_outputs").size(1000).query(
      rangeQuery("id").gt(idx-1000).lte(idx)
    )//.aggs{
    // sumAgg("my_agg","id")
    //}
    val resp: Response[SearchResponse] = client.execute(s).await
    // resp is a Response[+U] ADT consisting of either a RequestFailure containing the
    // Elasticsearch error details, or a RequestSuccess[U] that depends on the type of request.
    // In this case it is a RequestSuccess[SearchResponse]

    println("---- Search Results ----")
    resp match {
      case failure: RequestFailure => println("We failed " + failure.error)
      case results: RequestSuccess[SearchResponse] => println(results.result.hits.hits.toList)
      case results: RequestSuccess[_] => println(results.result)
    }

    // Response also supports familiar combinators like map / flatMap / foreach:
    //resp foreach (search => println(s"There were ${search.totalHits} total hits"))

    val instances: IndexedSeq[Try[InstancePredictedLabel]] = resp.result.safeTo[InstancePredictedLabel]

    Map("A_A"-> instances.map(_.get.A_A).sum,
      "A_B"-> instances.map(_.get.A_B).sum,
      "B_A"-> instances.map(_.get.B_A).sum,
      "B_B"-> instances.map(_.get.B_B).sum
    )
  }

  def get_count(): Int ={
    val count_resp = client.execute(
      count("model_outputs")
    ).await
    count_resp match {
      case failure: RequestFailure =>
        println("We failed " + failure.error)
        0
      case results: RequestSuccess[CountResponse] =>  results.result.count.toInt
    }
  }
  //////////////////////////////Main///////////////////////////
  def main() {
    var state = 0
    var idx = 1000
    while (true){
      val _count = get_count()
      Thread.sleep(200)
      if (state==0 && _count>1000){
        state = 1
      } else if(_count > idx) {
        val conf_matrix = get_confusion_matrix(idx)
        idx += 1
        client.execute(
          indexInto("confusion_matrix") fieldValues(
            SimpleFieldValue("A_A", conf_matrix("A_A")),
            SimpleFieldValue("A_B", conf_matrix("A_B")),
            SimpleFieldValue("B_A", conf_matrix("B_A")),
            SimpleFieldValue("B_B", conf_matrix("B_B"))
          )
        ).await
      }
      Thread.sleep(1000)
    }
    client.close()
  }
}
