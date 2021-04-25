package esclient

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.requests.searches.queries._
import com.sksamuel.elastic4s.{Hit, HitReader, Indexable, RequestFailure, RequestSuccess, Response}
import com.sksamuel.elastic4s.requests.searches.SearchResponse

import scala.util.Try







object EsReader
  extends EsClient
    with EsConf
    with App {



  case class InstancePredictedLabel(id: Int, A_A: Int, A_B: Int, B_A: Int, B_B: Int)

  implicit val HitReader: HitReader[InstancePredictedLabel] = new HitReader[InstancePredictedLabel] {
    implicit val weights: Map[String, Double] = Map("w1"-> 0.2,"w2"-> 0.2,"w3"-> 0.2)

    override def read(hit: Hit): Try[InstancePredictedLabel] = {

      val model_1_A:Double = hit.sourceField("model1_A").asInstanceOf[Double]
      val model_2_A:Double = hit.sourceField("model2_A").asInstanceOf[Double]
      val model_3_A:Double = hit.sourceField("model3_A").asInstanceOf[Double]
      val model_1_B:Double = hit.sourceField("model1_B").asInstanceOf[Double]
      val model_2_B:Double = hit.sourceField("model2_B").asInstanceOf[Double]
      val model_3_B:Double = hit.sourceField("model3_B").asInstanceOf[Double]
      val given_label = hit.sourceField("given_label")
      //val given_label = given_label2.asInstanceOf[Char]
      val id2 = hit.sourceField("id")//.asInstanceOf[Int]
      val id = id2.asInstanceOf[Int]
      // val label_A = 'A'
      val pred_A = weights("w1") * model_1_A + weights("w2") * model_2_A + weights("w3") * model_3_A
      val pred_B = weights("w1") * model_1_B + weights("w2") * model_2_B + weights("w3") * model_3_B
      if (pred_A>=pred_B) {
        if (given_label == 'A') {
          Try(InstancePredictedLabel(id, 1, 0, 0, 0))
        } else{
          Try(InstancePredictedLabel(id, 0, 0, 1, 0))
        }
      } else{
        if (given_label == 'A') {
          Try(InstancePredictedLabel(id, 0, 1, 0, 0))
        } else{
          Try(InstancePredictedLabel(id, 0, 0, 0, 1))
        }
      }
    }
  }


  // now we can search for the document we just indexed
  //val s = search("model_outputs").query(
  //  boolQuery().must(
  //    matchQuery("id", 103)
  //  )
  //)

  //val s = search("model_outputs").query("id").sourceFiltering().start(5).limit(10)
  val s = search("model_outputs").query(
    BoolQuery().must(rangeQuery("id").gt(10).lt(20))
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
  resp foreach (search => println(s"There were ${search.totalHits} total hits"))
  val instances = resp.result.safeTo[InstancePredictedLabel]
  //instances.sum()<
  val sum = instances.map(_.get.A_B).sum
  client.close()
}
