/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package dk.teg.bigmathfast;

import java.util.Arrays;

/**
 * Auto growing byte array with max size 2^51 bytes.
 *
 * Not thread safe.
 *
 * Values are stored in blocks of 2^20 bytes (1 MB). This should work fairly well with the garbage collector.
 */
public class GrowableByteArray {
    public static final int DEFAULT_BLOCK_BITS = 20; // 1MB
    final int BLOCK_BITS;
    final int BLOCK_SIZE;
    final long BLOCK_MASK;
    // Max index is 2^(31+BLOCK_BITS)

    byte[][] backing = new byte[0][];
    long size = 0;

    public GrowableByteArray() {
        this(DEFAULT_BLOCK_BITS);
    }

    public GrowableByteArray(int blockBits) {
        BLOCK_BITS = blockBits;
        BLOCK_SIZE = 1<<BLOCK_BITS;
        BLOCK_MASK = ~((~1L)<<(BLOCK_BITS-1));
    }

    /**
     * Copy the content of src into this array, starting at position {@link #size()}.
     * @param src where to get the content from.
     */
    public void add(byte[] src) {
        copyIn(src, 0, size, src.length);
    }
    /**
     * Copy the full content of src into this array.
     *
     * @param src where to get the content from.
     * @param destIndex write the bytes starting at this position.
     */
    public void copyIn(byte[] src, long destIndex) {
        copyIn(src, 0, destIndex, src.length);
    }
    /**
     * Copy the content of src into this array.
     *
     * @param src where to get the content from.
     * @param srcIndex read bytes from src starting at this position.
     * @param destIndex write the bytes starting at this position.
     * @param length copy this number of bytes.
     */
    public void copyIn(byte[] src, int srcIndex, long destIndex, int length) {
        int sIndex = srcIndex;
        int dBlock = (int) (destIndex >> BLOCK_BITS);
        int dIndex = (int) (destIndex & BLOCK_MASK);
        long wordsLeft = length;

        while (wordsLeft > 0) {
            ensureBlockExistence(dBlock);
            long toCopy = BLOCK_SIZE-dIndex;
            if (toCopy > wordsLeft) {
                toCopy = wordsLeft;
            }
            System.arraycopy(src, sIndex, backing[dBlock], dIndex, (int) toCopy);
            wordsLeft -= toCopy;
            // Move to next backing block in case there are more data
            sIndex += toCopy;
            dBlock++;
            dIndex = 0;
        }
        if (destIndex+length > size) {
            size = destIndex+length;
        }
    }

    /**
     * Set the given byte at the specified index.
     * @param index where to set the byte.
     * @param value the byte to set.
     */
    public void set(long index, byte value) {
        final int block = (int) (index >> BLOCK_BITS);
        ensureBlockExistence(block);
        backing[block][(int) (index & BLOCK_MASK)] = value;
        if (index+1 > size) {
            size = index+1;
        }
    }

    /**
     * Add the given byte at position {@link #size()}.
     * @param value th byte to add.
     * @return the position of the added byte.
     */
    public long add(byte value) {
        set(size, value);
        return size-1;
    }

    private void ensureBlockExistence(int block) {
        if (block >= backing.length) {
            byte[][] newBacking = new byte[block+1][];
            System.arraycopy(backing, 0, newBacking, 0, backing.length);
            backing = newBacking;
        }
        if (backing[block] == null) {
            backing[block] = new byte[BLOCK_SIZE];
        }
    }

    public byte get(long index) {
        final int block = (int) (index >> BLOCK_BITS);
        return block >= backing.length || backing[block] == null ? 0 :
                backing[(int) (index >> BLOCK_BITS)][(int) (index & BLOCK_MASK)];
    }

    public void copyOut(long srcIndex, byte[] destination) {
        copyOut(srcIndex, destination, 0, destination.length);
    }
    public void copyOut(long srcIndex, byte[] dest, int destIndex, int length) {
        int sBlock = (int) (srcIndex >> BLOCK_BITS);
        int sIndex = (int) (srcIndex & BLOCK_MASK);
        int dIndex = destIndex;
        long wordsLeft = length;

        while (wordsLeft > 0) {
            ensureBlockExistence(sBlock);
            long toCopy = BLOCK_SIZE-sIndex;
            if (toCopy > wordsLeft) {
                toCopy = wordsLeft;
            }
            System.arraycopy(backing[sBlock], sIndex, dest, dIndex, (int) toCopy);
            wordsLeft -= toCopy;
            // Move to next backing block in case there are more data
            sBlock++;
            sIndex = 0;
            dIndex += toCopy;
        }
    }

    /**
     * @return the number of words (i.e. bytes) in this array.
     */
    public long size() {
        return size;
        //return backing.length * BLOCK_SIZE;
    }

    public void clear() {
        backing = new byte[0][];
        size = 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GrowableByteArray)) {
            return false;
        }
        GrowableByteArray other = (GrowableByteArray)obj;
        if (size != other.size || backing.length != other.backing.length) {
            return false;
        }
        for (int i = 0 ; i < backing.length ; i++) {
            if (!Arrays.equals(backing[i], other.backing[i])) {
                return false;
            }
        }
        return true;
    }
}
