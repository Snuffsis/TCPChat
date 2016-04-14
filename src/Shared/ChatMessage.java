package Shared;

import java.io.Serializable;

import org.json.simple.JSONObject;

public class ChatMessage implements Serializable{
	
	private JSONObject obj = new JSONObject();
	
	public ChatMessage(String msgType, String message){
		obj.put("message", message);
		obj.put("msgType", msgType);
		obj.put("time", System.currentTimeMillis());
	}
	public ChatMessage(String msgType, String message, String name){
		obj.put("message", message);
		obj.put("msgType", msgType);
		obj.put("name", name);
		obj.put("time", System.currentTimeMillis());
	}
	public ChatMessage(String message){
		obj.put("message", message);
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
	public String getName(){
		return (String) obj.get("name");
	}

}
