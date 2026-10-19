package org.reina_s;

import javax.swing.*;
import java.lang.reflect.Array;
import java.util.*;


public class MyLinkedList<E> implements List<E> {
    private class MyLinkedListNode<E>{
        E value;
        MyLinkedListNode<E> next;
        MyLinkedListNode<E> previous;

        public MyLinkedListNode(E value){
            this.value = value;
        }

        public MyLinkedListNode(E value, MyLinkedListNode<E> previous){
            this.value = value;
            this.previous = previous;
        }

        public MyLinkedListNode(E value, MyLinkedListNode<E> previous, MyLinkedListNode<E> next){
            this.value = value;
            this.previous = previous;
            this.next = next;
        }
    }

    private class MyIterator implements ListIterator<E> {
        private MyLinkedListNode<E> nextNode;     // узел, который вернёт next()
        private MyLinkedListNode<E> lastReturned; // узел, который вернули последним
        private int nextIndex;
        private MyLinkedList<E> list;


        MyIterator(MyLinkedList<E> list) {
            this.list = list;
            nextNode = this.list.left;
            nextIndex = 0;
        }

        MyIterator(MyLinkedList<E> list, int index) {
            this.list = list;
            nextNode = (index == size) ? null : getNodeByIndex(index);
            nextIndex = index;
        }

        @Override
        public boolean hasNext() {
            return nextIndex < size;
        }

        @Override
        public E next() {
            if (!hasNext()) throw new NoSuchElementException();
            lastReturned = nextNode;
            nextNode = nextNode.next;
            nextIndex++;
            return lastReturned.value;
        }

        @Override
        public boolean hasPrevious() {
            return nextIndex > 0;
        }

        @Override
        public E previous() {
            if (!hasPrevious()) throw new NoSuchElementException();
            nextNode = (nextNode == null) ? right : nextNode.previous;
            lastReturned = nextNode;
            nextIndex--;
            return lastReturned.value;
        }

        @Override
        public int nextIndex() {
            return nextIndex;
        }

        @Override
        public int previousIndex() {
            return nextIndex - 1;
        }

        @Override
        public void remove() {
            if (lastReturned == null) throw new IllegalStateException();
            MyLinkedListNode<E> lastNext = lastReturned.next;
            this.list.remove(lastReturned);
            if (nextNode == lastReturned) // последним был вызов previous()
                nextNode = lastNext;
            else                          // последним был вызов next()
                nextIndex--;
            lastReturned = null;
        }

        @Override
        public void set(E e) {
            if (lastReturned == null) throw new IllegalStateException();
            lastReturned.value = e;
        }

        @Override
        public void add(E e) {
            this.list.add(nextIndex, e);
            nextIndex++;
            lastReturned = null;
        }

    }


    MyLinkedListNode<E> left;
    MyLinkedListNode<E> right;
    private int size;

    public MyLinkedList(){
        this.size = 0;
        this.left = null;
        this.right = null;
    };


    private MyLinkedListNode<E> getNodeByIndex(int index) {
        boolean reverse_mode = index < 0;
        if (index < 0)
            index = this.size + index;
        if (index >= size)
            throw new IndexOutOfBoundsException();
        MyLinkedListNode<E> now;
        if (index < size / 2) {
            now = left;
            for (int i = 0; i < index; i++)
                now = now.next;
        } else {
            now = right;
            for (int i = size - 1; i > index; i--)
                now = now.previous;
        }
        return now;
    }

    public void append(E element){
        this.add(-1, element);
    }


    @Override
    public int size(){
        return this.size;
    }

    @Override
    public boolean isEmpty(){
        return this.size == 0;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    @Override
    public Object[] toArray() {
        Object[] array = new Object[this.size];
        int index = 0;
        var now = this.left;
        while (now != null){
            array[index] = now.value;
            now = now.next;
            ++index;
        }
        return array;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length < size)
            a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
        Object[] result = a;
        int i = 0;
        for (MyLinkedListNode<E> now = left; now != null; now = now.next)
            result[i++] = now.value;
        if (a.length > size)
            a[size] = null;
        return a;
    }

    @Override
    public boolean add(E e) {
        this.add(-1, e);
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return addAll(size, c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        boolean changed = false;
        for (var i : c.toArray()){
            this.append((E)i);
            changed = true;
        }
        return changed;
    }

    @Override
    public void clear() {
        this.left = null;
        this.right = null;
        this.size = 0;
    }

    @Override
    public E get(int index) {
        return this.getNodeByIndex(index).value;
    }

    @Override
    public void add(int index, E element) {
        if (index == 0){
            var old_left = this.left;
            this.left = new MyLinkedListNode<>(element);
            this.left.next = old_left;
            if (old_left != null)
                old_left.previous = this.left;
            ++this.size;
            if (this.right == null)
                this.right = this.left;
            return;
        }
        if (index == -1 || index == this.size)
        {
            var old_right = this.right;
            this.right = new MyLinkedListNode<>(element);
            this.right.previous = old_right;
            if (old_right != null)
                old_right.next = this.right;
            ++this.size;
            if (this.left == null)
                this.left = this.right;
            return;
        }

        var node = this.getNodeByIndex(index);
        var previous_node = node.previous;
        var new_node = new MyLinkedListNode<>(element, previous_node, node);
        previous_node.next = new_node;
        node.previous = new_node;
        ++this.size;


    }

    @Override
    public E set(int index, E element) {
        var node = getNodeByIndex(index);
        E old_value = node.value;
        node.value = element;
        return old_value;
    }

    @Override
    public E remove(int index) {
        var node = this.getNodeByIndex(index);
        if (node.previous != null)
            node.previous.next = node.next;
        else
            this.left = node.next;
        if (node.next != null)
            node.next.previous = node.previous;
        else
            this.right = node.previous;
        --this.size;
        return node.value;
    }

    @Override
    public boolean remove(Object o) {
        int index = this.indexOf(o);
        boolean modified = false;
        if (index > -1)
        {
            this.remove(index);
            index = this.indexOf(o);
            modified = true;
        }
        return modified;
    }

    private boolean remove(MyLinkedListNode<E> node) {
        if (node.previous != null)
            node.previous.next = node.next;
        else
            this.left = node.next;
        if (node.next != null)
            node.next.previous = node.previous;
        else
            this.right = node.previous;
        --this.size;
        return true;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (var item : c){
            if (! this.contains(c))
                return false;
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (var i : c){
            var target_index = this.indexOf(i);
            if (target_index != -1)
            {
                changed = true;
                this.remove(target_index);
            }
        }
        return changed;
    }


    @Override
    public boolean retainAll(Collection<?> c) {
        boolean changed = false;
        for (Iterator<E> it = iterator(); it.hasNext(); ) {
            if (!c.contains(it.next())) {
                it.remove();
                changed = true;
            }
        }
        return changed;
    }


    @Override
    public int indexOf(Object o) {
        MyLinkedListNode<E> now = this.left;
        int index = 0;
        while (now != null){
            if ((o == null && now.value == null) || now.value.equals(o))
                return index;
            now = now.next;
            ++index;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        MyLinkedListNode<E> now = this.right;
        int index = this.size - 1;
        while (now != null){
            if (now.value == o)
                return index;
            now = now.previous;
            --index;
        }
        return -1;
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        var result = new MyLinkedList<E>();
        result.left = this.getNodeByIndex(fromIndex);
        MyLinkedListNode<E> right = null;
        if (toIndex >= this.size)
            right = this.right;
        else
            right = this.getNodeByIndex(toIndex);
        if (right != this.left)
            result.right = right;
        else
            result.right = result.left;
        return result;
    }

    public E getFirst() {
        if (this.left == null)
            throw new NoSuchElementException();
        return this.left.value;
    }

    public E getLast() {
        if (this.right == null)
            throw new NoSuchElementException();
        return this.right.value;
    }

    @Override
    public Iterator<E> iterator() {
        return new MyIterator(this);
    }

    @Override
    public ListIterator<E> listIterator() {
        return new MyIterator(this);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return new MyIterator(this, index);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ", "[", "]");
        for (E e : this)
            sj.add(String.valueOf(e));
        return sj.toString();
    }

    @Override
    public int hashCode() {
        int h = 1;
        for (E e : this)
            h = 31 * h + Objects.hashCode(e);
        return h;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof List<?> other)) return false;
        Iterator<?> it = other.iterator();
        for (E e : this) {
            if (!it.hasNext() || !Objects.equals(e, it.next()))
                return false;
        }
        return !it.hasNext();
    }

}
