/*
 * Copyright 2015 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.agrona.collections;

import uk.co.real_logic.agrona.generation.DoNotSub;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator for a sequence of primitive integers.
 */
public class IntIterator implements Iterator<Integer>
{
    private final int missingValue;

    private int[] values;
    @DoNotSub private int capacity;
    @DoNotSub private int mask;
    @DoNotSub private int positionCounter;
    @DoNotSub private int stopCounter;
    private boolean isPositionValid = false;

    /**
     * Construct an {@link Iterator} over an array of primitives ints.
     *
     * @param missingValue to indicate the value is missing, i.e. not present or null.
     * @param values       to iterate over.
     */
    public IntIterator(final int missingValue, final int[] values)
    {
        this.missingValue = missingValue;
        reset(values);
    }

    /**
     * Reset methods for fixed size collections.
     */
    void reset()
    {
        reset(values);
    }

    /**
     * Reset method for expandable collections.
     *
     * @param values
     */
    void reset(final int[] values)
    {
        this.values = values;
        capacity = values.length;
        mask = capacity - 1;

        @DoNotSub int i = capacity;
        if (values[capacity - 1] != missingValue)
        {
            i = 0;
            for (@DoNotSub int size = capacity; i < size; i++)
            {
                if (values[i] == missingValue)
                {
                    break;
                }
            }
        }

        stopCounter = i;
        positionCounter = i + capacity;
    }

    @DoNotSub protected int getPosition()
    {
        return positionCounter & mask;
    }

    public boolean hasNext()
    {
        for (@DoNotSub int i = positionCounter - 1; i >= stopCounter; i--)
        {
            @DoNotSub final int index = i & mask;
            if (values[index] != missingValue)
            {
                return true;
            }
        }

        return false;
    }

    protected void findNext()
    {
        isPositionValid = false;

        for (@DoNotSub int i = positionCounter - 1; i >= stopCounter; i--)
        {
            @DoNotSub final int index = i & mask;
            if (values[index] != missingValue)
            {
                positionCounter = i;
                isPositionValid = true;
                return;
            }
        }

        throw new NoSuchElementException();
    }

    public Integer next()
    {
        return nextValue();
    }

    /**
     * Strongly typed alternative of {@link Iterator#next()} not to avoid boxing.
     *
     * @return the next int value.
     */
    public int nextValue()
    {
        findNext();

        return values[getPosition()];
    }

}
