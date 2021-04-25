package esclient
import com.sksamuel.elastic4s.fields.{FloatField, IntegerField, TextField}
import com.sksamuel.elastic4s.ElasticDsl.{indexInto, _}
import com.sksamuel.elastic4s._
import org.elasticsearch.common.xcontent.XContentBuilder

import scala.concurrent.duration._
import java.util.Date
import java.text.SimpleDateFormat
import com.github.tototoshi.csv._
import com.sksamuel.elastic4s.requests.bulk.BulkCompatibleRequest
import com.sksamuel.elastic4s.requests.indexes.GetIndexRequest
import com.sksamuel.elastic4s.requests.indexes.IndexRequest

import java.io._

object EsWriter extends EsClient with EsConf {

  client.execute {
    createIndex("model_outputs").mapping(
      properties(
        IntegerField("id"),
        TextField("given_label"),
        FloatField("model1_A"),
        FloatField("model1_B"),
        FloatField("model2_A"),
        FloatField("model2_B"),
        FloatField("model3_A"),
        FloatField("model3_B")
      )
    )
  }.await
  lazy val path = "/Users/akif/IdeaProjects/ElasticSearch-Scala/src/main/resources/project-data.csv"
  val reader: CSVReader = CSVReader.open(new File(path))

  val ls: Seq[Map[String, String]] = reader.allWithHeaders()
  val ls_g: Seq[Seq[Map[String, String]]] =ls.grouped(100).toList

  """
  client.execute(
    indexInto("model_outputs") fieldValues (
      SimpleFieldValue("id", 2),
      SimpleFieldValue("given_label",'A'),
      SimpleFieldValue("model1_A",0.53),
      SimpleFieldValue("model1_B",0.53),
      SimpleFieldValue("model2_A",0.53),
      SimpleFieldValue("model2_B",0.53),
      SimpleFieldValue("model3_A",0.53),
      SimpleFieldValue("model3_B",0.53)//,
      //CustomDateFieldValue("post_date", new Date())
    )
  ).await
  """

  for (i <- ls_g.indices) {
      var bulk_seq = Seq[BulkCompatibleRequest]()
      for ( j <- ls_g(i).indices){
          bulk_seq = bulk_seq :+ (indexInto("model_outputs") fieldValues (
            SimpleFieldValue("id", ls_g(i)(j)("id").toInt),
            SimpleFieldValue("given_label",ls_g(i)(j)("given_label")),
            SimpleFieldValue("model1_A",ls_g(i)(j)("model1_A").toFloat),
            SimpleFieldValue("model1_B",ls_g(i)(j)("model1_B").toFloat),
            SimpleFieldValue("model2_A",ls_g(i)(j)("model2_A").toFloat),
            SimpleFieldValue("model2_B",ls_g(i)(j)("model2_B").toFloat),
            SimpleFieldValue("model3_A",ls_g(i)(j)("model3_A").toFloat),
            SimpleFieldValue("model3_B",ls_g(i)(j)("model3_B").toFloat)//,
          ))
      }
      client.execute(bulk(bulk_seq)).await
      Thread.sleep(1000)
  }


  println("Closing client")
  client.close()
}


case class CustomDateFieldValue(name: String, date: Date) extends FieldValue {
  private val dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

  def output(source: XContentBuilder): Unit = {
    source.field(name, dateFormat.format(date))
  }
}


