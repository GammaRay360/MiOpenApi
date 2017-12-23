package com.github.gammaray360.MiOpenApi;

import com.github.gammaray360.MiOpenApi.MiCommands.MiCommand;
import com.github.gammaray360.MiOpenApi.MiCommands.MiCommandToggle;

import java.io.IOException;
import java.net.InetAddress;

import static org.junit.Assert.*;

public class MiDeviceTest {

    public static void TestWithoutToken() throws IOException, InterruptedException {
        // Will only work for uninitialized Mi devices.
        // Initialized yeelight devices will not share their token.
        InetAddress ip = InetAddress.getByName("192.168.13.1");
        int flags =
                MiDevice.OVERRIDE_TOKEN |
                        MiDevice.OVERRIDE_TIME |
                        MiDevice.OVERRIDE_DEVICE_ID;

        MiDevice device = new MiDevice(ip);
        device.connect(flags);
        MiCommand command = new MiCommandToggle();
        MiCommandJob job = new MiCommandJob(device,command);
        String response;
        job.execute();
        response = job.getResponse();
        System.out.println("got response: " + response);
        Thread.sleep(3000);
        job = new MiCommandJob(device,command);
        job.execute();
        response = job.getResponse();
        System.out.println("got response: " + response);
        Thread.sleep(3000);
        job = new MiCommandJob(device,command);
        job.execute();
        response = job.getResponse();
        System.out.println("got response: " + response);
        Thread.sleep(3000);
        device.disconnect();
    }

    public static void TestWithToken() throws IOException, InterruptedException {
        // Will work for any Mi devices, as long as you have it's token.
        InetAddress ip = InetAddress.getByName("192.168.13.1");
        byte[] token= new byte[]{0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x10,
                0x11,0x12,0x13,0x14,0x15,0x16};
        int flags =
                MiDevice.OVERRIDE_TIME |
                        MiDevice.OVERRIDE_DEVICE_ID;

        MiDevice device = new MiDevice(ip);
        device.setToken(token);
        device.connect(flags);
        MiCommand command = new MiCommandToggle();
        MiCommandJob job = new MiCommandJob(device,command);
        job.execute();
        Thread.sleep(3000);
        job = new MiCommandJob(device,command);
        job.execute();
        Thread.sleep(3000);
        job = new MiCommandJob(device,command);
        job.execute();
        Thread.sleep(3000);
        device.disconnect();
    }


}