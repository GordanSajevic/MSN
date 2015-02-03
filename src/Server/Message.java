package Server;

import java.util.LinkedList;
import java.util.Queue;

public class Message {
	private String content;
	private String sender;
	private static Queue<Message> msgQueue = new LinkedList<Message>();
	
	//KONSTRUKTOR
	public Message(String content, String sender){
		this.content = content;
		this.sender = sender;
		msgQueue.add(this);
	}
	
	//metoda koja provjerava da li je queue prazan
	public static boolean hasNext(){
		return !msgQueue.isEmpty();
	}
	//metoda koja vraca i uklanja
	public static Message next(){
		return msgQueue.poll();
	}
}
