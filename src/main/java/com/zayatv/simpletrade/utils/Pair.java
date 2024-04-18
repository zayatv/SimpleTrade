package com.zayatv.simpletrade.utils;

public class Pair<T, U> {

    private T key;
    private U value;

    public Pair(T t, U u)
    {
        this.key = t;
        this.value = u;
    }

    public void setKey(T newKey)
    {
        key = newKey;
    }

    public void setValue(U newValue)
    {
        value = newValue;
    }

    public T getKey()
    {
        return key;
    }

    public U getValue()
    {
        return value;
    }

    public Pair<U, T> reversed()
    {
        return new Pair<U, T>(value, key);
    }

    public boolean equalsPair(Pair<T, U> pair)
    {
        return pair.getKey() == key && pair.getValue() == value;
    }

    public boolean equalsPairOrderIgnored(Pair<T, U> pair)
    {
        return (pair.getKey() == key && pair.getValue() == value) || (pair.getKey() == value && pair.getValue() == key);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) return true;

        if (!(obj instanceof Pair)) return false;

        Pair pair = (Pair) obj;
        return equalsPair(pair);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 17;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }
}
