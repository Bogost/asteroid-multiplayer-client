package edu.bo.app.asteroid_multiplayer_client.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class SoundManager {

    private int clipSize = 4096;

    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private Connector connector;
    SourceDataLine audioLine;

    private volatile boolean running = true;

    public SoundManager(Connector connector) {
        this.connector = connector;
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 16000;
        int sampleSizeInBits = 16;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = true;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
        return format;
    }

    private void filterClip(byte[] buffer) {
        short max = -1;
        short value;
        short buf1, buf2;
        for (int i = 2; i < buffer.length; i += 2) {
            buf1 = (short) ((buffer[i] & 0xff) << 8);
            buf2 = (short) (buffer[i + 1] & 0xff);
            value = (short) (buf1 + buf2);
            if (value > max) {
                max = value;
            }
        }
        if (max < 600) {
            Arrays.fill(buffer, (byte) 0);
        }
    }

    private void runOutput() {
        Thread th = new Thread() {

            @Override
            public void run() {
                try {
                    AudioFormat format = getAudioFormat();
                    DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

                    // checks if system supports the data line
                    if (!AudioSystem.isLineSupported(info)) {
                        System.out.println("Line not supported");
                        System.exit(0);
                    }
                    TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
                    line.open(format);
                    line.start();   // start capturing

                    System.out.println("Start capturing...");

                    AudioInputStream ais = new AudioInputStream(line);

                    System.out.println("Start recording...");

                    // start recording
                    int position = 0;
                    int readed;
                    byte[] buffer = new byte[clipSize];

                    while (running) {
                        readed = ais.read(buffer, position, buffer.length - position);
                        if (readed == -1) {
                            continue;
                        }
                        position += readed;
                        if (buffer.length - 1 <= position) {
                            filterClip(buffer);// --------------------------------------
                            outputStream.write(buffer);
                            outputStream.flush();
                            position = 0;
                        }
                    }
                } catch (LineUnavailableException ex) {
                    System.err.println("Audio line for input is unavailable.");
                    AlertManager.exceptionAlert("Audio input unavaible", "Audio line for input is unavailable.", ex);
                    ex.printStackTrace();
                    connector.disconnect();
                } catch (IOException ioe) {
                    if (connector.isConnected) {
                        System.err.println("Error playing the audio file.");
                        AlertManager.connectionProblemAlert(ioe);
                        ioe.printStackTrace();
                        connector.disconnect();
                    }
                }
            }
        };
        th.start();
    }

    private void runInput() {
        Thread th = new Thread() {

            @Override
            public void run() {
                try {
                    AudioFormat format = getAudioFormat();
                    DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                    SourceDataLine audioLine = (SourceDataLine) AudioSystem.getLine(info);
                    System.out.println("info: " + info);
                    audioLine.open(format);
                    audioLine.start();
                    System.out.println("Playback started.");
                    byte[] buffer = new byte[clipSize];
                    while (running) {
                        inputStream.read(buffer);
                        audioLine.write(buffer, 0, buffer.length);
                    }
                    audioLine.close();

                } catch (LineUnavailableException ex) {
                    System.err.println("Audio line for playing back is unavailable.");
                    AlertManager.exceptionAlert("Audio Output unavaible", "Audio line for playing back is unavailable.", ex);
                    ex.printStackTrace();
                    connector.disconnect();
                } catch (IOException ex) {
                    if (connector.isConnected) {
                        System.err.println("Error playing the audio file.");
                        AlertManager.connectionProblemAlert(ex);
                        ex.printStackTrace();
                        connector.disconnect();
                    }
                    if (audioLine != null) {
                        audioLine.close();
                    }
                }
            }
        };
        th.start();
        Thread th2 = new Thread() {

            @Override
            public void run() {
                while (running) {
                    audioLine.drain();
                }
            }
        };
        th2.start();
    }

    public void start(DataInputStream inputStream, DataOutputStream outputStream) {
        running = true;
        this.outputStream = outputStream;
        this.inputStream = inputStream;
        runOutput();
        runInput();
    }

}
