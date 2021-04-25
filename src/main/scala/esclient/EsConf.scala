package esclient


import com.typesafe.config.{Config, ConfigFactory}


trait EsConf {
        val elasticConf: Config = ConfigFactory.load("elastic.conf")

        lazy val elasticCluster: String = elasticConf.getString("elastic.cluster")

        lazy val elasticNode: String = elasticConf.getString("elastic.nodes")//.split(",").toSeq.map(_.trim)

        lazy val elasticHosts: Seq[String] = elasticConf.getString("elastic.hosts").split(",").toSeq.map(_.trim)
        lazy val elasticPort: Int = elasticConf.getString("elastic.port").toInt
        }
