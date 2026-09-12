package server.world.entity;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public abstract class EntityList<T extends Entity> implements Iterable<T> {

    private final Entity[] entities;
    private final int capacity;

    private int size;
    private int lastIndex;

    protected EntityList(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be positive");
        }

        this.capacity = capacity;
        this.entities = new Entity[capacity + 1];
    }

    public final boolean add(T entity) {
        Objects.requireNonNull(entity, "entity");

        if (entity.getIndex() != Entity.NO_INDEX) {
            throw new IllegalStateException("Entity is already in an entity list.");
        }

        int index = nextFreeIndex();
        if (index == Entity.NO_INDEX) {
            return false;
        }

        entities[index] = entity;
        entity.setIndex(index);
        lastIndex = index;
        size++;
        return true;
    }

    public final boolean remove(T entity) {
        Objects.requireNonNull(entity, "entity");

        int index = entity.getIndex();
        if (!validIndex(index) || entities[index] != entity) {
            return false;
        }

        entities[index] = null;
        entity.setIndex(Entity.NO_INDEX);
        size--;
        return true;
    }

    @SuppressWarnings("unchecked")
    public final T get(int index) {
        checkIndex(index);
        return (T) entities[index];
    }

    public final int size() {
        return size;
    }

    public final boolean isEmpty() {
        return size == 0;
    }

    public final boolean isFull() {
        return size == capacity;
    }

    public final int capacity() {
        return capacity;
    }

    private int nextFreeIndex() {
        for (int index = lastIndex + 1; index <= capacity; index++) {
            if (entities[index] == null) {
                return index;
            }
        }

        for (int index = 1; index <= lastIndex; index++) {
            if (entities[index] == null) {
                return index;
            }
        }

        return Entity.NO_INDEX;
    }

    private boolean validIndex(int index) {
        return index >= 1 && index <= capacity;
    }

    private void checkIndex(int index) {
        if (!validIndex(index)) {
            throw new IndexOutOfBoundsException("Entity index out of bounds: " + index);
        }
    }

    @Override
    public final Iterator<T> iterator() {
        return new Iterator<>() {

            private int index = 1;

            @Override
            public boolean hasNext() {
                while (index <= capacity && entities[index] == null) {
                    index++;
                }
                return index <= capacity;
            }

            @Override
            @SuppressWarnings("unchecked")
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return (T) entities[index++];
            }
        };
    }
}
