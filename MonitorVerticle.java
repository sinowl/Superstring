
import java.util.ArrayList;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.lang.String;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MonitorVerticle extends Verticle {
	ArrayList<String> macidaddressBuffer;
	ArrayList<String> beaconidaddressBuffer;
	String verticleAddress;
	EventBus eb;
	BufferVerticle bv;
	
	public void putStream(String line){
		String key;
		key = line.substring(23,24);
		if (key.equals("m"))
			this.macidaddressBuffer.add(line);
		if (key.equals("b"))
			this.beaconidaddressBuffer.add(line);
	}
	public void display_address(){
		int i=0;
		int j=0;
		for (String item : macidaddressBuffer)
		{
			i++;
			System.out.println("Verticle MAC Address" + "[" + i + "] : " + item);
		}
		for (String item : beaconidaddressBuffer)
		{
			j++;
			System.out.println("Verticle Beacon Address" + "[" + j + "] : " + item);
		}
	}
	public void sendAddressToFilter(String address){
		eb = vertx.eventBus();
		eb.send("filterVerticle.address:AddressHandler", address);
	}
	
	public void spareBufferVerticle(){
		
	}
	
	public void printUsage() {
		OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
		System.out.println(verticleAddress);
			for (Method method : operatingSystemMXBean.getClass().getDeclaredMethods()) {
				method.setAccessible(true);
				if (method.getName().startsWith("get") && Modifier.isPublic(method.getModifiers())) {
							Object value;
							try {
								value = method.invoke(operatingSystemMXBean);
							} catch (Exception e) {
								value = e;
							} // try
							System.out.println(method.getName() + " = " + value);
				} // if
	  } // for
	}
	
	public void addBufferVerticle( JsonObject input ){
		eb = vertx.eventBus();
		String address = input.getString("address");
		int num = input.getInteger("num");
		if (num == 10){
			eb.send("spareBufferVerticle.addition", address);
		}
	}
	
	public void start(){
		macidaddressBuffer = new ArrayList<String>();
		beaconidaddressBuffer = new ArrayList<String>();
		eb = vertx.eventBus();
		JsonObject verticleConfig;
		
		AddressBufferHandler myaddressbufferhandler = new AddressBufferHandler();
		StatusBufferHandler mystatusbufferhandler = new StatusBufferHandler();
		
		verticleConfig = container.config();
		verticleAddress = verticleConfig.getString("address");
		System.out.println("Succeed in deploying MonitorVerticle.java");
		eb.registerHandler("MonitorVerticle.address", myaddressbufferhandler);
		eb.registerHandler("MonitorVerticle.status", mystatusbufferhandler);
	}
	public class AddressBufferHandler implements Handler<Message<String>>{
		public void handle(Message<String> message){
			System.out.println("I received a Verticle Address : " + message.body() + " in MonitorVerticle");
			putStream(message.body());
			display_address();
			sendAddressToFilter(message.body());
			//printUsage();
		}
	}
	public class StatusBufferHandler implements Handler<Message<JsonObject>>{
		public void handle(Message<JsonObject> message){
			System.out.println("I received a BufferVerticle status : " + message.body() + " in MonitorVerticle");
			addBufferVerticle( message.body() );
		}
	}
}
