import java.util.ArrayList;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.platform.Verticle;

public class WebVerticle extends Verticle{
	EventBus eb;
	ArrayList<String> addressBuffer;
	int numAddress;
	
	public void putAddress(String input){
		this.addressBuffer.add(input);
	}
	
	public void start(){
		eb = vertx.eventBus();
		addressBuffer = new ArrayList<String>();
		HttpServer server = vertx.createHttpServer();
		
		AddressWebHandler myaddresswebhandler = new AddressWebHandler(); 
		
		eb.registerHandler("webVerticle.address", myaddresswebhandler);
		
		server.requestHandler(new Handler<HttpServerRequest>(){
			public void handle(HttpServerRequest req){
				System.out.println("A request has arrived on the server!");
				numAddress = addressBuffer.size();
				for(int i=0;i<numAddress-1;i++){
					req.response().setChunked(true).write(addressBuffer.get(i));
					req.response().setChunked(true).write("\n");
				}
				req.response().setChunked(true).write(addressBuffer.get(numAddress-1)).end();
			}
		});
		System.out.println("Succeed in deploying WebVerticle.java");
		server.listen(8080, "localhost");
	}

	public class AddressWebHandler implements Handler<Message<String>>{
		public void handle(Message<String> message){
			System.out.println("I received address : " + message.body() + " in WebVerticle");
			putAddress(message.body());
		}
	}
}