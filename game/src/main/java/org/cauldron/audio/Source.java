package org.cauldron.audio;

import org.lwjgl.openal.AL10;

import static org.lwjgl.openal.AL10.AL_NONE;

public class Source {

    private int sourceId;
    private float volume = 1f;

    public Source() {
        this.sourceId = AL10.alGenSources();
        AL10.alSourcef(sourceId, AL10.AL_GAIN, volume);
        AL10.alSourcef(sourceId, AL10.AL_PITCH, 1);
        AL10.alSource3f(sourceId, AL10.AL_POSITION, 0, 0, 0);
    }

    public void playAudio(int buffer) {
        stopAudio();
        AL10.alSourcei(sourceId, AL10.AL_BUFFER, buffer);
        AL10.alSourcePlay(sourceId);
    }

    public void playAudio(String name) {
        AL10.alSourcei(sourceId, AL10.AL_BUFFER, AudioManager.audioBuffers.get(name));
        AL10.alSourcePlay(sourceId);
    }

    public void pauseAudio() {
        AL10.alSourcePause(sourceId);
    }

    public void continuePlaying() {
        AL10.alSourcePlay(sourceId);
    }

    public void stopAudio() {
        AL10.alSourceStop(sourceId);
        AL10.alSourcei(sourceId, AL10.AL_BUFFER, AL_NONE);
    }

    public void muteAudio(int buffer) {
        AL10.alSourcei(sourceId, AL10.AL_BUFFER, buffer);
        AL10.alSourcef(sourceId, AL10.AL_GAIN, 0);
    }

    public void unmuteAudio(int buffer) {
        AL10.alSourcei(sourceId, AL10.AL_BUFFER, buffer);
        AL10.alSourcef(sourceId, AL10.AL_GAIN, volume);
    }

    public void setVolume(float volume) {
        AL10.alSourcef(sourceId, AL10.AL_GAIN, volume);
    }

    public void setPitch(float pitch) {
        AL10.alSourcef(sourceId, AL10.AL_PITCH, pitch);
    }

    public void setPosition(float x, float y, float z) {
        AL10.alSource3f(sourceId, AL10.AL_POSITION, x, y, z);
    }

    public void setVelocity(float x, float y, float z) {
        AL10.alSource3f(sourceId, AL10.AL_VELOCITY, x, y, z);
    }

    public void setLoop(boolean loop) {
        AL10.alSourcei(sourceId, AL10.AL_LOOPING, loop ? AL10.AL_TRUE : AL10.AL_FALSE);
    }

    public float getVolume() {
        return this.volume;
    }

    public int getID() {
        return this.sourceId;
    }

    public boolean isPlaying() {
        return AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
    }

    public void delete() {
        stopAudio();
        AL10.alDeleteSources(sourceId);
    }
}
