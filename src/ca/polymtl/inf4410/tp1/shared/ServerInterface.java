package ca.polymtl.inf4410.tp1.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.List;
import java.io.Serializable;

public interface ServerInterface extends Remote 
{
	int execute(int a, int b) throws RemoteException;
	int execute(byte[] arg) throws RemoteException;
	int create(String nom) throws RemoteException;
	List<String> list() throws RemoteException;
	RemoteFile sync(String nom, long sommeDeControle) throws RemoteException;
	List<Long> push(String nom, byte[] contenu, long sommeDeControle) throws RemoteException;
	
	public class RemoteFile implements Serializable 
	{
		public long checksum_;
		public byte[] file_;
		
		public RemoteFile(long checksum, byte[] file)
		{
			checksum_ = checksum;
			file_ = file;
		}
	}
}


