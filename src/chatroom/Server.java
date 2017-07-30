package chatroom;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 简易聊天室：服务端
 * 
 * @author Administrator
 *
 */
public class Server
{
    private ServerSocket server;

    private Map<String, PrintWriter> outMap;

    private ExecutorService threadPool;

    public Server()
    {
        try
        {
            server = new ServerSocket(8088);
            outMap = new HashMap<String, PrintWriter>();
            threadPool = Executors.newFixedThreadPool(30);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        Server server = new Server();
        server.start();
    }

    private void start()
    {
        while (true)
        {
            try
            {
                Socket client = server.accept();
                ClientHandler handler = new ClientHandler(client);
                threadPool.execute(handler);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private synchronized void addOut(String nickName, PrintWriter out)
    {
        outMap.put(nickName, out);
    }

    private synchronized void removeOut(String nickName)
    {
        outMap.remove(nickName);
    }

    private synchronized void sendMessage(String message)
    {
        for (PrintWriter out : outMap.values())
        {
            out.println(message);
        }
    }

    private synchronized void sendMessageToOne(String nickName, String message)
    {
        PrintWriter out = outMap.get(nickName);
        if (out != null)
        {
            out.println(message);
        }
    }

    private class ClientHandler implements Runnable
    {
        private Socket client;

        private String host;

        private String nickName;

        public ClientHandler(Socket client)
        {
            this.client = client;
        }

        public void run()
        {
            BufferedReader reader = null;
            PrintWriter writer = null;
            try
            {
                reader = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));
                writer = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), "UTF-8"), true);

                setNickName(reader, writer);
                addOut(nickName, writer);
                sendMessage(nickName + " 上线了！");

                String message = null;
                while ((message = reader.readLine()) != null)
                {
                    if (message.startsWith("\\"))
                    {
                        int index = message.indexOf("：");
                        if (index > 0)
                        {
                            String name = message.substring(1, index);
                            String info = message.substring(1 + index);
                            info = nickName + "悄悄对你说：" + info;
                            sendMessageToOne(name, info);
                            continue;
                        }
                    }
                    sendMessage(message);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    client.close();
                    removeOut(nickName);
                    sendMessage(nickName + " 下线了！");
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                close(reader);
                close(writer);
            }
        }

        private void setNickName(BufferedReader reader, PrintWriter writer) throws IOException
        {
            String nickName = null;

            while ((nickName = reader.readLine()) != null)
            {
                if (outMap.containsKey(nickName))
                {
                    writer.println("昵称已被使用，请换一个！");
                }
                else
                {
                    writer.println("OK");
                    writer.println("你好，" + nickName + "！开始聊天吧！");
                    this.nickName = nickName;
                    break;
                }
            }
        }

        private void close(Closeable c)
        {
            if (c != null)
            {
                try
                {
                    c.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

    }

}
