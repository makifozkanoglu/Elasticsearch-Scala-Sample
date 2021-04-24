package esclient
import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties}
import com.sksamuel.elastic4s.http.JavaClient
//import java.net.InetAddress
//import java.net.InetSocketTr
import org.elasticsearch.common.settings.Settings
//import org.elasticsearch.common.transport.TransportAddress
//import org.elasticsearch.transport.client.PreBuiltTransportClient

trait EsClient extends EsConf {
        System.setProperty("es.set.netty.runtime.available.processors", "false")

        lazy val settings: Settings = Settings.builder()
                .put("cluster.name", elasticCluster).put("node.name","es0")//.put("client.transport.sniff", true)
                .build()

        //val client = new PreBuiltTransportClient(settings)
        //elasticHosts.foreach(h => client.addTransportAddress(new TransportAddress(InetAddress.getByName(h), elasticPort)))
        val client: ElasticClient = ElasticClient(JavaClient(ElasticProperties(s"http://${sys.env.getOrElse("ES_HOST", "127.0.0.1")}:${sys.env.getOrElse("ES_PORT", "9200")}")))

}
