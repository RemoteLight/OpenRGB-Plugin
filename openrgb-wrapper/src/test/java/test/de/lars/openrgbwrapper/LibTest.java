package test.de.lars.openrgbwrapper;

import de.lars.openrgbwrapper.Device;
import de.lars.openrgbwrapper.OpenRGB;
import de.lars.openrgbwrapper.models.Color;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class LibTest {

    public static void main(String[] args) throws IOException {
        OpenRGB orgb = new OpenRGB("127.0.0.1", 6742);
        orgb.connect();
        System.out.println("Connected controllers: " + orgb.getControllerCount());

        // get controller
        final int deviceId = 0;
        Device device = orgb.getControllerData(deviceId);
        System.out.println("Controller data: " + device);

        System.out.print("All controllers: ");
        Arrays.stream(orgb.getAllControllerData()).forEach(d -> System.out.print(d.name + ", "));
        System.out.println();

        // update leds
        for(int i = 0; i < device.colors.length; i++) {
            if(i % 2 == 0)
                device.colors[i] = new Color(0, 255, 0);
            else
                device.colors[i] = new Color(0, 0, 255);
        }
        orgb.updateLeds(deviceId, device.colors);

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
