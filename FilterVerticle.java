import java.lang.String;
import java.util.ArrayList;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.*;

public class FilterVerticle extends Verticle {
	
	ArrayList<String> addressBuffer;
	ArrayList<String> additionaddressBuffer;
	static String filterVerticleBuffer;
	private JsonObject inputJson;

	public boolean isVerticle(ArrayList<String> source ,String address){
		for (String item : source){
			if (item.equals(address)){
				return true;
			} 
		}
		return false;
	}
	
	public String isallVerticle(ArrayList<String> source ,String address){
		for (String item : source){
			if (item.equals(address)){
				address = isallVerticle(source, address+"-1");
			} 
		}
		return address;
	}

	public void additionBuffer(String address){
		JsonObject config = new JsonObject();
		config.putString("address", address+"-1");
		container.deployVerticle("BufferVerticle.java",config);
		
		additionaddressBuffer.add(address);
		addressBuffer.remove(address);
		System.out.println(addressBuffer);
		System.out.println(additionaddressBuffer);
		System.out.println("deploying addition BufferVerticle: "+ address );
	}
	
	public void sendToBufferVerticle(String key){
		EventBus eb = vertx.eventBus();
		JsonObject config = new JsonObject();
		String Buffaddress = "bufferVerticle.address:" + key;
		String additionBuffaddressinter = isallVerticle(additionaddressBuffer, Buffaddress);
		String additionBuffaddress = additionBuffaddressinter.substring(0, additionBuffaddressinter.length()-2);
		System.out.println("buf ars: "+ Buffaddress);
		System.out.println("add buf ars: "+additionBuffaddress);
		config.putString("address", Buffaddress);
		config.putString("key", key);
		if (!isVerticle(addressBuffer,Buffaddress) && !isVerticle(additionaddressBuffer, additionBuffaddress)){
			container.deployVerticle("BufferVerticle.java",config, new ReplyHandler(Buffaddress, inputJson, eb));
			System.out.println("deploying BufferVerticle: "+inputJson.getString("macid")+" "+inputJson.getString("beaconid"));
		} else if ( !isVerticle(addressBuffer,Buffaddress) && isVerticle(additionaddressBuffer, additionBuffaddress)){
			inputJson.putValue("address", additionBuffaddress+"-1");
			eb.send( additionBuffaddress+"-1" , inputJson);
		}	else {
			inputJson.putValue("address", Buffaddress);
			eb.send(Buffaddress , inputJson);
		}
	}
	public void filterKey(){
				String inputMacid, inputBeaconid;
				inputMacid = inputJson.getString("macid");
				inputBeaconid = inputJson.getString("beaconid");
				sendToBufferVerticle(inputMacid.substring(0,5));
				sendToBufferVerticle(inputBeaconid.substring(0,8));
	}
public void start(){
		addressBuffer = new ArrayList<String>();
		additionaddressBuffer = new ArrayList<String>();
		System.out.println("succeed in deploying FilterVertcicle.java");
		final EventBus eb = vertx.eventBus();
		eb.registerHandler("filterVerticle.address:MessageGetterHandler", new MessageGetterHandler());
		eb.registerHandler("filterVerticle.address:AddressHandler",new AddressHandler());
		eb.registerHandler("spareBufferVerticle.addition", new AdditionHandler());
		System.out.println("start() method was called at once when this verticle deployed:");
	}

	public JsonObject getMessage(){
		return this.inputJson;
	}
	public void setMessage(JsonObject body){
		this.inputJson = body;
	}
	public class MessageGetterHandler implements Handler<Message<JsonObject>>{
		JsonObject jsonMessage;
		
		public void handle(Message<JsonObject> message){
			jsonMessage = message.body();
			setMessage(jsonMessage);
			filterKey();
		}		
	}
	public class MessageFilterHandler implements Handler<Message<JsonObject>>{
		public void handle(Message<JsonObject> message){
			filterKey();
		}
	}
	public class AddressHandler implements Handler<Message<String>>{
		public void handle(Message<String> message){
			addressBuffer.add(message.body());
		}
	}
	
	public class AdditionHandler implements Handler<Message<String>>{
		public void handle(Message<String> message){
			additionBuffer(message.body());
		}
	}
	
	public class ReplyHandler implements AsyncResultHandler<String>{
		private String address;
		private JsonObject inputJsonObject;
		private EventBus eb;
		
		public ReplyHandler(String address, JsonObject inputJson , EventBus eb){
			this.address = address;
			this.inputJsonObject = inputJson;
			this.eb = eb;
		}
		public void handle(AsyncResult<String> asyncResult){
			if (asyncResult.succeeded()){
				eb.send(address, this.inputJsonObject );
			} else {
				asyncResult.cause().printStackTrace();
			}
		}
	}
}

