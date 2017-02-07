package chat;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;

public class SocketServer {
	
	public static void main(String[] args) {
		
		Configuration config = new Configuration();
		config.setHostname("localhost");
		config.setPort(9092);
		
		SocketIOServer server = new SocketIOServer(config); 
		CharteventListener listner = new CharteventListener();
		listner.setServer(server);
		server.addEventListener("chatevent", ChatObject.class, listner);
		server.start();
	}
}
