import java.io.*;
import java.net.Socket;

public class Connection extends Thread {
    DataInputStream in;
    DataOutput out;
    Socket clientSocket;
    private String clientName;

    public Connection (Socket aClientSocket) {
        try {
            clientSocket = aClientSocket;
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());

            // pede o dado do nome do novo usuario
            this.clientName = in.readUTF();
            System.out.println("Cliente '" + clientName + "' conectado.");

            TCPServer.addConnection(this);

            // Informa a todos os outros usuarios que um novo usuario entrou
            TCPServer.broadcastMessage("SERVER: " + clientName + " entrou no chat.", this);
            this.start();
        } catch (IOException e) {
            System.out.println("Connection creation error: " + e.getMessage());
            try {
                if(clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException ex) {
                System.out.println("Error closing socket on connection error: " + ex.getMessage());
            }
        }
    }

    public String getClientName() {
        return clientName;
    }

    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            System.out.println("Error sending message to " + clientName + ": " + e.getMessage());
            // se nao enviu, provavelmente o usuario desconectou
            TCPServer.removeConnection(this);
            try {
                clientSocket.close();
            } catch (IOException ex) {
                System.out.println("Error closing socket after send failure" + ex.getMessage());
            }
        }
    }

    public void run() {
        try {
            while (true) {
                String rawData = in.readUTF();
                System.out.println("Recebido de: " + clientName + ".");

                if(rawData.startsWith("TODOS:")) {
                    String messageContent = rawData.substring("TODOS:".length()).trim();
                    TCPServer.broadcastMessage("Mensagem para todos de '" + this.getClientName() +"': " + messageContent, this);
                } else if(rawData.startsWith("AMIGO:")) {
                    String[] parts = rawData.substring("AMIGO:".length()).split(":", 2);

                    if(parts.length == 2) {
                        String friendName = parts[0];
                        String messageContent = parts[1];

                        TCPServer.messagePrivate(messageContent, friendName, this);
                    } else {
                        sendMessage("SERVER: Formato de mensagem privada invalido.");
                    }
                } else {
                    sendMessage("SERVER: Comando desconhecido. Use 'TODOS:<mensagem>' ou 'AMIGO:<nome>:<mensagem>'.");
                }
            }
        } catch (EOFException e) {
            System.out.println("Cliente'" + clientName + "' desconectou(EOF).");
        } catch (IOException e) {
            System.out.println("IO error for client '" + clientName + "': " + e.getMessage());
        } finally {
                TCPServer.removeConnection(this);
                TCPServer.broadcastMessage(clientName + "saiu do chat.", this);
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.out.println("Close Failed for client '" + clientName + "': " + e.getMessage());
                }
        }
    }
}
