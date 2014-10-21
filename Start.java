
import org.vertx.java.platform.*;

public class Start extends Verticle {

	public void start(){
		
		container.deployVerticle("Server.java");
		container.deployVerticle("WebVerticle.java");
		
	}
	
}
