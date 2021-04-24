import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.sql.streaming.Trigger






object streamES extends App {
  //val conf = new SparkConf()
  //conf.set("es.batch.size.entries", 1111)
  //val sc = new SparkContext(conf)
  //val ssc = new StreamingContext(sc, Seconds(1))
  //ssc.queueStream()
  //val rdd = sc.makeRDD(Seq(json1, json2))
  //val microbatch = mutable.Queue(rdd)
  //ssc.queueStream(microbatch).saveJsonToEs("spark/json-trips")

  //ssc.start()
  val spark: SparkSession = SparkSession.builder()
    .appName("EsStreamingExample")
    .master("local[*]")
    .config("spark.es.nodes","localhost")
    .config("es.index.auto.create", "true") // this will ensure that index is also created on first POST
    .config("es.nodes.wan.only", "true") // needed to run against dockerized ES for local tests
    .getOrCreate()

  // case class used to define the DataFrame

  val userSchema: StructType = new StructType().add("name", "string")
    .add("age", "integer")
  val csvDF: DataFrame = spark
    .readStream
    .option("sep", ";").schema(userSchema)   // Specify schema of the csv files
    .option("rowsPerSecond", 1000)
    .csv("/Users/akif/IdeaProjects/ElasticSearch-Scala/src/main/resources/tazi-se-interview-project-data.csv")    // Equivalent to format("csv").load("/path/to/directory")

  csvDF.writeStream
    .option("checkpointLocation", "/save/location")
    .trigger(Trigger.Continuous("1 second"))
    .format("org.elasticsearch.spark.sql")
    .option("es.resource","index/type")
    .start("spark/models")
    .awaitTermination(10000000)
}
