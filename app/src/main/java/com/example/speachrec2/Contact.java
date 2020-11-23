package com.example.speachrec2;
import java.util.ArrayList;

public class Contact {
    private String name;
    private ArrayList<String> numbers;
    private String id;

    public Contact(String name, String id) {
        this.name = name;
        this.numbers = new ArrayList<String>();
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getNumbers() {
        return numbers;
    }

    public String getId() {
        return id;
    }
    public void addNumber(String number){
        numbers.add(number);
    }
}
