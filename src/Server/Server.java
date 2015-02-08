package Server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import Client.LogIn;
import GUI.ChatGui;


public class Server {

	public static final int port = 1818;

	public static void serverStart() throws IOException {
		ServerSocket server = new ServerSocket(port);
		ServerGUI sg = new ServerGUI();
		ConnectionWriter cw = new ConnectionWriter();
		cw.start();
		while (true) {
			String str = "waiting for connection";
			System.out.println(str);
			Socket client;
			try {
				client = server.accept();
				String[] array = handShake(client.getInputStream());
				String clientName = array[0];
				String password = array[1];
				password = hashPassword(password);
				if (array[0] != null && array[1]!= null) {
					while (ConnectionWriter.connections.containsKey(clientName)) {
						clientName += new Random().nextInt(1000);
					}
					ConnectionWriter.connections.put(clientName,
							client.getOutputStream());
					ConnectionListener cl = new ConnectionListener(
							client.getInputStream(), clientName);
					cl.start();
					new Message("join%" + clientName, "%server%");
					client.getOutputStream().write(0);
					sg.logConnection(client.getInetAddress().getHostAddress(), clientName);
					
					
				} 
				else if (array[0]== null && array[1]!= null){
					client.getOutputStream().write(-1);
				}
				else if (array[0] != null && array[1]== null)
				{
					client.getOutputStream().write(-2);
				}
				else
				{
					client.getOutputStream().write(-3);
				}
				File file = ChatGui.file;
				if (file != null)
				{
					
					server = new ServerSocket(port);
					client = server.accept();
					byte[] byteArray = new byte[(int)file.length()];
					InputStream in = new FileInputStream(file);
					BufferedInputStream bin = new BufferedInputStream(in);
					bin.read(byteArray, 0, byteArray.length);
					OutputStream out = client.getOutputStream();
					System.out.println("Slanje filea: " + file.getName() + " (" + byteArray.length + " bajtova)");
					out.write(byteArray, 0, byteArray.length);
					int numOfBytes = byteArray.length;
					LogIn.receiveFile(file);
					out.flush();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static String[] handShake(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String str = br.readLine();
		str = str.replaceAll("%", "");
		String password = br.readLine();
		int rezultat = XmlConnection.userLogin(str, password);
		if(rezultat != 0){
			return null;
		}
		String[] array = new String[2];
		array[0] = str;
		array[1] = password;
		return array;
	}

	public static String hashPassword(String password)
	{
		String passwordToHash = "password";
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(passwordToHash.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return generatedPassword;
	}
	
	public static void main(String[] args) {
		try {
			new XmlConnection();
		} catch (ParserConfigurationException | SAXException | IOException e1) {
			e1.printStackTrace();
		}
		try {
			serverStart();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
