package com.konjex.util;

import org.junit.Test;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Unit tests for DoublyLinkedList.
 */
public class DoublyLinkedListTest {

    private Random random = new Random();

    @Test
    public void defaultConstructor(){
        //Exercise
        DoublyLinkedList<String> list = new DoublyLinkedList<>();

        //Verify
        assertTrue(list.isEmpty());
    }

    @Test
    public void variadicConstructor(){
        //Exercise
        DoublyLinkedList<String> list = new DoublyLinkedList<>("a", "b", "c");

        //Verify
        assertCorrectOrder(list, "a", "b", "c");
    }

    @Test
    public void addFirst_empty(){
        //Setup
        DoublyLinkedList<String> list = new DoublyLinkedList<>();

        //Exercise
        list.addFirst("first");

        //Verify
        assertEquals(1, list.size());
        assertEquals("first", list.getFirst().getValue());
        assertEquals("first", list.getLast().getValue());
    }

    @Test
    public void addFirst_notEmpty(){
        //Setup
        DoublyLinkedList<String> list = new DoublyLinkedList<>();

        //Exercise
        list.addFirst("second");
        list.addFirst("first");

        //Verify
        assertEquals(2, list.size());
        assertEquals("first", list.getFirst().getValue());
        assertEquals("first", list.getLast().getPrev().getValue());
        assertEquals("second", list.getLast().getValue());
        assertEquals("second", list.getFirst().getNext().getValue());
    }

    @Test
    public void addLast_empty(){
        //Setup
        DoublyLinkedList<String> list = new DoublyLinkedList<>();

        //Exercise
        list.addLast("first");

        //Verify
        assertCorrectOrder(list, "first");
    }

    @Test
    public void addLast_notEmpty(){
        //Setup
        DoublyLinkedList<String> list = new DoublyLinkedList<>();

        //Exercise
        list.addLast("first");
        list.addLast("second");

        //Verify
        assertCorrectOrder(list, "first", "second");
    }

    @Test
    public void removeFirst_noElements(){
        //Setup
        DoublyLinkedList<String> list = new DoublyLinkedList<>();

        //Exercise
        list.removeFirst();

        //Verify
        assertTrue(list.isEmpty());
    }

    @Test
    public void removeFirst_oneElement(){
        //Setup
        DoublyLinkedList<String> list = new DoublyLinkedList<>("a");

        //Exercise
        list.removeFirst();

        //Verify
        assertTrue(list.isEmpty());
    }

    @Test
    public void removeFirst_twoElements(){
        //Setup
        DoublyLinkedList<String> list = new DoublyLinkedList<>("a", "b");

        //Exercise
        list.removeFirst();

        //Verify
        assertCorrectOrder(list, "b");
    }

    @Test
    public void removeFirst_threeOrMoreElements(){
        //Setup
        DoublyLinkedList<String> list = new DoublyLinkedList<>("a", "b", "c");

        //Exercise
        list.removeFirst();

        //Verify
        assertCorrectOrder(list, "b", "c");
    }

    @Test
    public void removeLast_noElements(){
        //Setup
        DoublyLinkedList<String> list = new DoublyLinkedList<>();

        //Exercise
        list.removeLast();

        //Verify
        assertTrue(list.isEmpty());
    }

    @Test
    public void removeLast_oneElement(){
        //Setup
        DoublyLinkedList<String> list = new DoublyLinkedList<>("a");

        //Exercise
        list.removeLast();

        //Verify
        assertTrue(list.isEmpty());
    }

    @Test
    public void removeLast_twoElements(){
        //Setup
        DoublyLinkedList<String> list = new DoublyLinkedList<>("a", "b");

        //Exercise
        list.removeLast();

        //Verify
        assertCorrectOrder(list, "a");
    }

    @Test
    public void removeLast_threeOrMoreElements(){
        //Setup
        DoublyLinkedList<String> list = new DoublyLinkedList<>("a", "b", "c");

        //Exercise
        list.removeLast();

        //Verify
        assertCorrectOrder(list, "a", "b");
    }

    @Test
    public void isEmpty_empty(){
        //Setup
        DoublyLinkedList<String> list = new DoublyLinkedList<>();

        //Exercise/Verify
        assertTrue(list.isEmpty());
    }

    @Test
    public void isEmpty_notEmpty(){
        //Setup
        DoublyLinkedList<String> list = new DoublyLinkedList<>("a");

        //Exercise/Verify
        assertFalse(list.isEmpty());
    }

    @Test
    public void getFirst_empty(){
        //Setup
        DoublyLinkedList<String> list = new DoublyLinkedList<>();

        //Exercise
        LinkedListNode<String> first = list.getFirst();

        //Verify
        assertNull(first);
    }

    @Test
    public void getFirst_oneElement(){
        //Setup
        DoublyLinkedList<String> list = new DoublyLinkedList<>("a");

        //Exercise
        LinkedListNode<String> first = list.getFirst();

        //Verify
        assertEquals("a", first.getValue());
    }

    @Test
    public void getFirst_twoElements(){
        //Setup
        DoublyLinkedList<String> list = new DoublyLinkedList<>("a", "b");

        //Exercise
        LinkedListNode<String> first = list.getFirst();

        //Verify
        assertEquals("a", first.getValue());
    }

    @Test
    public void getLast_empty(){
        //Setup
        DoublyLinkedList<String> list = new DoublyLinkedList<>();

        //Exercise
        LinkedListNode<String> last = list.getFirst();

        //Verify
        assertNull(last);
    }

    @Test
    public void getLast_oneElement(){
        //Setup
        DoublyLinkedList<String> list = new DoublyLinkedList<>("a");

        //Exercise
        LinkedListNode<String> last = list.getLast();

        //Verify
        assertEquals("a", last.getValue());
    }

    @Test
    public void getLast_twoElements(){
        //Setup
        DoublyLinkedList<String> list = new DoublyLinkedList<>("a", "b");

        //Exercise
        LinkedListNode<String> last = list.getLast();

        //Verify
        assertEquals("b", last.getValue());
    }

    @Test
    public void size_empty(){
        //Setup
        DoublyLinkedList<String> list = new DoublyLinkedList<>();

        //Exercise
        int size = list.size();

        //Verify
        assertEquals(0, size);
    }

    @Test
    public void size_random(){
        //Setup
        DoublyLinkedList<String> list = new DoublyLinkedList<>();
        AtomicInteger expectedSize = new AtomicInteger(0);

        //Exercise
        int iters = random.nextInt(10) + 1;
        Repeatable.invoke(() -> {
            if(list.isEmpty() || random.nextBoolean() || random.nextBoolean()){
                list.addFirst("element");
                expectedSize.incrementAndGet();
            }
            else{
                list.removeFirst();
                expectedSize.decrementAndGet();
            }
        }, iters);

        //Verify
        assertEquals(expectedSize.get(), list.size());
    }

    @Test
    public void iterator_empty(){
        //Setup
        DoublyLinkedList<String> list = new DoublyLinkedList<>();

        //Exercise/Verify
        for(String ignored : list){
            assertTrue(false);
        }
    }

    @Test
    public void iterator_notEmpty(){
        //Setup
        DoublyLinkedList<String> list = new DoublyLinkedList<>("a", "b", "c");
        DoublyLinkedList<String> newList = new DoublyLinkedList<>("a", "b", "c");

        //Exercise
        for(String string : list){
            newList.addLast(string);
        }

        //Verify
        assertCorrectOrder(list, "a", "b", "c");
    }

    private static void assertCorrectOrder(DoublyLinkedList<String> list, String... elements){
        assertEquals(elements.length, list.size());
        for(int i = 0; i < elements.length; i ++){
            assertElementEqualFromBothSides(list, elements[i], i);
        }
    }

    private static void assertElementEqualFromBothSides(DoublyLinkedList<String> list, String expectedValue, int index){
        LinkedListNode<String> currNode = list.getFirst();
        for(int i = 0; i < index; i ++){
            currNode = currNode.getNext();
        }
        assertEquals(expectedValue, currNode.getValue());
        currNode = list.getLast();
        for(int i = 0; i < list.size() - index - 1; i ++){
            currNode = currNode.getPrev();
        }
        assertEquals(expectedValue, currNode.getValue());
    }

}