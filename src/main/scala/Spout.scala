import org.apache.storm.{Config, StormSubmitter}
import org.apache.storm.kafka.bolt.KafkaBolt
import org.apache.storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper
import org.apache.storm.kafka.bolt.selector.DefaultTopicSelector
import org.apache.storm.kafka.spout.KafkaSpoutConfig.{Builder, ProcessingGuarantee}
import org.apache.storm.kafka.spout.internal.CommonKafkaSpoutConfig.DEFAULT_FIRST_POLL_OFFSET_STRATEGY
import org.apache.storm.kafka.spout.{KafkaSpout, KafkaSpoutConfig, SimpleRecordTranslator}
import org.apache.storm.topology.TopologyBuilder
import org.apache.storm.tuple.{Fields, Values}

import java.util.Properties

class Spout {
  /*
    writing to kafka topic Request
   */
  val builder = new TopologyBuilder()
  builder.setSpout("spout", new ResponseSpout(), 1)
  val props: Properties = new Properties()
  props.put("bootstrap.servers", "localhost:9092")
  props.put("acks", "1")
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

  val bolt = new KafkaBolt()
    .withProducerProperties(props)
    .withTopicSelector(new DefaultTopicSelector("Request"))
    .withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper())

  builder.setBolt("forwardTokafka", bolt, 1).shuffleGrouping("spout")
 /*
        reading form kafka topic Request
  */
  val spoutprops = new Properties()
  spoutprops.put("bootstrap.servers", "localhost:9092")
  spoutprops.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  spoutprops.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  spoutprops.put("auto.offset.reset", "latest")
  spoutprops.put("group.id", "StormKafkaReader")

  val recordTranslator = new SimpleRecordTranslator[String,String](record => new Values(record.topic(),record.key(),record.value()),new Fields("Topic","Key","Value"))

  val spoutBuilder = new Builder[String,String]("localhost:9092","Request")
      .setProp(spoutprops)
      .setFirstPollOffsetStrategy(DEFAULT_FIRST_POLL_OFFSET_STRATEGY)
      .setRecordTranslator(recordTranslator)
      .build()

  val kafkaSpout = new KafkaSpout(spoutBuilder)
  builder.setSpout("ReadingFromKafka",kafkaSpout)
  builder.setBolt("ReaderBolt",new ReadKafka,1).shuffleGrouping("ReadingFromKafka")

  /*
      Writing to topic Response
   */
  val boltResponse = new KafkaBolt()
    .withProducerProperties(props)
    .withTopicSelector(new DefaultTopicSelector("Response"))
    .withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper())
  builder.setBolt("ResponseBolt", boltResponse , 2).shuffleGrouping("ReaderBolt")

  val conf = new Config()
  StormSubmitter.submitTopology("KafkaboltTest", conf, builder.createTopology())
}
object Spout {
  def main (args: Array[String]): Unit = {
    new Spout()
  }
}
