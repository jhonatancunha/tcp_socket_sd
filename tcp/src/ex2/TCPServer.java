package ex2;

/**
 * TCPServer: Servidor para conexao TCP com Threads Descricao: Recebe uma
 * conexao, cria uma thread, recebe uma mensagem e finaliza a conexao
 */
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.*;

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
			} // while

		} catch (IOException e) {
			System.out.println("Listen socket:" + e.getMessage());
		} // catch
	} // main
} // class

/**
 * Classe ClientThread: Thread responsavel pela comunicacao
 * Descricao: Recebe um socket, cria os objetos de leitura e escrita,
 * aguarda msgs clientes e responde com a msg + :OK
 */
class ClientThread extends Thread {
	private static final byte SUCCESS = 1;
	private static final byte ERROR = 2;

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

			File theDir = new File("./src/ex2/jhonatan");
			if (!theDir.exists()) {
				theDir.mkdirs();
			}

			// aqui devemos concatenar o nome da pasta do usuario
			this.currentPath = System.getProperty("user.dir") + "/src/ex2/jhonatan";
		} catch (IOException ioe) {
			System.out.println("Connection:" + ioe.getMessage());
		} // catch
	} // construtor

	public ByteBuffer createResponseHeader(byte messageType, byte commandId, byte statusCode) {
		ByteBuffer header = ByteBuffer.allocate(3);
		header.order(ByteOrder.BIG_ENDIAN);

		header.put(0, messageType);
		header.put(1, commandId);
		header.put(2, statusCode);

		return header;
	}

	public byte addFile(String filename, String content) throws IOException {
		String path = this.currentPath + "/" + filename;
		File file = new File(path);

		if (file.createNewFile()) {
			FileWriter writer = new FileWriter(path, true);
			BufferedWriter buffer = new BufferedWriter(writer);
			buffer.write(content);
			buffer.flush();
			buffer.close();
			return SUCCESS;
		} else {
			return ERROR;
		}
	}

	public byte removeFile(String filename) {
		String path = this.currentPath + "/" + filename;
		File file = new File(path);

		if (file.exists()) {
			if (file.delete()) {
				return SUCCESS;
			}
		}

		return ERROR;
	}

	public List<String> getFilesList(String directory) {
		File dir = new File(directory);
		File[] arrayFiles = dir.listFiles();

		// create a new ArrayList
		List<String> fileList = new ArrayList<String>();

		for (File f : arrayFiles) {
			if (f.isFile()) {
				fileList.add(f.getName());
			}
		}

		return fileList;
	}

	public byte[] getFile(String filename) {
		byte[] response = null;
		try {
			String path = this.currentPath + "/" + filename;
			File file = new File(path);

			FileInputStream inputStream = new FileInputStream(file);
			response = inputStream.readAllBytes();
			inputStream.close();

			return response;
		} catch (Exception e) {
			return response;
		}
	}

	/* metodo executado ao iniciar a thread - start() */
	@Override
	public void run() {
		try {

			System.out.println("args: " + this.currentPath);

			int headerSize = 259;

			while (true) {
				byte[] bytes = new byte[headerSize];
				this.in.read(bytes);
				ByteBuffer header = ByteBuffer.wrap(bytes);
				header.order(ByteOrder.BIG_ENDIAN);

				// ***********************************
				// Obtendo cabeçalho de solicitação
				// ***********************************
				byte messageType = header.get(0);
				byte commandId = header.get(1);
				byte filenameSize = header.get(2);
				byte[] byteFilename = Arrays.copyOfRange(bytes, 3, filenameSize + 3);
				String filename = new String(byteFilename);

				byte statusCode = ERROR;
				byte[] responseContent = null;
				List<String> getFilesListRespondeContent = null;

				switch (commandId) {
					case 1: // ADDFILE
						statusCode = addFile(filename, "conteudo");
						break;
					case 2: // DELETE
						statusCode = removeFile(filename);
						break;
					case 3: // GETFILESLIST
						getFilesListRespondeContent = getFilesList(this.currentPath);
						break;
					case 4: // GETFILE
						responseContent = getFile(filename);
						break;
				}

				// ***********************************************
				// Enviado cabeçalho de resposta com tamanho fixo
				// ***********************************************
				byte responseCode = 2;
				ByteBuffer buffer = this.createResponseHeader(responseCode, commandId, statusCode);
				bytes = buffer.array();
				int size = buffer.limit();
				out.write(bytes, 0, size);
				out.flush();
				// ***********************************************
				// Enviado conteudos do arquivos
				// ***********************************************

				switch (commandId) {
					case 3: // GETFILESLIST
						int listOfFilesSize;
						if (getFilesListRespondeContent == null)
							listOfFilesSize = 0;
						else
							listOfFilesSize = getFilesListRespondeContent.size();

						buffer = ByteBuffer.allocate(4);
						buffer.order(ByteOrder.BIG_ENDIAN);
						buffer.putInt(listOfFilesSize);
						bytes = buffer.array();
						size = buffer.limit();
						out.write(bytes, 0, size);
						out.flush();

						for (String fileName : getFilesListRespondeContent) {
							byte[] filenameInBytes = fileName.getBytes();
							int filenameLength = fileName.length();

							out.write((byte) filenameLength);

							for (int i = 0; i < filenameLength; i++) {
								System.out.println("Enviou byte: " + filenameInBytes[i]);
								out.write(filenameInBytes[i]);
								out.flush();
							}
						}

						break;
					case 4: // GETFILE
						int sizeResponseContent;
						if (responseContent == null)
							sizeResponseContent = 0;
						else
							sizeResponseContent = responseContent.length;

						buffer = ByteBuffer.allocate(4);
						buffer.order(ByteOrder.BIG_ENDIAN);
						buffer.putInt(sizeResponseContent);
						bytes = buffer.array();
						size = buffer.limit();
						out.write(bytes, 0, size);
						out.flush();

						for (int i = 0; i < sizeResponseContent; i++) {
							System.out.println("Enviou byte: " + responseContent[i]);
							out.write(responseContent[i]);
							out.flush();
						}

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
	}
}