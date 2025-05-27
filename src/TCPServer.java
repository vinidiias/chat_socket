// TCPServer.java

import java.net.*;
import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TCPServer {
    // Lista para armazenar todas as threads de conexão ativas
    // Precisa ser 'static' para que todas as instâncias de Connection (threads) possam acessá-la
    // e 'synchronized' (ou usar um ConcurrentList) para garantir segurança em ambiente multi-thread
    private static Map<String, Connection> activeConnections = new ConcurrentHashMap<>();

    // Método para adicionar uma nova conexão
    public static synchronized void addConnection(Connection connection) {
        activeConnections.put(connection.getClientName(), connection);
        System.out.println("Novo cliente conectado. Total de clientes: " + activeConnections.size());
    }

    // Método para remover uma conexão
    public static synchronized void removeConnection(Connection connection) {
        activeConnections.remove(connection.getClientName());
        System.out.println("Cliente desconectado. Total de clientes: " + activeConnections.size());
    }

    // Método para enviar uma mensagem para TODOS os clientes conectados
    public static synchronized void broadcastMessage(String message, Connection sender) {
        System.out.println("Broadcast: " + message);
        for (Connection c : activeConnections.values()) {
            // Não enviar a mensagem de volta para quem a enviou, a menos que queira que o remetente veja a própria mensagem ecoada.
            // if (c != sender) {
            c.sendMessage(message); // Chama o método sendMessage da Connection
            // }
        }
    }

    public static synchronized void messagePrivate(String msg, String recipientName, Connection sender) {
        Connection recipientConnection = activeConnections.get(recipientName);

        if(recipientConnection != null) {
            recipientConnection.sendMessage("Mensagem privada de '" + sender.getClientName() + "': " + msg);

            sender.sendMessage("Mensagem privada para '" + recipientConnection.getClientName() + "' enviado: " + msg);
            System.out.println("Mensagem privade de '" + sender.getClientName() + "' para '" + recipientConnection.getClientName() + "' " + "enviado.");
        } else {
            sender.sendMessage("SERVER: O usuario '" + recipientName + "' nao esta online ou nao existe.");
            System.out.println("Mensagem privada de '" + sender.getClientName() + "' para '" + recipientName + "' FALHOU (usuário não encontrado).");
        }
    }

    public static void main(String[] args) {
        try {
            int serverPort = 7896;
            ServerSocket listenSocker = new ServerSocket(serverPort);
            System.out.println("Servidor de Chat escutando na porta " + serverPort + "...");

            while(true) {
                Socket clientSocket = listenSocker.accept();
                new Connection(clientSocket);
            }
        } catch(IOException e) {
            System.out.println("Listen: " + e.getMessage());
        }
    }
}