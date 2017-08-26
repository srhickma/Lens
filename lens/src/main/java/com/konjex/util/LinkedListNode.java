package com.konjex.util;

/**
 * Created by shane on 23/08/17.
 */
public class LinkedListNode<T> {

    private T val;
    private LinkedListNode<T> prev;
    private LinkedListNode<T> next;

    LinkedListNode(T val, LinkedListNode<T> prev, LinkedListNode<T> next){
        this.val = val;
        this.prev = prev;
        this.next = next;
    }

    public T getValue(){
        return val;
    }

    public LinkedListNode<T> getPrev(){
        return prev;
    }

    public LinkedListNode<T> getNext(){
        return next;
    }

    public void setValue(T val){
       this.val = val;
    }

    void setPrev(LinkedListNode<T> prev){
        this.prev = prev;
    }

    void setNext(LinkedListNode<T> next){
        this.next = next;
    }

    public boolean hasPrev(){
        return prev != null;
    }

    public boolean hasNext(){
        return next != null;
    }

}
