package Client;

import org.json.simple.JSONObject;

public class ChatMessage {
	
	private JSONObject obj = new JSONObject();
	
	ChatMessage(String msgType, String message){
		obj.put("message", message);
		obj.put("msgType", msgType);
		obj.put("time", System.currentTimeMillis());
	}
	
	public String getMsgType(){
		return (String) obj.get("msgType");
	}
	public String getMessage(){
		return (String) obj.get("message");
	}
	public String getTime(){
		return (String) obj.get("time");
	}

}
