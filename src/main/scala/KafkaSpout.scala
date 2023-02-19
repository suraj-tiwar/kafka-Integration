
import org.apache.storm.kafka.bolt._
import org.apache.storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper
import org.apache.storm.kafka.bolt.selector.DefaultTopicSelector
import org.apache.storm.{Config, StormSubmitter}
import org.apache.storm.topology.TopologyBuilder

import java.util.Properties

class KafkaSpout {
  val builder = new TopologyBuilder()
  builder.setSpout("spout",new ResponseSpout(),1)
  val props : Properties = new  Properties()
  props.put("bootstrap.servers","localhost:9092")
  props.put("acks","1")
  props.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer")

  val bolt = new KafkaBolt()
    .withProducerProperties(props)
    .withTopicSelector(new DefaultTopicSelector("test"))
    .withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper())

  builder.setBolt("forwardTokafka", bolt, 1).shuffleGrouping("spout")
  val conf = new Config();
  StormSubmitter.submitTopology("KafkaboltTest",conf,builder.createTopology())
}

object KafkaSpout {
  def main(args : Array[String]): Unit ={
    new Spout()
  }
}

