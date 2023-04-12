package ex2;

/**
 * Descrição: Servidor para conexao TCP com Threads Descricao: Recebe uma
 * conexao, cria uma thread, recebe uma mensagem e finaliza a conexao.
 * 
 * Autores: Jhonantan Guilherme de Oliveira Cunha, Jessé Pires Barbato Rocha
 * 
 * Data de criação: 17/03/2023
 * Data última atualização: 11/04/2023
 */
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.*;

import java.util.logging.*;

public class TCPServer {

	public static void main(String args[]) {
		try {
			int serverPort = 6666; // porta do servidor

			/* cria um socket e mapeia a porta para aguardar conexao */
			ServerSocket listenSocket = new ServerSocket(serverPort);

			while (true) {
				logger.info("Servidor aguardando conexao ...");
				System.out.println("Servidor aguardando conexao ...");

				/* aguarda conexoes */
				Socket clientSocket = listenSocket.accept();

				logger.info("Cliente conectado ... Criando thread ...");
				System.out.println("Cliente conectado ... Criando thread ...");

				/* cria um thread para atender a conexao */
				ClientThread c = new ClientThread(clientSocket);

				/* inicializa a thread */
				c.start();
			} // while

		} catch (IOException e) {
			logger.info("Listen socket:" + e.getMessage());
			System.out.println("Listen socket:" + e.getMessage());
		} // catch
	} // main
} // class

/**
 * Descrição: Thread responsavel pela comunicacao
 * Descricao: Recebe um socket, cria os objetos de leitura e escrita,
 * aguarda msgs clientes e responde com a msg + :OK
 * 
 * Autores: Jhonantan Guilherme de Oliveira Cunha, Jessé Pires Barbato Rocha
 * 
 * Data de criação: 17/03/2023
 * Data última atualização: 11/04/2023
 */
class ClientThread extends Thread {
	private static final Logger logger = Logger.getLogger("tcp");

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

			FileHandler fileHandler = new FileHandler("./ex2/tcp.log");
			fileHandler.setFormatter(new SimpleFormatter());
			logger.addHandler(fileHandler);

			this.clientSocket = clientSocket;
			in = new DataInputStream(clientSocket.getInputStream());
			out = new DataOutputStream(clientSocket.getOutputStream());

			File theDir = new File("./ex2/jhonatan");
			if (!theDir.exists()) {
				theDir.mkdirs();
			}

			logger.info("Cliente se conectou");

			// aqui devemos concatenar o nome da pasta do usuario
			this.currentPath = System.getProperty("user.dir") + "/ex2/jhonatan";
		} catch (IOException ioe) {
			logger.info("Connection:" + ioe.getMessage());
			System.out.println("Connection:" + ioe.getMessage());
		} // catch
	} // construtor

	public ByteBuffer createResponseHeader(byte messageType, byte commandId, byte statusCode) {
		ByteBuffer header = ByteBuffer.allocate(3);
		header.order(ByteOrder.BIG_ENDIAN);

		header.put(0, messageType);
		header.put(1, commandId);
		header.put(2, statusCode);
		logger.info("ResponseHeader criado");

		return header;
	}

	public byte addFile(String filename, String content) throws IOException {
		String path = this.currentPath + "/" + filename;
		File file = new File(path);

		if (file.createNewFile()) {
			FileWriter writer = new FileWriter(path, true);
			logger.info("Criando arquivo");
			BufferedWriter buffer = new BufferedWriter(writer);
			logger.info("Escrevendo conteúdo no arquivo");
			buffer.write(content);
			buffer.flush();
			buffer.close();
			logger.info("Arquivo criado com sucesso");
			return SUCCESS;
		} else {
			logger.warning("Erro ao criar arquivo");
			return ERROR;
		}
	}

	public byte removeFile(String filename) {
		String path = this.currentPath + "/" + filename;
		File file = new File(path);

		if (file.exists()) {
			if (file.delete()) {
				logger.info("Arquivo deletado com sucesso");
				return SUCCESS;
			}
		}

		logger.warning("Erro ao deletar arquivo");
		return ERROR;
	}

	public List<String> getFilesList(String directory) {
		System.out.println(directory);
		File dir = new File(directory);
		File[] arrayFiles = dir.listFiles();

		// create a new ArrayList
		List<String> fileList = new ArrayList<String>();

		logger.info("Obtendo arquivos do diretório");
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

			logger.info("Obtendo conteúdo do arquivo");
			FileInputStream inputStream = new FileInputStream(file);
			response = inputStream.readAllBytes();
			logger.info("Fechando Stream de dados");
			inputStream.close();

			return response;
		} catch (Exception e) {
			logger.warning("Erro ao obter arquivo: " + e.getMessage());
			return response;
		}
	}

	/* metodo executado ao iniciar a thread - start() */
	@Override
	public void run() {
		try {
			int headerSize = 259;

			while (true) {
				logger.info("Criando e configurando header");
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
				List<String> getFilesListResponseContent = null;

				switch (commandId) {
					case 1: // ADDFILE
						statusCode = addFile(filename, "conteudo");
						break;
					case 2: // DELETE
						statusCode = removeFile(filename);
						break;
					case 3: // GETFILESLIST
						getFilesListResponseContent = getFilesList(this.currentPath);
						break;
					case 4: // GETFILE
						responseContent = getFile(filename);
						break;
				}

				// ***********************************************
				// Enviado cabeçalho de resposta com tamanho fixo
				// ***********************************************
				logger.info("Enviando header de resposta de tamanho fixo");
				byte responseCode = 2;
				ByteBuffer buffer = this.createResponseHeader(responseCode, commandId, statusCode);
				bytes = buffer.array();
				int size = buffer.limit();
				out.write(bytes, 0, size);
				out.flush();
				// ***********************************************
				// Enviado conteudos do arquivos
				// ***********************************************
				logger.info("Enviando conteúdo");
				switch (commandId) {
					case 3: // GETFILESLIST
						logger.info("Iniciando envio da resposta do comando GETFILELIST");
						int listOfFilesSize;
						if (getFilesListResponseContent == null)
							listOfFilesSize = 0;
						else
							listOfFilesSize = getFilesListResponseContent.size();

						logger.info("Criando e adicionando dados no buffer");
						buffer = ByteBuffer.allocate(2);
						buffer.put((byte) ((listOfFilesSize >> 8) & 0xFF)); // INSERINDO BYTE MAIS SIGNIFICATIVO
						buffer.put((byte) (listOfFilesSize & 0xFF)); // INSERINDO BYTE MENOS SIGNIFICATIVO

						bytes = buffer.array();
						size = buffer.limit();

						out.write(bytes, 0, size);
						out.flush();

						for (String fileName : getFilesListResponseContent) {
							byte[] filenameInBytes = fileName.getBytes();
							byte filenameLength = (byte) fileName.length();

							out.write(filenameLength);
							out.flush();

							logger.info("Enviando nomes dos arquivos byte a byte");
							for (int i = 0; i < filenameLength; i++) {
								logger.info("Enviou byte: " + filenameInBytes[i]);
								out.write(filenameInBytes[i]);
								out.flush();
							}
						}

						break;
					case 4: // GETFILE
						logger.info("Iniciando envio da resposta do comando GETFILE");
						int sizeResponseContent;
						if (responseContent == null)
							sizeResponseContent = 0;
						else
							sizeResponseContent = responseContent.length;

						logger.info("Criando e adicionando dados no buffer");
						buffer = ByteBuffer.allocate(4);
						buffer.order(ByteOrder.BIG_ENDIAN);
						buffer.putInt(sizeResponseContent);
						bytes = buffer.array();
						size = buffer.limit();
						out.write(bytes, 0, size);
						out.flush();

						logger.info("Enviando conteúdo do arquivo byte a byte");
						for (int i = 0; i < sizeResponseContent; i++) {
							logger.info("Enviou byte: " + responseContent[i]);
							out.write(responseContent[i]);
							out.flush();
						}

						break;
				}

			}
		} catch (EOFException eofe) {
			logger.info("End Of File Exception: " + eofe.getMessage());
			System.out.println("EOF: " + eofe.getMessage());
		} catch (IOException ioe) {
			logger.info("I/O Exception: " + ioe.getMessage());
			System.out.println("IOE: " + ioe.getMessage());
		} finally {
			try {
				logger.info("Fechando conexão");
				in.close();
				out.close();
				clientSocket.close();
			} catch (IOException ioe) {
				logger.info("I/O Exception ao fechar conexão: " + ioe.getMessage());
				System.err.println("IOE: " + ioe);
			}
		}

		logger.info("Thread comunicação cliente finalizada.");
		System.out.println("Thread comunicação cliente finalizada.");
	}
}