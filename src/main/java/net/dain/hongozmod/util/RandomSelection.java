package net.dain.hongozmod.util;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class RandomSelection<E> {
    private final NavigableMap<Double, E> map = new TreeMap<Double, E>();
    private final Random random;
    private double total = 0;

    public  RandomSelection(){
        this(new Random());
    }
    public RandomSelection(Random random){
        this.random = random;
    }

    public RandomSelection<E> add(E element, double weight){
        if(weight > 0){
            this.total += weight;
            map.put(total, element);
        }
        return this;
    }

    public E next(){
        double value = this.random.nextDouble() * this.total;
        return this.map.higherEntry(value).getValue();
    }

}
