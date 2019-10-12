package Util;

public class Window {
    public static void clear(){
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    public static void gotoxy(int row, int column){
        System.out.printf("%c[%d;%df", 0x1B, row, column);
    }
}
