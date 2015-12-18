import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import org.json.simple.JSONObject;

public class UDPServer {
	
	// Variables
	//private DatagramSocket ds;
	private static int port = 8767;
	private int identifier;
	private InetAddress addresse;
	private UDPListener uListen;
	private UserInput uInput;
	private String bootstrap;
	private ArrayList <node> routingTable;
	private boolean boot = false;
	
	public UDPServer (String nodeName){
		/* try {
			 this.addresse = InetAddress.getByName(InetAddress.getLocalHost().getHostName());
			 //this.ds = new DatagramSocket(port, addresse);
		} catch (UnknownHostException e) {
			System.err.println("No available NIC.");
			e.printStackTrace();
		} */
		 this.uListen = new UDPListener(port);
		 this.uInput = new UserInput(uListen);
		 this.identifier = hashingCode(nodeName);
		 this.bootstrap = null;
		 this.routingTable = new ArrayList <node> ();
	}
	
	public int hashingCode(String str){
		int hash = 0;
		for (int i = 0; i < str.length(); i++){
			hash = hash * 31 + str.charAt(i);
		}
		return Math.abs(hash);
	}
	/**
	 * 
	 * @param i 0 to add a new routing entry, 1 to remove one. int
	 * @param nodeIP IP address of the node. String
	 * @param nodeID ID of the node. int
	 */
	public void updateRoutingTable (int i, String nodeIP, int nodeID){
		node temp = new node(nodeIP, nodeID);
		if (i == 0){
			this.routingTable.add(temp);
		}
		else if (i == 1){
			Iterator <node> it =  this.routingTable.iterator();
			while (it.hasNext()){
				if((temp = it.next()).getID() == nodeID){
					this.routingTable.remove(temp);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
			if (args.length != 2 || args.length != 4){
				throw new Exception ("Wrong number of arguments.");
			}
			if (args.length == 2){
				if (Integer.parseInt(args[1]) > (2^32)){
					throw new Exception ("Wrong ID : "+args[1]);
				}
				UDPServer server = new UDPServer(args[1]);
				server.setBoot(true);
				server.ustart();
			}
			if (args.length == 4){
				UDPServer server = new UDPServer(args[3]);
				server.setBootstrap(args[1]);
				server.
				server.ustart();
			}
			// May add test on IP address value
		}
		catch (Exception e){
			System.err.println(e.getMessage());
		}
	}
	
	public void ustart(){
		uListen.start();
		uInput.start();
		while(uListen.getStatus() != 4);
		UDPSender uSend = new UDPSender(getPort(), "MSG");
		uSend.start();
	}
	
	private class node {
		String adress;
		int id;
		
		public node (String adr, int id){
			this.adress = adr;
			this.id = id;
		}
		
		public int getID (){
			return this.id;
		}
	}
	
	private class UDPListener extends Thread {
		int port;
		byte[] rcvbfr;
		int status = 0;
		boolean run = true;
		
		public UDPListener (int port) {
			this.port = port;
			this.rcvbfr = new byte[1500];
			setStatus(1);
		}
		
		public synchronized void setStatus (int status){
			this.status = status;
		}
		
		public synchronized int getStatus (){
			return this.status;
		}
		
		public synchronized boolean getRun (){
			return this.run;
		}
		
		public synchronized void setRun (boolean run){
			this.run = run;
		}
		
		public void run(){
			setStatus(2);
			try{
				DatagramSocket ds = new DatagramSocket(port);
				ds.setSoTimeout(30000);
				System.out.println("Server running on : "+ds.getLocalAddress());
				setStatus(3);
				while (getRun()){
					setStatus(4);
					try{
						DatagramPacket rcvPkt = new DatagramPacket(rcvbfr, rcvbfr.length);
						ds.receive(rcvPkt);
						String msg = new String (rcvPkt.getData());
						System.out.println("From client : "+msg);
					}
					catch (IOException ioe){
						
					}
				}
			}catch (SocketException e) {
				System.err.println("Impossible to create DatagramSocket on : "+addresse.toString());
				e.printStackTrace();
			}
		}
	}
	
	private class UDPSender extends Thread {
		//private byte[] sndbfr;
		int port;
		String msg;
		
		public UDPSender(int port, String msg){
			this.port = port;
			this.msg = msg;
			//this.sndbfr = new byte[1500];
		}
		
		public void run(){
			try {
				DatagramSocket cds = new DatagramSocket();
				InetAddress adresse = InetAddress.getByName(InetAddress.getLocalHost().getHostName());
				DatagramPacket sndPckt =new DatagramPacket(msg.getBytes(), msg.getBytes().length, adresse, port);
				System.out.println("msg length : "+msg.getBytes().length);
				System.out.println("msg bytes : "+msg.getBytes());
				cds.send(sndPckt);
				cds.close();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}
	
	private class UserInput extends Thread {
		UDPListener uListen;
		boolean uiRun= true;
		
		public UserInput (UDPListener uListen){
			this.uListen = uListen;
		}
		
		public void run(){
			Scanner scan = new Scanner(System.in);
			while (getUiRun()){
				String msg = scan.nextLine();
				if(msg.equalsIgnoreCase("exit")){
					uListen.setRun(false);
					scan.close();
					setUiRun(false);
				}
				else{
					UDPSender sndr = new UDPSender(port, msg);
					sndr.start();
				}
			}
		}
		
		public boolean getUiRun(){
			return this.uiRun;
		}
		
		public void setUiRun(boolean uiRun){
			this.uiRun = uiRun;
		}
	}

	public static int getPort() {
		return port;
	}

	public String getBootstrap() {
		return bootstrap;
	}

	public void setBootstrap(String bootstrap) {
		this.bootstrap = bootstrap;
	}

	public boolean isBoot() {
		return boot;
	}

	public void setBoot(boolean boot) {
		this.boot = boot;
	}
}
