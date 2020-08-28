package dk.teg.bigmathfast;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
class BigIntegerArrayTest {

    @Test
    void baseTest() {
        BigIntegerArray bia = new BigIntegerArray();
        BigInteger bi = BigInteger.valueOf(1);
        for (int i = 0 ; i < 10 ; i++) {
            bia.add(i == 0 ? BigInteger.ZERO : bi);
            bi = bi.multiply(BigInteger.valueOf(Integer.MAX_VALUE));
        }
        assertEquals(10, bia.valueCount);

        bi = BigInteger.valueOf(1);
        int count = 0;
        for (BigInteger value: bia) {
            assertEquals(count++ == 0 ? BigInteger.ZERO : bi, value);
            bi = bi.multiply(BigInteger.valueOf(Integer.MAX_VALUE));
        }
    }
}