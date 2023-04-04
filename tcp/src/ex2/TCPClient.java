package ex2;

/**
 * TCPClient: Cliente para conexao TCP
 * Descricao: Envia uma informacao ao servidor e recebe confirmações ECHO
 * Ao enviar "PARAR", a conexão é finalizada.
 */

import java.net.*;
import java.nio.ByteBuffer;
import java.io.*;
import java.util.Scanner;




public class TCPClient {
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_RESET = "\u001B[0m";
	public static String user = "user";

	
	public static ByteBuffer createHeader(Integer messageType, Integer commandId, Integer sizeFile, String filename) {
        ByteBuffer header = ByteBuffer.allocate(259); // tamanho do cabeçalho de solicitação
        // header.order(ByteOrder.LITTLE_ENDIAN);



        
        header.put(0, messageType.byteValue());
        header.put(1, commandId.byteValue());
        header.put(2, sizeFile.byteValue());
        header.put(3, filename.getBytes());
        
        return header;
	}
	
	public static void main (String args[]) {
	    Socket clientSocket = null; // socket do cliente
            
            
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
                Scanner reader = new Scanner(System.in); // ler mensagens via teclado
                String buffer = "";
                
                ByteBuffer header = createHeader(1, 2, 40, "Jhonatan");

                Byte value0 = header.get(0);            
                Byte value1 = header.get(1);
                Byte value2 = header.get(2);
                while (true) {
                    System.out.print(ANSI_GREEN+user+"$ "+ANSI_RESET);
                    buffer = reader.nextLine(); // lê mensagem via teclado
                    
                    out.writeUTF(buffer);      	// envia a mensagem para o servidor
                    
                    if (buffer.equals("PARAR")) break;
                    
                    buffer = in.readUTF();      // aguarda resposta do servidor
                    
                    boolean isNumber = buffer.matches("[0-9]+");
                    
                    // de acordo com a especificação da atividade alguns comandos retornam a quantidade de mensagens que irao enviar em seguida
                    if(isNumber) {
                    	int amountOfMessages = Integer.parseInt(buffer);
                    	while(amountOfMessages > 0) {
                    		buffer = in.readUTF();
                    		System.out.println(buffer);
                    		amountOfMessages--;
                    	}
                    	
                    }else {
                    	System.out.println(buffer);                    	
                    }    
                } 

	    }catch (Exception ue){
            System.out.println("Socket:" + ue.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ioe) {
                System.out.println("IO: " + ioe);;
            }
        }
     } //main
} //class

