package br.com.sbrw.mp.util;

import java.util.Objects;

/**
 * Utility class to read values from a byte array, in order.
 * Works like any other decent programming language would.
 * Because Java isn't exactly decent in all aspects.
 * <p>
 * Essentially, this emulates pointer arithmetic. Fun!
 */
public class ArrayReader
{
    private byte[] bytes;
    private int position;

    public ArrayReader(byte[] bytes)
    {
        Objects.requireNonNull(bytes);

        this.bytes = bytes;
        this.position = 0;
    }

    public void seek(int position)
    {
        seek(position, false);
    }

    public void seek(int position, boolean relative)
    {
        if (relative)
        {
            if (this.position + position > this.bytes.length)
            {
                throw new IndexOutOfBoundsException(String.format("Cannot seek to %d from %d - maximum is %d", this.position + position, this.position, this.bytes.length));
            }

            this.position += position;
        } else
        {
            if (position < 0)
            {
                throw new IndexOutOfBoundsException(String.format("Cannot seek to %d. Why are you doing a negative non-relative seek?", position));
            }

            if (position > this.bytes.length)
            {
                throw new IndexOutOfBoundsException(String.format("Cannot seek to %d - maximum is %d", position, this.bytes.length));
            }

            this.position = position;
        }
    }

    public byte readByte()
    {
        return this.bytes[this.position++];
    }

    public char readChar()
    {
        return (char) this.bytes[this.position++];
    }

    public byte[] readBytes(int count)
    {
        byte[] bytes = new byte[count];

        for (int i = 0; i < count; i++)
        {
            bytes[i] = readByte();
        }

        return bytes;
    }

    public char[] readChars(int count)
    {
        char[] chars = new char[count];

        for (int i = 0; i < count; i++)
        {
            chars[i] = readChar();
        }

        return chars;
    }

    public short readShort()
    {
        return BitConverter.ToInt16(readBytes(2), 0);
    }

    public int readInt()
    {
        return BitConverter.ToInt32(readBytes(4), 0);
    }

    public String readString(int length)
    {
        return new String(readChars(length));
    }

    public int getPosition()
    {
        return this.position;
    }

    public int getLength()
    {
        return this.bytes.length;
    }

    private void readCheck()
    {
        if (this.position == this.bytes.length - 1)
        {
            throw new IndexOutOfBoundsException("Cannot read further; we are at the end of the data.");
        }
    }
}
