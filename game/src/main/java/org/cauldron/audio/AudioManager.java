package org.cauldron.audio;

import org.cauldron.game.Settings;
import org.lwjgl.openal.*;
import org.lwjgl.stb.STBVorbisInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class AudioManager {

    public static HashMap<String, Integer> audioBuffers = new HashMap<>();
    private static long device;
    public static Map<String, Source> sources = new HashMap<String, Source>();
    static long context;
    static float volume = 1f;

    public static void init() {
        String defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
        System.out.println(defaultDeviceName);
        device = alcOpenDevice(defaultDeviceName);
        int[] attributes = {0};
        context = alcCreateContext(device, attributes);
        alcMakeContextCurrent(context);
        ALCCapabilities alcCapabilities = ALC.createCapabilities(device);
        ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);
        setListenerData();
    }

    public static void setListenerData() {
        AL10.alListener3f(AL10.AL_POSITION, 0, 0, 0);
        AL10.alListener3f(AL10.AL_VELOCITY, 0, 0, 0);
    }

    public static int loadAudio(String filePath) {
        int buffer = AL10.alGenBuffers();
        //buffers.add(buffer);
        try (STBVorbisInfo vorbisInfo = STBVorbisInfo.malloc()) {
            ShortBuffer file = OggFileReader.readOgg(filePath, 32 * 1024, vorbisInfo);
            AL10.alBufferData(buffer, vorbisInfo.channels() == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16, file, vorbisInfo.sample_rate());
        }
        return buffer;
    }

    public static HashMap<String, Integer> loadAudioFiles(String audioList) {
        List<String> audioFiles = new ArrayList<>();
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(audioList);
        BufferedReader rdr = new BufferedReader(new InputStreamReader(in));
        String line;
        try {
            while ((line = rdr.readLine()) != null) {
                audioFiles.add(line);
            }
            rdr.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        for (String s : audioFiles)
            System.out.println(s);

        String audioName = "";
        int posSlash = audioList.lastIndexOf("/");
        String audioPath = audioList.substring(0, posSlash + 1);
        glActiveTexture(GL_TEXTURE0);
        for (String file : audioFiles) {
            audioName = file;
            int pos = audioName.lastIndexOf(".");
            if (pos == -1)
                continue;
            if (!audioName.substring(audioName.length() - 3).equals("ogg"))
                continue;
            audioName = file.substring(0, pos);
            int bufferId = loadAudio(audioPath + file);
            audioBuffers.put(audioName, bufferId);
        }

        return audioBuffers;
    }

    public static void setSources(String[] names) {
        destroySources();
        for (String name : names) {
            sources.put(name, new Source());
        }
        setVolume(((float) Settings.volume) / 100f);
    }

    public static void play(String name, String file, boolean loop) {
        if (loop)
            sources.get(name).setLoop(true);
        sources.get(name).playAudio(file);
    }

    public static void play(String name, String file) {
        sources.get(name).playAudio(file);
    }

    public static void cleanup() {
        alcDestroyContext(context);
        alcCloseDevice(device);
        destroySources();
    }

    public static void destroySources() {
        for (Map.Entry<String, Source> entry : sources.entrySet()) {
            entry.getValue().delete();
            sources.remove(entry);
        }
    }

    public static float getVolume() {
        return volume;
    }

    public static void setVolume(float v) {
        volume = v;
        for (Map.Entry<String, Source> entry : sources.entrySet()) {
            entry.getValue().setVolume(v);
        }
    }
}
