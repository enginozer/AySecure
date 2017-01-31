package com.nevitech.aysecure.place.base;

/**
 * Created by Emre on 30.1.2017.
 */

import android.graphics.RectF;
import android.support.v4.media.TransportMediator;
import com.nevitech.aysecure.place.base.Pos2D;
import com.nevitech.aysecure.place.base.CharEncoding;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;
import java.util.UUID;

public class Deserializer

{

    private ByteArrayInputStream bais16;
    private ByteArrayInputStream bais2;
    private ByteArrayInputStream bais4;
    private ByteArrayInputStream bais8;

    private byte[] buffer16;
    private byte[] buffer2;
    private byte[] buffer4;
    private byte[] buffer8;

    private DataInputStream dis16;
    private DataInputStream dis2;
    private DataInputStream dis4;
    private DataInputStream dis8;

    private boolean    initOk;
    private long       readPosition;
    private ByteBuffer theByteBuffer;

    private class ByteBuffer

    {

        private final int             READ_BUFFER_SIZE;
        private       int             bytesAvailable;
        private       byte[]          readBuffer;
        private       int             readPos;
        private       DataInputStream stream;

        public ByteBuffer(DataInputStream stream)

        {

            this.READ_BUFFER_SIZE = 100000;
            Runtime rt            = Runtime.getRuntime();

            try

            {

                long mem = rt.freeMemory() / 1000;
                rt.gc();
                mem      = rt.freeMemory() / 1000;

                this.readBuffer     = new byte[100000];
                this.readPos        = 0;
                this.bytesAvailable = 0;
                this.stream         = stream;

            }
            catch (OutOfMemoryError e)

            {

                long memErr = rt.freeMemory();
                e.getMessage();

            }
            catch (Exception e2)

            {


            }

        }

        public void close()

        {

            try

            {

                this.stream.close();

            }
            catch (Exception e)

            {


            }

        }

        public byte readByte() throws IOException

        {

            if (this.bytesAvailable == 0)

            {

                try

                {

                    this.bytesAvailable = this.stream.read(this.readBuffer);
                    this.readPos = 0;

                }
                catch (IOException e)

                {

                    String s = e.getMessage();
                    throw e;

                }

            }

            if (this.bytesAvailable <= 0)

            {

                return (byte) 0;

            }

            byte result = this.readBuffer[this.readPos];
            this.readPos++;
            this.bytesAvailable--;
            return result;

        }

        public int readBytes(byte[] buffer)

        {

            int bytesNeeded = buffer.length;
            int writeIndex  = 0;

            if (this.bytesAvailable < 0)

            {

                return -1;

            }

            while (bytesNeeded > 0)

            {

                if (this.bytesAvailable < 0)

                {

                    return writeIndex;

                }

                if (this.bytesAvailable == 0)

                {

                    try

                    {

                        this.bytesAvailable = this.stream.read(this.readBuffer);
                        this.readPos        = 0;

                        if (this.bytesAvailable <= 0)

                        {

                            return writeIndex;

                        }

                    }
                    catch (Exception e)

                    {

                        return -1;

                    }

                }

                int copyCount = bytesNeeded;

                if (bytesNeeded > this.bytesAvailable)

                {

                    copyCount = this.bytesAvailable;

                }

                System.arraycopy(this.readBuffer,
                                 this.readPos,
                                 buffer,
                                 writeIndex,
                                 copyCount);

                bytesNeeded         -= copyCount;
                this.bytesAvailable -= copyCount;
                writeIndex          += copyCount;
                this.readPos        += copyCount;

            }

            return writeIndex;

        }

    }

    public boolean isInitOk()

    {

        return this.initOk;

    }

    public Deserializer(DataInputStream stream)

    {

        this.initOk = false;
        init(stream);

    }

    public Deserializer(File file)

    {

        this.initOk = false;

        try

        {

            init(new DataInputStream(new FileInputStream(file)));
            this.initOk = true;

        }
        catch (FileNotFoundException e)

        {

            this.initOk = false;

        }

    }

    protected void init(DataInputStream stream)

    {

        this.theByteBuffer = new ByteBuffer(stream);
        this.readPosition  = 0;
        this.buffer2       = new byte[2];
        this.buffer4       = new byte[4];
        this.buffer8       = new byte[8];
        this.buffer16      = new byte[16];

        this.bais2         = new ByteArrayInputStream(this.buffer2);
        this.dis2          = new DataInputStream(this.bais2);
        this.bais4         = new ByteArrayInputStream(this.buffer4);
        this.dis4          = new DataInputStream(this.bais4);
        this.bais8         = new ByteArrayInputStream(this.buffer8);
        this.dis8          = new DataInputStream(this.bais8);
        this.bais16        = new ByteArrayInputStream(this.buffer16);
        this.dis16         = new DataInputStream(this.bais16);
    }

    public int readCount() throws IOException

    {

        return readInt();

    }

    public short readVersion() throws IOException

    {

        return readShort();

    }

    public double readDouble() throws IOException

    {

        try

        {

            this.dis8.reset();

            this.buffer8[7] = readByte();
            this.buffer8[6] = readByte();
            this.buffer8[5] = readByte();
            this.buffer8[4] = readByte();
            this.buffer8[3] = readByte();
            this.buffer8[2] = readByte();
            this.buffer8[1] = readByte();
            this.buffer8[0] = readByte();

            return this.dis8.readDouble();

        }
        catch (IOException ioException)

        {


            throw ioException;


        }

    }

    public boolean readAndTestVersion(short versionTest) throws IOException

    {

        if (readShort() == versionTest)

        {

            return true;

        }

        return false;

    }

    public Pos2D readPoint() throws IOException

    {

        return new Pos2D(readFloat(), readFloat());

    }

    public RectF readRectF() throws IOException

    {

        float x = readFloat();
        float y = readFloat();

        return new RectF(x, y, x + readFloat(), y + readFloat());

    }

    public ArrayList<Pos2D> readPoints() throws IOException

    {

        int count               = readInt();
        ArrayList<Pos2D> result = new ArrayList();

        for (int i = 0; i < count; i++)

        {

            result.add(readPoint());

        }

        return result;

    }

    public int readBytes(byte[] buffer)

    {

        return this.theByteBuffer.readBytes(buffer);

    }

    public byte readByte() throws IOException

    {

        this.readPosition++;
        return this.theByteBuffer.readByte();

    }

    public int readInt() throws IOException

    {

        try

        {

            this.dis4.reset();

            this.buffer4[3] = readByte();
            this.buffer4[2] = readByte();
            this.buffer4[1] = readByte();
            this.buffer4[0] = readByte();

            return this.dis4.readInt();

        }
        catch (IOException ioException)

        {

            throw ioException;

        }

    }

    public TreeMap<String, String> readProperties() throws IOException

    {

        TreeMap<String, String> result = new TreeMap();
        int count                      = readCount();

        for (int ind = 0; ind < count; ind++)

        {

            result.put(readUnicodeString(), readUnicodeString());

        }

        return result;

    }

    public long readLong() throws IOException

    {

        try

        {

            this.dis8.reset();

            this.buffer8[7] = readByte();
            this.buffer8[6] = readByte();
            this.buffer8[5] = readByte();
            this.buffer8[4] = readByte();
            this.buffer8[3] = readByte();
            this.buffer8[2] = readByte();
            this.buffer8[1] = readByte();
            this.buffer8[0] = readByte();

            return this.dis8.readLong();

        }
        catch (IOException ioException)

        {

            throw ioException;

        }

    }

    public long readDateAsLong() throws IOException

    {

        try

        {

            this.dis8.reset();

            this.buffer8[7] = readByte();
            this.buffer8[6] = readByte();
            this.buffer8[5] = readByte();
            this.buffer8[4] = readByte();
            this.buffer8[3] = readByte();
            this.buffer8[2] = readByte();
            this.buffer8[1] = readByte();
            this.buffer8[0] = readByte();

            return this.dis8.readLong();

        }
        catch (IOException ioException)

        {

            throw ioException;

        }

    }

    public float readFloat() throws IOException

    {

        try

        {

            this.dis4.reset();

            this.buffer4[3] = readByte();
            this.buffer4[2] = readByte();
            this.buffer4[1] = readByte();
            this.buffer4[0] = readByte();

            return this.dis4.readFloat();

        }
        catch (IOException ioException)

        {

            throw ioException;

        }

    }

    public Boolean readBoolean() throws IOException

    {

        boolean z = true;

        if (readByte() != (byte) 1)

        {

            z = false;

        }

        return Boolean.valueOf(z);

    }

    public void skipBytes(int len) throws IOException

    {

        for (int i = 0; i < len; i++)

        {

            readByte();

        }

    }

    public String readString() throws IOException

    {

        try

        {

            byte[] b = new byte[readStringLength()];
            readBytes(b);
            return new String(b);

        }
        catch (IOException e)

        {

            throw e;

        }

    }

    public String readUnicodeString() throws IOException

    {

        try

        {

            byte[] b = new byte[readStringLength()];
            readBytes(b);
            return new String(b, CharEncoding.UTF_16LE);

        }
        catch (IOException e)

        {

            throw e;

        }

    }

    public UUID readUUID() throws IOException

    {

        try

        {

            int readCount = readInt();
            this.dis16.reset();

            this.buffer16[3] = readByte();
            this.buffer16[2] = readByte();
            this.buffer16[1] = readByte();
            this.buffer16[0] = readByte();
            this.buffer16[5] = readByte();
            this.buffer16[4] = readByte();
            this.buffer16[7] = readByte();
            this.buffer16[6] = readByte();
            this.buffer16[8] = readByte();
            this.buffer16[9] = readByte();
            this.buffer16[10] = readByte();
            this.buffer16[11] = readByte();
            this.buffer16[12] = readByte();
            this.buffer16[13] = readByte();
            this.buffer16[14] = readByte();
            this.buffer16[15] = readByte();

            return new UUID(this.dis16.readLong(), this.dis16.readLong());

        }
        catch (IOException e)

        {

            throw e;

        }

    }

    public Date readDotNetDateTime() throws IOException

    {

        try

        {
            return new Date(readLong());

        }
        catch (IOException e)

        {

            throw e;

        }

    }

    public short readShort() throws IOException

    {

        try

        {

            this.dis2.reset();
            this.buffer2[1] = readByte();
            this.buffer2[0] = readByte();
            return this.dis2.readShort();

        }
        catch (IOException e)

        {

            throw e;

        }

    }

    public int readEnum() throws IOException

    {

        return readShort();

    }

    public boolean readAndTestSerializeID(int expectedID) throws IOException

    {

        try

        {

            return readShort() == expectedID;

        }
        catch (IOException e)

        {

            throw e;

        }

    }

    public boolean readAndTestObjectHashID(int expectedID) throws IOException

    {

        try

        {

            return readInt() == expectedID;

        }
        catch (IOException e)

        {

            throw e;

        }

    }

    public void close()

    {

        try

        {

            this.theByteBuffer.close();

        }
        catch (Exception e)

        {


        }

    }

    private int readStringLength() throws IOException

    {

        int length       = 0;
        int multi        = 1;
        byte currentByte = 0;

        do

        {

            try

            {

                currentByte = readByte();
                length     += (currentByte & TransportMediator.KEYCODE_MEDIA_PAUSE) * multi;
                multi      *= TransportMediator.FLAG_KEY_MEDIA_NEXT;

            }
            catch (Exception e)

            {

                if (e instanceof IOException)

                {

                    throw ((IOException) e);

                }

            }

        }
        while ((currentByte & TransportMediator.FLAG_KEY_MEDIA_NEXT) != 0);

        if (length < 0)

        {

            return 0;

        }

        return length;

    }

}
