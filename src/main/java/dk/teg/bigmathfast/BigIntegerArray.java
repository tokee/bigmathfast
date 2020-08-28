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

import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Special purpose representation of BigIntegers with a singular focus on the the access pattern
 * used by bigmathfast.
 *
 * Space: O(n), where n is the number of bytes in the BigIntegers + 1 byte/BigInteger
 *
 * Speed:
 *   Add: O(1)
 *   Iteration: O(1) for next BigInteger
 *
 *   Random: O(n), where n is the number of BigIntegers
 *   Lookup: O(n), where n is the number of BigIntegers
 *   Replacing: Not possible
 *   Delete: Not possible
 */
public class BigIntegerArray implements Iterable<BigInteger> {
    final GrowableByteArray backing = new GrowableByteArray();
    long valueCount = 0;

    public void add(BigInteger value) {
        byte[] bytes = value.toByteArray();
        backing.add((byte) bytes.length);
        backing.add(bytes);
        valueCount++;
    }

    public long getValueCount() {
        return valueCount;
    }

    @Override
    public Iterator<BigInteger> iterator() {
        return new BIAIterator(this);
    }

    class BIAIterator implements Iterator<BigInteger> {
        final BigIntegerArray bia;
        long pos = 0;

        public BIAIterator(BigIntegerArray bia) {
            this.bia = bia;
        }

        @Override
        public boolean hasNext() {
            return pos < bia.backing.size();
        }

        @Override
        public BigInteger next() {
            if (!hasNext()) {
                throw new NoSuchElementException("The backing BigIntegerArray only contains " + valueCount + " values");
            }
            int cardinality = 0xFF & bia.backing.get(pos++);
            byte[] bytes = new byte[cardinality];
            bia.backing.copyOut(pos, bytes);
            pos += cardinality;
            return new BigInteger(bytes);
        }
    }
}
