import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;

public class Client extends JFrame {
    private final Socket socket;
    private final DataOutputStream out;
    private final DataInputStream in;

    public Client() throws IOException { //конструктор
        socket = new Socket("localhost", 5678);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());

        setSize(300, 300); //размер окна
        JPanel panel = new JPanel(new GridLayout(2, 1)); //в окне 2строки 1 стобец

        JButton btnSend = new JButton("SEND"); //создадим кнопку
        JTextField textField = new JTextField(); //создадим текстовое поле

        btnSend.addActionListener(a -> {  //действие на нажатие кнопки
            // upload 1.txt
            // download img.png
            String[] cmd = textField.getText().split(" ");
            if ("upload".equals(cmd[0])) {
                sendFile(cmd[1]);
            } else if ("download".equals(cmd[0])) {
                getFile(cmd[1]);
            }
        });

        panel.add(textField);//добавим на панель текстовое поле
        panel.add(btnSend); //добавим на панель кнопку

        add(panel); //добавим саму панель

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                sendMessage("exit");
            }
        });
        setVisible(true);
    }

    private void getFile(String filename) {
        // TODO: 14.06.2021 получение файла с сервера
        try {
            File file = new File("server" + File.separator + filename);
            if (!file.exists()) {
                throw  new FileNotFoundException();
            }

            long fileLength = file.length();
            FileInputStream fis = new FileInputStream(file);

            out.writeUTF("download");
            out.writeUTF(filename);
            out.writeLong(fileLength);

            int read=0;
            byte[] buffer = new byte[8 * 1024];
            while ((read = fis.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();

            String status = in.readUTF();
            System.out.println("downloading status: " + status);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void sendFile(String filename) { //отправка файла на сервер
        try {
            File file = new File("client" + File.separator + filename); //создаем объект класса "файл"
            if (!file.exists()) {
                throw  new FileNotFoundException();
            }

            long fileLength = file.length(); //считываем размерность файла в байтах
            FileInputStream fis = new FileInputStream(file);

            out.writeUTF("upload"); //указываем серверу что хотим передать ему что-то
            out.writeUTF(filename);     //указываем что хотим передать файл с таким то именем
            out.writeLong(fileLength);  //передаем файл такой то длины

            int read = 0;
            byte[] buffer = new byte[8 * 1024]; //создаем буффер
            while ((read = fis.read(buffer)) != -1) {
                out.write(buffer, 0, read); //отправляем буффер с 0 до конца
            }
            out.flush(); //чтобы ничего не осталось в канале (очистили канал)

            String status = in.readUTF();
            fis.close();
            System.out.println("sending status: " + status);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String message) {
        try {
            out.writeUTF(message); //отправка сообщения, которое мы ввели, в канал
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        new Client();
    }
}