package iot_framework_big_data.kafka_storm_mongodb;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.kafka.Broker;
import org.apache.storm.kafka.KafkaSpout;
import org.apache.storm.kafka.SpoutConfig;
import org.apache.storm.kafka.StaticHosts;
import org.apache.storm.kafka.trident.GlobalPartitionInformation;
import org.apache.storm.mongodb.bolt.MongoInsertBolt;
import org.apache.storm.mongodb.common.mapper.MongoMapper;
import org.apache.storm.shade.com.google.common.collect.ImmutableList;
import org.apache.storm.spout.RawMultiScheme;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.ITuple;
import org.apache.storm.tuple.Tuple;
import org.bson.Document;

public class IotKafkaMongoTopology {
	
	public static class BytesToJson implements MongoMapper{

		public Document toDocument(ITuple tuple) {
			// TODO Auto-generated method stub
			try{
				String json = new String(tuple.getBinaryByField("bytes"));
		    	//This is necessary, it seems the string literal for json includes escaped quotes and a first and last quote.
		    	json = json.substring(1, json.length() - 1).replace("\\", "");
				Document doc = Document.parse(json);
				System.out.println("Inserting doc to mongodb: " + doc.toJson());
				return doc;
			} catch (Exception e){
				System.out.println("error: " + e.toString());
				return null;
			}

		}
		
	}
	
	private StaticHosts hosts;
	
	public IotKafkaMongoTopology(String brokerHost, int partition, String discriminator){
		Broker broker = new Broker(brokerHost);
		GlobalPartitionInformation partitionInfo = new GlobalPartitionInformation(discriminator);
		partitionInfo.addPartition(partition, broker);
		hosts = new StaticHosts(partitionInfo);
	}
	
	public StormTopology buildTopology(String dburl, String discriminator, String kafkaUrl, String systemName){
		SpoutConfig kafkaConfig = new SpoutConfig(hosts, discriminator, "/brokers", systemName);
		kafkaConfig.zkServers = ImmutableList.of(kafkaUrl);
		kafkaConfig.zkPort = 2181;
		kafkaConfig.scheme = new RawMultiScheme();
		String url = dburl;
		String collectionName = discriminator;
		MongoMapper mapper = new BytesToJson();
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout("kafkaspout", new KafkaSpout(kafkaConfig), 10);
		builder.setBolt("mongobolt", new MongoInsertBolt(url, collectionName, mapper)).shuffleGrouping("kafkaspout");
		return builder.createTopology();
	}
	
	public static void main (String args[]) throws Exception{
		String iotconfig = args[0];
		String systemName = args[1];
		if (iotconfig == null){
			throw new Exception("Please provide iot config file path for storm.");
		}
		Properties iot_props = new Properties();
		iot_props.load(new FileInputStream(iotconfig));
		String kafkaHost = iot_props.getProperty("KAFKA_IP");
		System.out.println("Kafka ip: " + kafkaHost);
		String discriminator = iot_props.getProperty("DISCRIMINATOR");
		System.out.println("Discriminator: " + discriminator);
		String kafkaPort = iot_props.getProperty("KAFKA_PORT");
		System.out.println("Kafka port: " + kafkaPort);
		IotKafkaMongoTopology top = new IotKafkaMongoTopology(kafkaHost, 0, discriminator);
		System.out.println("Created topology.");
		Config conf = new Config();
		conf.put(Config.TOPOLOGY_TRIDENT_BATCH_EMIT_INTERVAL_MILLIS, 2000);
		StormTopology stormTopology = top.buildTopology(iot_props.getProperty("MONGODB_STR"), discriminator, kafkaHost, systemName);
		conf.setNumWorkers(2);
		conf.setMaxTaskParallelism(2);
		LocalCluster cluster = new LocalCluster();
		System.out.println("Created cluster.");
		cluster.submitTopology("kafka", conf, stormTopology);
		System.out.println("Running topology in cluster");
	}
}
