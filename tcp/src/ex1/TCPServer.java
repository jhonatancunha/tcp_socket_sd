package ex1;

/**
 * TCPServer: Servidor para conexao TCP com Threads Descricao: Recebe uma
 * conexao, cria uma thread, recebe uma mensagem e finaliza a conexao
 */
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;

public class TCPServer {

    public static void main(String args[]) {
        try {
            int serverPort = 6666; // porta do servidor

            
            
            /* cria um socket e mapeia a porta para aguardar conexao */
            ServerSocket listenSocket = new ServerSocket(serverPort);

            while (true) {
                System.out.println("Servidor aguardando conexao ...");

                /* aguarda conexoes */
                Socket clientSocket = listenSocket.accept();

                System.out.println("Cliente conectado ... Criando thread ...");

                /* cria um thread para atender a conexao */
                ClientThread c = new ClientThread(clientSocket);

                /* inicializa a thread */
                c.start();
            } //while

        } catch (IOException e) {
            System.out.println("Listen socket:" + e.getMessage());
        } //catch
    } //main
} //class

/**
 * Classe ClientThread: Thread responsavel pela comunicacao
 * Descricao: Rebebe um socket, cria os objetos de leitura e escrita,
 * aguarda msgs clientes e responde com a msg + :OK
 */
class ClientThread extends Thread {

    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;
    
    String currentPath;

    Auth authenticator = new Auth();
    public boolean isConnected = false;
    
    public ClientThread(Socket clientSocket) {
        try {
            this.clientSocket = clientSocket;
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            
            File theDir = new File("./src/ex1/jhonatan");
        	if (!theDir.exists()){
        	    theDir.mkdirs();
        	}
            
        	// aqui devemos concatenar o nome da pasta do usuario
            this.currentPath = System.getProperty("user.dir")+"/src/ex1/jhonatan";
        } catch (IOException ioe) {
            System.out.println("Connection:" + ioe.getMessage());
        } //catch
    } //construtor
    
    
    private void pwdCommand() {

    	try {
			this.out.writeUTF(this.currentPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    
    private void lsCommand() {
    	try {
	          File directory = new File(this.currentPath);
	          File[] arrayFiles = directory.listFiles();
	          
	          // create a new ArrayList
	          List<String> fileList= new ArrayList<String>();
	          
			  for (File f : arrayFiles) {
			          if (f.isFile()) {
			        	  fileList.add(f.getName());
			          }
			  }

			  Integer numberOfFiles = fileList.size();
		      this.out.writeUTF(numberOfFiles.toString());
				
			  for (String fileItem : fileList) {
				  System.out.println(fileItem);
				  this.out.writeUTF(fileItem);
			  }			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void getDirs() {
    	try {
	          File directory = new File(this.currentPath);
	          File[] arrayFiles = directory.listFiles();
	          
	          // create a new ArrayList
	          List<String> fileList= new ArrayList<String>();
	          
			  for (File f : arrayFiles) {
			          if (f.isDirectory()) {
			        	  fileList.add(f.getName());
			          }
			  }

			  Integer numberOfFiles = fileList.size();
		      this.out.writeUTF(numberOfFiles.toString());
				
			  for (String fileItem : fileList) {
				  System.out.println(fileItem);
				  this.out.writeUTF(fileItem);
			  }			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	 
    }
    
    private void cdCommand(String args) {
    	try {
    		String[] argsArray = args.split("/");
    		
    		if(argsArray.length == 0) {
    			this.out.writeUTF("ERRO");
    			return;
    		}
    		
    		String[] currentDir = this.currentPath.split("/");
    		List<String> listCurrentDir = new ArrayList<String>(Arrays.asList(currentDir));

    		for (String p : argsArray) {
    			if(!p.equals(".")) {

    				if(p.equals("..")) {
    					listCurrentDir.remove(listCurrentDir.size() - 1);
    				}else {
    					listCurrentDir.add(p);
    				}
    				
    			}
		    } 	
    		

    		String newPwd = String.join("/", listCurrentDir);
    		Path newPath = Paths.get(newPwd);

    		if(Files.isDirectory(newPath)) {
    			this.out.writeUTF("SUCESSO");
    			this.currentPath = newPwd;
    		}else {
    			this.out.writeUTF("ERRO");
    		}
	       
	        
	    				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	 
    }
    
    private void connectCommand(String args) {
    	try {
			String[] argsArray = args.split(",");
			String hashedPassword = authenticator.getHash(argsArray[1]);
			
			boolean isAuthenticated = authenticator.verifyUser(argsArray[0], hashedPassword);
			
			if (isAuthenticated) {				
				this.out.writeUTF("SUCCESS");
				isConnected = true;
			} else {
				this.out.writeUTF("ERROR");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    /* metodo executado ao iniciar a thread - start() */
    @Override
    public void run() {
        try {
        	
        	
        	System.out.println("args: " + this.currentPath);
        	 
            String buffer = "";
            loop: while (true) {
                buffer = in.readUTF();   /* aguarda o envio de dados */

                String[] args = buffer.split(" ", 2);
                System.out.println("args: " + args[0]);
                
                switch(args[0]) {
                	case "CONNECT":
                		this.connectCommand(args[1]);
                		break;
	                case "PWD":
	                	if (isConnected) {	                		
	                		System.out.println("Comando pwd");
	                		this.pwdCommand();
	                	} else {
	                		out.writeUTF("Use o comando CONNECT primeiro");
	                	}
	                	break;
	                case "GETFILES":
	                	if (isConnected) {
	                		
	                		this.lsCommand();
	                	} else {
	                		out.writeUTF("Use o comando CONNECT primeiro");
	                	}
	                	break;
	                case "CHDIR":
	                	if (isConnected) {
	                		// verificar tamanho do args se for menor q 2 voltar erro caso contrario voltar sucesso
	                		this.cdCommand(args[1]);
                		} else {
                			out.writeUTF("Use o comando CONNECT primeiro");
                		}
	                	break;
	                case "GETDIRS":
	                	if (isConnected) {
	                		this.getDirs();
                		} else {
                			out.writeUTF("Use o comando CONNECT primeiro");
                		}
	                	break;
	                case "EXIT":
	                	break loop;
	            	default:
	            		buffer = "Comando não encontrado";
	            		out.writeUTF(buffer);
	            		break;
                }
                
                
                
            }
        } catch (EOFException eofe) {
            System.out.println("EOF: " + eofe.getMessage());
        } catch (IOException ioe) {
            System.out.println("IOE: " + ioe.getMessage());
        } finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException ioe) {
                System.err.println("IOE: " + ioe);
            }
        }
        
        System.out.println("Thread comunicação cliente finalizada.");
    } //run
} //class