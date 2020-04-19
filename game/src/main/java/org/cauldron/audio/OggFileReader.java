package org.cauldron.audio;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBVorbisInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class OggFileReader {

    public static ShortBuffer readOgg(String filePath, int bufferSize, STBVorbisInfo info) {
        ByteBuffer vorbis;
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
            SeekableInMemoryByteChannel seekableByteChannel = new SeekableInMemoryByteChannel(IOUtils.toByteArray(is));
            vorbis = createByteBuffer((int) seekableByteChannel.size() + 1);
            seekableByteChannel.read(vorbis);
            vorbis.flip();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        IntBuffer error = BufferUtils.createIntBuffer(1);
        long decoder = stb_vorbis_open_memory(vorbis, error, null);
        if (decoder == NULL) {
            throw new RuntimeException("Failed to open File. Error: " + error.get(0));
        }

        stb_vorbis_get_info(decoder, info);

        int channels = info.channels();

        ShortBuffer audiofile = BufferUtils.createShortBuffer(stb_vorbis_stream_length_in_samples(decoder) * channels);

        stb_vorbis_get_samples_short_interleaved(decoder, channels, audiofile);
        stb_vorbis_close(decoder);

        return audiofile;
    }

}
