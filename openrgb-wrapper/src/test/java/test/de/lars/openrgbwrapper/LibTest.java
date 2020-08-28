package test.de.lars.openrgbwrapper;

import de.lars.openrgbwrapper.OpenRGB;

import java.io.IOException;
import java.util.Scanner;

public class LibTest {

    public static void main(String[] args) throws IOException {
        OpenRGB orgb = new OpenRGB("127.0.0.1", 6742);
        orgb.connect();
        System.out.println("Connected controllers: " + orgb.getControllerCount());

        // keep running
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter something to exit...");
        while(scanner.hasNext()) {
            if(scanner.nextLine() != null) {
                System.out.println("Exiting...");
                orgb.getClient().disconnect();
                break;
            }
        }
    }

}
