package ca.polymtl.inf4410.tp1.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.List;
import java.io.File;
import java.io.IOException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import ca.polymtl.inf4410.tp1.shared.ServerInterface;
import ca.polymtl.inf4410.tp1.shared.ServerInterface.RemoteFile;

public class Client 
{
	public static void main(String[] args) throws RemoteException 
	{
		String command = null;

		if (args.length > 0) 
		{
			command = args[0];
		}
		else
		{
			System.err.println("Error invalid arguments");
			System.exit(-1);
		}

		//Check if metaData file exists 
		File checksumFile = new File("ChecksumMetaData.xml");
		
		try
		{
			if(!checksumFile.exists())
			{
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				
				// root elements
				Document doc = docBuilder.newDocument();
				Element rootElement = doc.createElement("MetaData");
				doc.appendChild(rootElement);
				
				//write the content into xml file
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(checksumFile);
				
				transformer.transform(source, result);
				
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		catch (ParserConfigurationException pce) 
		{
			pce.printStackTrace();
		} 
		catch (TransformerException tfe) 
		{
			tfe.printStackTrace();
		}

		
		Client client = new Client();
		
		if(command.equals("create"))
		{
			String fileName = null;
			
			if (args.length >= 2)
			{	
				fileName = args[1];
				int errorCode = client.create(fileName);
				
				if(errorCode == 0)
				{
					System.out.println(fileName + " ajoute");
				}
				else
				{
					System.out.println("Le fichier existe deja");
				}
				
			}
			else
			{
				System.err.println("Erreur arguments invalides");
				System.exit(-1);
			}
			
			
		}
		else if(command.equals("list"))
		{
			List<String> list;
			list = client.list();
			
			for(int i=0; i<list.size(); ++i)
			{
				System.out.println(list.get(i));
			}
			
			System.out.println(Integer.toString(list.size()) + " fichiers(s)");
		}
		else if(command.equals("sync"))
		{
			String fileName = null;
			
			if (args.length >= 2)
			{	
				fileName = args[1];	
				
				File file = new File(fileName);
				
				try
				{
					if(file.exists())
					{
						//Sync
						int checkSum;
						
						//Read XML
						DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
						DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
						Document doc = dBuilder.parse(checksumFile);
						
						NodeList nList = doc.getElementsByTagName(fileName);
						Node nNode = nList.item(0);
						
						Element eElement = (Element) nNode;
						checkSum = Integer.valueOf(eElement.getAttribute("CRC32"));
												
						RemoteFile remoteFile = sync(fileName,checkSum);
						
						//Update file and metaData
						try
						{
							//Write data
							FileWriter fw = new FileWriter(file.getAbsoluteFile());
							BufferedWriter bw = new BufferedWriter(fw);
							bw.write(new String(remoteFile.file_));
							bw.close();
							
							//Write new checksum
							eElement.setAttribute("CRC32", Integer.toString(remoteFile.checksum_));
							
							TransformerFactory transformerFactory = TransformerFactory.newInstance();
							Transformer transformer = transformerFactory.newTransformer();
							DOMSource source = new DOMSource(doc);
							StreamResult result = new StreamResult(checksumFile);
							
							transformer.transform(source, result);
							
						}
						catch(IOException e)
						{
							System.out.println("Erreur : " + e.getMessage());
						}
					    catch (ParserConfigurationException pce) 
					    {
					    	pce.printStackTrace();
					    } 
						catch (TransformerException tfe) 
					    {
							tfe.printStackTrace();
					    }
						
								
					}
					else
					{
						RemoteFile remoteFile = sync(fileName,-1);
						
						if(RemoteFile != null)
						{
							//Create file
							file.createNewFile();
							
							//Write data
							FileWriter fw = new FileWriter(file.getAbsoluteFile());
							BufferedWriter bw = new BufferedWriter(fw);
							bw.write(new String(remoteFile.file_));
							bw.close();
							
							//Write
							DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
							DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
							Document doc = dBuilder.parse(checksumFile);
							
							doc.getDocumentElement().normalize();
							Element root = doc.getDocumentElement();
							
							Element fileTag = doc.createElement(fileName);
							root.appendChild(fileTag);
							
							Attr attr = doc.createAttribute("CRC32");
							attr.setValue(Integer.toString(remoteFile.checksum_));
							fileTag.setAttributeNode(attr);
														
							// write the content into xml file
							TransformerFactory transformerFactory = TransformerFactory.newInstance();
							Transformer transformer = transformerFactory.newTransformer();
							DOMSource source = new DOMSource(doc);
							StreamResult result = new StreamResult(checksumFile);
				
							transformer.transform(source, result);
							
						}
						else
						{
							System.out.println("File already up-to-date");
						}
						
				     }
				}
				catch (Exception e) 
				{
					e.printStackTrace();
			    }
				catch(IOException e)
				{
					e.printStackTrace();
				}
				
			}
			else
			{
				System.err.println("Erreur arguments invalides");
				System.exit(-1);
			}
		}
	}

	private ServerInterface serverStub = null;

	public Client() 
	{
		super();

		if (System.getSecurityManager() == null) 
		{
			System.setSecurityManager(new SecurityManager());
		}

		//load server
		serverStub = loadServerStub("127.0.0.1");
	}

	private ServerInterface loadServerStub(String hostname) 
	{
		ServerInterface stub = null;

		try 
		{
			Registry registry = LocateRegistry.getRegistry(hostname);
			stub = (ServerInterface) registry.lookup("server");
		} 
		catch (NotBoundException e) 
		{
			System.out.println("Erreur: Le nom '" + e.getMessage()
					+ "' n'est pas défini dans le registre.");
		} 
		catch (AccessException e) 
		{
			System.out.println("Erreur: " + e.getMessage());
		} 
		catch (RemoteException e) 
		{
			System.out.println("Erreur: " + e.getMessage());
		}

		return stub;
	
	}
	
	private int create(String nom)
	{
		int returnValue=-2;
		
		if(serverStub!=null)
		{
			try
			{
				returnValue = serverStub.create(nom);
			}
			catch(RemoteException e)
			{
				System.out.println("Erreur list : " + e.getMessage());
			}
		}
		
		return returnValue;
	}
	
	private List<String> list() 
	{
		List<String> list=null;
		
		if(serverStub!=null)
		{
			try
			{
				list = serverStub.list();
			}
			catch(RemoteException e)
			{
				System.out.println("Erreur list: " + e.getMessage());
			}
		}
		
		return list;
	}

	private RemoteFile sync(String nom, int sommeDeControle)
	{
		RemoteFile remoteFile=null;
		
		if(serverStub!=null)
		{
			try
			{
				remoteFile = serverStub.sync(nom,sommeDeControle);
			}
			catch(RemoteException e)
			{
				System.out.println("Erreur sync: " + e.getMessage());
			}
		}
		
		return remoteFile;
	}
}
