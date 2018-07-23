/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.google.speech.principal;

import com.google.speech.microphone.MicrophoneAnalyzer;
import com.google.speech.recognizer.GoogleResponse;
import com.google.speech.recognizer.Recognizer;
import java.awt.AWTException;
import java.awt.Robot;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import net.sourceforge.javaflacencoder.FLACFileWriter;

/**
 *
 * @author Juan
 */
public class Principal {

    public static void ambientListeningLoop(String[] args) {

        String reppp = "Mouse en movimiento";
        grabar(reppp);
        //Instancia la clase microfono que va a guardar la voz en un file
        MicrophoneAnalyzer mic = new MicrophoneAnalyzer(FLACFileWriter.FLAC);
        //seteamos el archivo de voz
        mic.setAudioFile(new File("AudioTestNow.flac"));
        while (true) {
            mic.open();
            final int THRESHOLD = 10;
            int volume = mic.getAudioVolume();
            boolean isSpeaking = (volume > THRESHOLD);
            grabar("Asistente escuchando");
            if (isSpeaking) {
                try {
                    System.out.println("Escuchando...");
                    mic.captureAudioToFile(mic.getAudioFile());//Guarda el audio en un archivo.

                    do {
                        Thread.sleep(1000);//Esucha un segundo 
                    } while (mic.getAudioVolume() > THRESHOLD);
                    System.out.println("Grabacion completa!");
                    System.out.println("Reconociendo...");
		            //En el segundo parametro de entrada va la clave proporcionada por Google Speech
                    Recognizer rec = new Recognizer("es-EC", "XXXXXXXXXXXXXXXXXXXXXX");
                    //Obtiene el reultado de la grabacion y 3 posibles soluciones
                    GoogleResponse response = rec.getRecognizedDataForFlac(mic.getAudioFile(), 3);
                    displayResponse(response);//Envia el resultado a ser imprimido
                    System.out.println("Regresando....");//Regresa a grabar
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    System.out.println("Error....");
                } finally {
                    mic.close();//Makes sure microphone closes on exit.
                }
            }
        }
    }

    private static void displayResponse(GoogleResponse gr) throws IOException, AWTException {
        if (gr.getResponse() == null) {
            System.out.println((String) null);
            return;
        }
        System.out.println("Google Respuesta: " + gr.getResponse());
        System.out.println("Google esta " + Double.parseDouble(gr.getConfidence()) * 100 + "% seguro"
                + " de la respuesta");
        String respuesta = gr.getResponse();
        String resp[] = respuesta.split(" ");
        String escr[] = new String[resp.length - 1];

        if (resp[0].equalsIgnoreCase("escribir")) {
            for (int i = 1; i < resp.length; i++) {
                escr[i - 1] = resp[i];
            }
            escribirTexto(escr);
        } else {
            accionVoz(resp);
        }
        grabar(respuesta);
        System.out.println("Otras posibles : ");
        for (String s : gr.getOtherPossibleResponses()) {
            System.out.println("\t" + s);
        }

    }

    public static void accionVoz(String accion[]) throws IOException {
        boolean realizado = false;
        String prog = "";
        if (accion[0].equalsIgnoreCase("abrir")) {
            if ((accion[1].equalsIgnoreCase("word"))) {
                prog = "win" + accion[1];
                Process p = Runtime.getRuntime().exec("cmd.exe /c start " + prog);
                realizado = true;
            }
            if ((accion[1].equalsIgnoreCase("facebook")) || (accion[1].equalsIgnoreCase("youtube"))) {
                prog = "www." + accion[1] + ".com";
                Process p = Runtime.getRuntime().exec("cmd.exe /c start Firefox " + prog);
                realizado = true;
            }
            if ((accion[1].equalsIgnoreCase("navegador"))) {
                prog = "firefox";
                Process p = Runtime.getRuntime().exec("cmd.exe /c start " + prog);
                realizado = true;
            }
            if ((accion[1].equalsIgnoreCase("excel"))) {
                prog = accion[1];
                Process p = Runtime.getRuntime().exec("cmd.exe /c start " + prog);
                realizado = true;
            }

        }
        if (realizado == false) {
            System.out.println("No se pudo realizar accion....");
        }
        if (accion[0].equalsIgnoreCase("cerrar")) {
            if ((accion[1].equalsIgnoreCase("word"))) {
                prog = "win" + accion[1];
                Process p = Runtime.getRuntime().exec("cmd.exe /c taskkill /IM " + prog + ".exe");
                realizado = true;
            }

            if ((accion[1].equalsIgnoreCase("firefox"))) {
                prog = accion[1];
                Process p = Runtime.getRuntime().exec("cmd.exe /c taskkill /IM " + prog + ".exe");
                realizado = true;
            }
            if ((accion[1].equalsIgnoreCase("excel"))) {
                prog = accion[1];
                Process p = Runtime.getRuntime().exec("cmd.exe /c taskkill /IM " + prog + ".exe");
                realizado = true;
            }
        }
    }

    private static void scapiExample(URL APICall) {
        AudioInputStream din = null;
        try {

            AudioInputStream in = AudioSystem.getAudioInputStream(APICall);
            AudioFormat baseFormat = in.getFormat();
            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
                    baseFormat.getChannels() * 2, baseFormat.getSampleRate(),
                    false);
            din = AudioSystem.getAudioInputStream(decodedFormat, in);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            if (line != null) {
                line.open(decodedFormat);
                byte[] data = new byte[4096];
                // Start
                line.start();

                int nBytesRead;
                while ((nBytesRead = din.read(data, 0, data.length)) != -1) {
                    line.write(data, 0, nBytesRead);
                }
                // Stop
                line.drain();
                line.stop();
                line.close();
                din.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (din != null) {
                try {
                    din.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private static void escribirTexto(String buscar[]) throws AWTException {
        StringBuilder escr = new StringBuilder();
        for (int i = 0; i < buscar.length; i++) {
            escr = escr.append(buscar[i].toUpperCase()).append(" ");
        }
        System.out.println("strBuil..." + escr);
        Robot robot = new Robot();

        for (int i = 0; i < escr.length(); i++) {
            int codletra = escr.codePointAt(i);
            robot.keyPress(codletra);
            robot.keyRelease(codletra);
            //dormimos el robot por 250 mili segundos luego de usar cada tecla
            robot.delay(250);
        }
    }

    private static void grabar(String grab) {
        //Clave temporal para usar ReadSpeaker
        String ApiKey = "ff776d1637b294e9599cd490d9e9a6bf";
        String lang = "es_es";
        String voice = "Pilar";
        // URL to API
        String apiURL = "http://tts.readspeaker.com/a/speak";
        URL APICall = null;

        try {
            APICall = new URL(apiURL + "?key=" + ApiKey + "&lang=" + lang + "&voice=" + voice + "&text=" + URLEncoder.encode(grab) + "&audioformat=pcm&streaming=0");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (APICall != null) {
            scapiExample(APICall);
        }
    }

    public static void main(String[] args) {
        ambientListeningLoop(args);
    }
}
