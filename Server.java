
import org.vertx.java.platform.*;

public class Server extends Verticle{
	
	public void start() {
		System.out.println("Succeed in deploying Server.java");
		container.deployVerticle("TcpServer.java");
		container.deployVerticle("MonitorVerticle.java");
	}
}
