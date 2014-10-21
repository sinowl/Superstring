import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.lang.String;
import java.util.ArrayList;


public class BufferVerticle extends Verticle {
	ArrayList<JsonObject> buff;
	JsonObject verticleConfig;
	String verticleAddress,verticleKey;
	
	public void putStream(JsonObject line){
		//line.removeField("address");
		this.buff.add(line);
	}
	
	public void display_buf(){
//		int i=0;
//		for (JsonObject item : buff)
//		{
//			i++;
//			System.out.println(verticleKey+" BufferData"+"["+i+"] : "+item.getString("macid")+" "+item.getString("beaconid")+" "+item.getString("distance")+" "+item.getString("time"));
//		}
		System.out.println(verticleAddress + ": " + buff.size() );
	}
	public void sendTomonitor(JsonObject input){
		EventBus eb = vertx.eventBus();
		int addBuffersize = 10;
		if (buff.size() == addBuffersize){
			input.putValue("num", addBuffersize );
			eb.send("MonitorVerticle.status", input);
		}
	}
	
	public ArrayList<JsonObject> getBuffer(){
		return this.buff;
	}
	public String getVerticleAddress(){
		return this.verticleAddress;
	}
	public void start(){
		buff = new ArrayList<JsonObject>();
		EventBus eb = vertx.eventBus();
		
		verticleConfig = container.config();
		verticleAddress = verticleConfig.getString("address");
		verticleKey = verticleConfig.getString("key");
		
		System.out.println("Succeed in deploying BufferVerticle.java");
		eb.registerHandler(verticleAddress, new MessageBufferHandler());
		
		eb.send("MonitorVerticle.address", verticleAddress);
	}
	public class MessageBufferHandler implements Handler<Message<JsonObject>>{
		public void handle(Message<JsonObject> message){
			JsonObject inputJson = message.body();
			putStream(inputJson);
			display_buf();
			sendTomonitor(inputJson);
		}
	}
}
