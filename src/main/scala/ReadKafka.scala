import org.apache.storm.task.{OutputCollector, TopologyContext}
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.topology.base.BaseRichBolt
import org.apache.storm.tuple.{Fields, Tuple, Values}

import java.util

class ReadKafka extends BaseRichBolt{
  var collector : OutputCollector = _
  override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = {
    declarer.declare(new Fields("key","message"))
  }

  override def prepare(topoConf: util.Map[String, AnyRef], context: TopologyContext, collector: OutputCollector): Unit = {
    this.collector = collector
  }

  override def execute(input: Tuple): Unit = {
    println(input.getValueByField("Topic")+" : " + input.getValueByField("Key")+ " -> " + input.getValueByField("Value"))
    val time = input.getValueByField("Value").toString.toLong - System.nanoTime()
    println(time)
    collector.emit(new Values(input.getValueByField("Key"),System.nanoTime().toString))
    collector.ack(input)
  }
}
