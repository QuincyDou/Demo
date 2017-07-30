package chatroom;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client
{
    private Socket socket;

    public Client()
    {
        try
        {
            socket = new Socket("127.0.0.1", 8088);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        Client client = new Client();
        client.start();
    }

    private void start()
    {
        BufferedReader reader = null;
        PrintWriter writer = null;
        Scanner scan = null;
        try
        {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            scan = new Scanner(System.in);
            
            inputNickName(reader, writer, scan);
            ServerHandler handler = new ServerHandler();
            Thread t = new Thread(handler);
            t.setDaemon(true);
            t.start();

            
            while (true)
            {
                writer.println(scan.nextLine());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                socket.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            close(reader);
            close(writer);
            close(scan);
        }
    }

    private void inputNickName(BufferedReader reader, PrintWriter writer, Scanner scan)
    {
        try
        {
            while (true)
            {
                System.out.println("请输入昵称：");

                String nickName = scan.nextLine();
                if ("".equals(nickName))
                {
                    System.out.println("昵称不能为空！");
                    continue;
                }
                writer.println(nickName);
                String message = reader.readLine();
                if ("OK".equals(message))
                {
                    System.out.println(reader.readLine());
                    break;
                }
                else
                {
                    System.out.println(message);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
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

    private class ServerHandler implements Runnable
    {
        public void run()
        {
            try
            {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                while (true)
                {
                    System.out.println(reader.readLine());
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
