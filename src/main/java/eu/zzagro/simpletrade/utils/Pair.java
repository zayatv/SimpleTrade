package eu.zzagro.simpletrade.utils;

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
}
