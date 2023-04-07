package ex2;

/**
 * TCPClient: Cliente para conexao TCP
 * Descricao: Envia uma informacao ao servidor e recebe confirmações ECHO
 * Ao enviar "PARAR", a conexão é finalizada.
 */

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.*;
import java.util.Scanner;




public class TCPClient {
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_RESET = "\u001B[0m";
	public static String user = "user";

	
	public static ByteBuffer createHeader(byte messageType, byte commandId, byte filenameSize, String filename) {
        ByteBuffer header = ByteBuffer.allocate(259); // tamanho do cabeçalho de solicitação
        header.order(ByteOrder.BIG_ENDIAN);

        header.put(0, messageType);
        header.put(1, commandId);
        header.put(2, filenameSize);
        header.position(3);
        header.put(filename.getBytes());
        
        return header;
	}

    public static void createFile(String filename, String content){

    }
	
	public static void main (String args[]) {
	    Socket clientSocket = null; // socket do cliente
        Scanner reader = null;
            
            try{
                /* Endereço e porta do servidor */
                int serverPort = 6666;   
                InetAddress serverAddr = InetAddress.getByName("127.0.0.1");
                
                /* conecta com o servidor */  
                clientSocket = new Socket(serverAddr, serverPort);  
                
                /* cria objetos de leitura e escrita */
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            
                /* protocolo de comunicação */
                String buffer = "";
                byte[] bytes = null;
                reader = new Scanner(System.in); // ler mensagens via teclado
                
                loop:while (true) {
                    System.out.print(ANSI_GREEN+user+"$ "+ANSI_RESET);
                    buffer = reader.nextLine(); // lê mensagem via teclado
                    String[] argCommand = buffer.split(" ");
                    
                    
                    String command = argCommand[0];
                    String filename = "";
                    byte messageType = 1;
                    byte commandId = 0;

                    switch(command){
                        case "PARAR":
                            break loop;
                        case "ADDFILE":
                            filename = argCommand[1];
                            commandId = 1;                            
                            break;
                        case "DELETE":
                            filename = argCommand[1];    
                            commandId = 2;
                            break;
                        case "GETFILELIST":
                            commandId = 3;
                            break;
                        case "GETFILE":
                            commandId = 4;
                            break;
                    }

                    byte filenameSize = (byte) filename.length();
                    ByteBuffer header = createHeader(messageType, commandId, filenameSize, filename);

                    bytes = header.array();
                    int size = header.limit();
                    out.write(bytes, 0, size);      	// envia o header para o servidor

                    

                    // ***********************************
                    // Aguardando resposta do servidor
                    // ***********************************
                    in.read(bytes);    
                    
                    ByteBuffer responseHeader = ByteBuffer.wrap(bytes);
                    responseHeader.order(ByteOrder.BIG_ENDIAN);
                    byte responseMessageType = responseHeader.get(0);
                    byte responseCommandId = responseHeader.get(1);
                    byte responseStatusCode = responseHeader.get(2);

                    System.out.println("responseMessageType: "+ responseMessageType
                    +" responseCommandId: "+responseCommandId
                    +" responseStatusCode: "+responseStatusCode);          


                    // boolean isNumber = buffer.matches("[0-9]+");
                    
                    // de acordo com a especificação da atividade alguns comandos retornam a quantidade de mensagens que irao enviar em seguida
                    // if(isNumber) {
                    // 	int amountOfMessages = Integer.parseInt(buffer);
                    // 	while(amountOfMessages > 0) {
                    // 		buffer = in.readUTF();
                    // 		System.out.println(buffer);
                    // 		amountOfMessages--;
                    // 	}
                    	
                    // }else {
                    // 	System.out.println(buffer);                    	
                    // }    
                } 

	    }catch (Exception ue){
            System.out.println("Socket:" + ue.getMessage());
        } finally {
            reader.close();
            try {
                clientSocket.close();
            } catch (IOException ioe) {
                System.out.println("IO: " + ioe);;
            }
        }
     }
}

