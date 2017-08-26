package com.konjex.util;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

/**
 * Created by shane on 23/08/17.
 */
public class DoublyLinkedList<T> implements Iterable<T> {

    private LinkedListNode<T> first;
    private LinkedListNode<T> last;
    private int length;

    public DoublyLinkedList(){

    }

    public DoublyLinkedList(T... elements){
        for(T element : elements){
            addLast(element);
        }
    }

    public void addFirst(T value){
        if(isEmpty()){
            first = new LinkedListNode<>(value, null, null);
            last = first;
        }
        else{
            LinkedListNode<T> newNode = new LinkedListNode<>(value, null, first);
            first.setPrev(newNode);
            first = newNode;
        }
        length ++;
    }

    public void addLast(T value){
        if(isEmpty()){
            first = new LinkedListNode<>(value, null, null);
            last = first;
        }
        else{
            LinkedListNode<T> newNode = new LinkedListNode<>(value, last, null);
            last.setNext(newNode);
            last = newNode;
        }
        length ++;
    }

    public void removeFirst(){
        if(!removeIfLast()){
            first = first.getNext();
            first.setPrev(null);
        }
    }

    public void removeLast(){
        if(!removeIfLast()){
            last = last.getPrev();
            last.setNext(null);
        }
    }

    private boolean removeIfLast(){
        if(isEmpty()){
            return true;
        }
        length --;
        if(length == 0){
            first = null;
            last = null;
            return true;
        }
        return false;
    }

    public boolean isEmpty(){
        return first == null;
    }

    public LinkedListNode<T> getFirst(){
        return first;
    }

    public LinkedListNode<T> getLast(){
        return last;
    }

    public int size(){
        return length;
    }

    @NotNull
    @Override
    public Iterator<T> iterator(){
        return new Iterator<T>(){
            private LinkedListNode<T> currentNode = first;

            @Override
            public boolean hasNext() {
                return !isEmpty() && currentNode.hasNext();
            }

            @Override
            public T next() {
                T value = currentNode.getValue();
                currentNode = currentNode.getNext();
                return value;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

}
