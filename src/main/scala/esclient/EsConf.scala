package esclient


import com.typesafe.config.{Config, ConfigFactory}


trait EsConf {
        val elasticConf: Config = ConfigFactory.load("elastic.conf")

        lazy val elasticCluster: String = elasticConf.getString("elastic.cluster")

        lazy val documentsElasticIndex: String = elasticConf.getString("elastic.documents-index")
        lazy val documentsElasticDocType: String = elasticConf.getString("elastic.documents-doc-type")

        lazy val offersElasticIndex: String = elasticConf.getString("elastic.offers-index")
        lazy val offersElasticDocType: String = elasticConf.getString("elastic.offers-doc-type")

        lazy val elasticHosts: Seq[String] = elasticConf.getString("elastic.hosts").split(",").toSeq.map(_.trim)
        lazy val elasticPort: Int = elasticConf.getString("elastic.port").toInt
        }
