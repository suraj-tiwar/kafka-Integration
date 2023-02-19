import org.apache.storm.spout.SpoutOutputCollector
import org.apache.storm.task.TopologyContext
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.topology.base.BaseRichSpout
import org.apache.storm.tuple.{Fields, Values}

import java.util
import scala.util.Random

class ResponseSpout extends BaseRichSpout{
  var collector : SpoutOutputCollector = _
  var key   = List("rohit","kl","puju","virat","suryakumar","ksBharat","ashwin","jadeja","axar","siraj","shami")
  var random : Random = _
  override def open(conf: util.Map[String, AnyRef], context: TopologyContext, collector: SpoutOutputCollector): Unit = {
    this.collector = collector
    random = new Random()
  }

  override def nextTuple(): Unit = {
    collector.emit(new Values(key(random.nextInt(key.size)),System.nanoTime().toString))
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = {
    declarer.declare(new Fields("key","message"))
  }
}
