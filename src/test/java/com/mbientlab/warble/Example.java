/*
 * Copyright 2014-2019 MbientLab Inc. All rights reserved.
 *
 * IMPORTANT: Your use of this Software is limited to those specific rights granted under the terms of a software
 * license agreement between the user who downloaded the software, his/her employer (which must be your
 * employer) and MbientLab Inc, (the "License").  You may not use this Software unless you agree to abide by the
 * terms of the License which can be found at www.mbientlab.com/terms.  The License limits your use, and you
 * acknowledge, that the Software may be modified, copied, and distributed when used in conjunction with an
 * MbientLab Inc, product.  Other than for the foregoing purpose, you may not use, reproduce, copy, prepare
 * derivative works of, modify, distribute, perform, display or sell this Software and/or its documentation for any
 * purpose.
 *
 * YOU FURTHER ACKNOWLEDGE AND AGREE THAT THE SOFTWARE AND DOCUMENTATION ARE PROVIDED "AS IS" WITHOUT WARRANTY
 * OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF MERCHANTABILITY, TITLE,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT SHALL MBIENTLAB OR ITS LICENSORS BE LIABLE OR
 * OBLIGATED UNDER CONTRACT, NEGLIGENCE, STRICT LIABILITY, CONTRIBUTION, BREACH OF WARRANTY, OR OTHER LEGAL EQUITABLE
 * THEORY ANY DIRECT OR INDIRECT DAMAGES OR EXPENSES INCLUDING BUT NOT LIMITED TO ANY INCIDENTAL, SPECIAL, INDIRECT,
 * PUNITIVE OR CONSEQUENTIAL DAMAGES, LOST PROFITS OR LOST DATA, COST OF PROCUREMENT OF SUBSTITUTE GOODS, TECHNOLOGY,
 * SERVICES, OR ANY CLAIMS BY THIRD PARTIES (INCLUDING BUT NOT LIMITED TO ANY DEFENSE THEREOF), OR OTHER SIMILAR COSTS.
 *
 * Should you have any questions regarding your right to use this Software, contact MbientLab via email:
 * hello@mbientlab.com.
 */
package com.mbientlab.warble;

import org.testng.SkipException;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

import java.lang.System;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CompletableFuture;

public class Example {
    private static final String PROPERTY_DEVICE_MAC = "warble.mac",
            PROPERTY_HCI_MAC = "warble.hci.mac",
            PROPERTY_ADDR_TYPE = "warble.address_type",
            PROPERTY_LESCAN_ENABLE = "warble.lescan.enable",
            PROPERTY_LESCAN_TYPE = "warble.lescan.type";

    private Gatt setupGatt() {
        final Gatt.Builder builder;
        final Properties properties = System.getProperties();
        if (!properties.containsKey(PROPERTY_DEVICE_MAC)) {
            throw new IllegalArgumentException("Missing required property: " + PROPERTY_DEVICE_MAC);
        } else {
            builder = new Gatt.Builder(properties.getProperty(PROPERTY_DEVICE_MAC));
        }
        if (properties.containsKey(PROPERTY_HCI_MAC)) {
            builder.withHci(properties.getProperty(PROPERTY_HCI_MAC));
        }
        if (properties.containsKey(PROPERTY_ADDR_TYPE)) {
            builder.withAddressType(Gatt.AddressType.valueOf(properties.getProperty(PROPERTY_ADDR_TYPE).toUpperCase(Locale.US)));
        }

        return builder.build();
    }

    @Test(timeOut = 15000)
    public void connect() throws InterruptedException, ExecutionException {
        final Gatt gatt = setupGatt();
        gatt.connectAsync().thenCompose(ignored -> {
            assertTrue(gatt.isConnected());

            try {
                System.out.println("Connected to: " + gatt.mac);
                Thread.sleep(5000);

                CompletableFuture<Integer> dcTask = new CompletableFuture<>();
                gatt.onDisconnect = dcTask::complete;
                gatt.disconnect();
                
                return dcTask;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        })
        .thenAccept(result -> System.out.println("Disconnected: status = " + result))
        .get();

        assertFalse(gatt.isConnected());
    }

    @Test(timeOut = 10000)
    public void readDeviceInfo() throws InterruptedException, ExecutionException {
        final Gatt gatt = setupGatt();
        gatt.connectAsync().thenCompose(ignored -> {
            CompletableFuture<Void> task = CompletableFuture.completedFuture(null);
            for(final String uuid: new String[] {
                "00002a26-0000-1000-8000-00805f9b34fb",
                "00002a24-0000-1000-8000-00805f9b34fb",
                "00002a27-0000-1000-8000-00805f9b34fb",
                "00002a29-0000-1000-8000-00805f9b34fb",
                "00002a25-0000-1000-8000-00805f9b34fb"
            }) {
                task = task.thenCompose(ignored2 -> {
                    System.out.println("uuid var: " + uuid);

                    GattCharacteristic gattChar = gatt.findCharacteristic(uuid);
                    if (gattChar == null) {
                        System.out.println("Does not exist");
                        return CompletableFuture.completedFuture(new byte[0]);
                    }

                    assertEquals(uuid, gattChar.uuid);
                    return gattChar.readAsync();
                }).thenAccept(result -> System.out.println(new String(result)));
            }

            return task;
        }).thenCompose(ignored -> {
            System.out.println("Disconnecting...");
            CompletableFuture<Integer> dcTask = new CompletableFuture<>();

            gatt.onDisconnect = dcTask::complete;
            gatt.disconnect();
            
            return dcTask;
        }).get();

        System.out.println("Disconnected");
    }

    @Test(timeOut = 10000)
    public void scanTest() throws InterruptedException, ExecutionException {
        if (!Boolean.valueOf(System.getProperty(PROPERTY_LESCAN_ENABLE, "false"))) {
            throw new SkipException("Skipping le scan test");
        }

        final CompletableFuture<Void> asyncTask = new CompletableFuture<>();

        final AtomicInteger i = new AtomicInteger();
        Scanner.onResultReceived(result -> {
            byte[] data = result.getManufacturerData(0x067e);
            System.out.printf("name = %s, mac = %s, rssi = %d, mbl scan result = %s%n", result.getName(), result.getMac(), result.getRssi(),
                    data == null ? "[]" : Arrays.toString(data));
            
            if (i.incrementAndGet() > 10) {
                asyncTask.complete(null);
            }
        });
        String scanTypeName = System.getProperty(PROPERTY_LESCAN_TYPE, null);
        Scanner.start(scanTypeName != null ? Scanner.ScanType.valueOf(scanTypeName) : null, System.getProperty(PROPERTY_HCI_MAC, null));

        asyncTask.get();
        Scanner.stop();
    }

    @Test
    public void checkLibraryVersion() {
        String version = Library.version();
        System.out.println("warble version: " + version);
        assertEquals("1.1", version.substring(0, version.lastIndexOf(".")));
    }
}