package iot_framework_big_data.kafka_storm_mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.util.Iterator;

import org.bson.Document;
import org.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Hello world!
 *
 */
public class App 
{
	public static final Logger LOG = LoggerFactory.getLogger(IotKafkaMongoTopology.class);
    public static void main( String[] args )
    {
    	String json = "{\"message\": {\"geotag\":{\"lat\":\"44.985351987\",\"lon\":\"-93.250846023\",\"time\":\"2016-05-10 17:52:40+00:00\",\"alt\":\"288.268\",\"cnt\":\"11\"},\"payload\":{\"fahrenheit\":\"76.4366\",\"celsius\":\"24.687\",\"kelvin\":\"297.837\"}}}";
    	Document doc = Document.parse(json);
    	LOG.info("json: " + doc.toJson());
    	JSONObject message = new JSONObject(json).getJSONObject("message");
    	JSONObject geotag = message.getJSONObject("geotag");
    	JSONObject payload = message.getJSONObject("payload");
    	LOG.info("message: " + message.toString());
    	LOG.info("geotag: " + geotag.toString());
    	LOG.info("payload: " + payload.toString());
    	Document dbMessage = new Document(); 
    	Document dbGeotag = new Document();
    	Document dbPayload = new Document();
    	Iterator<String> geotagIter = geotag.keys();
    	while (geotagIter.hasNext()){
    		String key = geotagIter.next();
    		dbGeotag.put(key, geotag.get(key));
    	}
    	Iterator<String> payloadIter = payload.keys();
    	while (payloadIter.hasNext()){
    		String key = payloadIter.next();
    		dbPayload.put(key, payload.get(key));
    	}
    	dbMessage.put("geotag", dbGeotag);
    	dbMessage.put("payload", dbPayload);
    	
    	
    	
    }
}
