package ca.polymtl.inf4410.tp1.server;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

import ca.polymtl.inf4410.tp1.shared.ServerInterface;

public class Server implements ServerInterface {

	public static void main(String[] args) throws Exception{
		Server server = new Server();
		server.run();
	}

	public Server() {
		super();
	}

	private void run() throws Exception {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {
			ServerInterface stub = (ServerInterface) UnicastRemoteObject
					.exportObject(this, 0);

			Registry registry = LocateRegistry.getRegistry()
			registry.rebind("server", stub);
			System.out.println("Server ready.");
		} catch (ConnectException e) {
			System.err
					.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}
	
	
	public int create(String nom)
	{
		return 0;
	}
	
	public List<String> list()
	{
		return new LinkedList();
	}
	
	public RemoteFile sync(String nom, int sommeDeControle)
	{
		return new RemoteFile(0,new byte[4]);
	}
	
	public int push(String nom, byte[] contenu, int sommeDeControle)
	{
		return 0;
	}
	
	public class RemoteFile
	{
		private int checksum_;
		private byte[] file_;
		
		public remoteFile(int checksum, byte[] file)
		{
			checksum_ = checksum;
			file_ = file;
		}
	}
	
	
}
