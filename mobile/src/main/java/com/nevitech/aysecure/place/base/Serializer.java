package com.nevitech.aysecure.place.base;

/**
 * Created by Emre on 27.1.2017.
 */
import android.support.v4.media.TransportMediator;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public class Serializer

{

    private ByteArrayOutputStream baos;
    private byte[]                buffer;
    private DataOutputStream      dos;
    private ByteBuffer            theByteBuffer;

    private class ByteBuffer

    {

        private final int              WRITE_BUFFER_SIZE;
        private       int              bytesFree;
        private       DataOutputStream stream;
        private       byte[]           writeBuffer;
        private       int              writePos;

        public ByteBuffer(DataOutputStream stream)

        {

            this.WRITE_BUFFER_SIZE = 100000;
            this.writeBuffer       = new byte[100000];
            this.writePos          = 0;
            this.bytesFree         = 0;
            this.stream            = stream;

        }

        public void close()

        {

            try

            {

                flushBuffer();
                this.stream.close();

            }
            catch (Exception e)

            {


            }

        }

        private boolean flushBuffer()

        {

            try

            {

                this.stream.write(this.writeBuffer, 0, this.writePos);
                this.stream.flush();
                this.writePos  = 0;
                this.bytesFree = this.writeBuffer.length;
                return true;

            }
            catch (Exception e)

            {

                String s = e.getMessage();
                return false;

            }

        }

        public boolean writeByte(byte b)

        {

            if (this.bytesFree < 1)

            {

                flushBuffer();

            }

            if (this.bytesFree < 1)

            {

                return false;

            }

            this.writeBuffer[this.writePos] = b;
            this.writePos++;
            this.bytesFree--;
            return true;

        }

        public boolean writeBytes(byte[] buffer)

        {

            int bytesNeeded = buffer.length;
            int readIndex   = 0;

            while (bytesNeeded > 0)

            {

                int writeCount;

                if (bytesNeeded < this.bytesFree)

                {

                    writeCount = bytesNeeded;

                }
                else

                {

                    writeCount = this.bytesFree;

                }

                System.arraycopy(buffer,
                                 readIndex,
                                 this.writeBuffer,
                                 this.writePos,
                                 writeCount);

                this.writePos  += writeCount;
                this.bytesFree -= writeCount;
                readIndex      += writeCount;
                bytesNeeded    -= writeCount;

                if (this.bytesFree < 1)

                {

                    flushBuffer();

                }

            }

            return true;

        }

    }

    public Serializer(DataOutputStream stream)

    {

        init(stream);

    }

    public Serializer(File file)

    {

        try

        {

            init(new DataOutputStream(new FileOutputStream(file)));

        }
        catch (IOException e)

        {

        }

    }

    protected void init(DataOutputStream stream)

    {

        this.theByteBuffer = new ByteBuffer(stream);
        this.baos          = new ByteArrayOutputStream(16);
        this.dos           = new DataOutputStream(this.baos);

    }

    public boolean writeCount(int val)

    {

        return writeInt(val);

    }

    public boolean writeVersion(short val)

    {

        return writeShort(val);

    }

    public boolean writePoint(Pos2D val)

    {

        return (true & writeFloat(val.f37x)) & writeFloat(val.f38y);

    }

    public boolean writeBytes(byte[] buffer)

    {

        return this.theByteBuffer.writeBytes(buffer);

    }

    public boolean writeByte(byte val)

    {

        return this.theByteBuffer.writeByte(val);

    }

    public boolean writeInt(int val)

    {

        try

        {

            this.baos.reset();
            this.dos.writeInt(val);
            this.buffer = this.baos.toByteArray();
            writeByte(this.buffer[3]);
            writeByte(this.buffer[2]);
            writeByte(this.buffer[1]);
            writeByte(this.buffer[0]);
            return true;

        }
        catch (IOException e)

        {

            return false;

        }

    }

    public boolean writeLong(long val)

    {

        try

        {

            this.baos.reset();
            this.dos.writeLong(val);
            this.buffer = this.baos.toByteArray();
            writeByte(this.buffer[7]);
            writeByte(this.buffer[6]);
            writeByte(this.buffer[5]);
            writeByte(this.buffer[4]);
            writeByte(this.buffer[3]);
            writeByte(this.buffer[2]);
            writeByte(this.buffer[1]);
            writeByte(this.buffer[0]);
            return true;

        }
        catch (IOException e)

        {

            return false;

        }

    }

    public boolean writeDateAsLong(long val)

    {

        try

        {

            this.baos.reset();
            this.dos.writeLong(val);
            this.buffer = this.baos.toByteArray();
            writeByte(this.buffer[7]);
            writeByte(this.buffer[6]);
            writeByte(this.buffer[5]);
            writeByte(this.buffer[4]);
            writeByte(this.buffer[3]);
            writeByte(this.buffer[2]);
            writeByte(this.buffer[1]);
            writeByte(this.buffer[0]);
            return true;

        }
        catch (IOException e)

        {

            return false;

        }

    }

    public boolean writeFloat(float val)

    {

        try

        {

            this.baos.reset();
            this.dos.writeFloat(val);
            this.buffer = this.baos.toByteArray();
            this.theByteBuffer.writeByte(this.buffer[3]);
            this.theByteBuffer.writeByte(this.buffer[2]);
            this.theByteBuffer.writeByte(this.buffer[1]);
            this.theByteBuffer.writeByte(this.buffer[0]);
            return true;

        }
        catch (IOException e)

        {

            return false;

        }

    }

    public boolean writeBoolean(boolean val)

    {

        byte b = (byte) 0;

        if (val)

        {

            b = (byte) 1;

        }

        return writeByte(b);

    }

    public boolean writeString(String val)

    {

        byte[] b = val.getBytes();
        return (true & writeStringLength(b.length)) & this.theByteBuffer.writeBytes(b);

    }

    public boolean writeUnicodeString(String val)

    {

        try

        {

            byte[] b = val.getBytes(CharEncoding.UTF_16LE);
            return (true & writeStringLength(b.length)) & this.theByteBuffer.writeBytes(b);

        }
        catch (Exception e)

        {

            return false;

        }

    }

    public boolean writeUUID(UUID val)

    {

        long mostSignificant = val.getMostSignificantBits();
        long leasSignificant = val.getLeastSignificantBits();
        boolean bOk          = true & writeInt(16);
        this.baos.reset();

        try

        {

            this.dos.writeLong(mostSignificant);
            this.dos.writeLong(leasSignificant);
            this.buffer = this.baos.toByteArray();
            return (((((((((((((((bOk & writeByte(this.buffer[3])) & writeByte(this.buffer[2])) & writeByte(this.buffer[1])) & writeByte(this.buffer[0])) & writeByte(this.buffer[5])) & writeByte(this.buffer[4])) & writeByte(this.buffer[7])) & writeByte(this.buffer[6])) & writeByte(this.buffer[8])) & writeByte(this.buffer[9])) & writeByte(this.buffer[10])) & writeByte(this.buffer[11])) & writeByte(this.buffer[12])) & writeByte(this.buffer[13])) & writeByte(this.buffer[14])) & writeByte(this.buffer[15]);

        }
        catch (IOException e)

        {

            return false;

        }

    }

    public boolean writeDotNetDateTime(Date val)

    {

        return writeLong(val.getTime());

    }

    public boolean writeShort(short val)

    {

        try

        {

            this.baos.reset();
            this.dos.writeShort(val);
            this.buffer = this.baos.toByteArray();
            boolean bOk = (true & writeByte(this.buffer[1])) & writeByte(this.buffer[0]);
            return true;

        }
        catch (IOException e)

        {

            return false;

        }

    }

    public boolean writeEnum(int val)

    {

        return writeShort((short) val);

    }

    public boolean writeSerializeID(int val)

    {

        return writeShort((short) val);

    }

    public boolean writeObjectHashID(int val)

    {

        return writeInt(val);

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

    private boolean writeStringLength(int val)

    {

        int help = val;

        while (help >= TransportMediator.FLAG_KEY_MEDIA_NEXT)

        {

            writeByte((byte) (((byte) (help & TransportMediator.KEYCODE_MEDIA_PAUSE)) | TransportMediator.FLAG_KEY_MEDIA_NEXT));
            help /= TransportMediator.FLAG_KEY_MEDIA_NEXT;

        }

        writeByte((byte) help);
        return true;

    }

}
