
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import org.vertx.java.platform.Verticle;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;

public class TcpServer extends Verticle{
		
		public Socket client;
		public ServerSocket server_socket;
		public int get_port;
		public final static int port_num=8000;
		
		public void socket_connect() throws IOException {
		server_socket = new ServerSocket(port_num);
		String inLine;
		EventBus eb = vertx.eventBus();
		
		boolean init = true;
		while(init){
			System.out.println("Server Start with port : "+ port_num );
			try {
				client = server_socket.accept();
			} catch (IOException e){
				System.out.println("Error accept");
				init = !init;
			}
			get_port = client.getLocalPort();
			System.out.println("Client is connected with port : " + get_port);
			InputStreamReader in = new InputStreamReader( client.getInputStream() );
			BufferedReader is = new BufferedReader(in);
			loops:
			try{
			while(true){
				inLine  = is.readLine();
				if (inLine.equals("exit"))
				{
					break;
				}
				if ( inLine.length() > 0 ){
					JsonObject inputJson = new JsonObject(inLine);
					Calendar calendar = Calendar.getInstance();
					java.util.Date date = calendar.getTime();
					String timeOfnow = (new SimpleDateFormat("yyyyMMddHHmmss").format(date));
					inputJson.putString("time", timeOfnow);
					System.out.println( "[ " + get_port +" ] "+ inputJson);
					eb.send("filterVerticle.address:MessageGetterHandler", inputJson);	
				}
			}
			} catch ( NullPointerException e){
				System.out.println("disconnected...");
				break loops;
			}
			client.close();
		}
	}
		
	public void start() {
		
			System.out.println("succeed in deploying TcpServer.java");
						
			try {
				container.deployVerticle("FilterVerticle.java");
				this.socket_connect();
			} catch (Exception e){ 
				e.printStackTrace();
				System.out.println("Error connect");
				System.exit(1);
			}			
	}
}
