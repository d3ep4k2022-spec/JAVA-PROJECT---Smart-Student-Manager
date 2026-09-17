package com.studentmanager.util;

import java.util.Scanner;

public class InputUtil {
    private final Scanner sc=new Scanner(System.in);
    public String text(String prompt){System.out.print(prompt);return sc.nextLine().trim();}
    public int integer(String prompt){
        while(true) try{return Integer.parseInt(text(prompt));}
        catch(NumberFormatException e){System.out.println("Enter a valid integer.");}
    }
    public boolean yesNo(String prompt){return text(prompt+" (y/n): ").equalsIgnoreCase("y");}
}
