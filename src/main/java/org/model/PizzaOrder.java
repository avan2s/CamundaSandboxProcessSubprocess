package org.model;

import java.io.Serializable;

/**
 * Created by Andy on 25.02.2017.
 */
public class PizzaOrder implements Serializable {
    private String pizzaname;
    private int numberOfPizzas;

    public PizzaOrder(String pizzaname, int numberOfPizzas) {
        this.pizzaname = pizzaname;
        this.numberOfPizzas = numberOfPizzas;
    }

    public String getPizzaname() {
        return pizzaname;
    }

    public void setPizzaname(String pizzaname) {
        this.pizzaname = pizzaname;
    }

    public int getNumberOfPizzas() {
        return numberOfPizzas;
    }

    public void setNumberOfPizzas(int numberOfPizzas) {
        this.numberOfPizzas = numberOfPizzas;
    }
}
